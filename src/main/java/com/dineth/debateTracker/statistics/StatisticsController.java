package com.dineth.debateTracker.statistics;


import com.dineth.debateTracker.ballot.Ballot;
import com.dineth.debateTracker.dtos.JudgeSentimentDTO;
import com.dineth.debateTracker.dtos.statistics.WinLossStatDTO;
import com.dineth.debateTracker.tournament.Tournament;
import com.dineth.debateTracker.tournament.TournamentService;
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
    private final StatisticsService statisticsService;
    private final TournamentService tournamentService;


    @Autowired
    public StatisticsController(StatisticsService statisticsService, TournamentService tournamentService) {
        this.statisticsService = statisticsService;
        this.tournamentService = tournamentService;
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
        return statisticsService.getGlobalDistribution();
    }

    /**
     * Get scoring sentiment of judges
     *
     * @param allowedDeviation - the extent to which a judge is allowed to deviate from the average score of a debater before being considered lenient or harsh
     * @return List of JudgeSentimentDTO
     */
    @GetMapping(path = "sentiment")
    public List<JudgeSentimentDTO> getSentiments(@RequestParam(value = "allowed-deviation", required = false, defaultValue = "0.5") double allowedDeviation) {
        return statisticsService.getSentiment(allowedDeviation);
    }

    /**
     * Get the win-loss statistics of debaters
     *
     * @return List of WinLossStatDTO
     */
    @GetMapping(path = "win-loss")
    public List<WinLossStatDTO> getWinLossStats() {
        return statisticsService.calculateWinLoss();
    }

    @GetMapping(path = "tournament-average")
    public HashMap<String, HashMap<String, Double>> getTournamentAverage() {
        HashMap<String, HashMap<String, Double>> tournamentAverages = new HashMap<>();
        List<Tournament> tournaments = tournamentService.getTournaments();

        for (Tournament tournament : tournaments) {
            //filter out where the speaker score is null or less than 41
            List<Ballot> ballots = tournamentService.getPrelimBallotsByTournamentId(tournament.getId()).stream().filter(ballot -> ballot.getSpeakerScore() != null && ballot.getSpeakerScore() >= 69).toList();
            Integer count = ballots.size();
            Double sum = ballots.stream().mapToDouble(Ballot::getSpeakerScore).sum();
            Double average = sum / count;
            tournamentAverages.put(tournament.getShortName(), new HashMap<>() {{
                put("average", average);
                put("count", (double) count);
            }});
        }
        return tournamentAverages;

    }

}
