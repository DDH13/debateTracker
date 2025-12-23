package com.dineth.debateTracker.eliminationballot;

import com.dineth.debateTracker.judge.Judge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EliminationBallotService {
    private final EliminationBallotRepository eliminationBallotRepository;

    @Autowired
    public EliminationBallotService(EliminationBallotRepository eliminationBallotRepository) {
        this.eliminationBallotRepository = eliminationBallotRepository;
    }

    public List<EliminationBallot> getEliminationBallots() {
        return eliminationBallotRepository.findAll();
    }

    public EliminationBallot addEliminationBallot(EliminationBallot eliminationBallot) {
        return eliminationBallotRepository.save(eliminationBallot);
    }

    public EliminationBallot findEliminationBallotById(Long id) {
        return eliminationBallotRepository.findById(id).orElse(null);
    }

    /**
     * Replace an old judge by a new judge in all elimination ballots
     */
    @Transactional
    public void replaceJudge(Judge oldJudge, Judge newJudge) {
        List<EliminationBallot> ballots = eliminationBallotRepository.findByJudgeId(oldJudge.getId());
        for (EliminationBallot ballot : ballots) {
            ballot.setJudge(newJudge);
        }
        eliminationBallotRepository.saveAll(ballots);
    }

}
