package com.dineth.debateTracker.round;

import com.dineth.debateTracker.motion.Motion;
import com.dineth.debateTracker.room.Room;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.util.List;

@Entity @Table(name="round") @Getter @Setter @NoArgsConstructor
public class Round implements Serializable {
    @Id
    private String id;
    private String roundName;
    private Integer roundNo;
    private Boolean isBreakRound;
    @OneToOne
    private Motion motion;
    @OneToMany
    private List<Room> rooms;

}
