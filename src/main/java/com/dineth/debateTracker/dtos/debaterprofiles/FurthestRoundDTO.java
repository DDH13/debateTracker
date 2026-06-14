package com.dineth.debateTracker.dtos.debaterprofiles;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FurthestRoundDTO {
    private String tournamentName;
    private String roundName;
    private Boolean won;
    private Date date;
}
