package ru.itis.diploma.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.diploma.dto.AdvertisingIntensity;
import ru.itis.diploma.service.StatisticsService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/game/{gameId}/revenue-data/{accountId}")
    public Map<Integer, BigDecimal> getRevenueData(@PathVariable Long gameId,
                                                   @PathVariable Long accountId) {
        return statisticsService.getRevenueData(gameId, accountId);
    }

    @GetMapping("/game/{gameId}/advertising-intensity/{accountId}")
    public List<AdvertisingIntensity> getAdvertisingIntensityData(@PathVariable Long gameId,
                                                                  @PathVariable Long accountId) {
        return statisticsService.getAdvertisingIntensityData(gameId, accountId);
    }

}
