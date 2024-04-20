package com.dineth.debateTracker.ballot;

import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.judge.Judge;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity @Table(name="ballot") @Getter @Setter @NoArgsConstructor
public class Ballot implements Serializable {
    @Id
    private Long id;
    @ManyToOne
    private Debater prop1;
    private Float prop1Score;
    @ManyToOne
    private Debater prop2;
    private Float prop2Score;
    @ManyToOne
    private Debater prop3;
    private Float prop3Score;
    @ManyToOne
    private Debater propReply;
    private Float propReplyScore;
    @ManyToOne
    private Debater opp1;
    private Float opp1Score;
    @ManyToOne
    private Debater opp2;
    private Float opp2Score;
    @ManyToOne
    private Debater opp3;
    private Float opp3Score;
    @ManyToOne
    private Debater oppReply;
    private Float oppReplyScore;
    @ManyToOne
    private Judge judge;

    public String getWinner() {
        float propScore = prop1Score + prop2Score + prop3Score + propReplyScore;
        float oppScore = opp1Score + opp2Score + opp3Score + oppReplyScore;
        if (propScore > oppScore) {
            return "proposition";
        } else {
            return "opposition";
        }
    }

}
