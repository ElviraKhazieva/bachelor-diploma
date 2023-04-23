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
import ru.itis.diploma.dto.CommonProductionParameters;
import ru.itis.diploma.dto.InitialProductionParameters;
import ru.itis.diploma.dto.ManufacturerFinancialStatus;
import ru.itis.diploma.dto.ManufacturerParameters;
import ru.itis.diploma.dto.NewProductionParameters;
import ru.itis.diploma.model.Game;
import ru.itis.diploma.security.details.AccountUserDetails;
import ru.itis.diploma.service.AccountService;
import ru.itis.diploma.service.GameService;
import ru.itis.diploma.service.ManufacturerService;
import ru.itis.diploma.util.EconomicIndicatorsCalculator;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ManufacturerController {

    private final ManufacturerService manufacturerService;
    private final AccountService accountService;
    private final GameService gameService;
    private final EconomicIndicatorsCalculator economicIndicatorsCalculator;

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/game/{id}/initial-production-parameters")
    public String defineInitialProductionParameters(@AuthenticationPrincipal AccountUserDetails userDetails,
                                                    @PathVariable("id") Long gameId,
                                                    InitialProductionParameters initialProductionParameters,
                                                    Model model) {
        Game game = gameService.getGameById(gameId);
        if (notValidAdvertisingParameters(initialProductionParameters)) {
            model.addAttribute("game", game);
            model.addAttribute("errorMessage", "Некорректные параметры рекламы");
            return "define_initial_production_params";
        }
        manufacturerService.defineInitialProductionParameters(initialProductionParameters, userDetails.getAccount().getId(), game);
        return "redirect:/game/" + gameId;
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/game/{id}/production-parameters")
    public String defineNewProductionParameters(@AuthenticationPrincipal AccountUserDetails userDetails,
                                                @PathVariable("id") Long gameId,
                                                NewProductionParameters newProductionParameters,
                                                Model model) {
        var game = gameService.getGameById(gameId);
        if (notValidAdvertisingParameters(newProductionParameters)) {
            model.addAttribute("game", game);
            model.addAttribute("balance", manufacturerService.getManufacturerByAccountIdAndGameId(
                userDetails.getAccount().getId(), gameId).getBalance());
            model.addAttribute("errorMessage", "Некорректные параметры рекламы");
            return "define_new_production_params";
        }
        manufacturerService.defineNewProductionParameters(newProductionParameters, userDetails.getAccount().getId(), game);
        return "redirect:/game/" + gameId;
    }

    @GetMapping("/game/{id}/production-parameters")
    public String defineNewProductionParameters(@AuthenticationPrincipal AccountUserDetails userDetails,
                                                @PathVariable("id") Long gameId,
                                                Model model) {
        var game = gameService.getGameById(gameId);
        model.addAttribute("game", game);
        model.addAttribute("balance", manufacturerService.getManufacturerByAccountIdAndGameId(
            userDetails.getAccount().getId(), gameId).getBalance());
        return "define_new_production_params";
    }

    @ResponseBody
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/game/{id}/manufacturers-parameters")
    public List<ManufacturerParameters> getManufacturersParameters(@PathVariable("id") Long gameId) {
        return manufacturerService.getManufacturersActualProductionParameters(gameId);
    }

    @ResponseBody
    @GetMapping("/game/{id}/financial-status/{accountId}")
    //@PreAuthorize("isAuthenticated()")
    public ManufacturerFinancialStatus getAccountGames(@PathVariable("id") Long gameId,
                                                       @PathVariable Long accountId) {
        var game = gameService.getGameById(gameId);
        return manufacturerService.getManufacturerFinancialStatus(game, accountId);
    }

    private boolean notValidAdvertisingParameters(CommonProductionParameters commonProductionParameters) {
        return (commonProductionParameters.getAdvertisingIntensityIndex() == 0 &&
            commonProductionParameters.getAdvertisingDays() != 0) ||
            (commonProductionParameters.getAdvertisingDays() == 0 &&
                commonProductionParameters.getAdvertisingIntensityIndex() != 0);
    }
}
