package com.dineth.debateTracker.judge;

import com.dineth.debateTracker.dtos.JudgeMergeInfoDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JudgeRepository extends JpaRepository<Judge, Long> {
    Judge findByFnameAndLname(String fname, String lname);

    /**
     * Get all tournaments and scores for a judge in the preliminary rounds
     * @param judgeId The ID of the judge
     */
    @Query(value = "SELECT t.id as tid, t.shortName, r.id as rid, r.roundName, b.speakerScore, b.speakerPosition " +
            "FROM Tournament t " +
            "JOIN t.rounds r " +
            "JOIN r.debates d " +
            "JOIN d.ballots b " +
            "JOIN b.judge j " +
            "WHERE b.judge.id = :judgeId " +
            "ORDER BY t.shortName, r.roundName")
    List<Object> findTournamentsAndScoresForJudge(@Param("judgeId") Long judgeId);

    /**
     * Get all tournaments and break rounds judged by a judge
     * @param judgeId The ID of the judge
     */
    @Query(value = "SELECT t.id as tid, t.shortName, r.id as rid, r.roundName " +
            "FROM Tournament t " +
            "JOIN t.rounds r " +
            "JOIN r.debates d " +
            "JOIN d.eliminationBallots eb " +
            "JOIN eb.judge j " +
            "WHERE eb.judge.id = :judgeId " +
            "ORDER BY t.shortName, r.roundName")
    List<Object> findTournamentsAndBreaksJudged(@Param("judgeId") Long judgeId);

    @SuppressWarnings("SqlDialectInspection")
    @Query(value = "SELECT " +
            "    j.id, " +
            "    j.fname AS firstName, " +
            "    j.lname AS lastName, " +
            "    COUNT(DISTINCT eb.id) AS breaks, " +
            "    (COUNT(DISTINCT b.id) / 8.0) AS prelims, " + // Division by 8 included
            "    COALESCE(ARRAY_AGG(DISTINCT t.short_name) FILTER (WHERE t.short_name IS NOT NULL), '{}') AS tournaments " +
            "FROM judge j " +
            "LEFT JOIN ballot b ON j.id = b.judge_id " +
            "LEFT JOIN debate d1 ON b.debate_id = d1.id " +
            "LEFT JOIN elimination_ballot eb ON j.id = eb.judge_id " +
            "LEFT JOIN debate d2 ON eb.debate_id = d2.id " +
            "LEFT JOIN round r ON r.id = COALESCE(d1.round_id, d2.round_id) " +
            "LEFT JOIN tournament t ON r.tournament_id = t.id " +
            "GROUP BY j.id, j.fname, j.lname " +
            "ORDER BY j.lname ASC, j.fname ASC",
            nativeQuery = true)
    List<Object> findJudgePrelimsBreaksTournaments();

}
