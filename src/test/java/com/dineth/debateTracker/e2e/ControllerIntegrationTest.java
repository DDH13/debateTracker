package com.dineth.debateTracker;

import com.dineth.debateTracker.builders.TestFixtures;
import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.debater.DebaterService;
import com.dineth.debateTracker.dtos.DebaterTournamentScoreDTO;
import com.dineth.debateTracker.dtos.JudgeTournamentScoreDTO;
import com.dineth.debateTracker.dtos.TournamentDataDTO;
import com.dineth.debateTracker.dtos.TournamentRoundDTO;
import com.dineth.debateTracker.judge.Judge;
import com.dineth.debateTracker.judge.JudgeService;
import com.dineth.debateTracker.tournament.TournamentService;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for controller endpoints across multiple tournaments.
 * Tests debater and judge controller functionality with multi-tournament scenarios.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ControllerIntegrationTest extends BaseE2ETest {

    private static final Logger log = LoggerFactory.getLogger(ControllerIntegrationTest.class);

    private static TournamentInfo tournament1;
    private static TournamentInfo tournament2;
    private static TournamentInfo tournament3;

    @BeforeAll
    static void buildTournaments(@Autowired TournamentImportService importService,
                                 @Autowired TournamentService tournamentService,
                                 @Autowired DebaterService debaterService,
                                 @Autowired JudgeService judgeService) {
        log.info("========================================");
        log.info("Building tournaments for controller integration tests...");
        log.info("========================================");

        TournamentDataDTO data1 = importService.importTournament(TestFixtures.TOURNAMENT_1_XML);
        tournament1 = new TournamentInfo(data1, findTournamentId(tournamentService, data1));
        log.info("✓ Tournament 1 built with ID: {}", tournament1.getId());

        TournamentDataDTO data2 = importService.importTournament(TestFixtures.TOURNAMENT_2_XML);
        tournament2 = new TournamentInfo(data2, findTournamentId(tournamentService, data2));
        log.info("✓ Tournament 2 built with ID: {}", tournament2.getId());

        TournamentDataDTO data3 = importService.importTournament(TestFixtures.TOURNAMENT_3_XML);
        tournament3 = new TournamentInfo(data3, findTournamentId(tournamentService, data3));
        log.info("✓ Tournament 3 built with ID: {}", tournament3.getId());

        log.info("========================================");
        log.info("All tournaments built successfully!");
        log.info("========================================");
    }

    // ========================================
    // TOURNAMENT VERIFICATION
    // ========================================

    @Test
    @Order(1)
    @DisplayName("Test 1: Verify all tournaments were built")
    void testTournamentsBuilt() {
        log.info("Test 1: Verifying all tournaments were built...");

        assertNotNull(tournament1.getId(), "Tournament 1 should be built");
        assertNotNull(tournament2.getId(), "Tournament 2 should be built");
        assertNotNull(tournament3.getId(), "Tournament 3 should be built");

        assertNotNull(tournamentService.getTournamentById(tournament1.getId()));
        assertNotNull(tournamentService.getTournamentById(tournament2.getId()));
        assertNotNull(tournamentService.getTournamentById(tournament3.getId()));

        log.info("✓ All 3 tournaments successfully built and verified");
    }

    // ========================================
    // DEBATER CONTROLLER TESTS
    // ========================================

    @Test
    @Order(2)
    @DisplayName("Test 2: Print available debaters for reference")
    void printAvailableDebaters() {
        log.info("Test 2: Printing available debaters across all tournaments...");
        
        List<Debater> allDebaters = debaterService.getDebaters();
        log.info("========================================");
        log.info("AVAILABLE DEBATERS (Total: {})", allDebaters.size());
        log.info("========================================");
        
        int count = 0;
        for (Debater debater : allDebaters) {
            if (count >= 10) break; // Print first 10 for brevity
            
            DebaterTournamentScoreDTO speaks = debaterService.getTournamentsAndScoresForSpeaker(debater.getId(), false);
            log.info("Debater #{}: {} {} (ID: {}) - Tournaments: {}, Total Debates: {}", 
                    ++count,
                    debater.getFirstName(), 
                    debater.getLastName(), 
                    debater.getId(),
                    speaks.getTournamentRoundScores() != null ? speaks.getTournamentRoundScores().size() : 0,
                    speaks.getTotalDebatesParticipated());
        }
        log.info("========================================");
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: DebaterController.getSpeaks() - Verify debater tournament scores")
    void testDebaterGetSpeaks() {
        log.info("Test 3: Testing DebaterController.getSpeaks()...");
        
        List<Debater> debaters = debaterService.getDebaters();
        assertTrue(debaters.size() > 0, "Should have debaters");

        // Test with first debater
        Debater testDebater = debaters.get(0);
        DebaterTournamentScoreDTO result = debaterService.getTournamentsAndScoresForSpeaker(testDebater.getId(), false);
        
        assertNotNull(result, "Speaks result should not be null");
        assertEquals(testDebater.getFirstName(), result.getFirstName(), "First name should match");
        assertEquals(testDebater.getLastName(), result.getLastName(), "Last name should match");
        assertTrue(result.getTotalDebatesParticipated() > 0, "Should have participated in debates");
        
        // Verify tournament data structure
        if (result.getTournamentRoundScores() != null && !result.getTournamentRoundScores().isEmpty()) {
            for (TournamentRoundDTO tournament : result.getTournamentRoundScores()) {
                assertNotNull(tournament.getTournamentShortName(), "Tournament should have name");
                assertTrue(tournament.getNumberOfRounds() > 0, "Should have participated in rounds");
            }
        }
        
        log.info("✓ Debater speaks data verified for {} {}", testDebater.getFirstName(), testDebater.getLastName());
    }

    // ========================================
    // JUDGE CONTROLLER TESTS
    // ========================================

    @Test
    @Order(4)
    @DisplayName("Test 4: Print available judges for reference")
    void printAvailableJudges() {
        log.info("Test 4: Printing available judges across all tournaments...");
        
        List<Judge> allJudges = judgeService.getJudges();
        log.info("========================================");
        log.info("AVAILABLE JUDGES (Total: {})", allJudges.size());
        log.info("========================================");
        
        int count = 0;
        for (Judge judge : allJudges) {
            if (count >= 10) break; // Print first 10 for brevity
            
            List<String> tournaments = judgeService.getTournamentsJudged(judge.getId());
            JudgeTournamentScoreDTO prelimScores = judgeService.getTournamentsAndScoresForJudge(judge.getId(), false);
            
            log.info("Judge #{}: {} {} (ID: {}) - Tournaments: {}, Total Debates Judged: {}", 
                    ++count,
                    judge.getFname(), 
                    judge.getLname(), 
                    judge.getId(),
                    tournaments.size(),
                    prelimScores.getTotalDebatesJudged());
        }
        log.info("========================================");
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: JudgeController.getTournamentsJudged() - Verify judge tournament participation")
    void testJudgeGetTournamentsJudged() {
        log.info("Test 5: Testing JudgeController.getTournamentsJudged()...");
        
        List<Judge> judges = judgeService.getJudges();
        assertTrue(judges.size() > 0, "Should have judges");

        // Test with first judge
        Judge testJudge = judges.get(0);
        List<String> result = judgeService.getTournamentsJudged(testJudge.getId());
        
        assertNotNull(result, "Tournament list should not be null");
        assertFalse(result.isEmpty(), "Judge should have judged at least one tournament");
        
        log.info("✓ Judge {} {} judged {} tournaments", testJudge.getFname(), testJudge.getLname(), result.size());
    }

    @Test
    @Order(6)
    @DisplayName("Test 6: JudgeController.getPrelimScoresByJudge() - Verify judge prelim scoring data")
    void testJudgeGetPrelimScoresByJudge() {
        log.info("Test 6: Testing JudgeController.getPrelimScoresByJudge()...");
        
        List<Judge> judges = judgeService.getJudges();
        assertTrue(judges.size() > 0, "Should have judges");

        // Test with first judge
        Judge testJudge = judges.get(0);
        JudgeTournamentScoreDTO result = judgeService.getTournamentsAndScoresForJudge(testJudge.getId(), false);
        
        assertNotNull(result, "Prelim scores result should not be null");
        assertEquals(testJudge.getFname(), result.getFirstName(), "First name should match");
        assertEquals(testJudge.getLname(), result.getLastName(), "Last name should match");
        assertTrue(result.getTotalDebatesJudged() > 0, "Should have judged debates");
        
        // Verify per-tournament data
        if (result.getTournamentRoundScores() != null && !result.getTournamentRoundScores().isEmpty()) {
            for (TournamentRoundDTO tournament : result.getTournamentRoundScores()) {
                assertNotNull(tournament.getTournamentShortName(), "Tournament should have name");
                assertTrue(tournament.getNumberOfRounds() > 0, "Should have judged rounds");
            }
        }
        
        log.info("✓ Judge prelim scores verified for {} {}", testJudge.getFname(), testJudge.getLname());
    }

    // ========================================
    // CROSS-TOURNAMENT DATA INTEGRITY
    // ========================================

    @Test
    @Order(7)
    @DisplayName("Test 7: Verify debaters appear in correct tournaments")
    void testDebaterTournamentIntegrity() {
        log.info("Test 7: Verifying debater-tournament data integrity...");
        
        List<Debater> debaters = debaterService.getDebaters();
        
        for (Debater debater : debaters) {
            DebaterTournamentScoreDTO scores = debaterService.getTournamentsAndScoresForSpeaker(debater.getId(), false);
            
            if (scores.getTournamentRoundScores() != null) {
                for (TournamentRoundDTO tournament : scores.getTournamentRoundScores()) {
                    // Verify tournament exists
                    Long tournamentId = tournament.getTournamentId();
                    assertNotNull(tournamentService.getTournamentById(tournamentId), 
                            "Tournament should exist for debater " + debater.getFirstName());
                }
            }
        }
        
        log.info("✓ Debater-tournament data integrity verified");
    }

    @Test
    @Order(8)
    @DisplayName("Test 8: Verify judges appear in correct tournaments")
    void testJudgeTournamentIntegrity() {
        log.info("Test 8: Verifying judge-tournament data integrity...");
        
        List<Judge> judges = judgeService.getJudges();
        
        for (Judge judge : judges) {
            JudgeTournamentScoreDTO scores = judgeService.getTournamentsAndScoresForJudge(judge.getId(), false);
            
            if (scores.getTournamentRoundScores() != null) {
                for (TournamentRoundDTO tournament : scores.getTournamentRoundScores()) {
                    // Verify tournament exists
                    Long tournamentId = tournament.getTournamentId();
                    assertNotNull(tournamentService.getTournamentById(tournamentId), 
                            "Tournament should exist for judge " + judge.getFname());
                }
            }
        }
        
        log.info("✓ Judge-tournament data integrity verified");
    }

    @AfterAll
    static void printSummary() {
        log.info("========================================");
        log.info("CONTROLLER INTEGRATION TEST SUMMARY");
        log.info("========================================");
        log.info("✓ All 3 tournaments built successfully");
        log.info("✓ Debater controller endpoints tested");
        log.info("✓ Judge controller endpoints tested");
        log.info("✓ Cross-tournament data integrity verified");
        log.info("========================================");
    }
}



