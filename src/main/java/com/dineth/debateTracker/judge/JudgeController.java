package com.dineth.debateTracker.judge;

import com.dineth.debateTracker.dtos.JudgeRoundStatDTO;
import com.dineth.debateTracker.dtos.JudgeTournamentDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @Slf4j
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/judge")
public class JudgeController {
    private final JudgeService judgeService;

    @Autowired
    public JudgeController(JudgeService judgeService) {
        this.judgeService = judgeService;
    }

    @GetMapping
    public List<Judge> getJudges() {
        return judgeService.getJudges();
    }

    /**
     * Get all judges and the rounds they've judged in each tournament
     */
    @GetMapping(path = "tournament-rounds")
    public List<JudgeTournamentDTO> getJudgesByTournamentWithRounds() {
        return judgeService.getJudgesByTournamentWithRounds();
    }

    /**
     * Get all judges and the tournaments they've judged at
     */
    @GetMapping(path = "tournaments")
    public List<JudgeTournamentDTO> getJudgesByTournament() {
        return judgeService.getJudgesByTournament();
    }
    @GetMapping(path = "stats/rounds")
    public List<JudgeRoundStatDTO> getRoundCount() {
        return judgeService.getRoundCount();
    }

    @PostMapping
    public Judge addJudge(@RequestBody Judge judge) {
        return judgeService.addJudge(judge);
    }
}
