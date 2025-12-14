package com.dineth.debateTracker.debate;

import com.dineth.debateTracker.ballot.Ballot;
import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.team.Team;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class DebateService {
    private final DebateRepository debateRepository;

    @Autowired
    public DebateService(DebateRepository debateRepository) {
        this.debateRepository = debateRepository;
    }

    public List<Debate> getDebate() {
        return debateRepository.findAll();
    }

    public Debate addDebate(Debate debate) {
        return debateRepository.save(debate);
    }

    /**
     * Find all the prelims where a debater spoke
     **/
    public List<Debate> findPrelimsByDebaterId(Long debaterId) {
        return debateRepository.findPrelimsByDebaterId(debaterId);
    }

    /**
     * Find all the break rounds where a debater was in a team that debated
     **/
    public List<Debate> findBreaksByDebaterId(Long debaterId) {
        return debateRepository.findBreaksByDebaterId(debaterId);
    }

    /**
     * Find all the debaters in a debate and returns them in two teams
     * Excludes the debaters who didn't speak in the debate if it's a preliminary round
     **/
    public List<Team> findDebatersSpeakingInDebate(Debate debate) {
        List<Team> teams = new ArrayList<>();

        Team proposition = debate.getProposition();
        Team opposition = debate.getOpposition();

        boolean isElimination;
        isElimination = debate.getEliminationBallots() != null && !debate.getEliminationBallots().isEmpty();
        Set<Debater> debaters = new HashSet<>();
//       If it's a preliminary round add only the debaters who spoke
        if (!isElimination) {
            List<Ballot> ballots = debate.getBallots();
            for (Ballot ballot : ballots) {
                debaters.add(ballot.getDebater());
            }
        } else {
//            If it's an elimination round add all the debaters who are in the team
            debaters.addAll(proposition.getDebaters());
            debaters.addAll(opposition.getDebaters());
        }

        List<Debater> propositionDebaters = new ArrayList<>();
        List<Debater> oppositionDebaters = new ArrayList<>();

        for (Debater debater : proposition.getDebaters()) {
            if (debaters.contains(debater)) {
                propositionDebaters.add(debater);
            }
        }
        for (Debater debater : opposition.getDebaters()) {
            if (debaters.contains(debater)) {
                oppositionDebaters.add(debater);
            }
        }
        proposition.setDebaters(propositionDebaters);
        opposition.setDebaters(oppositionDebaters);
        teams.add(proposition);
        teams.add(opposition);
        return teams;
    }

    /**
     * Calculate Head-to-Head score between all debaters
     **/
    public Map<Debater, Map<Debater, Integer>> calculateHeadToHeadScore() {
        Map<Debater, Map<Debater, Integer>> headToHead = new HashMap<>();
        List<Debate> debates = debateRepository.findAll();

        for (Debate debate : debates) {
            Team winner = debate.getWinner();
            if (winner == null) {
                log.warn("Debate ID {} has no winner set.", debate.getId());
                continue; // Skip debates without a winner
//                TODO: Check why there are debates without a winner
            }

            // Retrieve the teams and their debaters
            List<Team> teams = findDebatersSpeakingInDebate(debate);

            List<Debater> winners = winner.equals(teams.get(0)) ? teams.get(0).getDebaters() : teams.get(1).getDebaters();
            List<Debater> losers = winner.equals(teams.get(0)) ? teams.get(1).getDebaters() : teams.get(0).getDebaters();

            // Update the head-to-head scores
            for (Debater winnerDebater : winners) {
                for (Debater loserDebater : losers) {
                    headToHead
                            .computeIfAbsent(winnerDebater, k -> new HashMap<>())
                            .merge(loserDebater, 1, Integer::sum);
                }
            }
        }

        return headToHead;
    }

}
