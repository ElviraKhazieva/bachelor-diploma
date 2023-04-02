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
import ru.itis.diploma.dto.CreateGameDto;
import ru.itis.diploma.dto.GameDto;
import ru.itis.diploma.model.Account;
import ru.itis.diploma.security.details.AccountUserDetails;
import ru.itis.diploma.service.AccountService;
import ru.itis.diploma.service.GameService;
import ru.itis.diploma.service.ManufacturerService;

import java.util.Comparator;
import java.util.List;

import static ru.itis.diploma.model.GameStatus.STARTED;

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
    public String createGame(CreateGameDto gameDto) {
        gameService.createGame(gameDto);
        return "redirect:/profile";
    }

    @GetMapping("/game/{id}")
    @PreAuthorize("isAuthenticated()")
    public String getGamePage(@PathVariable Long id,
                              @AuthenticationPrincipal AccountUserDetails userDetails,
                              Model model) {
        var game = gameService.getGameById(id);
        model.addAttribute("game", game);

        if (Account.Role.ADMIN.equals(userDetails.getAccount().getRole())) {
            model.addAttribute("startedTradingSessions", STARTED.equals(game.getStatus()));
            model.addAttribute("manufacturers", manufacturerService.getGameManufacturers(id));
            return "game_admin";
        }

        var manufacturer = manufacturerService.getManufacturerByAccountIdAndGameId(userDetails.getAccount().getId(), id);
        if (manufacturer.isEnteredInitialProductionParameters()) {
            return "game";
        } else {
            return "initial_product_params_form";
        }
    }

    @ResponseBody
    @GetMapping("/{accountId}/games")
    @PreAuthorize("hasAuthority('USER')")
    public List<GameDto> getAccountGames(@PathVariable Long accountId) {
//        model.addAttribute("games",
        return gameService.getAccountGames(accountId).stream()
            .sorted(Comparator.comparing(GameDto::getStartDate).reversed())
            .toList();
//        return "account_games";
    }

    @ResponseBody
    @GetMapping("/games")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<GameDto> getAllGamesPage() {
//        model.addAttribute("games",
        return gameService.getAllGames().stream()
            .sorted(Comparator.comparing(GameDto::getStartDate).reversed())
            .toList();
//        return "all_games";
    }


}
