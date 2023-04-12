package ru.itis.diploma.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itis.diploma.model.Advertisement;
import ru.itis.diploma.model.Game;
import ru.itis.diploma.model.ProductionParameters;
import ru.itis.diploma.repository.ManufacturerRepository;
import ru.itis.diploma.repository.TradingSessionResultsRepository;
import ru.itis.diploma.util.EconomicIndicatorsCalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

import static ru.itis.diploma.service.impl.ManufacturerServiceImpl.MANUFACTURER_CURRENT_PRODUCT_COUNT;

@Service
@RequiredArgsConstructor
public class CronService {

    private final ManufacturerService manufacturerService;
    private final ManufacturerRepository manufacturerRepository;
    private final TradingSessionResultsRepository tradingSessionResultsRepository;
    private final EconomicIndicatorsCalculator economicIndicatorsCalculator;
    private final BuyerService buyerService;
    private final PaymentService paymentService;

    public void doDaysActivities(Game game) {
        Game.currentDay++;
        var manufacturers = manufacturerService.getGameManufacturers(game.getId());
        var productionParametersList = manufacturers.stream()
            .map(manufacturer -> manufacturerService.getActualProductionParameters(manufacturer.getId()).get())
            .sorted(Comparator.comparingDouble(p -> calculateValue(game, (ProductionParameters) p)).reversed())
            .toList();
        produceManufacturersProductsToMarket(productionParametersList);
        buyerService.makePurchases(productionParametersList); //todo: сначала оуществляем покупки, потом считаем налоги? влияет на то, считаем ли в выручку сегодняшние покупки
        paymentService.makePayments(game);
    }

    private void produceManufacturersProductsToMarket(List<ProductionParameters> productionParametersList) {
        for (ProductionParameters productionParameters : productionParametersList) {
//            var timeToMarket = economicIndicatorsCalculator.calculateTimeToMarket(
//                productionParameters.getProductCount(), productionParameters.getProductionCapacityPerDay());
            var timeToMarket = productionParameters.getTimeToMarket();
            MANUFACTURER_CURRENT_PRODUCT_COUNT.putIfAbsent(productionParameters.getManufacturer().getId(), 0);// либо вернет прошлое значение либо положит 0
            if ((Game.currentDay - productionParameters.getStartDate()) <= timeToMarket) {
                if ((productionParameters.getStartDate() + timeToMarket) == Game.currentDay) {
                    MANUFACTURER_CURRENT_PRODUCT_COUNT.put(productionParameters.getManufacturer().getId(),
                        MANUFACTURER_CURRENT_PRODUCT_COUNT.get(productionParameters.getManufacturer().getId()) +
                            (productionParameters.getProductCount() - (timeToMarket - 1) *
                                productionParameters.getProductionCapacityPerDay()));
                } else {
                    MANUFACTURER_CURRENT_PRODUCT_COUNT.put(productionParameters.getManufacturer().getId(),
                        MANUFACTURER_CURRENT_PRODUCT_COUNT.get(productionParameters.getManufacturer().getId()) +
                            productionParameters.getProductionCapacityPerDay());
                }
            }
        }
    }


    private double calculateValue(Game game, ProductionParameters productionParameters) {
        BigDecimal assortmentWeight = game.getAssortmentWeight();//0.3; //todo: подсчитать из соцопроса рекомендуемые значения
        BigDecimal qualityWeight = game.getQualityWeight();//0.4;
        BigDecimal advertisementWeight = game.getAdvertisementWeight();//0.3;

        Advertisement advertisement = manufacturerService.getActualAdvertisement(productionParameters.getManufacturer().getId()).get();
        return productionParameters.getQualityIndex().multiply(qualityWeight)
            .add(BigDecimal.valueOf(advertisement.getIntensityIndex())).multiply(advertisementWeight)
            .add(BigDecimal.valueOf(productionParameters.getAssortment()).multiply(assortmentWeight))
            .divide(productionParameters.getPrice(), RoundingMode.HALF_UP).doubleValue();
    }


}
