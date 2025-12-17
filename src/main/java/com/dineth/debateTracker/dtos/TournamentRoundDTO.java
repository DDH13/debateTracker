package com.dineth.debateTracker.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TournamentRoundDTO {
    private String tournamentShortName;
    private Long tournamentId;
    private Date date;
    private List<RoundScoreDTO> roundScores;

    public TournamentRoundDTO(String tournamentShortName, Long tournamentId, List<RoundScoreDTO> roundScores) {
        this.tournamentShortName = tournamentShortName;
        this.tournamentId = tournamentId;
        this.roundScores = roundScores;
    }

    public void addRoundScore(RoundScoreDTO roundScore) {
        if (roundScores == null) {
            roundScores = new ArrayList<>();
        }
        roundScores.add(roundScore);
    }

    public Integer getNumberOfRounds() {
        return (int) roundScores.stream().map(RoundScoreDTO::getRoundId).distinct().count();
    }
    
    public Double getAverageScore() {
        if (roundScores == null || roundScores.isEmpty()) {
            return 0.0;
        }
        return roundScores.stream().mapToDouble(RoundScoreDTO::getScore).average().orElse(0.0);
    }


}
