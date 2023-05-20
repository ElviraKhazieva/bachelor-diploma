package ru.itis.diploma.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itis.diploma.model.BusinessCreditPayment;
import ru.itis.diploma.model.Game;
import ru.itis.diploma.model.InvestmentCreditPayment;
import ru.itis.diploma.model.Manufacturer;
import ru.itis.diploma.model.ProductionParameters;
import ru.itis.diploma.model.SalesTaxPayment;
import ru.itis.diploma.model.StatisticsInfo;
import ru.itis.diploma.repository.BusinessCreditPaymentRepository;
import ru.itis.diploma.repository.InvestmentCreditPaymentRepository;
import ru.itis.diploma.repository.ManufacturerRepository;
import ru.itis.diploma.repository.ProductionParametersRepository;
import ru.itis.diploma.repository.SalesTaxPaymentRepository;
import ru.itis.diploma.repository.TradingSessionResultsRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.itis.diploma.service.CronService.MANUFACTURER_STATISTICS_INFO;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final int MONTH = 30;
    private final BusinessCreditPaymentRepository businessCreditPaymentRepository;
    private final ProductionParametersRepository productionParametersRepository;
    private final InvestmentCreditPaymentRepository investmentCreditPaymentRepository;
    private final SalesTaxPaymentRepository salesTaxPaymentRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final ManufacturerService manufacturerService;
    private final TradingSessionResultsRepository tradingSessionResultsRepository;

    public void makePayments(Game game) {
        makeSalesTaxPayments(game);
        makeInvestmentCreditPayments(game);
        makeBusinessCreditPayments(game);
    }

    //todo: налог с продаж должен осуществляться с выручки за все время игры? нет ведь, с пршлой даты взятия налога, до тек дня
    private void makeSalesTaxPayments(Game game) {
        var manufacturers = manufacturerService.getGameManufacturers(game.getId());
        BigDecimal salesTax = game.getSalesTax();
        if (Game.currentDay % MONTH == 0) {
            ArrayList<SalesTaxPayment> salesTaxPayments = new ArrayList<>();
            for (Manufacturer manufacturer : manufacturers) {
                //найти последний платеж по этому производителю(макс дата), взять его день - фром
                Optional<SalesTaxPayment> lastPayment = salesTaxPaymentRepository.findLastByManufacturerId(manufacturer.getId());
                int lastPaymentDate = lastPayment.isPresent() ? lastPayment.get().getDate() : 1;

                BigDecimal purchasesTotal = tradingSessionResultsRepository.getPurchasesTotal(manufacturer.getId(), lastPaymentDate, Game.currentDay);
                StatisticsInfo statisticsInfo = MANUFACTURER_STATISTICS_INFO.get(manufacturer.getId());
                if (purchasesTotal != null) {
                    var taxAmount = purchasesTotal.multiply(salesTax).divide(BigDecimal.valueOf(100), 2, RoundingMode.UP);
                    var payment = SalesTaxPayment.builder()
                        .amount(taxAmount)
                        .date(Game.currentDay)
                        .manufacturer(manufacturer)
                        .build();
                    salesTaxPayments.add(payment);
                    statisticsInfo.setPaidTaxesAmount(taxAmount);
                    //допускаем отрицательный баланс, условно списываем деньги по поступлению средств
                    manufacturer.setBalance(manufacturer.getBalance().subtract(taxAmount));
                    manufacturerRepository.save(manufacturer);
                } else {
                    statisticsInfo.setPaidTaxesAmount(BigDecimal.ZERO);
                }
            }
            salesTaxPaymentRepository.saveAll(salesTaxPayments);
        }
    }

    private void makeBusinessCreditPayments(Game game) {
        var manufacturers = manufacturerService.getGameManufacturers(game.getId());
        for (Manufacturer manufacturer : manufacturers) {
            List<ProductionParameters> manufacturerProductionParameters = productionParametersRepository.findByManufacturerId(manufacturer.getId());
            List<BusinessCreditPayment> payments = businessCreditPaymentRepository.findAllByProductionParametersIdInAndNextDate(
                manufacturerProductionParameters.stream()
                    .map(ProductionParameters::getId)
                    .toList(),
                Game.currentDay);
            for (BusinessCreditPayment payment : payments) {
                var nextAmount = payment.getNextAmount();
                var paymentProductionParameters = payment.getProductionParameters();
                var newPayment = BusinessCreditPayment.builder()
                    .productionParameters(paymentProductionParameters)
                    .date(Game.currentDay)
                    .build();
                if (nextAmount.compareTo(manufacturer.getBalance()) < 0) {
                    newPayment.setAmount(nextAmount);
                    manufacturer.setBalance(manufacturer.getBalance().subtract(nextAmount));
                } else {
                    newPayment.setAmount(manufacturer.getBalance());
                    paymentProductionParameters.setInterestRateBusinessCredit(paymentProductionParameters.getInterestRateBusinessCredit().add(BigDecimal.ONE));
                    productionParametersRepository.save(paymentProductionParameters);
                    var amount = nextAmount.subtract(manufacturer.getBalance());
                    newPayment.setNextAmount(amount.add(calculateBusinessCreditInterestAmount(
                        amount,
                        paymentProductionParameters.getInterestRateBusinessCredit(),
                        paymentProductionParameters.getTimeToMarket())));
                    newPayment.setNextDate(Game.currentDay + paymentProductionParameters.getTimeToMarket());
                    manufacturer.setBalance(BigDecimal.ZERO);
                }
                businessCreditPaymentRepository.save(newPayment);
                manufacturerRepository.save(manufacturer);
                StatisticsInfo statisticsInfo = MANUFACTURER_STATISTICS_INFO.get(manufacturer.getId());
                statisticsInfo.setRepaidBusinessCreditAmount(statisticsInfo.getRepaidBusinessCreditAmount().add(newPayment.getAmount()));
            }
        }
    }
//    private void makeBusinessCreditPayments(Game game) {
//        var manufacturers = manufacturerService.getGameManufacturers(game.getId());
//        for (Manufacturer manufacturer : manufacturers) {
//            List<ProductionParameters> manufacturerProductionParameters = productionParametersRepository.findByManufacturerId(manufacturer.getId());
//            List<BusinessCreditPayment> payments = businessCreditPaymentRepository.findAllByProductionParametersIdInAndNextDate(
//                manufacturerProductionParameters.stream()
//                    .map(ProductionParameters::getId)
//                    .toList(),
//                Game.currentDay);
//            for (BusinessCreditPayment payment : payments) {
//                var paymentProductionParameters = payment.getProductionParameters();
//                if (!paymentProductionParameters.getBusinessCreditIsRepaid()) {
//                    var principalPaymentsSum = businessCreditPaymentRepository
//                        .findAllByProductionParametersId(paymentProductionParameters.getId())
//                        .stream()
//                        .map(BusinessCreditPayment::getPrincipalPayment)
//                        .filter(Objects::nonNull)
//                        .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//                    if (principalPaymentsSum.compareTo(paymentProductionParameters.getBusinessCreditAmount()) < 0) {
//                        //todo: проверка суммы платежей по бизнес кредиту, если она 0, то списываем всю сумму бизнес кредита,
//                        // еcли не 0, то разницу между суммой бизнес кредита и суммой выплат
//                        var principalPayment = principalPaymentsSum.compareTo(BigDecimal.ZERO) == 0
//                            ? paymentProductionParameters.getBusinessCreditAmount()
//                            : paymentProductionParameters.getBusinessCreditAmount().subtract(principalPaymentsSum);
//                        var fullPrincipalPayment = principalPayment.add(paymentProductionParameters.getBusinessCreditDebt());
//
//                        var interestRateBusinessCredit = paymentProductionParameters.getInterestRateBusinessCredit() != null
//                            ? paymentProductionParameters.getInterestRateBusinessCredit()
//                            : game.getInterestRateBusinessCredit();
//                        var interestAmount = calculateBusinessCreditInterestAmount(
//                            fullPrincipalPayment,
//                            interestRateBusinessCredit,
//                            paymentProductionParameters.getTimeToMarket());
//
//                        var newPayment = BusinessCreditPayment.builder()
//                            .productionParameters(paymentProductionParameters)
//                            .date(Game.currentDay)
//                            .build();
//
//                        if (fullPrincipalPayment.add(interestAmount).compareTo(manufacturer.getBalance()) <= 0) {
//                            newPayment.setPrincipalPayment(fullPrincipalPayment);
//                            newPayment.setInterestAmount(interestAmount);
//                            manufacturer.setBalance(manufacturer.getBalance().subtract(interestAmount).subtract(fullPrincipalPayment));
//                            paymentProductionParameters.setBusinessCreditDebt(BigDecimal.ZERO);
//                        } else if (principalPayment.add(interestAmount).compareTo(manufacturer.getBalance()) <= 0) {
//                            newPayment.setPrincipalPayment(principalPayment);
//                            newPayment.setInterestAmount(interestAmount);
//                            manufacturer.setBalance(manufacturer.getBalance().subtract(interestAmount).subtract(principalPayment));
//                            paymentProductionParameters.setInterestRateBusinessCredit(paymentProductionParameters.getInterestRateBusinessCredit().add(BigDecimal.ONE));
//                        } else {
//                            if (interestAmount.compareTo(manufacturer.getBalance()) <= 0) {
//                                newPayment.setInterestAmount(interestAmount);
//                                newPayment.setPrincipalPayment(manufacturer.getBalance().subtract(interestAmount));
//                            } else {
//                                newPayment.setInterestAmount(manufacturer.getBalance());
//                                newPayment.setPrincipalPayment(BigDecimal.ZERO);
//                                paymentProductionParameters.setBusinessCreditDebt(
//                                    paymentProductionParameters.getBusinessCreditDebt()
//                                        .add(interestAmount.subtract(manufacturer.getBalance())));
//                            }
//                            manufacturer.setBalance(BigDecimal.ZERO);
//                            paymentProductionParameters.setInterestRateBusinessCredit(paymentProductionParameters.getInterestRateBusinessCredit().add(BigDecimal.ONE));
//                            payment.setNextDate(Game.currentDay + paymentProductionParameters.getTimeToMarket());
//                        }
//                        businessCreditPaymentRepository.save(newPayment);
//                        manufacturerRepository.save(manufacturer);
//                    } else {
//                        paymentProductionParameters.setBusinessCreditIsRepaid(true);
//                    }
//                    productionParametersRepository.save(paymentProductionParameters);
//                }
//            }
//        }
//
//    }

    private void makeInvestmentCreditPayments(Game game) {
        var manufacturers = manufacturerService.getGameManufacturers(game.getId());
        List<InvestmentCreditPayment> allPayments = investmentCreditPaymentRepository
            .findAllByManufacturerIdInAndNextDate(manufacturers.stream().map(Manufacturer::getId)
                .toList(), Game.currentDay);
        for (InvestmentCreditPayment payment : allPayments) {
            var manufacturer = payment.getManufacturer();
            var debt = manufacturer.getInvestmentCreditDebt();
            if (debt.compareTo(BigDecimal.ZERO) > 0) {
                var newPayment = InvestmentCreditPayment.builder()
                    .manufacturer(manufacturer)
                    .date(Game.currentDay)
                    .nextDate(Game.currentDay + MONTH)
                    .build();
                var monthlyPrincipalPayment = debt
                    .divide(manufacturer.getInvestmentCreditTermMonths()
                            .subtract(
                                BigDecimal.valueOf(investmentCreditPaymentRepository.findAllByManufacturerId(manufacturer.getId()).size())
                                    .subtract(BigDecimal.ONE)),
                        2, RoundingMode.UP);
                var interestAmount = calculateInvestmentCreditInterestAmount(debt, game.getInterestRateInvestmentCredit());
                var fullPayment = monthlyPrincipalPayment.add(interestAmount);
                if (fullPayment.compareTo(manufacturer.getBalance()) <= 0) {
                    newPayment.setPrincipalPayment(monthlyPrincipalPayment);
                    newPayment.setInterestAmount(interestAmount);
                    manufacturer.setBalance(manufacturer.getBalance().subtract(fullPayment));
                    manufacturer.setInvestmentCreditDebt(debt.subtract(monthlyPrincipalPayment));
                } else {
                    if (interestAmount.compareTo(manufacturer.getBalance()) <= 0) {
                        newPayment.setInterestAmount(interestAmount);
                        newPayment.setPrincipalPayment(manufacturer.getBalance().subtract(interestAmount));
                        if (newPayment.getPrincipalPayment().compareTo(BigDecimal.ZERO) > 0) {
                            manufacturer.setInvestmentCreditDebt(debt.subtract(newPayment.getPrincipalPayment()));
                        }
                    } else {
                        newPayment.setInterestAmount(manufacturer.getBalance());
                        newPayment.setPrincipalPayment(BigDecimal.ZERO);
                        manufacturer.setInvestmentCreditDebt(debt.add(interestAmount.subtract(manufacturer.getBalance())));
                    }
                    manufacturer.setBalance(BigDecimal.ZERO);
                    manufacturer.setInvestmentCreditTermMonths(manufacturer.getInvestmentCreditTermMonths().add(BigDecimal.ONE));
                }
                manufacturerRepository.save(manufacturer);
                investmentCreditPaymentRepository.save(newPayment);
                StatisticsInfo statisticsInfo = MANUFACTURER_STATISTICS_INFO.get(manufacturer.getId());
                statisticsInfo.setRepaidInvestmentCreditAmount(newPayment.getPrincipalPayment().add(newPayment.getInterestAmount()));
            }
        }
    }

//    private void makeInvestmentCreditPayments(Game game) {
//        var manufacturers = manufacturerService.getGameManufacturers(game.getId());
//        List<InvestmentCreditPayment> allPayments = investmentCreditPaymentRepository.findAllByManufacturerIdInAndNextDate(manufacturers.stream().map(Manufacturer::getId).toList(), Game.currentDay);
//        for (InvestmentCreditPayment payment : allPayments) {
//            var manufacturer = payment.getManufacturer();
//            if (!manufacturer.getInvestmentCreditIsRepaid()) {
//                var manufacturerPayments = investmentCreditPaymentRepository.findAllByManufacturerId(manufacturer.getId());
//
//                var principalPaymentsSum = manufacturerPayments
//                    .stream()
//                    .map(InvestmentCreditPayment::getPrincipalPayment)
//                    .filter(Objects::nonNull)
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//                var monthlyPrincipalPayment = manufacturer.getInvestmentCreditAmount()
//                    .divide(game.getInvestmentCreditTermMonths(), 2, RoundingMode.UP);
//                if (principalPaymentsSum.compareTo(manufacturer.getInvestmentCreditAmount()) < 0) { // проверка, что сумма платежей меньше суммы кредита(когда кто то погашал дополнительно вручную и верхняя проверка пройдет, но на самом деле уже внесена сумма), может быть такое, что пользователь погашал вручную и осталась задолженность меньше, чем ежемесячный платеж на основной долг и мы спишем больше? как проверять?
//                    var newPayment = InvestmentCreditPayment.builder()
//                        .manufacturer(manufacturer)
//                        .date(Game.currentDay)
//                        .nextDate(Game.currentDay + MONTH)
//                        .build();
//
//                    var interestAmount = calculateInvestmentCreditInterestAmount(
//                        principalPaymentsSum,
//                        manufacturer.getInvestmentCreditDebt(),
//                        manufacturer.getInvestmentCreditAmount(),
//                        game.getInterestRateInvestmentCredit());
//                    var fullPayment = monthlyPrincipalPayment.add(interestAmount).add(manufacturer.getInvestmentCreditDebt());
//                    if (fullPayment.compareTo(manufacturer.getBalance()) <= 0) {
//                        newPayment.setPrincipalPayment(monthlyPrincipalPayment.add(manufacturer.getInvestmentCreditDebt()));
//                        newPayment.setInterestAmount(interestAmount);
//                        manufacturer.setBalance(manufacturer.getBalance().subtract(fullPayment));
//                        manufacturer.setInvestmentCreditDebt(BigDecimal.ZERO);
//                    } else if (monthlyPrincipalPayment.add(interestAmount).compareTo(manufacturer.getBalance()) <= 0) {
//                        newPayment.setPrincipalPayment(monthlyPrincipalPayment);
//                        newPayment.setInterestAmount(interestAmount);
//                        manufacturer.setBalance(manufacturer.getBalance().subtract(monthlyPrincipalPayment).subtract(interestAmount));
//                    } else {
//                        if (interestAmount.compareTo(manufacturer.getBalance()) <= 0) {
//                            newPayment.setInterestAmount(interestAmount);
//                            newPayment.setPrincipalPayment(manufacturer.getBalance().subtract(interestAmount));
//                        } else {
//                            newPayment.setInterestAmount(manufacturer.getBalance());
//                            newPayment.setPrincipalPayment(BigDecimal.ZERO);
//                        }
//                        manufacturer.setInvestmentCreditDebt(manufacturer.getInvestmentCreditDebt()
//                            .add(fullPayment.subtract(manufacturer.getBalance())));
//                        manufacturer.setBalance(BigDecimal.ZERO);
//                    }
//                    investmentCreditPaymentRepository.save(newPayment);
//                } else {
//                    manufacturer.setInvestmentCreditIsRepaid(true);
//                }
//                manufacturerRepository.save(manufacturer);
//            }
//        }
//    }

    //    private BigDecimal calculateInvestmentCreditInterestAmount(BigDecimal principalPaymentSum,
//                                                               BigDecimal investmentCreditDebt,
//                                                               BigDecimal investmentCreditAmount,
//                                                               BigDecimal interestRateInvestmentCredit) {
//        return (interestRateInvestmentCredit.divide(BigDecimal.valueOf(100), 2, RoundingMode.UP))
//            .multiply(investmentCreditAmount.add(investmentCreditDebt).subtract(principalPaymentSum))
//            .divide(BigDecimal.valueOf(12), 2, RoundingMode.UP);
//        //списываем весь баланс в случае невозможности списать полный платеж, эту списанную сумму равную балансу мы разделяем на проценты и осн,  мы заносим в поле monthlyPrincipalPayment и проценты
//
//    }
    private BigDecimal calculateInvestmentCreditInterestAmount(BigDecimal investmentCreditDebt,
                                                               BigDecimal interestRateInvestmentCredit) {
        return (interestRateInvestmentCredit.divide(BigDecimal.valueOf(100), 2, RoundingMode.UP))
            .multiply(investmentCreditDebt)
            .divide(BigDecimal.valueOf(12), 2, RoundingMode.UP);
    }

    public static BigDecimal calculateBusinessCreditInterestAmount(BigDecimal amount,
                                                                   BigDecimal interestRateBusinessCredit,
                                                                   Integer timeToMarket) {
        return (interestRateBusinessCredit.divide(BigDecimal.valueOf(100), 2, RoundingMode.UP))
            .multiply(amount)
            .multiply(BigDecimal.valueOf(timeToMarket * 2 / 365d));
    }
}
