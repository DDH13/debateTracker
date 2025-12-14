package com.dineth.debateTracker.ballot;

import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.judge.Judge;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity @Table(name="ballot") @Getter @Setter @NoArgsConstructor
public class Ballot implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ballot_seq")
    @SequenceGenerator(name = "ballot_seq", sequenceName = "ballot_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    private Judge judge;
    @ManyToOne
    private Debater debater;
    private Float speakerScore;
    private Integer speakerPosition;
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

    public Ballot(Judge judge, Debater debater, Float speakerScore, Integer speakerPosition) {
        this.judge = judge;
        this.debater = debater;
        this.speakerScore = speakerScore;
        this.speakerPosition = speakerPosition;
    }
    
    public Boolean isReply() {
        return this.speakerPosition != null && this.speakerPosition != 4;
    }

    @Override
    public String toString() {
        return "Ballot{" +
                "id=" + id +
                ", judge=" + judge +
                ", debater=" + debater +
                ", speakerScore=" + speakerScore +
                ", speakerPosition=" + speakerPosition +
                '}';
    }

}
