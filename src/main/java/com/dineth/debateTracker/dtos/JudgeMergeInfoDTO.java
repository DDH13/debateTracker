package com.dineth.debateTracker.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor @AllArgsConstructor
@Setter @Getter
public class JudgeMergeInfoDTO {
    Long id;
    String firstName;
    String lastName;
    Integer breaks;
    Integer prelims;
    List<String> tournaments;
}
