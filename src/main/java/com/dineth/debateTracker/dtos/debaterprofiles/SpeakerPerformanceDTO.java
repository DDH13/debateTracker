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
    public String tournamentName;
    public Integer prelimsDebated;
    public Integer rank;
    public Float average;
    public Float standardDeviation;
    public Date date;
}
