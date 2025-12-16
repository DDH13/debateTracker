package com.dineth.debateTracker.debaterprofile;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String firstName;
    private String lastName;
    private String email;
    private Integer prelimsDebated;
    private Integer breaksDebated;
    private Float winPercentagePrelims;
    private Float winPercentageBreaks;
    
}
