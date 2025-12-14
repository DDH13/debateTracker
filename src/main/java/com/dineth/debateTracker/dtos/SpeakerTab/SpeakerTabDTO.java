package com.dineth.debateTracker.dtos.SpeakerTab;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SpeakerTabDTO {
    private Long tournamentId;
    private String tournamentShortName;
    private Integer minimumSpeeches;
    private List<SpeakerTabRowDTO> speakerTabRows;

    public SpeakerTabDTO(Long tournamentId, String tournamentShortName, Integer minimumSpeeches) {
        this.tournamentId = tournamentId;
        this.tournamentShortName = tournamentShortName;
        this.minimumSpeeches = minimumSpeeches;
    }

    public void addSpeakerTabRow(SpeakerTabRowDTO row) {
        if (row == null || row.getSpeechesCount() < this.minimumSpeeches)
            return;
        if (this.speakerTabRows == null) {
            this.speakerTabRows = new java.util.ArrayList<>();
        }
        this.speakerTabRows.add(row);
        this.speakerTabRows.sort((a, b) -> b.getAverageSpeakerScore().compareTo(a.getAverageSpeakerScore()));
    }

    public void setRanks() {
        if (this.speakerTabRows == null)
            return;

        int rank = 1;
        int prevRank = 1;
        Float previousScore = speakerTabRows.getFirst().getAverageSpeakerScore();

        for (SpeakerTabRowDTO row : this.speakerTabRows) {
            if (!row.getAverageSpeakerScore().equals(previousScore)) {
                prevRank = rank;
                previousScore = row.getAverageSpeakerScore();
            }
            rank++;
            row.setRank(prevRank);
        }
    }
    
}
