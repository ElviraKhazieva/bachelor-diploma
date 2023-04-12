package ru.itis.diploma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.itis.diploma.model.Game;
import ru.itis.diploma.model.Manufacturer;
import ru.itis.diploma.model.ProductionParameters;
import ru.itis.diploma.model.TradingSessionResults;
import ru.itis.diploma.repository.ManufacturerRepository;
import ru.itis.diploma.repository.TradingSessionResultsRepository;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static ru.itis.diploma.service.impl.ManufacturerServiceImpl.MANUFACTURER_CURRENT_PRODUCT_COUNT;
import static ru.itis.diploma.service.impl.ManufacturerServiceImpl.MANUFACTURER_REVENUE;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuyerService {

    private final ManufacturerRepository manufacturerRepository;
    private final TradingSessionResultsRepository tradingSessionResultsRepository;
    private final Random random = new Random();

    private static final Logger logger = LoggerFactory.getLogger(BuyerService.class);

    /*
        Каждый день генерируется количество товаров, которое купит покупатель, в диапазоне от ( 0 до (сумма всего товаров на рынке у производителей) )
        Производители отсортированы в порядке, у кого отношение цена/качество лучше
        Покупатель пытается купить мин(требуемое количество, количество товаров у пр-ля) количество товаров у каждого производителя в отсортированном списке
     */
    @Transactional
    public void makePurchases(List<ProductionParameters> productionParametersList) {
        var requiredQuantity = random.nextInt(1 + MANUFACTURER_CURRENT_PRODUCT_COUNT.values().stream().reduce(0, Integer::sum));
        List<TradingSessionResults> tradingSessionResultsList = new ArrayList<>();
        List<Manufacturer> manufacturers = new ArrayList<>();

        logger.info("НАЧИНАЮ СОВЕРШАТЬ ПОКУПКИ...");
        for (ProductionParameters productionParameters : productionParametersList) {
            var manufacturer = productionParameters.getManufacturer();
            var manufacturerCurrentProductCount = MANUFACTURER_CURRENT_PRODUCT_COUNT.get(manufacturer.getId());
            int purchaseQuantity = Math.min(requiredQuantity, manufacturerCurrentProductCount);
            logger.info("ПОКУПАЮ {} ТОВАРОВ", +purchaseQuantity);

            if (purchaseQuantity > 0) {
                BigDecimal totalPrice = productionParameters.getPrice().multiply(BigDecimal.valueOf(purchaseQuantity));
                TradingSessionResults tradingSessionResults = TradingSessionResults.builder()
                    .manufacturer(manufacturer)
                    .productNumber(purchaseQuantity)
                    .price(productionParameters.getPrice())
                    .tradeDate(Game.currentDay)
                    .build();
                tradingSessionResultsList.add(tradingSessionResults);
                MANUFACTURER_CURRENT_PRODUCT_COUNT.put(manufacturer.getId(), manufacturerCurrentProductCount - purchaseQuantity);
                MANUFACTURER_REVENUE.put(manufacturer.getId(), totalPrice);
                manufacturer.setBalance(manufacturer.getBalance().add(totalPrice));
                manufacturers.add(manufacturer);
                requiredQuantity -= purchaseQuantity;
            }

            logger.info("ОСТАЛОСЬ КУПИТЬ - {} ", requiredQuantity);
        }
        manufacturerRepository.saveAll(manufacturers);
        tradingSessionResultsRepository.saveAll(tradingSessionResultsList);
    }

}
