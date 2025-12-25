package com.dineth.debateTracker.dtos.statistics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class IronStatsDTO {
    private Long debaterId;
    private String firstName;
    private String lastName;
    private Map<String, Integer> ironPersonCount;
    private Integer totalIronPersonCount;
    private Integer totalDebates;
    
    public IronStatsDTO(Long debaterId, String firstName, String lastName) {
        this.debaterId = debaterId;
        this.firstName = firstName;
        this.lastName = lastName;
        totalIronPersonCount = 0;
        totalDebates = 0;
    }
}
