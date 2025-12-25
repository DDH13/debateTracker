package com.dineth.debateTracker.judgeprofile;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "judge_profile")
@Getter
@Setter
@NoArgsConstructor
public class JudgeProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "judge_profile_seq")
    @SequenceGenerator(name = "judge_profile_seq", sequenceName = "judge_profile_seq", allocationSize = 1)
    private Long id;
    private Long judgeId;
    private String firstName;
    private String lastName;
    private String email;
    private String code;
    
    private Integer prelimsJudged;
    private Integer breaksJudged;
    private Float activityPercentile;
    private Integer tournamentsJudged;
    private Integer overallActivityRank;
    private Integer breaksActivityRank;
    private Integer prelimsActivityRank;
    
    private Double averageFirst;
    private Double averageSecond;
    private Double averageThird;
    private Double averageSubstantive;

    private Integer leniencyCount;
    private Integer harshnessCount;
    private Integer neutralCount;
    private Double leniency;
    private Double harshness;
    private Double overallSentiment;
    
    public JudgeProfile(Long judgeId, String firstName, String lastName, String email) {
        this.judgeId = judgeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.code = UUID.randomUUID().toString();  
    }

}
