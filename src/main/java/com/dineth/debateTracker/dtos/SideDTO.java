package com.dineth.debateTracker.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SideDTO {
    private String teamId;
    private List<FinalTeamBallotDTO> finalTeamBallots;
    private List<SpeechDTO> speeches;
    private Long dbId;

    public SideDTO(String teamId, List<FinalTeamBallotDTO> finalTeamBallots, List<SpeechDTO> speeches) {
        this.teamId = teamId;
        this.finalTeamBallots = finalTeamBallots;
        this.speeches = speeches;
    }

    @Override
    public String toString() {
        return "SideDTO{" +
                "teamId='" + teamId + '\'' +
                ", ballots=" + finalTeamBallots +
                ", speeches=" + speeches +
                '}';
    }

}
