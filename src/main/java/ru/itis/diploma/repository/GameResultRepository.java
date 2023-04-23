package ru.itis.diploma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.diploma.model.GameResult;

import java.util.List;

public interface GameResultRepository extends JpaRepository<GameResult, Long> {
    List<GameResult> findByGameId(Long gameId);
}
