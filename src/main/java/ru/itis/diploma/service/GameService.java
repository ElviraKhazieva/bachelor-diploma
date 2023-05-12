package ru.itis.diploma.service;

import ru.itis.diploma.dto.CreateGameDto;
import ru.itis.diploma.dto.GameDto;
import ru.itis.diploma.model.Game;
import ru.itis.diploma.model.GameResult;

import java.util.List;

public interface GameService {

    Game getGameById(Long id);

    void createGame(CreateGameDto gameDto);

    List<GameDto> getAccountGames(Long accountId);

    List<GameDto> getAllGames();

    Game save(Game game);

    void finishGame(Long id);

    List<GameResult> getGameResults(Game game);

    boolean existActiveGame();
}
