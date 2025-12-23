package com.dineth.debateTracker.eliminationballot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EliminationBallotRepository extends JpaRepository<EliminationBallot, Long> {
    List<EliminationBallot> findByJudgeId(Long judgeId);
}
