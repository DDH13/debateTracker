package com.dineth.debateTracker.round;

import com.dineth.debateTracker.debate.Debate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity @Table(name="round") @Getter @Setter @NoArgsConstructor
public class Round implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "round_seq")
    @SequenceGenerator(name = "round_seq", sequenceName = "round_seq", allocationSize = 1)
    private Long id;
    private String roundName;
    private Integer roundNo;
    private Boolean isBreakRound;
    @OneToMany @JoinColumn(name = "round_id")
    private List<Debate> debates;
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
