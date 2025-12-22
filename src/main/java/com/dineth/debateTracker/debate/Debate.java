package com.dineth.debateTracker.debate;

import com.dineth.debateTracker.ballot.Ballot;
import com.dineth.debateTracker.eliminationballot.EliminationBallot;
import com.dineth.debateTracker.feedback.Feedback;
import com.dineth.debateTracker.motion.Motion;
import com.dineth.debateTracker.round.Round;
import com.dineth.debateTracker.team.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity @Table(name="debate") @Getter @Setter @NoArgsConstructor
public class Debate implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "debate_seq")
    @SequenceGenerator(name = "debate_seq", sequenceName = "debate_seq", allocationSize = 1)
    private Long id;
    @ManyToOne
    private Team proposition;
    @ManyToOne
    private Team opposition;
    @ManyToOne
    private Team winner;
    
    @OneToMany @JoinColumn(name = "debate_id")
    private List<Ballot> ballots;
    
    @OneToMany @JoinColumn(name = "debate_id")
    private List<EliminationBallot> eliminationBallots;
    
    @ManyToOne @JoinColumn(name = "motion_id")
    private Motion motion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private Round round;
    
    @OneToMany
    private List<Feedback> feedbacks;
    
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

    public Debate(Team proposition, Team opposition, Team winner, List<Ballot> ballots, Motion motion) {
        this.proposition = proposition;
        this.opposition = opposition;
        this.winner = winner;
        this.ballots = ballots;
        this.motion = motion;
    }

    @Override
    public String toString() {
        return "Debate{" +
                "id=" + id +
                ", proposition=" + proposition +
                ", opposition=" + opposition +
                ", winner=" + winner +
                ", ballots=" + ballots +
                ", motion=" + motion +
                '}';
    }
}
