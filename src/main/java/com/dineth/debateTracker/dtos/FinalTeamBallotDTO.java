package com.dineth.debateTracker.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor
public class FinalTeamBallotDTO {
    private List<String> adjudicatorIds;
    private boolean minority;
    private boolean ignored;
    private int rank;
    private double score;
    private Long dbId;

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
