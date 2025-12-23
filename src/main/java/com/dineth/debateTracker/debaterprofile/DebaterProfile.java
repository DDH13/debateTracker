package com.dineth.debateTracker.debaterprofile;

import com.dineth.debateTracker.dtos.debaterprofiles.FurthestRoundDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "debater_profile")
@Getter
@Setter
@NoArgsConstructor
public class DebaterProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "debater_profile_seq")
    @SequenceGenerator(name = "debater_profile_seq", sequenceName = "debater_profile_seq", allocationSize = 1)
    private Long id;
    private Long debaterId;
    private String firstName;
    private String lastName;
    private String email;
    
    private Integer prelimsDebated;
    private Integer breaksDebated;
    private Float activityPercentile;
    
    private Integer tournamentsDebated;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<FurthestRoundDTO> furthestRounds;
    
    private Float winPercentagePrelims;
    private Float winPercentagePrelimsPercentile;
    private Float winPercentageBreaks;
    private Float winPercentageBreaksPercentile;
    
    private Float averageSpeakerScore;
    private Float speakerScorePercentile;
    
    public DebaterProfile(Long debaterId, String firstName, String lastName, String email) {
        this.debaterId = debaterId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    
    
}
