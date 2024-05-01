package com.dineth.debateTracker.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SpeechDTO {
    private String speakerId;
    private boolean reply;
    private List<IndividualSpeechBallotDTO> individualSpeechBallots;
    private Long dbId;

    public SpeechDTO(String speakerId, boolean reply, List<IndividualSpeechBallotDTO> individualSpeechBallots) {
        this.speakerId = speakerId;
        this.reply = reply;
        this.individualSpeechBallots = individualSpeechBallots;
    }

    @Override
    public String toString() {
        return "SpeechDTO{" +
                "speakerId='" + speakerId + '\'' +
                ", reply=" + reply +
                ", ballots=" + individualSpeechBallots +
                '}';
    }

}
