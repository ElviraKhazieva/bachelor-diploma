package ru.itis.diploma.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itis.diploma.model.Advertisement;
import ru.itis.diploma.model.Game;
import ru.itis.diploma.model.Manufacturer;
import ru.itis.diploma.model.ProductionParameters;
import ru.itis.diploma.model.StatisticsInfo;
import ru.itis.diploma.model.TradingSessionResults;
import ru.itis.diploma.repository.StatisticsInfoRepository;
import ru.itis.diploma.repository.TradingSessionResultsRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.itis.diploma.service.impl.ManufacturerServiceImpl.MANUFACTURER_CURRENT_PRODUCT_COUNT;

@Service
@RequiredArgsConstructor
public class CronService {

    private final ManufacturerService manufacturerService;
    private final TradingSessionResultsRepository tradingSessionResultsRepository;
    private final BuyerService buyerService;
    private final PaymentService paymentService;
    private final StatisticsInfoRepository statisticsInfoRepository;

    public static Map<Long, StatisticsInfo> MANUFACTURER_STATISTICS_INFO = new HashMap<>();

    public void doDaysActivities(Game game) {
        Game.currentDay++;
        var manufacturers = manufacturerService.getGameManufacturers(game.getId());
        var purchaseCountsByManufacturerId = getPurchaseCountsByManufacturerId(
            manufacturers.stream()
                .map(Manufacturer::getId)
                .toList(), game.getHabitTrackingDays());
        var productionParametersList = manufacturers.stream()
            .map(manufacturer -> manufacturerService.getLastProductionParameters(manufacturer.getId()).get())
            .sorted(Comparator.comparingDouble(p -> {
                Integer manufacturerPurchaseCounts = purchaseCountsByManufacturerId.get(((ProductionParameters) p).getManufacturer().getId());
                if (manufacturerPurchaseCounts == null) {
                    manufacturerPurchaseCounts = 0;
                }
                return calculateValue(game, (ProductionParameters) p, manufacturerPurchaseCounts);
            }).reversed())
            .toList();
        produceManufacturersProductsToMarket(productionParametersList);
        buyerService.makePurchases(game, productionParametersList);
        paymentService.makePayments(game);
        setCurrentCreditDebtsAndBalanceToStatisticsInfo(game);
        statisticsInfoRepository.saveAll(MANUFACTURER_STATISTICS_INFO.values());
    }

    private void setCurrentCreditDebtsAndBalanceToStatisticsInfo(Game game) {
        var manufacturers = manufacturerService.getGameManufacturers(game.getId());
        manufacturers.forEach(m -> {
            StatisticsInfo statisticsInfo = MANUFACTURER_STATISTICS_INFO.get(m.getId());
            statisticsInfo.setBalance(m.getBalance());
            statisticsInfo.setCurrentInvestmentCreditDebtAmount(manufacturerService.calculateManufacturerInvestmentCreditDebt(m, game));
            statisticsInfo.setCurrentBusinessCreditDebtAmount(manufacturerService.calculateManufacturerBusinessCreditDebt(m));
        });
    }

    private void produceManufacturersProductsToMarket(List<ProductionParameters> productionParametersList) {
        for (ProductionParameters productionParameters : productionParametersList) {
            var statisticsInfo = new StatisticsInfo();
            statisticsInfo.setManufacturer(productionParameters.getManufacturer());
            statisticsInfo.setProductionCapacityPerDay(productionParameters.getProductionCapacityPerDay());
            var timeToMarket = productionParameters.getTimeToMarket();
            var productsProduced = 0;
            MANUFACTURER_CURRENT_PRODUCT_COUNT.putIfAbsent(productionParameters.getManufacturer().getId(), 0);// либо вернет прошлое значение либо положит 0
            if ((Game.currentDay - productionParameters.getStartDate()) <= timeToMarket) {
                if ((productionParameters.getStartDate() + timeToMarket) == Game.currentDay) {
                    productsProduced = productionParameters.getProductCount() - (timeToMarket - 1) *
                        productionParameters.getProductionCapacityPerDay();
                    MANUFACTURER_CURRENT_PRODUCT_COUNT.put(productionParameters.getManufacturer().getId(),
                        MANUFACTURER_CURRENT_PRODUCT_COUNT.get(productionParameters.getManufacturer().getId()) +
                            productsProduced);
                } else {
                    productsProduced = productionParameters.getProductionCapacityPerDay();
                    MANUFACTURER_CURRENT_PRODUCT_COUNT.put(productionParameters.getManufacturer().getId(),
                        MANUFACTURER_CURRENT_PRODUCT_COUNT.get(productionParameters.getManufacturer().getId()) +
                            productsProduced);
                }
            }
            statisticsInfo.setProductsProduced(productsProduced);
            statisticsInfo.setTradeDate(Game.currentDay);
            statisticsInfo.setPaidTaxesAmount(BigDecimal.ZERO);
            statisticsInfo.setCurrentInvestmentCreditDebtAmount(BigDecimal.ZERO);
            statisticsInfo.setCurrentBusinessCreditDebtAmount(BigDecimal.ZERO);
            statisticsInfo.setRepaidInvestmentCreditAmount(BigDecimal.ZERO);
            statisticsInfo.setRepaidBusinessCreditAmount(BigDecimal.ZERO);
            MANUFACTURER_STATISTICS_INFO.put(productionParameters.getManufacturer().getId(), statisticsInfo);
        }
    }

    public Map<Long, Integer> getPurchaseCountsByManufacturerId(List<Long> manufacturerIds, int habitTrackingDays) {
        int startDate = Game.currentDay - habitTrackingDays > 0 ? Game.currentDay - habitTrackingDays : 1;
        List<TradingSessionResults> recentPurchases = tradingSessionResultsRepository
            .findByTradeDateGreaterThanEqualAndManufacturerIdIn(startDate, manufacturerIds);
        return recentPurchases.stream()
            .collect(Collectors.groupingBy(tr -> tr.getManufacturer().getId(),
                Collectors.summingInt(TradingSessionResults::getProductNumber)));
    }

    private double calculateValue(Game game, ProductionParameters productionParameters, int manufacturerPurchaseCounts) {
        BigDecimal assortmentWeight = game.getAssortmentWeight();
        BigDecimal qualityWeight = game.getQualityWeight();
        BigDecimal advertisementWeight = game.getAdvertisementWeight();
        BigDecimal habitWeight = game.getHabitWeight();

        var habit = (double) manufacturerPurchaseCounts / (game.getPurchaseLimit() * game.getHabitTrackingDays());
        Optional<Advertisement> lastAdvertisement = manufacturerService.getLastAdvertisement(productionParameters.getManufacturer().getId());
        var intensityIndex = lastAdvertisement.isPresent() ? lastAdvertisement.get().getIntensityIndex() : 0;
        return productionParameters.getQualityIndex().multiply(qualityWeight)
            .add(BigDecimal.valueOf(intensityIndex).divide(BigDecimal.valueOf(7), 3, RoundingMode.HALF_UP)).multiply(advertisementWeight)
            .add(BigDecimal.valueOf(productionParameters.getAssortment()).divide(BigDecimal.valueOf(productionParameters.getProductCount()), 3, RoundingMode.HALF_UP)).multiply(assortmentWeight)
            .add(BigDecimal.valueOf(habit).multiply(habitWeight))
            .divide(productionParameters.getPrice(), 6, RoundingMode.HALF_UP).doubleValue();
    }


}
