package com.dineth.debateTracker.ballot;

import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.dtos.SpeakerTab.SpeakerTabBallot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BallotRepository extends JpaRepository<Ballot, Long> {
    List<Ballot> findBallotsByDebater(Debater debater);
    List<Ballot> findBallotsByJudgeId(Long judgeId);
    List<Ballot> findBallotBySpeakerScoreGreaterThan(Float speakerScore);
    
    @Query(value = """
            SELECT 
                b.id AS ballotId, b.speaker_score AS speakerScore, b.speaker_position AS speakerPosition,
                b.judge_id AS judgeId, r.id AS roundId
            FROM ballot b INNER JOIN debate d ON b.debate_id = d.id
            INNER JOIN round r ON d.round_id = r.id
            INNER JOIN tournament t ON r.tournament_id = t.id
            WHERE t.id = :tournamentId AND b.debater_id = :debaterId
            """, nativeQuery = true)
    List<SpeakerTabBallot> findBallotsByTournamentIdAndDebaterId(Long tournamentId, Long debaterId);
    
}
