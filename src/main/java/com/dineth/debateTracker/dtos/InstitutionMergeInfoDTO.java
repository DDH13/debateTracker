package com.dineth.debateTracker.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor @AllArgsConstructor
@Setter @Getter
public class InstitutionMergeInfoDTO {
    Long id;
    String name;
    String abbreviation;
    String teamCount;
    List<String> teams;
}
