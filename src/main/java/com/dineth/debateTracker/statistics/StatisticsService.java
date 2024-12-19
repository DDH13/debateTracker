package com.dineth.debateTracker.statistics;

import com.dineth.debateTracker.ballot.Ballot;
import com.dineth.debateTracker.ballot.BallotRepository;
import com.dineth.debateTracker.dtos.JudgeSentimentDTO;
import com.dineth.debateTracker.judge.Judge;
import com.dineth.debateTracker.judge.JudgeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service @Slf4j
public class StatisticsService {
    private final BallotRepository ballotRepository;
    private final JudgeRepository judgeRepository;

    StatisticsService(BallotRepository ballotRepository, JudgeRepository judgeRepository) {
        this.ballotRepository = ballotRepository;
        this.judgeRepository = judgeRepository;
    }

    public HashMap<String,Double> getGlobalDistribution(){
        List<Ballot> ballots = ballotRepository.findBallotBySpeakerScoreGreaterThan(40.5f);
        int totalBallots = ballots.size();
        double scores = ballots.stream().mapToDouble(Ballot::getSpeakerScore).sum();
        Double globalMean = scores/totalBallots;
        Double stdDeviation = Math.sqrt(ballots.stream().mapToDouble(ballot -> Math.pow(ballot.getSpeakerScore() - globalMean, 2)).average().orElse(0.0));
        return new HashMap<>() {{
            put("mean", globalMean);
            put("stdDeviation", stdDeviation);
        }};
    }
    /**
     * Get sentiment of judges
     *
     * @param allowedDeviation - the extent to which a judge is allowed to deviate from the average score of a debater before being considered lenient or harsh
     * @return List of JudgeSentimentDTO
     */

    public List<JudgeSentimentDTO> getSentiment(double allowedDeviation){
//        Querying db for initial data
        List <Ballot> ballots = ballotRepository.findBallotBySpeakerScoreGreaterThan(40.5f);
        List<Judge> judges = judgeRepository.findAll();

//      Storing the average scores of debaters excluding the scores given by a specific judge to inefficient repeated calculations
        HashMap<Long, HashMap<Long, Double>> debaterAverages = new HashMap<>();
        for (Ballot ballot: ballots){
            Long debaterId = ballot.getDebater().getId();
            Long judgeId = ballot.getJudge().getId();
            if (!debaterAverages.containsKey(debaterId)){
                debaterAverages.put(debaterId, new HashMap<>());
            }
            if (!debaterAverages.get(debaterId).containsKey(judgeId)){
                Double avg = calculateSpeakerAverageExcludingJudge(judgeId, debaterId, ballots);
                debaterAverages.get(debaterId).put(judgeId, avg);
            }
        }

        List<JudgeSentimentDTO> judgeSentiments = new ArrayList<>();

        for (Judge judge: judges){
//            Get all ballots for the given judge
            List<Ballot> judgeBallots = ballots.stream().filter(ballot -> ballot.getJudge().getId().equals(judge.getId())).toList();

//          If a judge has not judged any debates, they are not included in the analysis
            if (judgeBallots.isEmpty()){
                continue;
            }

            List<Double> leniency = new ArrayList<>();
            List<Double> harshness = new ArrayList<>();
            int neutrality = 0;
            int speechesConsidered = 0;

//            Calculating leniency and harshness
            for (Ballot ballot: judgeBallots){
                Double debaterAvg = debaterAverages.get(ballot.getDebater().getId()).get(judge.getId());
                if (debaterAvg != null){
                    if ((ballot.getSpeakerScore() - debaterAvg) >= allowedDeviation){
                        leniency.add(ballot.getSpeakerScore() - debaterAvg);
                    } else if ((ballot.getSpeakerScore() - debaterAvg) <= -allowedDeviation){
                        harshness.add(ballot.getSpeakerScore() - debaterAvg);
                    } else {
                        neutrality++;
                    }
                    speechesConsidered++;
                }
            }

            double leniencySum = leniency.stream().mapToDouble(Double::doubleValue).sum();
            double harshnessSum = harshness.stream().mapToDouble(Double::doubleValue).sum();
            double overallSentiment = (leniencySum + harshnessSum)/speechesConsidered;

            JudgeSentimentDTO judgeSentiment = new JudgeSentimentDTO(judge, speechesConsidered, leniency.size(), harshness.size(), neutrality, leniencySum, harshnessSum, overallSentiment);

            judgeSentiments.add(judgeSentiment);
        }
        return judgeSentiments;
    }

    /**
     * Calculate the average score of a debater excluding the scores given by a specific judge
     * @param judgeId - the id of the judge whose scores are to be excluded
     * @param debaterId - the id of the debater whose average score is to be calculated
     * @param ballots - list of all ballots to be considered
     */
    public Double calculateSpeakerAverageExcludingJudge(Long judgeId, Long debaterId, List<Ballot> ballots)
    {
//        get all ballots for a debater
        List<Ballot> debaterBallots = new ArrayList<>(ballots.stream().filter(ballot -> ballot.getDebater().getId().equals(debaterId)).toList());
//        remove ballots from the judge
        debaterBallots.removeIf(ballot -> ballot.getJudge().getId().equals(judgeId));
//        check if the debater has a minimum number of ballots to calculate average
        int minBallots = 5;
        if (debaterBallots.size() < minBallots){
            return null;
        }
        return debaterBallots.stream().mapToDouble(Ballot::getSpeakerScore).average().orElse(0.0);
    }

}
