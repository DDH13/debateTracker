package com.dineth.debateTracker.ballot;

import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.dtos.SpeakerTab.SpeakerTabBallot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class BallotService {
    private final BallotRepository ballotRepository;

    @Autowired
    public BallotService(BallotRepository ballotRepository) {
        this.ballotRepository = ballotRepository;
    }

    public List<Ballot> getBallots() {
        return ballotRepository.findAll();
    }

    public Ballot addBallot(Ballot ballot) {
        return ballotRepository.save(ballot);
    }

    public List<Ballot> findBallotsByDebater(Debater debater) {
        return ballotRepository.findBallotsByDebater(debater);
    }

    @Transactional
    public void replaceDebater(Debater oldDebater, Debater newDebater) {
        List<Ballot> ballots = findBallotsByDebater(oldDebater);
        for (Ballot ballot : ballots) {
            ballot.setDebater(newDebater);
            ballotRepository.save(ballot);
        }
    }
    
//    ----------------------------SPEAKER TAB METHODS-------------------------------------
    /**
     * This method finds the ballots for 
     * substantive speeches given by a debater in a specific tournament
     * This excludes reply speeches, panel judging and iron-personing (substitutes with highest score)
     * @return List of Ballots that would appear in the speaker tab
     */
    public List<SpeakerTabBallot> findBallotsByTournamentAndDebater(Long tournamentId, Long debaterId) {
        
//        Get all ballots for the debater in the tournament and remove reply speeches
        List<SpeakerTabBallot> ballots = ballotRepository.findBallotsByTournamentIdAndDebaterId(tournamentId,
                debaterId);
        ballots.removeIf(b -> b.getSpeakerPosition() == 4);

        //Get rounds
        List<Long> roundIds = ballots.stream().map(SpeakerTabBallot::getRoundId).distinct().toList();
        List<SpeakerTabBallot> finalBallots = new ArrayList<>();

        //Merge ballots for each round
        for (Long roundId : roundIds) {
            Set<SpeakerTabBallot> ballotsForRound = ballots.stream().filter(b -> b.getRoundId().equals(roundId))
                    .collect(java.util.stream.Collectors.toSet());
            SpeakerTabBallot mergedBallot = getMergedBallotForRound(ballotsForRound);
            mergedBallot.setBallotId(null); 
            mergedBallot.setJudgeId(null);
            finalBallots.add(mergedBallot);
        }

        return finalBallots;
    }

    /**
     * Takes a set of (substantive speech) ballots for a round and merges them into a single ballot.
     * This accounts for multiple judges submitting ballots for the same speaker position in the same round
     * In cases of a debater iron-personing, this will pick the highest scored ballot
     */
    private SpeakerTabBallot getMergedBallotForRound(Set<SpeakerTabBallot> ballots) {

        Long roundId = ballots.iterator().next().getRoundId();
        Map<Integer, Float> positionScoreMap = new HashMap<>();
        Map<Integer, Integer> positionCountMap = new HashMap<>();

        for (SpeakerTabBallot ballot : ballots) {
            Integer position = ballot.getSpeakerPosition();
            Float score = ballot.getSpeakerScore();
            Float currentScore = positionScoreMap.get(position);
            if (currentScore == null) {
                positionScoreMap.put(position, score);
                positionCountMap.put(position, 1);
            } else {
                Float newScore = currentScore + score;
                positionScoreMap.put(position, newScore);
                positionCountMap.put(position, positionCountMap.get(position) + 1);
            }
        }
        // Calculate averages
        for (Map.Entry<Integer, Float> entry : positionScoreMap.entrySet()) {
            Integer position = entry.getKey();
            Float totalScore = entry.getValue();
            Integer count = positionCountMap.get(position);
            positionScoreMap.put(position, totalScore / count);
        }

        //return the highest scored position and score as a SpeakerTabBallot
        Float s1 = positionScoreMap.getOrDefault(1, 0f);
        Float s2 = positionScoreMap.getOrDefault(2, 0f);
        Float s3 = positionScoreMap.getOrDefault(3, 0f);

        if (s1 >= s2 && s1 >= s3) {
            return new SpeakerTabBallot(null, s1, 1, null, roundId);
        } else if (s2 >= s3) {
            return new SpeakerTabBallot(null, s2, 2, null, roundId);
        } else {
            return new SpeakerTabBallot(null, s3, 3, null, roundId);
        }

    }


    //    ---------------------------- END OF SPEAKER TAB METHODS-------------------------------------
}
