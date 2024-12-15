package com.dineth.debateTracker.dtos;

import com.dineth.debateTracker.judge.Judge;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor
public class JudgeStatsDTO {
    private Judge judge;
    private int totalRoundsJudged;
    private int breaksJudged;
    private int prelimsJudged;
    private Double averageFirst;
    private Double averageSecond;
    private Double averageThird;
    private Double averageReply;
    private Double averageSubstantive;
    private Double stDeviation;
    private List<String> tournamentsJudged;
    private List<Double> substantiveScoresGiven;
    private List<Double> replyScoresGiven;

    public JudgeStatsDTO(Judge judge, int totalRoundsJudged, int breaksJudged, int prelimsJudged, Double averageFirst, Double averageSecond, Double averageThird, Double averageReply, Double averageSubstantive, Double stDeviation, List<String> tournamentsJudged, List<Double> substantiveScoresGiven, List<Double> replyScoresGiven) {
        this.judge = judge;
        this.totalRoundsJudged = totalRoundsJudged;
        this.breaksJudged = breaksJudged;
        this.prelimsJudged = prelimsJudged;
        this.averageFirst = averageFirst;
        this.averageSecond = averageSecond;
        this.averageThird = averageThird;
        this.averageReply = averageReply;
        this.averageSubstantive = averageSubstantive;
        this.stDeviation = stDeviation;
        this.tournamentsJudged = tournamentsJudged;
        this.substantiveScoresGiven = substantiveScoresGiven;
        this.replyScoresGiven = replyScoresGiven;
    }
}

