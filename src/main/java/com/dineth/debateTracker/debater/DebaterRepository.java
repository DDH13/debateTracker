package com.dineth.debateTracker.debater;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DebaterRepository extends JpaRepository<Debater, Long> {
    List<Debater> findDebatersByFirstNameEqualsIgnoreCaseAndLastNameEqualsIgnoreCase(String firstName, String lastName);

    List<Debater> findDebatersByFirstNameEqualsIgnoreCaseAndLastNameEqualsIgnoreCaseAndBirthdate(String firstName, String lastName, Date birthdate);

    @Query("SELECT d.firstName, d.lastName, COUNT(d.id) FROM Debater d GROUP BY d.firstName, d.lastName HAVING COUNT(d.id) > 1")
    List<Object[]> findDebaterNameDuplicates();

    @Query("SELECT d.firstName, d.lastName, d.birthdate, COUNT(d.id) FROM Debater d GROUP BY d.firstName, d.lastName, d.birthdate HAVING COUNT(d.id) > 1")
    List<Object[]> findDebaterNameAndBirthdayDuplicates();

    List<Debater> findByFirstNameAndLastNameAllIgnoreCase(String firstName, String lastName);

    @Query(value = "SELECT t.id as tid, t.shortName, r.id as rid, r.roundName, b.speakerScore, b.speakerPosition " +
            "FROM Tournament t " +
            "JOIN t.rounds r " +
            "JOIN r.debates d " +
            "JOIN d.ballots b " +
            "WHERE b.debater.id = :debaterID " +
            "ORDER BY t.shortName, r.roundName")
    List<Object> findTournamentsAndScoresForSpeaker(@Param("debaterID") Long debaterID);
    
    List<Debater> findByInstitutionId(Long institutionId);

    @SuppressWarnings("SqlDialectInspection")
    @Query(value = "SELECT d.id, d.first_name, d.last_name, d.full_name, d.phone, " +
            "COUNT(DISTINCT b.id) AS rounds, " +
            "ARRAY_AGG(DISTINCT tm.team_name) AS team_names " +
            "FROM debater d " +
            "JOIN ballot b ON b.debater_id = d.id " +
            "JOIN team_debaters td ON d.id = td.debaters_id " +
            "JOIN team tm ON td.team_id = tm.id " +
            "WHERE b.speaker_score > 40.5 " +
            "GROUP BY d.id, d.first_name, d.last_name, d.full_name, d.phone " +
            "ORDER BY d.first_name ASC, d.last_name ASC;",
            nativeQuery = true)
    List<Object> findDebatersWithTeamsSpeaksRounds();
    
    List<Debater> findDebatersByInstitutionId(Long institutionId);

}
