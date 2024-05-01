package com.dineth.debateTracker.debate;

import com.dineth.debateTracker.ballot.Ballot;
import com.dineth.debateTracker.motion.Motion;
import com.dineth.debateTracker.team.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
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
    @ManyToOne @JoinColumn(name = "motion_id")
    private Motion motion;

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
    public Team getWinner() {
        String winningSide;
        if (ballots.size() == 1) {
            winningSide = ballots.get(0).getWinner();
        } else {
            int propositionWins = 0;
            int oppositionWins = 0;
            for (Ballot ballot : ballots) {
                if (ballot.getWinner().equals("proposition")) {
                    propositionWins++;
                } else {
                    oppositionWins++;
                }
            }
            if (propositionWins > oppositionWins) {
                winningSide = "proposition";
            } else  {
                winningSide = "opposition";
            }

        }
        return winningSide.equals("proposition") ? proposition : opposition;
    }

}
