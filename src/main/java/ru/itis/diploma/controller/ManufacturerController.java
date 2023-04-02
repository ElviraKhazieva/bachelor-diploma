package ru.itis.diploma.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.itis.diploma.dto.InitialProductionParameters;
import ru.itis.diploma.dto.ManufacturerParameters;
import ru.itis.diploma.security.details.AccountUserDetails;
import ru.itis.diploma.service.AccountService;
import ru.itis.diploma.service.ManufacturerService;
import ru.itis.diploma.util.EconomicIndicatorsCalculator;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ManufacturerController {

    private final ManufacturerService manufacturerService;
    private final AccountService accountService;
    private final EconomicIndicatorsCalculator economicIndicatorsCalculator;

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/game/{id}/production-parameters")
    public String enterInitialProductionParameters(@AuthenticationPrincipal AccountUserDetails userDetails,
                                                   @PathVariable("id") Long gameId,
                                                   InitialProductionParameters initialProductionParameters) {
        manufacturerService.enterInitialProductionParameters(initialProductionParameters, userDetails.getAccount().getId(), gameId);
        return "redirect:/game/" + gameId;
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
                return ManufacturerParameters.builder()
                    .id(manufacturer.getId())
                    .fullName(account.getFullName())
                    .balance(manufacturer.getBalance())
                    .costPrice(productionParameters.getCostPrice())
                    .price(productionParameters.getPrice())
                    .productCount(productionParameters.getProductCount())
                    .assortment(productionParameters.getAssortment())
                    .productionCapacityPerDay(productionParameters.getProductionCapacityPerDay())
                    .investmentCreditAmount(manufacturer.getInvestmentCreditAmount())
                    .qualityIndex(productionParameters.getQualityIndex())
                    .advertisingStartDate(advertisement.getStartDate())
                    .advertisingEndDate(advertisement.getEndDate())
                    .advertisingIntensityIndex(advertisement.getIntensityIndex())
                    .advertisingCost(advertisement.getCost())
                    .timeToMarket(economicIndicatorsCalculator.calculateTimeToMarket(productionParameters.getProductCount(),
                        productionParameters.getProductionCapacityPerDay()))
                    .build();
            }).toList();

    }

}
