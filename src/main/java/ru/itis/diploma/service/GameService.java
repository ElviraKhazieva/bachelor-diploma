package ru.itis.diploma.service;

import ru.itis.diploma.dto.CreateGameDto;
import ru.itis.diploma.dto.GameDto;
import ru.itis.diploma.model.Game;
import ru.itis.diploma.model.Manufacturer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface GameService {

    Game getGameById(Long id);

    void createGame(CreateGameDto gameDto);

    List<GameDto> getAccountGames(Long accountId);

    List<GameDto> getAllGames();

    Game save(Game game);

    void finishGame(Long id);

    Map<Manufacturer, BigDecimal> getGameResults(Game game);
}
