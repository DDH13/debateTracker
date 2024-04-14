package com.dineth.debateTracker.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RoundDTO {
    private String name;
    private String abbreviation;
    private boolean elimination;
    private double feedbackWeight;
    private List<DebateDTO> debates;

    public RoundDTO(String name, String abbreviation, boolean elimination, double feedbackWeight, List<DebateDTO> debates) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.elimination = elimination;
        this.feedbackWeight = feedbackWeight;
        this.debates = debates;
    }

    @Override
    public String toString() {
        return "RoundDTO{" +
                "name='" + name + '\'' +
                ", abbreviation='" + abbreviation + '\'' +
                ", elimination=" + elimination +
                ", feedbackWeight=" + feedbackWeight +
                ", debates=" + debates +
                '}' + "\n";
    }
}

