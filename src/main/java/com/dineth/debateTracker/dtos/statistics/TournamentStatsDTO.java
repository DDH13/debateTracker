package com.dineth.debateTracker.dtos.statistics;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TournamentStatsDTO {
    private String shortName;
    private Long id;

    private Integer debaterCount;
    private Integer judgeCount;
    private Integer teamCount;
    private Integer substantiveCount;
    private Integer replyCount;

    private Double debaterAverage;
    private Double debaterMedian;
    private Double debaterUpperQuartile;
    private Double debaterLowerQuartile;

    private Double firstAverage;
    private Double secondAverage;
    private Double thirdAverage;
    private Double replyAverage;
}
