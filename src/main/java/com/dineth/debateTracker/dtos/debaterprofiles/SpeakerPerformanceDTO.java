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
public class SpeakerPerformanceDTO {
    private String tournamentName;
    private Integer prelimsDebated;
    private Integer rank;
    private Float average;
    private Float standardDeviation;
    private Date date;
}
