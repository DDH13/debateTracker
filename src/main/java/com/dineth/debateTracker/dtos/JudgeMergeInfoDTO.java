package com.dineth.debateTracker.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor @AllArgsConstructor
@Setter @Getter
public class JudgeMergeInfoDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private Integer breaks;
    private Integer prelims;
    private List<String> tournaments;
}
