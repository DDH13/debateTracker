package com.dineth.debateTracker.judge;

import com.dineth.debateTracker.dtos.JudgeTournamentScoreDTO;
import com.dineth.debateTracker.dtos.statistics.JudgeStatsDTO;
import com.dineth.debateTracker.utils.ReplacementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController @Slf4j
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/judge")
public class JudgeController {
    private final JudgeService judgeService;
    private final ReplacementService replacementService;

    @Autowired
    public JudgeController(JudgeService judgeService, ReplacementService replacementService) {
        this.judgeService = judgeService;
        this.replacementService = replacementService;
    }

    @GetMapping
    public List<Judge> getJudges() {
        return judgeService.getJudges();
    }

    @PostMapping
    public Judge addJudge(@RequestBody Judge judge) {
        return judgeService.addJudge(judge);
    }

    /**
     * Replace one debater with another in all references in the database
     *
     * @param values - oldDebaterId, newDebaterId
     */
    @PostMapping(path = "replace")
    public Judge replaceJudge(@RequestBody Map<String, String> values) {
        try {
            Long oldJudgeId = Long.parseLong(values.get("oldJudgeId"));
            Long newJudgeId = Long.parseLong(values.get("newJudgeId"));
            return replacementService.replaceJudge(oldJudgeId, newJudgeId);
        } catch (Exception e) {
            log.error("Error replacing judge: {}", e.getMessage());
        }
        return null;
    }
    /**
     * For a judge get the tournaments they've judged at
     */
    @GetMapping(path = "tournaments")
    public List<String> getTournamentsJudged(@RequestParam Long id) {
        return judgeService.getTournamentsJudged(id);
    }

    /**
     * Get the number of rounds judged by a judge {prelims, breaks}
     */
    @GetMapping(path = "rounds/count")
    public HashMap<String, Integer> getRoundsJudged(@RequestParam Long id) {
        return judgeService.getRoundsJudged(id);
    }

    /**
     * For a judge get the tournaments they've judged prelims at along with scores given
     */
    @GetMapping(path = "rounds/prelims")
    public JudgeTournamentScoreDTO getPrelimScoresByJudge(@RequestParam Long id,
                                                          @RequestParam(value = "reply", required = false, defaultValue = "false") Boolean reply) {
        return judgeService.getTournamentsAndScoresForJudge(id,reply);
    }
    /**
     * For a judge get the tournaments they've judged break rounds at
     */
    @GetMapping(path = "rounds/breaks")
    public JudgeTournamentScoreDTO getBreakScoresByJudge(@RequestParam Long id) {
        return judgeService.getTournamentsAndBreaksJudged(id);
    }

    /**
     * For all judges get the tournaments they've judged at
     */
    @GetMapping(path = "tournaments/all")
    public HashMap<Judge, List<String>> getTournamentsJudged() {
        List<Judge> allJudges = judgeService.getJudges();
        HashMap<Judge, List<String>> judgeTournaments = new HashMap<>();
        for (Judge judge : allJudges) {
            judgeTournaments.put(judge, judgeService.getTournamentsJudged(judge.getId()));
        }
        return judgeTournaments;
    }

    /**
     * For all judges get the number of rounds judged by each judge {prelims, breaks}
     */
    @GetMapping(path = "rounds/count/all")
    public HashMap<Judge, HashMap<String, Integer>> getRoundsJudged() {
        List<Judge> allJudges = judgeService.getJudges();
        HashMap<Judge, HashMap<String, Integer>> judgeRounds = new HashMap<>();
        for (Judge judge : allJudges) {
            judgeRounds.put(judge, judgeService.getRoundsJudged(judge.getId()));
        }
        return judgeRounds;
    }

    /**
     * For all judges get the tournaments they've judged prelims at along with scores given
     */
    @GetMapping(path = "rounds/prelims/all")
    public List<JudgeTournamentScoreDTO> getPrelimScoresByJudge() {
        List<Judge> allJudges = judgeService.getJudges();
        List<JudgeTournamentScoreDTO> judgeScores = new ArrayList<>();
        for (Judge judge : allJudges) {
            judgeScores.add(judgeService.getTournamentsAndScoresForJudge(judge.getId(), false));
        }
        return judgeScores;
    }

    /**
     * For all judges get the tournaments they've judged break rounds at
     */
    @GetMapping(path = "rounds/breaks/all")
    public List<JudgeTournamentScoreDTO> getBreakScoresByJudge() {
        List<Judge> allJudges = judgeService.getJudges();
        List<JudgeTournamentScoreDTO> judgeScores = new ArrayList<>();
        for (Judge judge : allJudges) {
            judgeScores.add(judgeService.getTournamentsAndBreaksJudged(judge.getId()));
        }
        return judgeScores;
    }

    /**
     * Get statistics for a judge
     */
    @GetMapping(path = "stats")
    public JudgeStatsDTO getJudgeStats(@RequestParam Long id) {
        return judgeService.getJudgeStats(id);
    }

    /**
     * Get statistics for all judges
     */
    @GetMapping(path = "stats/all")
    public List<JudgeStatsDTO> getJudgeStats() {
        List<Judge> allJudges = judgeService.getJudges();
        List<JudgeStatsDTO> judgeStats = new ArrayList<>();
        for (Judge judge : allJudges) {
            judgeStats.add(judgeService.getJudgeStats(judge.getId()));
        }
        return judgeStats;
    }



}
