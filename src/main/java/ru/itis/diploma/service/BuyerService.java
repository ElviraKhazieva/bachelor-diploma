package ru.itis.diploma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.itis.diploma.model.Game;
import ru.itis.diploma.model.Manufacturer;
import ru.itis.diploma.model.ProductionParameters;
import ru.itis.diploma.model.StatisticsInfo;
import ru.itis.diploma.model.TradingSessionResults;
import ru.itis.diploma.repository.ManufacturerRepository;
import ru.itis.diploma.repository.TradingSessionResultsRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ru.itis.diploma.service.CronService.MANUFACTURER_STATISTICS_INFO;
import static ru.itis.diploma.service.impl.ManufacturerServiceImpl.MANUFACTURER_CURRENT_PRODUCT_COUNT;
import static ru.itis.diploma.service.impl.ManufacturerServiceImpl.MANUFACTURER_REVENUE;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuyerService {

    private final ManufacturerRepository manufacturerRepository;
    private final TradingSessionResultsRepository tradingSessionResultsRepository;

    private static final Logger logger = LoggerFactory.getLogger(BuyerService.class);

    /*
        Каждый день генерируется количество товаров, которое купит покупатель, в диапазоне от ( 0 до (сумма всего товаров на рынке у производителей) )
        Производители отсортированы в порядке, у кого отношение цена/качество лучше
        Покупатель пытается купить мин(требуемое количество, количество товаров у пр-ля) количество товаров у каждого производителя в отсортированном списке
     */
    public void makePurchases(Game game, List<ProductionParameters> productionParametersList) {
        updateWornOutFlag(game);
        BigDecimal dailySpendingLimit = game.getDailySpendingLimit();
        int requiredQuantity = game.getPurchaseLimit() - getUnwornProductsCount(game);
        List<TradingSessionResults> tradingSessionResultsList = new ArrayList<>();
        List<Manufacturer> manufacturers = new ArrayList<>();

        logger.info("НАЧИНАЮ СОВЕРШАТЬ ПОКУПКИ...");
        for (ProductionParameters productionParameters : productionParametersList) {
            var manufacturer = productionParameters.getManufacturer();
            var manufacturerCurrentProductCount = MANUFACTURER_CURRENT_PRODUCT_COUNT.get(manufacturer.getId());

            int purchaseQuantity = calculatePurchaseQuantity(dailySpendingLimit, productionParameters.getPrice(),
                requiredQuantity, manufacturerCurrentProductCount);

            TradingSessionResults tradingSessionResults = TradingSessionResults.builder()
                .manufacturer(manufacturer)
                .productNumber(purchaseQuantity)
                .price(productionParameters.getPrice())
                .qualityIndex(productionParameters.getQualityIndex())
                .tradeDate(Game.currentDay)
                .isWornOut(false)
                .build();
            tradingSessionResultsList.add(tradingSessionResults);
            StatisticsInfo statisticsInfo = MANUFACTURER_STATISTICS_INFO.get(productionParameters.getManufacturer().getId());
            statisticsInfo.setProductsSold(purchaseQuantity);
            statisticsInfo.setPrice(productionParameters.getPrice());

            if (purchaseQuantity > 0) {
                logger.info("ПОКУПАЮ {} ТОВАРОВ", purchaseQuantity);
                BigDecimal totalPrice = productionParameters.getPrice().multiply(BigDecimal.valueOf(purchaseQuantity));
                MANUFACTURER_CURRENT_PRODUCT_COUNT.put(manufacturer.getId(), manufacturerCurrentProductCount - purchaseQuantity);
                MANUFACTURER_REVENUE.put(manufacturer.getId(), totalPrice);
                manufacturer.setBalance(manufacturer.getBalance().add(totalPrice));
                manufacturers.add(manufacturer);
                requiredQuantity -= purchaseQuantity;
                dailySpendingLimit = dailySpendingLimit.subtract(totalPrice);
            }
            logger.info("ОСТАЛОСЬ КУПИТЬ - {} ", requiredQuantity);
        }
        manufacturerRepository.saveAll(manufacturers);
        tradingSessionResultsRepository.saveAll(tradingSessionResultsList);
    }

    private int calculatePurchaseQuantity(BigDecimal dailySpendingLimit, BigDecimal productPrice, int requiredQuantity,
                                          int manufacturerCurrentProductCount) {
        int maxQuantityBySpendingLimit = dailySpendingLimit.divide(productPrice, RoundingMode.FLOOR).intValue();
        int purchaseQuantity = Math.min(requiredQuantity, manufacturerCurrentProductCount);
        return Math.min(purchaseQuantity, maxQuantityBySpendingLimit);
    }

    private void updateWornOutFlag(Game game) {
        tradingSessionResultsRepository.findAllByProductNumberGreaterThanZero().stream()
            .filter(t -> Objects.equals(t.getManufacturer().getGame().getId(), game.getId()))
            .forEach(t -> {
                var usageDays = Game.currentDay - t.getTradeDate();
                var productLifetime = t.getQualityIndex().multiply(BigDecimal.valueOf(game.getAbsoluteQualityProductLife()));
                if (BigDecimal.valueOf(usageDays).compareTo(productLifetime) >= 0) {
                    t.setIsWornOut(true);
                    tradingSessionResultsRepository.save(t);
                }
            });
    }

    private Integer getUnwornProductsCount(Game game) {
        Integer unwornProductsCount = tradingSessionResultsRepository.getUnwornProductsCountByGameId(game.getId());
        if (unwornProductsCount == null) return 0;
        return unwornProductsCount;
//        return tradingSessionResultsRepository.findAll().stream()
//            .filter(t -> Objects.equals(t.getManufacturer().getGame().getId(), game.getId())
//                && t.getIsWornOut().equals(Boolean.FALSE))
//            .map(TradingSessionResults::getProductNumber)
//            .reduce(0, Integer::sum);
    }

//    public void makePurchases(Game game, List<ProductionParameters> productionParametersList) {
//        updateWornOutFlag(game);
//        var dailySpendingLimit = game.getDailySpendingLimit();
//        var requiredQuantity = game.getPurchaseLimit() - getUnwornProductsCount(game);
//        List<TradingSessionResults> tradingSessionResultsList = new ArrayList<>();
//        List<Manufacturer> manufacturers = new ArrayList<>();
//
//        logger.info("НАЧИНАЮ СОВЕРШАТЬ ПОКУПКИ...");
//        for (ProductionParameters productionParameters : productionParametersList) {
//            var manufacturer = productionParameters.getManufacturer();
//            var manufacturerCurrentProductCount = MANUFACTURER_CURRENT_PRODUCT_COUNT.get(manufacturer.getId());
//            int purchaseQuantity = Math.min(requiredQuantity, manufacturerCurrentProductCount);
//            BigDecimal totalPrice = productionParameters.getPrice().multiply(BigDecimal.valueOf(purchaseQuantity));
//
//            if ((purchaseQuantity > 0) && (dailySpendingLimit.subtract(totalPrice).compareTo(BigDecimal.ZERO) >= 0) && (getUnwornProductsCount(game) + purchaseQuantity <= game.getPurchaseLimit())) {
//                logger.info("ПОКУПАЮ {} ТОВАРОВ", + purchaseQuantity);
//
//                TradingSessionResults tradingSessionResults = TradingSessionResults.builder()
//                    .manufacturer(manufacturer)
//                    .productNumber(purchaseQuantity)
//                    .price(productionParameters.getPrice())
//                    .qualityIndex(productionParameters.getQualityIndex())
//                    .tradeDate(Game.currentDay)
//                    .isWornOut(false)
//                    .build();
//                tradingSessionResultsList.add(tradingSessionResults);
//                MANUFACTURER_CURRENT_PRODUCT_COUNT.put(manufacturer.getId(), manufacturerCurrentProductCount - purchaseQuantity);
//                MANUFACTURER_REVENUE.put(manufacturer.getId(), totalPrice);
//                manufacturer.setBalance(manufacturer.getBalance().add(totalPrice));
//                manufacturers.add(manufacturer);
//                requiredQuantity -= purchaseQuantity;
//                dailySpendingLimit = dailySpendingLimit.subtract(totalPrice);
//            }
//
//            logger.info("ОСТАЛОСЬ КУПИТЬ - {} ", requiredQuantity);
//        }
//        manufacturerRepository.saveAll(manufacturers);
//        tradingSessionResultsRepository.saveAll(tradingSessionResultsList);
//    }
}
