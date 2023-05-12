package ru.itis.diploma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.diploma.model.Game;
import ru.itis.diploma.model.enums.GameStatus;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByIdIn(List<Long> gameIds);

    Optional<Game> findFirstByStatusNot(GameStatus status);
}
