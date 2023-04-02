package ru.itis.diploma.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itis.diploma.model.Advertisement;
import ru.itis.diploma.model.Game;
import ru.itis.diploma.model.Manufacturer;
import ru.itis.diploma.model.ProductionParameters;
import ru.itis.diploma.model.TradingSessionResults;
import ru.itis.diploma.repository.ManufacturerRepository;
import ru.itis.diploma.repository.TradingSessionResultsRepository;
import ru.itis.diploma.util.EconomicIndicatorsCalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static ru.itis.diploma.service.impl.ManufacturerServiceImpl.MANUFACTURER_CURRENT_PRODUCT_COUNT;

@Service
@RequiredArgsConstructor
public class BuyerService {

    private final GameService gameService;
    private final ManufacturerService manufacturerService;
    private final ManufacturerRepository manufacturerRepository;
    private final TradingSessionResultsRepository tradingSessionResultsRepository;

    private final EconomicIndicatorsCalculator economicIndicatorsCalculator;

    private Integer requiredQuantity;

    public void makePurchases(List<Long> manufacturerIds) {
        Game.currentDay ++;

        var productionParametersList = manufacturerIds.stream()
            .map(manufacturerService::getActualProductionParameters)
            .sorted(Comparator.comparingDouble(this::calculateValue).reversed())
            .toList();

        produceManufacturersProductsToMarket(productionParametersList);

        List<TradingSessionResults> tradingSessionResultsList = new ArrayList<>();
        List<Manufacturer> manufacturers = new ArrayList<>();
        System.out.println("НАЧИНАЮ СОВЕРШАТЬ ПОКУПКИ...");
        for (ProductionParameters productionParameters : productionParametersList) {
            var manufacturer = productionParameters.getManufacturer();
            var manufacturerCurrentProductCount = MANUFACTURER_CURRENT_PRODUCT_COUNT.get(manufacturer.getId());
            int purchaseQuantity = Math.min(requiredQuantity, manufacturerCurrentProductCount);
            System.out.println("ПОКУПАЮ " + purchaseQuantity + "ТОВАРОВ");
            BigDecimal totalPrice = productionParameters.getPrice().multiply(BigDecimal.valueOf(purchaseQuantity));

            TradingSessionResults tradingSessionResults = TradingSessionResults.builder()
                .manufacturer(manufacturer)
                .productNumber(purchaseQuantity)
                .price(totalPrice)
                .tradeDate(Game.currentDay)
                .build();
            tradingSessionResultsList.add(tradingSessionResults);

            MANUFACTURER_CURRENT_PRODUCT_COUNT.put(manufacturer.getId(), manufacturerCurrentProductCount - purchaseQuantity);
            manufacturer.setBalance(manufacturer.getBalance().add(totalPrice));
            manufacturers.add(manufacturer);

            requiredQuantity -= purchaseQuantity;
            System.out.println("ОСТАОСЬ КУПИТЬ - " + requiredQuantity);
        }
        manufacturerRepository.saveAll(manufacturers);
        tradingSessionResultsRepository.saveAll(tradingSessionResultsList);
    }

    private void produceManufacturersProductsToMarket(List<ProductionParameters> productionParametersList) {
        for (ProductionParameters productionParameters : productionParametersList) {
            var timeToMarket = economicIndicatorsCalculator.calculateTimeToMarket(
                productionParameters.getProductCount(), productionParameters.getProductionCapacityPerDay());
            if ((Game.currentDay - productionParameters.getStartDate()) <= timeToMarket) {
                if ((productionParameters.getStartDate() + timeToMarket) == Game.currentDay) {
                    MANUFACTURER_CURRENT_PRODUCT_COUNT.put(productionParameters.getManufacturer().getId(),
                        productionParameters.getProductCount() - (timeToMarket - 1) *
                            productionParameters.getProductionCapacityPerDay());
                } else {
                    MANUFACTURER_CURRENT_PRODUCT_COUNT.put(productionParameters.getManufacturer().getId(),
                        productionParameters.getProductionCapacityPerDay());
                }
            }
        }
    }


    private double calculateValue(ProductionParameters productionParameters) {
        double assortmentWeight = 0.3; //todo: вынести в параметры игры(админа)
        double qualityWeight = 0.4;
        double advertisementWeight = 0.3;

        Advertisement advertisement = manufacturerService.getActualAdvertisement(productionParameters.getManufacturer().getId());
        return productionParameters.getQualityIndex().multiply(BigDecimal.valueOf(qualityWeight))
            .add(BigDecimal.valueOf(advertisement.getIntensityIndex())).multiply(BigDecimal.valueOf(advertisementWeight))
            .add(BigDecimal.valueOf(productionParameters.getAssortment()).multiply(BigDecimal.valueOf(assortmentWeight)))
            .divide(productionParameters.getPrice(), RoundingMode.HALF_UP).doubleValue();
    }

    public void setRequiredQuantity(Integer requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }
}
