package com.dineth.debateTracker.round;

import com.dineth.debateTracker.motion.Motion;
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
    @OneToOne @JoinColumn(name = "motion_id")
    private Motion motion;
    @OneToMany @JoinColumn(name = "round_id")
    private List<Debate> debates;
}
