package ru.itis.diploma.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itis.diploma.dto.InitialProductionParameters;
import ru.itis.diploma.exception.EntityNotFoundException;
import ru.itis.diploma.model.Advertisement;
import ru.itis.diploma.model.GameStatus;
import ru.itis.diploma.model.Manufacturer;
import ru.itis.diploma.model.ProductionParameters;
import ru.itis.diploma.repository.AdvertisementRepository;
import ru.itis.diploma.repository.GameRepository;
import ru.itis.diploma.repository.ManufacturerRepository;
import ru.itis.diploma.repository.ProductionParametersRepository;
import ru.itis.diploma.service.ManufacturerService;
import ru.itis.diploma.util.EconomicIndicatorsCalculator;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ManufacturerServiceImpl implements ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;
    private final ProductionParametersRepository productionParametersRepository;
    private final AdvertisementRepository advertisementRepository;
    private final GameRepository gameRepository;
    private final EconomicIndicatorsCalculator economicIndicatorsCalculator;

    public static final Map<Long, Integer> MANUFACTURER_CURRENT_PRODUCT_COUNT = new HashMap<>(); // тек кол-во товаров у каждого производителя(условно кэш в мапе Map<Id производителя, Тек кол-во товаров производителя на рыке>)

    @Override
    public ProductionParameters getActualProductionParameters(Long manufacturerId) {
        return productionParametersRepository.findByManufacturerId(manufacturerId).stream()
            .sorted(Comparator.comparingInt(ProductionParameters::getStartDate).reversed())
            .toList()
            .get(0);
    }

    @Override
    public Advertisement getActualAdvertisement(Long manufacturerId) {
        return advertisementRepository.findByManufacturerId(manufacturerId).stream()
            .sorted(Comparator.comparingInt(Advertisement::getStartDate).reversed())
            .toList()
            .get(0);
    }

    @Override
    public List<Manufacturer> getGameManufacturers(Long gameId) {
        return manufacturerRepository.findByGame_Id(gameId);
    }

    /**
     *  Сумма инвестиционного редита вычисляется на фронте по формуле:
     *  Сумма инвестиционного кредита = Создание производства + Реклама(EconomicIndicatorsCalculator) + начальное пр-во
     *  Затраты на создание пр-ва = базовая стоимость единицы мощности (productPower из параметров админа) * кол-во продукции(productionCapacityPerDay) / M
     *  Затраты на начальное производство продукции = Количество продукции(product count) * Себестоимость(посчитана на фронте))
     *
     *  Баланс = 0, тк потратили весь инвестиционный кредит
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
    }

    @Override
    public Manufacturer getManufacturerByAccountIdAndGameId(Long accountId, Long gameId) {
        return manufacturerRepository.findByAccount_IdAndGame_Id(accountId, gameId);
    }

    private void saveAdvertisement(Manufacturer manufacturer, InitialProductionParameters initialProductionParameters) {
        var advertisement = Advertisement.builder()
            .manufacturer(manufacturer)
            .cost(initialProductionParameters.getAdvertisingCost())
            .intensityIndex(initialProductionParameters.getAdvertisingIntensityIndex())
            .startDate(1)
            .endDate(initialProductionParameters.getAdvertisingDays())
            .build();
        advertisementRepository.save(advertisement);
    }

    /**
     * productionCapacityPerDay - любое, вводится производителем
     * По этому параметру высчитывается количество дней, необходимых для производства(timeToMarket)
     */
    private void saveProductParameters(Manufacturer manufacturer, InitialProductionParameters initialProductionParameters) {
        var productionParameters = ProductionParameters.builder()
            .manufacturer(manufacturer)
            .productCount(initialProductionParameters.getProductCount())
            .price(initialProductionParameters.getPrice())
            .costPrice(initialProductionParameters.getCostPrice())
            .assortment(initialProductionParameters.getAssortment())
            .qualityIndex(initialProductionParameters.getQualityIndex())
            .productionCapacityPerDay(initialProductionParameters.getProductionCapacityPerDay())
            .businessCreditAmount(BigDecimal.ZERO)
            .startDate(0)
            .timeToMarket(
                economicIndicatorsCalculator.calculateTimeToMarket(initialProductionParameters.getProductCount(),
                    initialProductionParameters.getProductionCapacityPerDay()))
            .build();
        productionParametersRepository.save(productionParameters);

        MANUFACTURER_CURRENT_PRODUCT_COUNT.put(manufacturer.getId(), 0);
    }

}
