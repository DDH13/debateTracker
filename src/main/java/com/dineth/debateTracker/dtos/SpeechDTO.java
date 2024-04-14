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
    private List<IndividualSpeechBallotDTO> ballots;

    public SpeechDTO(String speakerId, boolean reply, List<IndividualSpeechBallotDTO> ballots) {
        this.speakerId = speakerId;
        this.reply = reply;
        this.ballots = ballots;
    }

    @Override
    public String toString() {
        return "SpeechDTO{" +
                "speakerId='" + speakerId + '\'' +
                ", reply=" + reply +
                ", ballots=" + ballots +
                '}';
    }

}
