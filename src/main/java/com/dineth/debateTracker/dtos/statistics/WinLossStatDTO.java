package com.dineth.debateTracker.dtos.statistics;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WinLossStatDTO {
    private String firstName;
    private String lastName;
    private Long id;
    private Integer prelimWins;
    private Integer prelimLosses;
    private Integer breakWins;
    private Integer breakLosses;

    public WinLossStatDTO(String firstName, String lastName, Long id, Integer prelimWins, Integer prelimLosses, Integer breakWins, Integer breakLosses) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        this.prelimWins = prelimWins;
        this.prelimLosses = prelimLosses;
        this.breakWins = breakWins;
        this.breakLosses = breakLosses;
    }
    public WinLossStatDTO(String firstName, String lastName, Long id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        this.prelimWins = 0;
        this.prelimLosses = 0;
        this.breakWins = 0;
        this.breakLosses = 0;
    }
}
