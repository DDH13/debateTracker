package com.dineth.debateTracker.dtos.statistics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class SpeakerScorePerformanceDTO {
    private Long debaterId;
    private String firstName;
    private String lastName;
    private Long tournamentId;
    private String tournamentShortName;
    private Double averageSpeakerScore;
    private Integer speechesGiven;
    private Integer rank;
}
