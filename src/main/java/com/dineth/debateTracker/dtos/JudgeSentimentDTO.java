package com.dineth.debateTracker.dtos;

import com.dineth.debateTracker.judge.Judge;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JudgeSentimentDTO {
    private Judge judge;
    private int speechesJudged;
    private int leniencyCount;
    private int harshnessCount;
    private int neutralCount;
    private double leniency;
    private double harshness;
    private double overallSentiment;

    public JudgeSentimentDTO(Judge judge, int speechesJudged, int leniencyCount, int harshnessCount, int neutralCount, double l, double h, double o) {
        this.judge = judge;
        this.speechesJudged = speechesJudged;
        this.leniencyCount = leniencyCount;
        this.harshnessCount = harshnessCount;
        this.neutralCount = neutralCount;
        this.leniency = l;
        this.harshness = h;
        this.overallSentiment = o;
    }
}

