package ru.itis.diploma.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itis.diploma.dto.CreateGameDto;
import ru.itis.diploma.dto.GameDto;
import ru.itis.diploma.exception.EntityNotFoundException;
import ru.itis.diploma.model.Game;
import ru.itis.diploma.model.GameResult;
import ru.itis.diploma.model.Manufacturer;
import ru.itis.diploma.model.enums.GameStatus;
import ru.itis.diploma.repository.GameRepository;
import ru.itis.diploma.repository.GameResultRepository;
import ru.itis.diploma.repository.ManufacturerRepository;
import ru.itis.diploma.service.AccountService;
import ru.itis.diploma.service.GameService;
import ru.itis.diploma.service.ManufacturerService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static ru.itis.diploma.service.CronService.MANUFACTURER_STATISTICS_INFO;
import static ru.itis.diploma.service.impl.ManufacturerServiceImpl.MANUFACTURER_CURRENT_PRODUCT_COUNT;
import static ru.itis.diploma.service.impl.ManufacturerServiceImpl.MANUFACTURER_REVENUE;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final AccountService accountService;
    private final GameRepository gameRepository;
    private final GameResultRepository gameResultRepository;
    private final ManufacturerService manufacturerService;
    private final ManufacturerRepository manufacturerRepository;

    @Override
    public Game getGameById(Long id) {
        return gameRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Игра не найдена"));
    }

    @Override
    public void createGame(CreateGameDto gameDto) {
        var newGame = Game.builder()
            .name(gameDto.getName())
            .timeUnit(gameDto.getTimeUnit())
            .startDate(LocalDateTime.now())
            .status(GameStatus.CREATED)
            .interestRateInvestmentCredit(gameDto.getInterestRateInvestmentCredit())
            .interestRateBusinessCredit(gameDto.getInterestRateBusinessCredit())
            .investmentCreditTermMonths(gameDto.getInvestmentCreditTermMonths())
            .salesTax(gameDto.getSalesTax())
            .baseCostPrice(gameDto.getBaseCostPrice())
            .baseAdvertisementPrice(gameDto.getBaseAdvertisementPrice())
            .productPower(gameDto.getProductPower())
            .assortmentWeight(gameDto.getAssortmentWeight())
            .advertisementWeight(gameDto.getAdvertisementWeight())
            .qualityWeight(gameDto.getQualityWeight())
            .purchaseLimit(gameDto.getPurchaseLimit())
            .habitWeight(gameDto.getHabitWeight())
            .dailySpendingLimit(gameDto.getDailySpendingLimit())
            .absoluteQualityProductLife(gameDto.getAbsoluteQualityProductLife())
            .habitTrackingDays(gameDto.getHabitTrackingDays())
            .build();
        gameRepository.save(newGame);
        Game.currentDay = 0;
        MANUFACTURER_CURRENT_PRODUCT_COUNT = new HashMap<>();
        MANUFACTURER_REVENUE = new HashMap<>();
        MANUFACTURER_STATISTICS_INFO = new HashMap<>();
        createManufacturers(newGame, gameDto.getAccountIds());
    }

    private void createManufacturers(Game newGame, List<Long> accountIds) {
        List<Manufacturer> manufacturers = accountIds.stream()
            .map(userId -> Manufacturer.builder()
                .game(newGame)
                .account(accountService.getById(userId))
//                .balance(BigDecimal.ZERO)
//                .investmentCreditAmount(BigDecimal.ZERO)
                .investmentCreditDebt(BigDecimal.ZERO)
                .investmentCreditIsRepaid(false)
                .build())
            .toList();
        manufacturerRepository.saveAll(manufacturers);
    }

    @Override
    public List<GameDto> getAccountGames(Long accountId) {
        List<Manufacturer> accountManufacturers = manufacturerRepository.findByAccount_Id(accountId);
        return GameDto.from(gameRepository.findByIdIn(
            accountManufacturers.stream()
                .map(manufacturer -> manufacturer.getGame().getId())
                .toList()));
    }

    @Override
    public List<GameDto> getAllGames() {
        return GameDto.from(gameRepository.findAll());
    }

    @Override
    public Game save(Game game) {
        return gameRepository.save(game);
    }

    @Override
    public void finishGame(Long id) {
        Game game = getGameById(id);
        game.setStatus(GameStatus.FINISHED);
        game.setEndDate(LocalDateTime.now());
        save(game);

        var manufacturers = manufacturerService.getGameManufacturers(game.getId());
        var gameResults = manufacturers.stream()
            .map(m -> GameResult.builder()
                .game(game)
                .manufacturer(m)
                .result(calculateFinishFinancialStatus(m, game))
                .build())
            .toList();
        gameResultRepository.saveAll(gameResults);
    }

    public List<GameResult> getGameResults(Game game) {
        return gameResultRepository.findByGameId(game.getId());
//        List<Manufacturer> manufacturers = manufacturerService.getGameManufacturers(game.getId());
//        return manufacturers.stream()
//            .collect(Collectors.toMap(
//                Function.identity(),
//                manufacturer -> calculateFinishFinancialStatus(manufacturer, game)
//            ))
//            .entrySet().stream()
//            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
//            .collect(Collectors.toMap(
//                Map.Entry::getKey,
//                Map.Entry::getValue,
//                (oldValue, newValue) -> oldValue,
//                LinkedHashMap::new
//            ));
    }

    @Override
    public boolean existActiveGame() {
        return gameRepository.findFirstByStatusNot(GameStatus.FINISHED).isPresent();
    }

    private BigDecimal calculateFinishFinancialStatus(Manufacturer manufacturer, Game game) {
        var debts = manufacturerService.calculateManufacturerInvestmentCreditDebt(manufacturer, game)
            .add(manufacturerService.calculateManufacturerBusinessCreditDebt(manufacturer));
        return manufacturer.getBalance().subtract(debts);
    }

}
