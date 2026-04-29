package com.dineth.debateTracker.institution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    // find institutions containing the name
    List<Institution> findByNameContaining(String name);

    Institution findByName(String institutionName);

    // Native query to get institution id, name, abbreviation, team count and team names for all institutions
    @SuppressWarnings("SqlDialectInspection")
    @Query(value = "SELECT " +
            "    i.id, " +
            "    i.name, " +
            "    i.abbreviation, " +
            "    COUNT(DISTINCT t.id) AS team_count, " +
            "    COALESCE(ARRAY_AGG(DISTINCT t.team_name) FILTER (WHERE t.team_name IS NOT NULL), '{}') AS team_names " +
            "FROM institution i " +
            "LEFT JOIN team t ON t.institution_id = i.id " +
            "GROUP BY i.id, i.name, i.abbreviation" +
            " ORDER BY i.name ASC;",
            nativeQuery = true)
    List<Object> findInstitutionsWithTeamsCounts();
}
