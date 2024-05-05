package com.dineth.debateTracker.team;

import com.dineth.debateTracker.debater.Debater;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
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

    public Team(String teamName, String teamCode, List<Debater> debaters) {
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
                '}';
    }

}
