package com.dineth.debateTracker.tournament;

import com.dineth.debateTracker.ballot.Ballot;
import com.dineth.debateTracker.breakcategory.BreakCategory;
import com.dineth.debateTracker.debate.Debate;
import com.dineth.debateTracker.motion.Motion;
import com.dineth.debateTracker.round.Round;
import com.dineth.debateTracker.round.RoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TournamentService {
    private final TournamentRepository tournamentRepository;
    private final RoundService roundService;

    @Autowired
    public TournamentService(TournamentRepository tournamentRepository, RoundService roundService) {
        this.tournamentRepository = tournamentRepository;
        this.roundService = roundService;
    }

    public List<Tournament> getTournaments() {
        return tournamentRepository.findAll();
    }

    public Tournament getTournamentById(Long id) {
        return tournamentRepository.findById(id).orElse(null);
    }

    public Tournament addTournament(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

    public Tournament findTournamentById(Long id) {
        return tournamentRepository.findById(id).orElse(null);
    }

    @Transactional
    public void addRoundToTournament(Long tournamentId, Round round) throws Exception {
        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow(
                () -> new Exception("Tournament with id " + tournamentId + " not found for adding round."));
        round.setTournament(tournament);
        tournament.getRounds().add(round);

        roundService.updateRound(round);
    }

    public void addMotionToTournament(Long tournamentId, Motion motion) {
        Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);
        if (tournament != null) {
            List<Motion> motions = tournament.getMotions();
            if (motions == null) {
                motions = new ArrayList<>();
            }
            motions.add(motion);
            tournament.setMotions(motions);
            tournamentRepository.save(tournament);
        }
    }

    public void addBreakCategoryToTournament(Long tournamentId, BreakCategory breakCategory) {
        Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);
        if (tournament != null) {
            List<BreakCategory> breakCategories = tournament.getBreakCategories();
            if (breakCategories == null) {
                breakCategories = new ArrayList<>();
            }
            breakCategories.add(breakCategory);
            tournament.setBreakCategories(breakCategories);
            tournamentRepository.save(tournament);
        }
    }

    /**
     * Get all the preliminary round ballots of a tournament
     */
    public List<Ballot> getPrelimBallotsByTournamentId(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);
        if (tournament != null) {
            List<Ballot> ballots = new ArrayList<>();
            for (Round round : tournament.getRounds()) {
                for (Debate debate : round.getDebates()) {
                    ballots.addAll(debate.getBallots());
                }
            }
            return ballots;
        }
        return null;
    }

}
