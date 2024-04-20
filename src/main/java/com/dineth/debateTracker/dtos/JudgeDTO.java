package com.dineth.debateTracker.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class JudgeDTO {
    private String id;
    private String name;
    private Float score;
    private Boolean core;
    private Boolean independent;
    private Long dbId;

    public JudgeDTO(String id, String name, Float score, Boolean core, Boolean independent) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.core = core;
        this.independent = independent;
    }
    @Override
    public String toString() {
        return "DebateDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", score='" + score + '\'' +
                ", core='" + core + '\'' +
                ", independent='" + independent + '\'' +
                '}';
    }

}
