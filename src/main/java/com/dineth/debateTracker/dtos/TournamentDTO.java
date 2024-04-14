package com.dineth.debateTracker.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class TournamentDTO {
    private String fullName;
    private String shortName;
    private Long dbId;

    public TournamentDTO(String fullName, String shortName) {
        this.fullName = fullName;
        this.shortName = shortName;
    }
}
