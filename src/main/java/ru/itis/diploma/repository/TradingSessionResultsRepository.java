package ru.itis.diploma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.diploma.model.TradingSessionResults;

public interface TradingSessionResultsRepository extends JpaRepository<TradingSessionResults, Long> {
}
