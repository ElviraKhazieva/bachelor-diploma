package ru.itis.diploma.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itis.diploma.dto.CommonProductionParameters;
import ru.itis.diploma.dto.EditProductionParameters;
import ru.itis.diploma.dto.InitialProductionParameters;
import ru.itis.diploma.dto.ManufacturerFinancialStatus;
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
import ru.itis.diploma.service.ManufacturerService;

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

    /**
     * Сумма инвестиционного редита вычисляется на фронте по формуле:
     * Сумма инвестиционного кредита = Создание производства + Реклама(EconomicIndicatorsCalculator) + начальное пр-во
     * Затраты на создание пр-ва = базовая стоимость единицы мощности (productPower из параметров админа) * кол-во продукции(productionCapacityPerDay) / M
     * Затраты на начальное производство продукции = Количество продукции(product count) * Себестоимость(посчитана на фронте))
     * <p>
     * Баланс = 0, тк потратили весь инвестиционный кредит
     */
    @Override
    public void enterInitialProductionParameters(InitialProductionParameters initialProductionParameters, Long accountId, Long gameId) {
        var manufacturer = getManufacturerByAccountIdAndGameId(accountId, gameId);
        manufacturer.setInvestmentCreditAmount(initialProductionParameters.getInvestmentCreditAmount());
        manufacturer.setEnteredInitialProductionParameters(true);
        manufacturer.setBalance(BigDecimal.ZERO);
        manufacturerRepository.save(manufacturer);

        saveAdvertisement(manufacturer, initialProductionParameters);
        saveProductParameters(manufacturer, initialProductionParameters);

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
    public void editProductionParameters(EditProductionParameters editProductionParameters, Long accountId, Long gameId) {
        var manufacturer = getManufacturerByAccountIdAndGameId(accountId, gameId);
        // разница между суммой бизнес кредита и затратами на производство(нужны только для помощи с определением суммы бизнес-кредита?)
        manufacturer.setBalance(manufacturer.getBalance().add(editProductionParameters.getBusinessCreditAmount().subtract(editProductionParameters.getProductionCosts())));
        manufacturerRepository.save(manufacturer);
        saveAdvertisement(manufacturer, editProductionParameters);
        var productionParameters = saveProductParameters(manufacturer, editProductionParameters);
        productionParameters.setBusinessCreditAmount(editProductionParameters.getBusinessCreditAmount());

        if (productionParameters.getBusinessCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
            var payment = BusinessCreditPayment.builder()
                .productionParameters(productionParameters)
                .principalPayment(BigDecimal.ZERO)
                .interestAmount(BigDecimal.ZERO)
                .nextDate(Game.currentDay + productionParameters.getTimeToMarket() * 2)
                .build();
            businessCreditPaymentRepository.save(payment);
            productionParameters.setBusinessCreditIsRepaid(false);
        } else {
            productionParameters.setBusinessCreditIsRepaid(true);
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
            .businessCreditDebt(calculateManufacturerBusinessCreditDebt(actualProductionParameters))
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
    public BigDecimal calculateManufacturerBusinessCreditDebt(ProductionParameters productionParameters) {
        BigDecimal debt = productionParameters.getBusinessCreditAmount().subtract(
            businessCreditPaymentRepository.findAllByProductionParametersId(productionParameters.getId()).stream()
                .map(BusinessCreditPayment::getPrincipalPayment)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        // .add(productionParameters.getBusinessCreditDebt());
        return productionParameters.getBusinessCreditDebt() != null ? debt.add(productionParameters.getBusinessCreditDebt()) : debt;
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
            .businessCreditAmount(BigDecimal.ZERO)
            .businessCreditDebt(BigDecimal.ZERO)
            .startDate(Game.currentDay)
            .timeToMarket(productionParameters.getTimeToMarket())
            .build();
        productionParametersRepository.save(resultProductionParameters);


        return resultProductionParameters;
    }

}
