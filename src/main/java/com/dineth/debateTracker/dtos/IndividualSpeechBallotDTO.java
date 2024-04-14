package com.dineth.debateTracker.dtos;

public class IndividualSpeechBallotDTO {
    private String adjudicatorId;
    private double score;

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
