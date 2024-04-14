package com.dineth.debateTracker.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class MotionDTO {
    private String id;
    private String motion;
    private String infoSlide;
    private String reference;
    private Long dbId;


    public MotionDTO(String id, String motion, String infoSlide, String reference) {
        this.id = id;
        this.motion = motion;
        this.infoSlide = infoSlide;
        this.reference = reference;
    }
}
