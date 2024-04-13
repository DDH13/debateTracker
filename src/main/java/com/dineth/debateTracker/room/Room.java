package com.dineth.debateTracker.room;

import com.dineth.debateTracker.ballot.Ballot;
import com.dineth.debateTracker.institution.Institution;
import com.dineth.debateTracker.team.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Entity @Table(name="room") @Getter @Setter @NoArgsConstructor
public class Room implements Serializable {
    @Id
    private Long id;
    @ManyToOne
    private Team proposition;
    @ManyToOne
    private Team opposition;
    private String name;
    @ManyToOne
    private Team winner;
    @OneToMany @JoinColumn(name = "room_id")
    private List<Ballot> ballots;
    @Transient
    private String tempId;

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
