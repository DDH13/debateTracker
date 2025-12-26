package com.dineth.debateTracker.statistics;


import com.dineth.debateTracker.ballot.Ballot;
import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.debater.DebaterService;
import com.dineth.debateTracker.dtos.DebaterTournamentScoreDTO;
import com.dineth.debateTracker.dtos.JudgeSentimentDTO;
import com.dineth.debateTracker.dtos.statistics.IronStatsDTO;
import com.dineth.debateTracker.dtos.statistics.SpeakerScorePerformanceDTO;
import com.dineth.debateTracker.dtos.statistics.WinLossStatDTO;
import com.dineth.debateTracker.tournament.Tournament;
import com.dineth.debateTracker.tournament.TournamentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;
    private final TournamentService tournamentService;
    private final DebaterService debaterService;


    @Autowired
    public StatisticsController(StatisticsService statisticsService, TournamentService tournamentService,
            DebaterService debaterService, DebaterService debaterService1) {
        this.statisticsService = statisticsService;
        this.tournamentService = tournamentService;
        this.debaterService = debaterService1;
    }

    /**
     * Get the performance of all debaters over all tournaments
     * (Merges multi panel ballots and considers the highest score for iron-personed debates)
     * @return Map of Debater ID to DebaterTournamentScoreDTO
     */
    @GetMapping(path = "speaker-scores")
    public Map<Long, DebaterTournamentScoreDTO> getAllSpeakerScores() {
        return statisticsService.getAllSpeakerTabScores();
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
    
    @GetMapping(path = "best-speaker-performances")
    public List<SpeakerScorePerformanceDTO> getTopSpeakerScorePerformances(@RequestParam(value = "top-n", required = false, defaultValue = 
            "10") int topN) {
        return statisticsService.getTopSpeakerScorePerformances(topN);
    }

}
