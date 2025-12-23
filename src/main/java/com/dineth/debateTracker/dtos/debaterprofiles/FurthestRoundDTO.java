package com.dineth.debateTracker.dtos.debaterprofiles;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FurthestRoundDTO {
    public String tournamentName;
    public String roundName;
    public Boolean won;
    
    public FurthestRoundDTO(String tournamentName, String roundName, Boolean won) {
        this.tournamentName = tournamentName;
        this.roundName = roundName;
        this.won = won;
    }
}
