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

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    public static Map<Long, Integer> MANUFACTURER_CURRENT_PRODUCT_COUNT = new HashMap<>();
    public static Map<Long, BigDecimal> MANUFACTURER_REVENUE = new HashMap<>();

    @Override
    public Optional<ProductionParameters> getLastProductionParameters(Long manufacturerId) {
        return productionParametersRepository.findByManufacturerId(manufacturerId).stream()
            .max(Comparator.comparingInt(ProductionParameters::getStartDate));
    }

    @Override
    public Optional<Advertisement> getLastAdvertisement(Long manufacturerId) {
        return advertisementRepository.findByManufacturerId(manufacturerId).stream()
            .max(Comparator.comparingInt(Advertisement::getStartDate));
    }

    @Override
    public Optional<ProductionParameters> getActualProductionParameters(Long manufacturerId) {
        var actualParameters = productionParametersRepository.findByManufacturerId(manufacturerId).stream()
            .filter(p -> p.getStartDate() >= Game.currentDay - p.getTimeToMarket())
            .toList();
        if (actualParameters.isEmpty()) {
            return Optional.empty();
        } else if (actualParameters.size() == 1) {
            return Optional.of(actualParameters.get(0));
        } else {
            throw new IllegalStateException(
                "Некорректное состояние системы - несколько актуальных параметров производства игрока");
        }
    }

    @Override
    public Optional<Advertisement> getActualAdvertisement(Long manufacturerId) {
        var actualAdvertisements = advertisementRepository.findByManufacturerId(manufacturerId).stream()
            .filter(a -> a.getEndDate() >= Game.currentDay)
            .toList();
        if (actualAdvertisements.isEmpty()) {
            return Optional.empty();
        } else if (actualAdvertisements.size() == 1) {
            return Optional.of(actualAdvertisements.get(0));
        } else {
            throw new IllegalStateException(
                "Некорректное состояние системы - несколько актуальных параметров рекламы игрока");
        }
    }

    @Override
    public List<Manufacturer> getGameManufacturers(Long gameId) {
        return manufacturerRepository.findByGame_IdAndEnteredInitialProductionParametersIsTrue(gameId);
    }

    @Override
    public List<Manufacturer> getCreateGameManufacturers(Long gameId) {
        return manufacturerRepository.findByGame_Id(gameId);
    }

    @Override
    public List<ManufacturerParameters> getManufacturersActualProductionParameters(Long gameId) {
        return getCreateGameManufacturers(gameId).stream()
            .map(manufacturer -> {
                var account = accountService.getById(manufacturer.getAccount().getId());
                var productionParameters = getLastProductionParameters(manufacturer.getId());
                var advertisement = getLastAdvertisement(manufacturer.getId());
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

    @Override
    public void defineInitialProductionParameters(InitialProductionParameters initialProductionParameters, Long accountId, Game game) {
        var manufacturer = getManufacturerByAccountIdAndGameId(accountId, game.getId());
        manufacturer.setInvestmentCreditAmount(initialProductionParameters.getInvestmentCreditAmount());
        manufacturer.setEnteredInitialProductionParameters(true);
        manufacturer.setBalance(BigDecimal.ZERO);
        manufacturer.setInvestmentCreditTermMonths(game.getInvestmentCreditTermMonths());
        manufacturer.setInvestmentCreditDebt(initialProductionParameters.getInvestmentCreditAmount());
        manufacturer.setProductionCapacityPerDay(initialProductionParameters.getProductionCapacityPerDay());
        manufacturerRepository.save(manufacturer);

        saveAdvertisement(manufacturer, initialProductionParameters);
        var productionParameters = saveProductParameters(manufacturer, initialProductionParameters);
        productionParameters.setBusinessCreditAmount(BigDecimal.ZERO);
        var payment = InvestmentCreditPayment.builder()
            .date(0)
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
        manufacturer.setBalance(manufacturer.getBalance()
            .add(newProductionParameters.getBusinessCreditAmount().subtract(newProductionParameters.getProductionCosts())));
        manufacturerRepository.save(manufacturer);
        saveAdvertisement(manufacturer, newProductionParameters);
        var productionParameters = saveProductParameters(manufacturer, newProductionParameters);
        productionParameters.setInterestRateBusinessCredit(manufacturer.getGame().getInterestRateBusinessCredit());
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
                .nextDate(Game.currentDay + productionParameters.getTimeToMarket() * 2)
                .build();
            businessCreditPaymentRepository.save(payment);
        }
        productionParametersRepository.save(productionParameters);

    }

    @Override
    public ManufacturerFinancialStatus getManufacturerFinancialStatus(Game game, Long accountId) {
        var manufacturer = getManufacturerByAccountIdAndGameId(accountId, game.getId());
        var actualProductionParameters = getLastProductionParameters(manufacturer.getId()).get();
        var activeManufacturingCycle = Game.currentDay <= (actualProductionParameters.getStartDate() +
            actualProductionParameters.getTimeToMarket());

        return ManufacturerFinancialStatus.builder()
            .day(Game.currentDay)
            .balance(manufacturer.getBalance())
            .todayRevenue(MANUFACTURER_REVENUE.getOrDefault(manufacturer.getId(), BigDecimal.ZERO))
            .investmentCreditDebt(calculateManufacturerInvestmentCreditDebt(manufacturer, game))
            .businessCreditDebt(calculateManufacturerBusinessCreditDebt(manufacturer))
            .timeToMarketEndDay(actualProductionParameters.getStartDate() + actualProductionParameters.getTimeToMarket())
            .activeManufacturingCycle(activeManufacturingCycle)
            .build();
    }

    @Override
    public BigDecimal calculateManufacturerInvestmentCreditDebt(Manufacturer manufacturer, Game game) {
        var lastPayment = investmentCreditPaymentRepository.findAllByManufacturerId(
                manufacturer.getId()).stream()
            .max(Comparator.comparingInt(InvestmentCreditPayment::getDate)).get();
        return manufacturer.getInvestmentCreditDebt()
            .add(calculateInvestmentCreditInterestAmountOnCurrentDay(
                manufacturer.getInvestmentCreditDebt(),
                game.getInvestmentCreditTermMonths(),
                Game.currentDay - lastPayment.getDate()));
    }

    private BigDecimal calculateInvestmentCreditInterestAmountOnCurrentDay(BigDecimal investmentCreditDebt,
                                                                           BigDecimal interestRateInvestmentCredit,
                                                                           int daysAfterLastPayment) {
        return (interestRateInvestmentCredit.divide(BigDecimal.valueOf(100), 2, RoundingMode.UP))
            .multiply(investmentCreditDebt)
            .multiply(BigDecimal.valueOf(daysAfterLastPayment))
            .divide(BigDecimal.valueOf(365), 2, RoundingMode.UP);
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
    }

    @Override
    public List<ManufacturerParameters> getAllProductionParameters(Manufacturer manufacturer) {
        return productionParametersRepository.findByManufacturerId(manufacturer.getId()).stream()
            .map(productionParameters -> {
                var advertisementOptional = advertisementRepository.findByManufacturerIdAndStartDate(
                    manufacturer.getId(), productionParameters.getStartDate() + 1);
                var manufacturerParameters = ManufacturerParameters.builder()
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
                    .build();
                if (advertisementOptional.isPresent()) {
                    var advertisement = advertisementOptional.get();
                    manufacturerParameters.setAdvertisingStartDate(advertisement.getStartDate());
                    manufacturerParameters.setAdvertisingEndDate(advertisement.getEndDate());
                    manufacturerParameters.setAdvertisingIntensityIndex(advertisement.getIntensityIndex());
                    manufacturerParameters.setAdvertisingCost(advertisement.getCost());
                }
                return manufacturerParameters;
            }).sorted(Comparator.comparing(ManufacturerParameters::getStartDate).reversed())
            .toList();
    }

    @Override
    public List<ManufacturerParameters> getCompetitorsData(Long id) {
        var manufacturer = manufacturerRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        var gameId = manufacturer.getGame().getId();
        return getGameManufacturers(gameId).stream()
            .filter(m -> !Objects.equals(m.getId(), manufacturer.getId()))
            .map(m -> getLastProductionParameters(m.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(p -> {
                Optional<Advertisement> lastAdvertisement = advertisementRepository.findByManufacturerIdAndStartDate(
                    p.getManufacturer().getId(), p.getStartDate() + 1);
                var advertisingIntensityIndex = lastAdvertisement.isPresent() ? lastAdvertisement.get().getIntensityIndex() : 0;
                return ManufacturerParameters.builder()
                    .id(p.getManufacturer().getId())
                    .fullName(p.getManufacturer().getAccount().getFullName())
                    .assortment(p.getAssortment())
                    .price(p.getPrice())
                    .qualityIndex(p.getQualityIndex())
                    .advertisingIntensityIndex(advertisingIntensityIndex)
                    .build();
            })
            .toList();
    }

    @Override
    public Manufacturer getManufacturerByAccountIdAndGameId(Long accountId, Long gameId) {
        return manufacturerRepository.findByAccount_IdAndGame_Id(accountId, gameId);
    }

    private void saveAdvertisement(Manufacturer manufacturer, CommonProductionParameters productionParameters) {
        if (productionParameters.getAdvertisingIntensityIndex() != 0 && productionParameters.getAdvertisingDays() != 0) {
            var advertisement = Advertisement.builder()
                .manufacturer(manufacturer)
                .cost(productionParameters.getAdvertisingCost())
                .intensityIndex(productionParameters.getAdvertisingIntensityIndex())
                .startDate(Game.currentDay + 1)
                .endDate(Game.currentDay + productionParameters.getAdvertisingDays())
                .build();
            advertisementRepository.save(advertisement);
        }
    }

    private ProductionParameters saveProductParameters(Manufacturer manufacturer, CommonProductionParameters productionParameters) {
        var resultProductionParameters = ProductionParameters.builder()
            .manufacturer(manufacturer)
            .productCount(productionParameters.getProductCount())
            .price(productionParameters.getPrice())
            .costPrice(productionParameters.getCostPrice())
            .assortment(productionParameters.getAssortment())
            .qualityIndex(productionParameters.getQualityIndex())
            .productionCapacityPerDay(manufacturer.getProductionCapacityPerDay())
            .startDate(Game.currentDay)
            .timeToMarket(productionParameters.getTimeToMarket())
            .build();
        productionParametersRepository.save(resultProductionParameters);

        return resultProductionParameters;
    }

}
