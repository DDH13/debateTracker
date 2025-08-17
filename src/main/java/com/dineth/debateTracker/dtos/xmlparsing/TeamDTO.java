package com.dineth.debateTracker.dtos.xmlparsing;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TeamDTO {
    private String id;
    private String name;
    private String code;
    private String breakEligibilities;
    private List<DebaterDTO> debaters;
    private Long dbId;
    private String institutionId;

    public TeamDTO(String id, String name, String code, List<DebaterDTO> debaters, String breakEligibilities) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.debaters = debaters;
        this.breakEligibilities = breakEligibilities;
    }

    @Override
    public String toString() {
        return "RoundDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", debaters=" + debaters +
                ", breakEligibilities='" + breakEligibilities + '\'' +
                '}';
    }
}

