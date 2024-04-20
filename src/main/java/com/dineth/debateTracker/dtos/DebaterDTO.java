package com.dineth.debateTracker.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DebaterDTO {
    private String id;
    private String name;
    private String institutionId;
    private String categories;
    private Long dbId;

    public DebaterDTO(String id, String name, String institutionId, String categories) {
        this.id = id;
        this.name = name;
        this.institutionId = institutionId;
        this.categories = categories;
    }

    @Override
    public String toString() {
        return "RoundDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", institutionId='" + institutionId + '\'' +
                ", categories='" + categories + '\'' +
                '}' + "\n";
    }
}

