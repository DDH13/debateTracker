package com.dineth.debateTracker.round;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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


}
