package com.dineth.debateTracker.round;

import com.dineth.debateTracker.debate.Debate;
import com.dineth.debateTracker.debate.DebateService;
import com.dineth.debateTracker.debater.Debater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoundService {
    private final RoundRepository roundRepository;
    private final DebateService debateService;

    @Autowired
    public RoundService(RoundRepository roundRepository, DebateService debateService) {
        this.roundRepository = roundRepository;
        this.debateService = debateService;
    }

    public List<Round> getRound() {
        return roundRepository.findAll();
    }

    public Round findRoundById(Long id) {
        return roundRepository.findById(id).orElse(null);
    }

    public Round addRound(Round round) {
        return roundRepository.save(round);
    }

    public void addDebateToRound(Long roundId, Debate debate) {
        Round round = roundRepository.findById(roundId).orElse(null);
        if (round != null) {
            List<Debate> rounds = round.getDebates();
            if (rounds == null) {
                rounds = new ArrayList<>();
            }
            rounds.add(debate);
            round.setDebates(rounds);
            roundRepository.save(round);
        }
    }

    /**
     * Get all the round names for a given tournament
     * @return List of Strings representing the round names
     */
    public List<String> getRoundNamesByTournamentId(Long tournamentId) {
        return roundRepository.findRoundNameByTournamentId(tournamentId);
    }

    /**
     * Check if a debater won a round
     * return true if the debater won, false if the debater lost, null if the debate is undecided
     */
    public Boolean didDebaterWinRound(Long roundId, Debater debater) {
        Round round = findRoundById(roundId);
        if (round == null) {
            return null;
        }
        List<Debate> debates = round.getDebates();
        for (Debate debate : debates) {
            if (debate.getProposition().getDebaters().contains(debater)
                    || debate.getOpposition().getDebaters().contains(debater)) {
                return debateService.didDebaterWinDebate(debate, debater);
            }
        }
        return null;
    }

}
