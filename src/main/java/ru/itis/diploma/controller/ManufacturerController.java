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
import ru.itis.diploma.dto.EditProductionParameters;
import ru.itis.diploma.dto.InitialProductionParameters;
import ru.itis.diploma.dto.ManufacturerFinancialStatus;
import ru.itis.diploma.dto.ManufacturerParameters;
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
    public String enterInitialProductionParameters(@AuthenticationPrincipal AccountUserDetails userDetails,
                                                   @PathVariable("id") Long gameId,
                                                   InitialProductionParameters initialProductionParameters,
                                                   Model model) {
        if (notValidAdvertisingParameters(initialProductionParameters)) {
            model.addAttribute("game", gameService.getGameById(gameId));
            model.addAttribute("errorMessage", "Некорректные параметры рекламы");
            return "enter_initial_production_params";
        }
        manufacturerService.enterInitialProductionParameters(initialProductionParameters, userDetails.getAccount().getId(), gameId);
        return "redirect:/game/" + gameId;
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/game/{id}/production-parameters")
    public String enterProductionParameters(@AuthenticationPrincipal AccountUserDetails userDetails,
                                            @PathVariable("id") Long gameId,
                                            EditProductionParameters editProductionParameters,
                                            Model model) {
        if (notValidAdvertisingParameters(editProductionParameters)) {
            var game = gameService.getGameById(gameId);
            model.addAttribute("game", game);
            model.addAttribute("balance", manufacturerService.getManufacturerByAccountIdAndGameId(
                userDetails.getAccount().getId(), gameId).getBalance());
            model.addAttribute("errorMessage", "Некорректные параметры рекламы");
            return "edit_production_params";
        }
        manufacturerService.editProductionParameters(editProductionParameters, userDetails.getAccount().getId(), gameId);
        return "redirect:/game/" + gameId;
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/game/{id}/production-parameters")
    public String enterProductionParameters(@AuthenticationPrincipal AccountUserDetails userDetails,
                                            @PathVariable("id") Long gameId,
                                            Model model) {
        var game = gameService.getGameById(gameId);
        model.addAttribute("game", game);
        model.addAttribute("balance", manufacturerService.getManufacturerByAccountIdAndGameId(
            userDetails.getAccount().getId(), gameId).getBalance());
        return "edit_production_params";
    }

    @ResponseBody
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/game/{id}/manufacturers-parameters")
    public List<ManufacturerParameters> getManufacturersParameters(@PathVariable("id") Long gameId) {
        return manufacturerService.getGameManufacturers(gameId).stream()
            .map(manufacturer -> {
                var account = accountService.getById(manufacturer.getAccount().getId());
                var productionParameters = manufacturerService.getActualProductionParameters(manufacturer.getId());
                var advertisement = manufacturerService.getActualAdvertisement(manufacturer.getId());
                ManufacturerParameters manufacturerParameters = ManufacturerParameters.builder()
                    .id(manufacturer.getId())
                    .fullName(account.getFullName())
                    .investmentCreditAmount(manufacturer.getInvestmentCreditAmount())
                    .balance(manufacturer.getBalance())
                    .build();
                productionParameters.ifPresent(p -> {
                    manufacturerParameters.setTimeToMarket(p.getTimeToMarket());
                    manufacturerParameters.setCostPrice(p.getCostPrice());
                    manufacturerParameters.setPrice(p.getPrice());
                    manufacturerParameters.setProductCount(p.getProductCount());
                    manufacturerParameters.setAssortment(p.getAssortment());
                    manufacturerParameters.setProductionCapacityPerDay(p.getProductionCapacityPerDay());
                    manufacturerParameters.setQualityIndex(p.getQualityIndex());
                });
                advertisement.ifPresent(a -> {
                    manufacturerParameters.setAdvertisingStartDate(a.getStartDate());
                    manufacturerParameters.setAdvertisingEndDate(a.getEndDate());
                    manufacturerParameters.setAdvertisingIntensityIndex(a.getIntensityIndex());
                    manufacturerParameters.setAdvertisingCost(a.getCost());
                });
                return manufacturerParameters;
            }).toList();

    }

    @ResponseBody
    @GetMapping("/game/{id}/financial-status/{accountId}")
    //@PreAuthorize("isAuthenticated()")
    public ManufacturerFinancialStatus getAccountGames(@PathVariable("id") Long gameId,
                                                       @PathVariable Long accountId) {
        return manufacturerService.getManufacturerFinancialStatus(gameId, accountId);
    }

    private boolean notValidAdvertisingParameters(CommonProductionParameters commonProductionParameters) {
        return (commonProductionParameters.getAdvertisingIntensityIndex() == 0 &&
            commonProductionParameters.getAdvertisingDays() != 0) ||
            (commonProductionParameters.getAdvertisingDays() == 0 &&
                commonProductionParameters.getAdvertisingIntensityIndex() != 0);
    }
}
