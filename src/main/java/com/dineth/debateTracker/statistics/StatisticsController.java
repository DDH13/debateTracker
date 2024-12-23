package com.dineth.debateTracker.statistics;


import com.dineth.debateTracker.dtos.JudgeSentimentDTO;
import com.dineth.debateTracker.dtos.statistics.WinLossStatDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@Slf4j
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/statistics")
public class StatisticsController {
    private final StatisticsService statistic;


    @Autowired
    public StatisticsController(StatisticsService statistic) {
        this.statistic = statistic;
    }

    /**
     * Get global distribution of speaker scores
     *
     * @return HashMap with mean and standard deviation
     * {
     * "mean": value,
     * "stdDeviation": value
     * }
     */
    @GetMapping(path = "global-dist")
    public HashMap<String, Double> getGlobalDistribution() {
        return statistic.getGlobalDistribution();
    }

    /**
     * Get scoring sentiment of judges
     *
     * @param allowedDeviation - the extent to which a judge is allowed to deviate from the average score of a debater before being considered lenient or harsh
     * @return List of JudgeSentimentDTO
     */
    @GetMapping(path = "sentiment")
    public List<JudgeSentimentDTO> getSentiments(@RequestParam(value = "allowed-deviation", required = false, defaultValue = "0.5") double allowedDeviation) {
        return statistic.getSentiment(allowedDeviation);
    }

    /**
     * Get the win-loss statistics of debaters
     *
     * @return List of WinLossStatDTO
     */
    @GetMapping(path = "win-loss")
    public List<WinLossStatDTO> getWinLossStats() {
        return statistic.calculateWinLoss();
    }

}
