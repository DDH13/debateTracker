package com.dineth.debateTracker.feedback;

import com.dineth.debateTracker.judge.Judge;
import com.dineth.debateTracker.team.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Getter
@Setter
@NoArgsConstructor
public class Feedback implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "feedback_seq")
    @SequenceGenerator(name = "feedback_seq", sequenceName = "feedback_seq", allocationSize = 1)
    private Long id;
    @ManyToOne
    private Judge targetJudge;
    private String agree;
    @Column(length = 3000)
    private String comments;
    private Float overallRating;
    private Float clashEvaluation;
    private Float clashOrganization;
    private Float trackingArguments;
    @ManyToOne
    private Judge sourceJudge;
    @ManyToOne
    private Team sourceTeam;
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

    public Feedback(Judge targetJudge, Float overallRating, Float clashEvaluation, Float clashOrganization, Float trackingArguments,String agree, String comments) {
        this.targetJudge = targetJudge;
        this.comments = comments;
        this.overallRating = overallRating;
        this.clashEvaluation = clashEvaluation;
        this.clashOrganization = clashOrganization;
        this.trackingArguments = trackingArguments;
        this.agree = agree;
    }

    @Override
    public String toString() {
        String source = sourceTeam!=null? sourceTeam.getTeamName() : sourceJudge.getFname() + " "+ sourceJudge.getLname();
        return "Feedback{" + "id='" + id + '\'' + ", name='" + targetJudge + '\'' + ", comments='" + comments + '\'' + ", overallRating='" + overallRating + '\'' + ", clashEvaluation='" + clashEvaluation + '\'' + ", clashOrganization='" + clashOrganization + '\'' + ", trackingArguments='" + trackingArguments + '\'' + "agree with decision= " + agree + '\'' + "submitted by " + source +'}';
    }
}
