package com.dineth.debateTracker.round;

import com.dineth.debateTracker.debate.Debate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.util.List;

@Entity @Table(name="round") @Getter @Setter @NoArgsConstructor
public class Round implements Serializable {
    @Id
    private Long id;
    private String roundName;
    private Integer roundNo;
    private Boolean isBreakRound;
    @OneToMany @JoinColumn(name = "round_id")
    private List<Debate> debates;

    public Round(String roundName, Integer roundNo, Boolean isBreakRound) {
        this.roundName = roundName;
        this.roundNo = roundNo;
        this.isBreakRound = isBreakRound;
    }

    @Override
    public String toString() {
        return "Round{" +
                "id=" + id +
                ", roundName='" + roundName + '\'' +
                ", roundNo=" + roundNo +
                ", isBreakRound=" + isBreakRound +
                '}';
    }
}
