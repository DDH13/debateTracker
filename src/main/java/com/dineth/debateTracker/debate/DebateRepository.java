package com.dineth.debateTracker.debate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DebateRepository extends JpaRepository<Debate, Long> {
    @Query("SELECT d FROM Debate d JOIN d.ballots b WHERE b.debater.id = :debaterId")
    List<Debate> findPrelimsByDebaterId(@Param("debaterId") Long debaterId);

    @Query("""
    SELECT d FROM Debate d JOIN d.eliminationBallots eb JOIN eb.winner w
    JOIN eb.loser l JOIN w.debaters wd JOIN l.debaters ld WHERE (wd.id = :debaterId OR ld.id = :debaterId)
""")
    List<Debate> findBreaksByDebaterId(@Param("debaterId") Long debaterId);

    @Query("SELECT d FROM Debate d WHERE d.round.id = :roundId")
    List<Debate> findDebatesByRoundId(@Param("roundId") Long roundId);

}
