package com.dineth.debateTracker.dtos.SpeakerTab;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SpeakerTabBallot {
    Long ballotId;
    Float speakerScore;
    Integer speakerPosition;
    Long judgeId;
    Long roundId;

    public SpeakerTabBallot(Long ballotId, Float speakerScore, Integer speakerPosition, Long judgeId, Long roundId) {
        this.ballotId = ballotId;
        this.speakerScore = speakerScore;
        this.speakerPosition = speakerPosition;
        this.judgeId = judgeId;
        this.roundId = roundId;
    }
}
