package com.dineth.debateTracker.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class IndividualSpeechBallotDTO {
    private String adjudicatorId;
    private double score;
    private Long dbId;

    public IndividualSpeechBallotDTO(String adjudicatorId, double score) {
        this.adjudicatorId = adjudicatorId;
        this.score = score;
    }
    @Override
    public String toString() {
        return "IndividualSpeechBallotDTO{" +
                "adjudicatorId='" + adjudicatorId + '\'' +
                ", score=" + score +
                '}';
    }

}
