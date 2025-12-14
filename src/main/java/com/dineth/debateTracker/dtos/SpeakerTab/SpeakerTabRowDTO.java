package com.dineth.debateTracker.dtos.SpeakerTab;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SpeakerTabRowDTO {
    private Long debaterId;
    private List<SpeakerTabBallot> speakerScoreBallots;
    private Float averageSpeakerScore;
    private Integer speechesCount;
    private Double standardDeviation;
    private Integer rank;

    private void setSpeechesCount() {
        if (this.speakerScoreBallots != null) {
            this.speechesCount = this.speakerScoreBallots.size();
        } else {
            this.speechesCount = 0;
        }
    }

    private void setAverageSpeakerScore() {
        if (this.speakerScoreBallots != null && !this.speakerScoreBallots.isEmpty()) {
            double avg = this.speakerScoreBallots.stream()
                    .mapToDouble(b -> b != null && b.getSpeakerScore() != null ? b.getSpeakerScore() : 0.0).average()
                    .orElse(0.0);
            this.averageSpeakerScore = (float) avg;
        } else {
            this.averageSpeakerScore = 0f;
        }
    }
    
    private void setStandardDeviation() {
        if (this.speakerScoreBallots != null && !this.speakerScoreBallots.isEmpty()) {
            double avg = this.averageSpeakerScore;
            double variance = this.speakerScoreBallots.stream()
                    .mapToDouble(b -> {
                        if (b != null && b.getSpeakerScore() != null) {
                            double diff = b.getSpeakerScore() - avg;
                            return diff * diff;
                        } else {
                            return 0.0;
                        }
                    })
                    .average()
                    .orElse(0.0);
            this.standardDeviation = Math.sqrt(variance);
        } else {
            this.standardDeviation = 0.0;
        }
    }

    public SpeakerTabRowDTO(Long debaterId, List<SpeakerTabBallot> speakerScoreBallots) {
        this.debaterId = debaterId;
        this.speakerScoreBallots = speakerScoreBallots;
        setAverageSpeakerScore();
        setSpeechesCount();
        setStandardDeviation();
    }
}

