package com.dineth.debateTracker.round;

import com.dineth.debateTracker.debate.Debate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoundService {
    private final RoundRepository roundRepository;

    @Autowired
    public RoundService(RoundRepository roundRepository) {
        this.roundRepository = roundRepository;
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

}
