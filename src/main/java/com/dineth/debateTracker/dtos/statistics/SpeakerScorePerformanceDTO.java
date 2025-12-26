package com.dineth.debateTracker.dtos.statistics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class SpeakerScorePerformanceDTO {
    public Long debaterId;
    public String firstName;
    public String lastName;
    public Long tournamentId;
    public String tournamentShortName;
    public Double averageSpeakerScore;
    public Integer SpeechesGiven;
    public Integer rank;
}
