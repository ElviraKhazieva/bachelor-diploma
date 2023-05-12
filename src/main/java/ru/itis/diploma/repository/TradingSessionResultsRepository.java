package ru.itis.diploma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itis.diploma.model.TradingSessionResults;

import java.math.BigDecimal;
import java.util.List;

public interface TradingSessionResultsRepository extends JpaRepository<TradingSessionResults, Long> {

    @Query(nativeQuery = true, value = "select sum(price * product_number) from trading_session_results " +
        "where manufacturer_id = :manufacturerId and trade_date > :from and trade_date <= :to")
    BigDecimal getPurchasesTotal(Long manufacturerId, Integer from, Integer to);

    List<TradingSessionResults> findByManufacturerId(Long manufacturerId);

    @Query("SELECT SUM(t.productNumber) FROM TradingSessionResults t WHERE t.manufacturer.game.id = :gameId AND t.isWornOut = false")
    Integer getUnwornProductsCountByGameId(@Param("gameId") Long gameId);

    List<TradingSessionResults> findByTradeDateGreaterThanEqualAndManufacturerIdIn(int startDate, List<Long> manufacturerIds);

    @Query("SELECT t FROM TradingSessionResults t WHERE t.productNumber > 0")
    List<TradingSessionResults> findAllByProductNumberGreaterThanZero();
}
