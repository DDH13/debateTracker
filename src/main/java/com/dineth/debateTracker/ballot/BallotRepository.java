package com.dineth.debateTracker.ballot;

import com.dineth.debateTracker.debater.Debater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BallotRepository extends JpaRepository<Ballot, Long> {
    List<Ballot> findBallotsByDebater(Debater debater);
    List<Ballot> findBallotsByJudgeId(Long judgeId);
    List<Ballot> findBallotBySpeakerScoreGreaterThan(Float speakerScore);
}
