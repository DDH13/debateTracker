package com.dineth.debateTracker.tournament;

import com.dineth.debateTracker.breakcategory.BreakCategory;
import com.dineth.debateTracker.motion.Motion;
import com.dineth.debateTracker.round.Round;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "tournament")
@Getter
@Setter
@NoArgsConstructor
public class Tournament implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tournament_seq")
    @SequenceGenerator(name = "tournament_seq", sequenceName = "tournament_seq", allocationSize = 1)
    private Long id;
    private String fullName;
    private String shortName;
    private Date date;
    
    @OneToMany @JoinColumn(name = "tournament_id")
    private List<BreakCategory> breakCategories;
    
    @OneToMany(mappedBy = "tournament")
    private List<Round> rounds;
    
    @OneToMany @JoinColumn(name = "tournament_id")
    private List<Motion> motions;
    
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

    public Tournament(String fullName, String shortName) {
        this.fullName = fullName;
        this.shortName = shortName;
    }

}
