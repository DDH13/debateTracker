package com.dineth.debateTracker.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DebateDTO {
    private String id;
    private String adjudicatorIds;
    private String chairId;
    private String venueIds;
    private String motionId;
    private List<SideDTO> sides;

    public DebateDTO(String id, String adjudicatorIds, String chairId, String venueIds, String motionId, List<SideDTO> sides) {
        this.id = id;
        this.adjudicatorIds = adjudicatorIds;
        this.chairId = chairId;
        this.venueIds = venueIds;
        this.motionId = motionId;
        this.sides = sides;
    }

    @Override
    public String toString() {
        return "DebateDTO{" +
                "id='" + id + '\'' +
                ", adjudicatorIds='" + adjudicatorIds + '\'' +
                ", chairId='" + chairId + '\'' +
                ", venueIds='" + venueIds + '\'' +
                ", motionId='" + motionId + '\'' +
                ", sides=" + sides +
                '}'+ "\n";
    }

}
