package ru.itis.diploma.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.itis.diploma.dto.InitialProductionParameters;
import ru.itis.diploma.security.details.AccountUserDetails;
import ru.itis.diploma.service.ManufacturerService;

@Controller
@RequiredArgsConstructor
public class ManufacturerController {

    private final ManufacturerService manufacturerService;

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/game/{id}/production-parameters")
    public String enterInitialProductionParameters(@AuthenticationPrincipal AccountUserDetails userDetails,
                                                   @PathVariable("id") Long gameId,
                                                   InitialProductionParameters initialProductionParameters) {
        manufacturerService.enterInitialProductionParameters(initialProductionParameters, userDetails.getAccount().getId(), gameId);
        return "redirect:/game/" + gameId ;
    }

}
