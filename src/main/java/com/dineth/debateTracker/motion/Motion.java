package com.dineth.debateTracker.motion;

import com.dineth.debateTracker.round.Round;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "motion")
@Getter
@Setter
@NoArgsConstructor
public class Motion implements Serializable {
    @Id
    private Long id;
    private String code;
    private String motion;
    private String infoslide;
    @OneToOne(mappedBy = "motion")
    private Round round;
    @Transient
    private String tempId;

    public Motion(String motion, String tempId, String infoslide, String code) {
        this.code = code != null ? code : "";
        this.tempId = tempId != null ? tempId : "";

        //replace all spaces, newlines, tabs with one space
        this.motion = motion != null ? motion.replaceAll("\\s+", " ").trim() : "";
        this.infoslide = infoslide != null ? infoslide.replaceAll("\\s+", " ").trim() : "";
    }

    @Override
    public String toString() {
        return "Motion{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", motion='" + motion + '\'' +
                ", infoslide='" + infoslide + '\'' +
                ", tempId='" + tempId + '\'' +
                '}';
    }

}
