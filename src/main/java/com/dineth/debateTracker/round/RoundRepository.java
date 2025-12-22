package com.dineth.debateTracker.round;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoundRepository extends JpaRepository<Round, Long> {
    
    @Query("SELECT r.roundName FROM Round r WHERE r.tournament.id = :tournamentId")
    List<String> findRoundNameByTournamentId(@Param("tournamentId") Long tournamentId);
}
