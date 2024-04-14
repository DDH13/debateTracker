package com.dineth.debateTracker.team;

import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.institution.Institution;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Entity @Table(name="team") @Getter @Setter @NoArgsConstructor
public class Team implements Serializable {
    @Id  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "team_seq")
    @SequenceGenerator(name = "team_seq", sequenceName = "team_seq", allocationSize = 1)
    private Long id;
    private String teamName;
    private String teamCode;
    private Boolean isEligibleForBreaks;
    @ManyToMany
    private List<Debater> debaters;
    @ManyToOne
    private Institution institution;
    @Transient
    private String tempId;
    @Transient
    private String institutionId;

    public Team(String tempId, String teamName, String teamCode, Institution institution, List<Debater> debaters) {
        this.tempId = tempId;
        this.teamName = teamName;
        this.teamCode = teamCode;
        this.debaters = debaters;
    }

    @Override
    public String toString() {
        return "Team{" +
                "id='" + id + '\'' +
                ", teamName='" + teamName + '\'' +
                ", teamCode='" + teamCode + '\'' +
                ", debaters=" + debaters +
                "tempId='" + tempId + '\'' +
                '}';
    }

}