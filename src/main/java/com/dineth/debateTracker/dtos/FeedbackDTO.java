package com.dineth.debateTracker.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class FeedbackDTO {
    private String debateId;
    private Float clashEvaluation;
    private Float clashOrganization;
    private Float trackingArguments;
    private String comments;
    private String targetJudgeId;
    private Float overallRating;
    private String sourceTeamId;
    private String sourceJudgeId;
    private String agree;

    public FeedbackDTO(String debateId, Float clashEvaluation, Float clashOrganization, Float trackingArguments, String comments, String targetJudgeId, Float overallRating, String sourceTeamId, String sourceJudgeId, String agree) {
        this.debateId = debateId;
        this.clashEvaluation = clashEvaluation;
        this.clashOrganization = clashOrganization;
        this.trackingArguments = trackingArguments;
        this.comments = comments;
        this.targetJudgeId = targetJudgeId;
        this.overallRating = overallRating;
        this.sourceTeamId = sourceTeamId;
        this.sourceJudgeId = sourceJudgeId;
        this.agree = agree;
    }

    @Override
    public String toString() {
        return "FeedbackDTO{" +
                "debateId='" + debateId + '\'' +
                ", clashEvaluation='" + clashEvaluation + '\'' +
                ", clashOrganization='" + clashOrganization + '\'' +
                ", trackingArguments='" + trackingArguments + '\'' +
                ", comments='" + comments + '\'' +
                ", targetJudgeId='" + targetJudgeId + '\'' +
                ", overallRating='" + overallRating + '\'' +
                ", sourceTeamId='" + sourceTeamId + '\'' +
                ", sourceJudgeId='" + sourceJudgeId + '\'' +
                ", agree='" + agree + '\'' +
                '}';
    }
}
