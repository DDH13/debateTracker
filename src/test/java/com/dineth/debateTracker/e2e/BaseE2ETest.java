package com.dineth.debateTracker;

import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.debater.DebaterService;
import com.dineth.debateTracker.dtos.TournamentDataDTO;
import com.dineth.debateTracker.judge.Judge;
import com.dineth.debateTracker.judge.JudgeService;
import com.dineth.debateTracker.tournament.Tournament;
import com.dineth.debateTracker.tournament.TournamentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

/**
 * Base class for End-to-End tests that provides common tournament building infrastructure
 * and helper methods for test data access.
 * 
 * This class handles:
 * - Tournament building from XML files
 * - Common helper methods for finding entities by name/ID
 * - Shared test infrastructure and utilities
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseE2ETest {

    private static final Logger log = LoggerFactory.getLogger(BaseE2ETest.class);

    @Autowired
    protected TournamentBuilder tournamentBuilder;

    @Autowired
    protected TournamentService tournamentService;

    @Autowired
    protected DebaterService debaterService;

    @Autowired
    protected JudgeService judgeService;

    // ========================================
    // TOURNAMENT BUILDING HELPERS
    // ========================================

    /**
     * Build a tournament from an XML file and return the tournament data and ID
     */
    protected TournamentInfo buildTournament(String xmlPath, String tournamentName) {
        log.info("Building {} from {}...", tournamentName, xmlPath);
        TournamentDataDTO data = tournamentBuilder.buildMyTournament(xmlPath);
        Long tournamentId = findTournamentId(tournamentService, data);
        log.info("✓ {} built successfully with ID: {}", tournamentName, tournamentId);
        
        return new TournamentInfo(data, tournamentId);
    }

    /**
     * Find tournament ID from database by matching the short name
     */
    protected static Long findTournamentId(TournamentService tournamentService, TournamentDataDTO tournamentData) {
        if (tournamentData == null || tournamentData.getTournament() == null) {
            return null;
        }
        String shortName = tournamentData.getTournament().getShortName();
        return tournamentService.getTournaments().stream()
                .filter(t -> shortName.equals(t.getShortName()))
                .map(Tournament::getId)
                .findFirst()
                .orElse(null);
    }

    // ========================================
    // ENTITY FINDER HELPERS
    // ========================================

    /**
     * Find a debater by first and last name
     */
    protected Debater findDebaterByName(String firstName, String lastName) {
        List<Debater> debaters = debaterService.getDebaters();
        return debaters.stream()
                .filter(d -> d.getFirstName().equals(firstName) && d.getLastName().equals(lastName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Find a judge by first and last name
     */
    protected Judge findJudgeByName(String firstName, String lastName) {
        List<Judge> judges = judgeService.getJudges();
        return judges.stream()
                .filter(j -> j.getFname().equals(firstName) && j.getLname().equals(lastName))
                .findFirst()
                .orElse(null);
    }

    // ========================================
    // NESTED CLASSES FOR TEST DATA
    // ========================================

    /**
     * Container for tournament information
     */
    protected static class TournamentInfo {
        private final TournamentDataDTO data;
        private final Long id;

        public TournamentInfo(TournamentDataDTO data, Long id) {
            this.data = data;
            this.id = id;
        }

        public TournamentDataDTO getData() {
            return data;
        }

        public Long getId() {
            return id;
        }
    }
}


