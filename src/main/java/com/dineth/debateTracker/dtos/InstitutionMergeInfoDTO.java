package com.dineth.debateTracker.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor @AllArgsConstructor
@Setter @Getter
public class InstitutionMergeInfoDTO {
    private Long id;
    private String name;
    private String abbreviation;
    private String teamCount;
    private List<String> teams;
}
