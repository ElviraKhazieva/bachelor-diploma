package ru.itis.diploma.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.itis.diploma.dto.AccountDto;
import ru.itis.diploma.dto.CreateGameDto;
import ru.itis.diploma.dto.GameDto;
import ru.itis.diploma.dto.ManufacturerParameters;
import ru.itis.diploma.model.Account;
import ru.itis.diploma.model.Advertisement;
import ru.itis.diploma.security.details.AccountUserDetails;
import ru.itis.diploma.service.AccountService;
import ru.itis.diploma.service.GameService;
import ru.itis.diploma.service.ManufacturerService;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.itis.diploma.model.enums.GameStatus.FINISHED;
import static ru.itis.diploma.model.enums.GameStatus.STARTED;

@Controller
@RequiredArgsConstructor
public class GameController {

    private final ManufacturerService manufacturerService;
    private final GameService gameService;
    private final AccountService accountService;

    @GetMapping("/game")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String getCreateGamePage(Model model) {
        model.addAttribute("accounts", accountService.getAccountsForNewGame());
        return "create_game";
    }

    @PostMapping("/game")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String createGame(CreateGameDto gameDto, Model model) {
        if (!gameService.existActiveGame()) {
            gameService.createGame(gameDto);
        } else {
            model.addAttribute("accounts", accountService.getAccountsForNewGame());
            model.addAttribute("errorMessage", "Невозможно создать новую игру, поскольку уже есть активная игра.");
            return "create_game";
        }
        return "redirect:/profile";
    }

    @GetMapping("/game/{id}")
    @PreAuthorize("isAuthenticated()")
    public String getGamePage(@PathVariable Long id,
                              @AuthenticationPrincipal AccountUserDetails userDetails,
                              Model model) {
        var game = gameService.getGameById(id);
        model.addAttribute("game", game);
        AccountDto account = accountService.getByEmail(userDetails.getUsername());
        model.addAttribute("account", account);

        if (FINISHED.equals(game.getStatus())) {
            model.addAttribute("gameResults", gameService.getGameResults(game));
        }

        if (Account.Role.ADMIN.equals(userDetails.getAccount().getRole())) {
            model.addAttribute("startedTradingSessions", STARTED.equals(game.getStatus()));
            model.addAttribute("manufacturers", manufacturerService.getGameManufacturers(id));
            return "game_admin";
        }

        var manufacturer = manufacturerService.getManufacturerByAccountIdAndGameId(userDetails.getAccount().getId(), id);
        if (manufacturer.isEnteredInitialProductionParameters()) {
            model.addAttribute("competitors",
                manufacturerService.getGameManufacturers(game.getId()).stream()
                    .filter(m -> !Objects.equals(m.getId(), manufacturer.getId()))
                    .map(m -> manufacturerService.getLastProductionParameters(m.getId()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(p -> {
                        Optional<Advertisement> lastAdvertisement = manufacturerService.getLastAdvertisement(manufacturer.getId());
                        var advertisingIntensityIndex = lastAdvertisement.isPresent() ? lastAdvertisement.get().getIntensityIndex() : 0;
                        return ManufacturerParameters.builder()
                            .id(p.getManufacturer().getId())
                            .fullName(p.getManufacturer().getAccount().getFullName())
                            .assortment(p.getAssortment())
                            .price(p.getPrice())
                            .qualityIndex(p.getQualityIndex())
                            .advertisingIntensityIndex(advertisingIntensityIndex)
                            .build();
                    })
                    .toList());
            model.addAttribute("manufacturer", manufacturer);
            model.addAttribute("productionParameters", manufacturerService.getAllProductionParameters(manufacturer));
            return "game";
        } else {
            return "define_initial_production_params";
        }
    }

    @PostMapping("/game/{id}/finish")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String finishGame(@PathVariable Long id,
                             @AuthenticationPrincipal AccountUserDetails userDetails,
                             Model model) {
        gameService.finishGame(id);
        return "redirect:/game/" + id;
    }

    @ResponseBody
    @GetMapping("/{accountId}/games")
    @PreAuthorize("hasAuthority('USER')")
    public List<GameDto> getAccountGames(@PathVariable Long accountId) {
        return gameService.getAccountGames(accountId).stream()
            .sorted(Comparator.comparing(GameDto::getStartDate).reversed())
            .toList();
    }

    @ResponseBody
    @GetMapping("/games")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<GameDto> getAllGamesPage() {
        return gameService.getAllGames().stream()
            .sorted(Comparator.comparing(GameDto::getStartDate).reversed())
            .toList();
    }


}
