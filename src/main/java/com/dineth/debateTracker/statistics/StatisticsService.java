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

    public List<JudgeSentimentDTO> getSentiment(){
//        Querying db for initial data
        List <Ballot> ballots = ballotRepository.findBallotBySpeakerScoreGreaterThan(40.5f);
        List<Judge> judges = judgeRepository.findAll();

//        Calculating average scores for all debaters
        HashMap<Long,List<Double>> debaterScores = new HashMap<>();
        HashMap<Long,Double> debaterAverages = new HashMap<>();

//        Sorting ballots by debater
        for (Ballot ballot: ballots){
            Long debaterId = ballot.getDebater().getId();
            if (debaterScores.containsKey(debaterId)){
                debaterScores.get(debaterId).add((double) ballot.getSpeakerScore());
            } else {
                List<Double> temp = new ArrayList<>();
                temp.add((double) ballot.getSpeakerScore());
                debaterScores.put(debaterId,temp);
            }
        }
//        Calculating average scores for all debaters
        for (Long debaterId: debaterScores.keySet()){
            List<Double> scores = debaterScores.get(debaterId);
            Double avg = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            debaterAverages.put(debaterId, avg);
        }

        List<JudgeSentimentDTO> judgeSentiments = new ArrayList<>();

        for (Judge judge: judges){
            List<Ballot> judgeBallots = ballots.stream().filter(ballot -> ballot.getJudge().getId().equals(judge.getId())).toList();

//          If a judge has not judged any debates, they are not included in the analysis
            if (judgeBallots.isEmpty()){
                continue;
            }

            List<Double> leniency = new ArrayList<>();
            List<Double> harshness = new ArrayList<>();
            int neutrality = 0;
//            Decides how much a judge is allowed to deviate from the average before being considered lenient or harsh
            double allowedDeviation = 0.5;

//            Calculating leniency and harshness
            for (Ballot ballot: judgeBallots){
                Double debaterAvg = debaterAverages.get(ballot.getDebater().getId());
                if (debaterAvg != null){
                    if ((ballot.getSpeakerScore() - debaterAvg) >= allowedDeviation){
                        leniency.add(ballot.getSpeakerScore() - debaterAvg);
                    } else if ((ballot.getSpeakerScore() - debaterAvg) <= -allowedDeviation){
                        harshness.add(ballot.getSpeakerScore() - debaterAvg);
                    } else {
                        neutrality++;
                    }
                }
            }

            double leniencySum = leniency.stream().mapToDouble(Double::doubleValue).sum();
            double harshnessSum = harshness.stream().mapToDouble(Double::doubleValue).sum();
            double overallSentiment = (leniencySum + harshnessSum)/(judgeBallots.size());

            JudgeSentimentDTO judgeSentiment = new JudgeSentimentDTO(judge, judgeBallots.size(), leniency.size(), harshness.size(), neutrality, leniencySum, harshnessSum, overallSentiment);

            judgeSentiments.add(judgeSentiment);
        }
        return judgeSentiments;
    }

}
