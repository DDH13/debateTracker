package com.dineth.debateTracker.institution;

import com.dineth.debateTracker.team.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Entity @Table(name="institution") @Getter @Setter @NoArgsConstructor
public class Institution implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "institution_seq")
    @SequenceGenerator(name = "institution_seq", sequenceName = "institution_seq", allocationSize = 1)
    private Long id;
    private String name;
    private String abbreviation;
    @OneToMany  @JoinColumn(name = "institution_id")
    List<Team> teams;
    @Transient
    private String tempId;

    public Institution(String tempId, String name, String abbreviation) {
        this.tempId = tempId;
        this.name = name;
        this.abbreviation = abbreviation;
    }

    @Override
    public String toString() {
        return "Institution{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", abbreviation='" + abbreviation + '\'' +
                '}';
    }
}
