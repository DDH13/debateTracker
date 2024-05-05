package com.dineth.debateTracker.motion;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "motion")
@Getter
@Setter
@NoArgsConstructor
public class Motion implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "motion_seq")
    @SequenceGenerator(name = "motion_seq", sequenceName = "motion_seq", allocationSize = 1)
    private Long id;
    private String code;
    @Column(columnDefinition = "TEXT")
    private String motion;
    @Column(columnDefinition = "TEXT")
    private String infoslide;
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Motion(String motion, String infoslide, String code) {
        this.code = code != null ? code : "";

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
                '}';
    }

}
