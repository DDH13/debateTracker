package com.dineth.debateTracker.dtos;

import java.util.List;

public class FinalTeamBallotDTO {
    private List<String> adjudicatorIds;
    private boolean minority;
    private boolean ignored;
    private int rank;
    private double score;

    public FinalTeamBallotDTO(List<String> adjudicatorIds, Boolean minority, boolean ignored, int rank, double score) {
        this.adjudicatorIds = adjudicatorIds;
        this.minority = minority;
        this.ignored = ignored;
        this.rank = rank;
        this.score = score;
    }

    @Override
    public String toString() {
        return "FinalTeamBallotDTO{" +
                "adjudicatorIds=" + adjudicatorIds +
                ", minority=" + minority +
                ", ignored=" + ignored +
                ", rank=" + rank +
                ", score=" + score +
                '}';
    }

}
