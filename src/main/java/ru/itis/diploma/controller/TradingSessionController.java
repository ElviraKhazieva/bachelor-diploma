package ru.itis.diploma.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.itis.diploma.model.GameStatus;
import ru.itis.diploma.model.Manufacturer;
import ru.itis.diploma.service.BuyerService;
import ru.itis.diploma.service.GameService;
import ru.itis.diploma.service.ManufacturerService;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Controller
@RequiredArgsConstructor
public class TradingSessionController {

    private final GameService gameService;
    private final ManufacturerService manufacturerService;
    private final BuyerService buyerService;
    private final TaskScheduler taskScheduler;

    private ScheduledFuture<?> scheduledTask;


    @GetMapping("game/{id}/trading-sessions/start/{timeUnit}")
    public String startTradingSessions(@PathVariable("id") Long gameId, @PathVariable long timeUnit) {
        if (scheduledTask == null) {
            var game = gameService.getGameById(gameId);
            game.setStatus(GameStatus.STARTED);
            gameService.save(game);

            PeriodicTrigger trigger = new PeriodicTrigger(timeUnit, TimeUnit.MINUTES);
            var manufacturerIds = manufacturerService.getGameManufacturers(game.getId()).stream()
                .map(Manufacturer::getId)
                .toList();
            buyerService.setRequiredQuantity(game.getRequiredQuantity());
            scheduledTask = taskScheduler.schedule(() -> buyerService.makePurchases(manufacturerIds), trigger);

        }
        return "redirect:/game/" + gameId;
    }

    @GetMapping("game/{id}/trading-sessions/stop")
    public String stopTradingSessions(@PathVariable("id") Long gameId) {
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
            scheduledTask = null;
        }
        var game = gameService.getGameById(gameId);
        game.setStatus(GameStatus.STOPPED);
        gameService.save(game);
        return "redirect:/game/" + gameId;
    }

}
