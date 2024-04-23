package com.dineth.debateTracker.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class BreakCategoryDTO {
    private String id;
    private String name;
    private Long dbId;

    public BreakCategoryDTO(String id, String name) {
        this.id = id;
        this.name = name;
    }
}


