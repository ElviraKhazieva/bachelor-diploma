package ru.itis.diploma.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itis.diploma.dto.CommonProductionParameters;
import ru.itis.diploma.dto.InitialProductionParameters;
import ru.itis.diploma.dto.ManufacturerFinancialStatus;
import ru.itis.diploma.dto.ManufacturerParameters;
import ru.itis.diploma.dto.NewProductionParameters;
import ru.itis.diploma.model.Advertisement;
import ru.itis.diploma.model.BusinessCreditPayment;
import ru.itis.diploma.model.Game;
import ru.itis.diploma.model.InvestmentCreditPayment;
import ru.itis.diploma.model.Manufacturer;
import ru.itis.diploma.model.ProductionParameters;
import ru.itis.diploma.repository.AdvertisementRepository;
import ru.itis.diploma.repository.BusinessCreditPaymentRepository;
import ru.itis.diploma.repository.InvestmentCreditPaymentRepository;
import ru.itis.diploma.repository.ManufacturerRepository;
import ru.itis.diploma.repository.ProductionParametersRepository;
import ru.itis.diploma.service.AccountService;
import ru.itis.diploma.service.ManufacturerService;
import ru.itis.diploma.service.PaymentService;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ManufacturerServiceImpl implements ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;
    private final AccountService accountService;
    private final ProductionParametersRepository productionParametersRepository;
    private final AdvertisementRepository advertisementRepository;
    private final BusinessCreditPaymentRepository businessCreditPaymentRepository;
    private final InvestmentCreditPaymentRepository investmentCreditPaymentRepository;
    private static final int MONTH = 30;
    public static final Map<Long, Integer> MANUFACTURER_CURRENT_PRODUCT_COUNT = new HashMap<>(); // тек кол-во товаров у каждого производителя(условно кэш в мапе Map<Id производителя, Тек кол-во товаров производителя на рыке>)
    public static final Map<Long, BigDecimal> MANUFACTURER_REVENUE = new HashMap<>(); // тек выручка привязана ко дню после каждой торговой сессии перезаписываем

    @Override
    public Optional<ProductionParameters> getActualProductionParameters(Long manufacturerId) {
        return productionParametersRepository.findByManufacturerId(manufacturerId).stream()
            .max(Comparator.comparingInt(ProductionParameters::getStartDate));
    }

    @Override
    public Optional<Advertisement> getActualAdvertisement(Long manufacturerId) {
        return advertisementRepository.findByManufacturerId(manufacturerId).stream()
            .max(Comparator.comparingInt(Advertisement::getStartDate));
    }

    @Override
    public List<Manufacturer> getGameManufacturers(Long gameId) {
        return manufacturerRepository.findByGame_Id(gameId);
    }

    @Override
    public List<ManufacturerParameters> getManufacturersActualProductionParameters(Long gameId) {
        return getGameManufacturers(gameId).stream()
            .map(manufacturer -> {
                var account = accountService.getById(manufacturer.getAccount().getId());
                var productionParameters = getActualProductionParameters(manufacturer.getId());
                var advertisement = getActualAdvertisement(manufacturer.getId());
                ManufacturerParameters manufacturerParameters = ManufacturerParameters.builder()
                    .id(manufacturer.getId())
                    .fullName(account.getFullName())
                    .investmentCreditAmount(manufacturer.getInvestmentCreditAmount())
                    .balance(manufacturer.getBalance())
                    .build();
                productionParameters.ifPresent(p -> {
                    manufacturerParameters.setTimeToMarket(p.getTimeToMarket());
                    manufacturerParameters.setCostPrice(p.getCostPrice());
                    manufacturerParameters.setPrice(p.getPrice());
                    manufacturerParameters.setProductCount(p.getProductCount());
                    manufacturerParameters.setAssortment(p.getAssortment());
                    manufacturerParameters.setProductionCapacityPerDay(p.getProductionCapacityPerDay());
                    manufacturerParameters.setQualityIndex(p.getQualityIndex());
                    manufacturerParameters.setBusinessCreditAmount(p.getBusinessCreditAmount());
                    manufacturerParameters.setInterestRateBusinessCredit(p.getInterestRateBusinessCredit());
                    manufacturerParameters.setStartDate(p.getStartDate());
                });
                advertisement.ifPresent(a -> {
                    manufacturerParameters.setAdvertisingStartDate(a.getStartDate());
                    manufacturerParameters.setAdvertisingEndDate(a.getEndDate());
                    manufacturerParameters.setAdvertisingIntensityIndex(a.getIntensityIndex());
                    manufacturerParameters.setAdvertisingCost(a.getCost());
                });
                return manufacturerParameters;
            }).toList();
    }

    /**
     * Сумма инвестиционного редита вычисляется на фронте по формуле:
     * Сумма инвестиционного кредита = Создание производства + Реклама(EconomicIndicatorsCalculator) + начальное пр-во
     * Затраты на создание пр-ва = базовая стоимость единицы мощности (productPower из параметров админа) * кол-во продукции(productionCapacityPerDay) / M
     * Затраты на начальное производство продукции = Количество продукции(product count) * Себестоимость(посчитана на фронте))
     * <p>
     * Баланс = 0, тк потратили весь инвестиционный кредит
     */
    @Override
    public void defineInitialProductionParameters(InitialProductionParameters initialProductionParameters, Long accountId, Long gameId) {
        var manufacturer = getManufacturerByAccountIdAndGameId(accountId, gameId);
        manufacturer.setInvestmentCreditAmount(initialProductionParameters.getInvestmentCreditAmount());
        manufacturer.setEnteredInitialProductionParameters(true);
        manufacturer.setBalance(BigDecimal.ZERO);
        manufacturerRepository.save(manufacturer);

        saveAdvertisement(manufacturer, initialProductionParameters);
        var productionParameters = saveProductParameters(manufacturer, initialProductionParameters);
        productionParameters.setBusinessCreditAmount(BigDecimal.ZERO);
        var payment = InvestmentCreditPayment.builder()
            .manufacturer(manufacturer)
            .principalPayment(BigDecimal.ZERO)
            .interestAmount(BigDecimal.ZERO)
            .nextDate(Game.currentDay + MONTH)
            .build();
        investmentCreditPaymentRepository.save(payment);
        MANUFACTURER_CURRENT_PRODUCT_COUNT.put(manufacturer.getId(), 0);
    }

    @Override
    public void defineNewProductionParameters(NewProductionParameters newProductionParameters, Long accountId, Game game) {
        var manufacturer = getManufacturerByAccountIdAndGameId(accountId, game.getId());

        // разница между суммой бизнес кредита и затратами на производство(нужны только для помощи с определением суммы бизнес-кредита?)
        manufacturer.setBalance(manufacturer.getBalance().add(newProductionParameters.getBusinessCreditAmount().subtract(newProductionParameters.getProductionCosts())));
        manufacturerRepository.save(manufacturer);
        saveAdvertisement(manufacturer, newProductionParameters);
        var productionParameters = saveProductParameters(manufacturer, newProductionParameters);
        productionParameters.setBusinessCreditAmount(newProductionParameters.getBusinessCreditAmount());
        if (productionParameters.getBusinessCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
            var payment = BusinessCreditPayment.builder()
                .productionParameters(productionParameters)
                .amount(BigDecimal.ZERO)
                .nextAmount(productionParameters.getBusinessCreditAmount()
                    .add(PaymentService.calculateBusinessCreditInterestAmount(
                        productionParameters.getBusinessCreditAmount(),
                        game.getInterestRateBusinessCredit(),
                        productionParameters.getTimeToMarket())))
//                .principalPayment(BigDecimal.ZERO)
//                .interestAmount(BigDecimal.ZERO)
                .nextDate(Game.currentDay + productionParameters.getTimeToMarket() * 2)
                .build();
            businessCreditPaymentRepository.save(payment);
        }
        productionParametersRepository.save(productionParameters);

    }

    @Override
    public ManufacturerFinancialStatus getManufacturerFinancialStatus(Long gameId, Long accountId) {
        var manufacturer = getManufacturerByAccountIdAndGameId(accountId, gameId);
        var actualProductionParameters = getActualProductionParameters(manufacturer.getId()).get();
        var activeManufacturingCycle = Game.currentDay <= (actualProductionParameters.getStartDate() + actualProductionParameters.getTimeToMarket());

        return ManufacturerFinancialStatus.builder()
            .day(Game.currentDay)
            .balance(manufacturer.getBalance())
            .todayRevenue(MANUFACTURER_REVENUE.getOrDefault(manufacturer.getId(), BigDecimal.ZERO))
            .investmentCreditDebt(calculateManufacturerInvestmentCreditDebt(manufacturer))
            .businessCreditDebt(calculateManufacturerBusinessCreditDebt(manufacturer))
            .timeToMarketEndDay(actualProductionParameters.getStartDate() + actualProductionParameters.getTimeToMarket())
            .activeManufacturingCycle(activeManufacturingCycle)
            .build();
    }

    @Override
    public BigDecimal calculateManufacturerInvestmentCreditDebt(Manufacturer manufacturer) {
        BigDecimal debt = manufacturer.getInvestmentCreditAmount().subtract(
            investmentCreditPaymentRepository.findAllByManufacturerId(manufacturer.getId()).stream()
                .map(InvestmentCreditPayment::getPrincipalPayment)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        return manufacturer.getInvestmentCreditDebt() != null ? debt.add(manufacturer.getInvestmentCreditDebt()) : debt;

    }

    @Override
    public BigDecimal calculateManufacturerBusinessCreditDebt(Manufacturer manufacturer) {
        List<ProductionParameters> manufacturerProductionParameters = productionParametersRepository.findByManufacturerId(manufacturer.getId());
        return businessCreditPaymentRepository.findAllByProductionParametersIdInAndNextDateAfter(
                manufacturerProductionParameters.stream()
                    .map(ProductionParameters::getId)
                    .toList(),
                Game.currentDay).stream()
            .map(BusinessCreditPayment::getNextAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
//        BigDecimal debt = productionParameters.getBusinessCreditAmount().subtract(
//            businessCreditPaymentRepository.findAllByProductionParametersId(productionParameters.getId()).stream()
////                .map(BusinessCreditPayment::getPrincipalPayment)
//                .map(BusinessCreditPayment::getAmount)
//                .filter(Objects::nonNull)
//                .reduce(BigDecimal.ZERO, BigDecimal::add));
//        // .add(productionParameters.getBusinessCreditDebt());
//        return productionParameters.getBusinessCreditDebt() != null ? debt.add(productionParameters.getBusinessCreditDebt()) : debt;
    }

    @Override
    public List<ManufacturerParameters> getAllProductionParameters(Manufacturer manufacturer) {
        return productionParametersRepository.findByManufacturerId(manufacturer.getId()).stream()
            .map(productionParameters -> {
                var advertisement = advertisementRepository.findByManufacturerIdAndStartDate(
                    manufacturer.getId(), productionParameters.getStartDate() + 1);
                return ManufacturerParameters.builder()
                    .startDate(productionParameters.getStartDate())
                    .timeToMarket(productionParameters.getTimeToMarket())
                    .costPrice(productionParameters.getCostPrice())
                    .price(productionParameters.getPrice())
                    .productCount(productionParameters.getProductCount())
                    .assortment(productionParameters.getAssortment())
                    .productionCapacityPerDay(productionParameters.getProductionCapacityPerDay())
                    .qualityIndex(productionParameters.getQualityIndex())
                    .businessCreditAmount(productionParameters.getBusinessCreditAmount())
                    .interestRateBusinessCredit(productionParameters.getInterestRateBusinessCredit())
                    .advertisingStartDate(advertisement.getStartDate())
                    .advertisingEndDate(advertisement.getEndDate())
                    .advertisingIntensityIndex(advertisement.getIntensityIndex())
                    .advertisingCost(advertisement.getCost())
                    .build();
            }).sorted(Comparator.comparing(ManufacturerParameters::getStartDate).reversed())
            .toList();
    }

    @Override
    public Manufacturer getManufacturerByAccountIdAndGameId(Long accountId, Long gameId) {
        return manufacturerRepository.findByAccount_IdAndGame_Id(accountId, gameId);
    }

    private void saveAdvertisement(Manufacturer manufacturer, CommonProductionParameters productionParameters) {
        var advertisement = Advertisement.builder()
            .manufacturer(manufacturer)
            .cost(productionParameters.getAdvertisingCost())
            .intensityIndex(productionParameters.getAdvertisingIntensityIndex())
            .startDate(Game.currentDay + 1)
            .endDate(Game.currentDay + productionParameters.getAdvertisingDays())
            .build();
        advertisementRepository.save(advertisement);
    }

    /**
     * productionCapacityPerDay - любое, вводится производителем
     * По этому параметру высчитывается количество дней, необходимых для производства(timeToMarket)
     */
    private ProductionParameters saveProductParameters(Manufacturer manufacturer, CommonProductionParameters productionParameters) {
        var resultProductionParameters = ProductionParameters.builder()
            .manufacturer(manufacturer)
            .productCount(productionParameters.getProductCount())
            .price(productionParameters.getPrice())
            .costPrice(productionParameters.getCostPrice())
            .assortment(productionParameters.getAssortment())
            .qualityIndex(productionParameters.getQualityIndex())
            .productionCapacityPerDay(productionParameters.getProductionCapacityPerDay())
            .interestRateBusinessCredit(manufacturer.getGame().getInterestRateBusinessCredit())

//            .businessCreditDebt(BigDecimal.ZERO)
            .startDate(Game.currentDay)
            .timeToMarket(productionParameters.getTimeToMarket())
            .build();
        productionParametersRepository.save(resultProductionParameters);


        return resultProductionParameters;
    }

}
