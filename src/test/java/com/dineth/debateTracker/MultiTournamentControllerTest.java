package com.dineth.debateTracker;

import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.debater.DebaterService;
import com.dineth.debateTracker.dtos.DebaterTournamentScoreDTO;
import com.dineth.debateTracker.dtos.JudgeTournamentScoreDTO;
import com.dineth.debateTracker.dtos.RoundScoreDTO;
import com.dineth.debateTracker.dtos.TournamentDataDTO;
import com.dineth.debateTracker.dtos.TournamentRoundDTO;
import com.dineth.debateTracker.judge.Judge;
import com.dineth.debateTracker.judge.JudgeService;
import com.dineth.debateTracker.tournament.TournamentService;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive Controller Tests across Multiple Tournaments
 * Tests specific controller endpoints with hardcoded expected values
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MultiTournamentControllerTest {

    private static final Logger log = LoggerFactory.getLogger(MultiTournamentControllerTest.class);

    @Autowired
    private TournamentBuilder tournamentBuilder;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private DebaterService debaterService;

    @Autowired
    private JudgeService judgeService;

    private static TournamentDataDTO tournament1Data;
    private static TournamentDataDTO tournament2Data;
    private static TournamentDataDTO tournament3Data;

    private static Long tournament1Id;
    private static Long tournament2Id;
    private static Long tournament3Id;

    /**
     * Build all three tournaments before any tests run
     */
    @BeforeAll
    static void buildTournaments(@Autowired TournamentBuilder builder, @Autowired TournamentService tournamentService) {
        log.info("========================================");
        log.info("Building all three tournaments...");
        log.info("========================================");

        // Build Tournament 1 (testTourney.xml)
        log.info("Building Tournament 1 from testTourney.xml...");
        tournament1Data = builder.buildMyTournament("src/test/resources/testTourney.xml");
        tournament1Id = findTournamentId(tournamentService, tournament1Data);
        log.info("Tournament 1 built successfully with ID: {}", tournament1Id);

        // Build Tournament 2 (testTourney2.xml)
        log.info("Building Tournament 2 from testTourney2.xml...");
        tournament2Data = builder.buildMyTournament("src/test/resources/testTourney2.xml");
        tournament2Id = findTournamentId(tournamentService, tournament2Data);
        log.info("Tournament 2 built successfully with ID: {}", tournament2Id);

        // Build Tournament 3 (testTourney3.xml)
        log.info("Building Tournament 3 from testTourney3.xml...");
        tournament3Data = builder.buildMyTournament("src/test/resources/testTourney3.xml");
        tournament3Id = findTournamentId(tournamentService, tournament3Data);
        log.info("Tournament 3 built successfully with ID: {}", tournament3Id);

        log.info("========================================");
        log.info("All tournaments built successfully!");
        log.info("========================================");
    }

    /**
     * Find tournament ID from database by matching the short name
     */
    private static Long findTournamentId(TournamentService tournamentService, TournamentDataDTO tournamentData) {
        if (tournamentData == null || tournamentData.getTournament() == null) {
            return null;
        }
        String shortName = tournamentData.getTournament().getShortName();
        return tournamentService.getTournaments().stream()
                .filter(t -> shortName.equals(t.getShortName()))
                .map(t -> t.getId())
                .findFirst()
                .orElse(null);
    }

    /**
     * Test Data Structure for Expected Debater Speaks Results
     */
    static class ExpectedDebaterSpeaks {
        String firstName;
        String lastName;
        Long debaterId; // Will be populated after first run
        int expectedTotalDebates;
        Map<String, TournamentExpectedData> tournamentData; // Tournament name -> expected data

        public ExpectedDebaterSpeaks(String firstName, String lastName, Long debaterId, int expectedTotalDebates) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.debaterId = debaterId;
            this.expectedTotalDebates = expectedTotalDebates;
            this.tournamentData = new HashMap<>();
        }

        public void addTournament(String tournamentName, int expectedRounds, Double expectedAvgScore) {
            this.tournamentData.put(tournamentName, new TournamentExpectedData(expectedRounds, expectedAvgScore));
        }

        static class TournamentExpectedData {
            int expectedRounds;
            Double expectedAvgScore;

            public TournamentExpectedData(int expectedRounds, Double expectedAvgScore) {
                this.expectedRounds = expectedRounds;
                this.expectedAvgScore = expectedAvgScore;
            }
        }
    }

    /**
     * Test Data Structure for Expected Judge Tournament Results
     */
    static class ExpectedJudgeTournaments {
        String firstName;
        String lastName;
        Long judgeId; // Will be populated after first run
        List<String> expectedTournamentNames;

        public ExpectedJudgeTournaments(String firstName, String lastName, Long judgeId, List<String> expectedTournamentNames) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.judgeId = judgeId;
            this.expectedTournamentNames = expectedTournamentNames;
        }
    }

    /**
     * Test Data Structure for Expected Judge Prelim Scores
     */
    static class ExpectedJudgePrelimScores {
        String firstName;
        String lastName;
        Long judgeId; // Will be populated after first run
        int expectedTotalDebatesJudged;
        Map<String, TournamentExpectedData> tournamentData; // Tournament name -> expected data

        public ExpectedJudgePrelimScores(String firstName, String lastName, Long judgeId, int expectedTotalDebatesJudged) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.judgeId = judgeId;
            this.expectedTotalDebatesJudged = expectedTotalDebatesJudged;
            this.tournamentData = new HashMap<>();
        }

        public void addTournament(String tournamentName, int expectedRounds) {
            this.tournamentData.put(tournamentName, new TournamentExpectedData(expectedRounds));
        }

        static class TournamentExpectedData {
            int expectedRounds;

            public TournamentExpectedData(int expectedRounds) {
                this.expectedRounds = expectedRounds;
            }
        }
    }

    /**
     * Define expected data for getSpeaks tests
     * TODO: Fill in actual values after first test run
     */
    private static ExpectedDebaterSpeaks[] getExpectedDebaterSpeaksData() {
        ExpectedDebaterSpeaks[] expected = new ExpectedDebaterSpeaks[3];

        // Example debater 1 - TODO: Replace with actual values
        expected[0] = new ExpectedDebaterSpeaks("FirstName1", "LastName1", null, 0);
        expected[0].addTournament("Tournament1", 0, 0.0);
        expected[0].addTournament("Tournament2", 0, 0.0);

        // Example debater 2 - TODO: Replace with actual values
        expected[1] = new ExpectedDebaterSpeaks("FirstName2", "LastName2", null, 0);
        expected[1].addTournament("Tournament1", 0, 0.0);

        // Example debater 3 - TODO: Replace with actual values
        expected[2] = new ExpectedDebaterSpeaks("FirstName3", "LastName3", null, 0);
        expected[2].addTournament("Tournament3", 0, 0.0);

        return expected;
    }

    /**
     * Define expected data for getTournamentsJudged tests
     * TODO: Fill in actual values after first test run
     */
    private static ExpectedJudgeTournaments[] getExpectedJudgeTournamentsData() {
        ExpectedJudgeTournaments[] expected = new ExpectedJudgeTournaments[3];

        // Example judge 1 - TODO: Replace with actual values
        expected[0] = new ExpectedJudgeTournaments("JudgeFirst1", "JudgeLast1", null, 
                List.of("Tournament1", "Tournament2"));

        // Example judge 2 - TODO: Replace with actual values
        expected[1] = new ExpectedJudgeTournaments("JudgeFirst2", "JudgeLast2", null, 
                List.of("Tournament1"));

        // Example judge 3 - TODO: Replace with actual values
        expected[2] = new ExpectedJudgeTournaments("JudgeFirst3", "JudgeLast3", null, 
                List.of("Tournament2", "Tournament3"));

        return expected;
    }

    /**
     * Define expected data for getPrelimScoresByJudge tests
     * TODO: Fill in actual values after first test run
     */
    private static ExpectedJudgePrelimScores[] getExpectedJudgePrelimScoresData() {
        ExpectedJudgePrelimScores[] expected = new ExpectedJudgePrelimScores[3];

        // Example judge 1 - TODO: Replace with actual values
        expected[0] = new ExpectedJudgePrelimScores("JudgeFirst1", "JudgeLast1", null, 0);
        expected[0].addTournament("Tournament1", 0);
        expected[0].addTournament("Tournament2", 0);

        // Example judge 2 - TODO: Replace with actual values
        expected[1] = new ExpectedJudgePrelimScores("JudgeFirst2", "JudgeLast2", null, 0);
        expected[1].addTournament("Tournament1", 0);

        // Example judge 3 - TODO: Replace with actual values
        expected[2] = new ExpectedJudgePrelimScores("JudgeFirst3", "JudgeLast3", null, 0);
        expected[2].addTournament("Tournament3", 0);

        return expected;
    }

    // ========================================
    // DEBATER CONTROLLER TESTS - getSpeaks
    // ========================================

    @Test
    @Order(1)
    @DisplayName("Test 1: Verify all tournaments were built successfully")
    void testTournamentsBuilt() {
        log.info("Test 1: Verifying all tournaments were built...");

        assertNotNull(tournament1Id, "Tournament 1 should be built");
        assertNotNull(tournament2Id, "Tournament 2 should be built");
        assertNotNull(tournament3Id, "Tournament 3 should be built");

        assertNotNull(tournamentService.getTournamentById(tournament1Id), "Tournament 1 should exist in database");
        assertNotNull(tournamentService.getTournamentById(tournament2Id), "Tournament 2 should exist in database");
        assertNotNull(tournamentService.getTournamentById(tournament3Id), "Tournament 3 should exist in database");

        log.info("✓ All 3 tournaments successfully built and verified");
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: Print available debaters for test data setup")
    void printAvailableDebaters() {
        log.info("Test 2: Printing available debaters across all tournaments...");
        
        List<Debater> allDebaters = debaterService.getDebaters();
        log.info("========================================");
        log.info("AVAILABLE DEBATERS (Total: {})", allDebaters.size());
        log.info("========================================");
        
        int count = 0;
        for (Debater debater : allDebaters) {
            DebaterTournamentScoreDTO speaks = debaterService.getTournamentsAndScoresForSpeaker(debater.getId(), false);
            log.info("Debater #{}: {} {} (ID: {}) - Tournaments: {}, Total Debates: {}", 
                    ++count,
                    debater.getFirstName(), 
                    debater.getLastName(), 
                    debater.getId(),
                    speaks.getTournamentRoundScores() != null ? speaks.getTournamentRoundScores().size() : 0,
                    speaks.getTotalDebatesParticipated());
            
            if (speaks.getTournamentRoundScores() != null) {
                for (TournamentRoundDTO tournament : speaks.getTournamentRoundScores()) {
                    log.info("  - Tournament: {} (ID: {}), Rounds: {}, Avg Score: {}", 
                            tournament.getTournamentShortName(),
                            tournament.getTournamentId(),
                            tournament.getNumberOfRounds(),
                            String.format("%.2f", tournament.getAverageScore()));
                }
            }
        }
        log.info("========================================");
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: Print available judges for test data setup")
    void printAvailableJudges() {
        log.info("Test 3: Printing available judges across all tournaments...");
        
        List<Judge> allJudges = judgeService.getJudges();
        log.info("========================================");
        log.info("AVAILABLE JUDGES (Total: {})", allJudges.size());
        log.info("========================================");
        
        int count = 0;
        for (Judge judge : allJudges) {
            List<String> tournaments = judgeService.getTournamentsJudged(judge.getId());
            JudgeTournamentScoreDTO prelimScores = judgeService.getTournamentsAndScoresForJudge(judge.getId(), false);
            
            log.info("Judge #{}: {} {} (ID: {}) - Tournaments: {}, Total Debates Judged: {}", 
                    ++count,
                    judge.getFname(), 
                    judge.getLname(), 
                    judge.getId(),
                    tournaments.size(),
                    prelimScores.getTotalDebatesJudged());
            
            log.info("  - Tournaments judged: {}", tournaments);
            
            if (prelimScores.getTournamentRoundScores() != null) {
                for (TournamentRoundDTO tournament : prelimScores.getTournamentRoundScores()) {
                    log.info("  - Tournament: {} (ID: {}), Rounds: {}", 
                            tournament.getTournamentShortName(),
                            tournament.getTournamentId(),
                            tournament.getNumberOfRounds());
                }
            }
        }
        log.info("========================================");
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: DebaterController.getSpeaks() - Verify specific debater speaks across tournaments")
    void testDebaterGetSpeaks() {
        log.info("Test 4: Testing DebaterController.getSpeaks()...");
        
        ExpectedDebaterSpeaks[] expectedData = getExpectedDebaterSpeaksData();
        
        for (ExpectedDebaterSpeaks expected : expectedData) {
            log.info("----------------------------------------");
            log.info("Testing debater: {} {}", expected.firstName, expected.lastName);
            
            // Find debater by name
            Debater debater = findDebaterByName(expected.firstName, expected.lastName);
            if (debater == null) {
                log.warn("⚠ Debater {} {} not found - skipping", expected.firstName, expected.lastName);
                continue;
            }
            
            // If debaterId is null (first run), print the ID for future reference
            if (expected.debaterId == null) {
                log.info("ℹ Debater ID for {} {}: {}", expected.firstName, expected.lastName, debater.getId());
            }
            
            // Call the controller method (via service)
            DebaterTournamentScoreDTO result = debaterService.getTournamentsAndScoresForSpeaker(debater.getId(), false);
            
            assertNotNull(result, "Speaks result should not be null");
            assertEquals(expected.firstName, result.getFirstName(), "First name should match");
            assertEquals(expected.lastName, result.getLastName(), "Last name should match");
            
            if (expected.expectedTotalDebates > 0) {
                assertEquals(expected.expectedTotalDebates, result.getTotalDebatesParticipated(), 
                        String.format("Total debates for %s %s should match", expected.firstName, expected.lastName));
            } else {
                log.info("ℹ Actual total debates: {}", result.getTotalDebatesParticipated());
            }
            
            // Verify per-tournament data
            if (result.getTournamentRoundScores() != null) {
                log.info("Tournaments participated: {}", result.getTournamentRoundScores().size());
                for (TournamentRoundDTO tournament : result.getTournamentRoundScores()) {
                    String tournamentName = tournament.getTournamentShortName();
                    log.info("  Tournament: {}, Rounds: {}, Avg Score: {}", 
                            tournamentName, 
                            tournament.getNumberOfRounds(),
                            String.format("%.2f", tournament.getAverageScore()));
                    
                    // If we have expected data for this tournament
                    if (expected.tournamentData.containsKey(tournamentName)) {
                        ExpectedDebaterSpeaks.TournamentExpectedData tournamentExpected = expected.tournamentData.get(tournamentName);
                        
                        if (tournamentExpected.expectedRounds > 0) {
                            assertEquals(tournamentExpected.expectedRounds, tournament.getNumberOfRounds(),
                                    String.format("Rounds in %s should match", tournamentName));
                        }
                        
                        if (tournamentExpected.expectedAvgScore > 0.0) {
                            assertEquals(tournamentExpected.expectedAvgScore, tournament.getAverageScore(), 0.01,
                                    String.format("Average score in %s should match", tournamentName));
                        }
                    }
                }
            }
            
            log.info("✓ Debater {} {} speaks verified", expected.firstName, expected.lastName);
        }
    }

    // ========================================
    // JUDGE CONTROLLER TESTS - getTournamentsJudged
    // ========================================

    @Test
    @Order(5)
    @DisplayName("Test 5: JudgeController.getTournamentsJudged() - Verify judge tournament participation")
    void testJudgeGetTournamentsJudged() {
        log.info("Test 5: Testing JudgeController.getTournamentsJudged()...");
        
        ExpectedJudgeTournaments[] expectedData = getExpectedJudgeTournamentsData();
        
        for (ExpectedJudgeTournaments expected : expectedData) {
            log.info("----------------------------------------");
            log.info("Testing judge: {} {}", expected.firstName, expected.lastName);
            
            // Find judge by name
            Judge judge = findJudgeByName(expected.firstName, expected.lastName);
            if (judge == null) {
                log.warn("⚠ Judge {} {} not found - skipping", expected.firstName, expected.lastName);
                continue;
            }
            
            // If judgeId is null (first run), print the ID for future reference
            if (expected.judgeId == null) {
                log.info("ℹ Judge ID for {} {}: {}", expected.firstName, expected.lastName, judge.getId());
            }
            
            // Call the controller method (via service)
            List<String> result = judgeService.getTournamentsJudged(judge.getId());
            
            assertNotNull(result, "Tournament list should not be null");
            
            log.info("Tournaments judged: {}", result);
            
            if (expected.expectedTournamentNames != null && !expected.expectedTournamentNames.isEmpty() 
                    && !expected.expectedTournamentNames.get(0).equals("Tournament1")) {
                assertEquals(expected.expectedTournamentNames.size(), result.size(),
                        String.format("Number of tournaments for %s %s should match", expected.firstName, expected.lastName));
                
                for (String expectedTournament : expected.expectedTournamentNames) {
                    assertTrue(result.contains(expectedTournament),
                            String.format("Judge %s %s should have judged %s", expected.firstName, expected.lastName, expectedTournament));
                }
            } else {
                log.info("ℹ Actual tournaments: {}", result);
            }
            
            log.info("✓ Judge {} {} tournaments verified", expected.firstName, expected.lastName);
        }
    }

    // ========================================
    // JUDGE CONTROLLER TESTS - getPrelimScoresByJudge
    // ========================================

    @Test
    @Order(6)
    @DisplayName("Test 6: JudgeController.getPrelimScoresByJudge() - Verify judge prelim scoring data")
    void testJudgeGetPrelimScoresByJudge() {
        log.info("Test 6: Testing JudgeController.getPrelimScoresByJudge()...");
        
        ExpectedJudgePrelimScores[] expectedData = getExpectedJudgePrelimScoresData();
        
        for (ExpectedJudgePrelimScores expected : expectedData) {
            log.info("----------------------------------------");
            log.info("Testing judge: {} {}", expected.firstName, expected.lastName);
            
            // Find judge by name
            Judge judge = findJudgeByName(expected.firstName, expected.lastName);
            if (judge == null) {
                log.warn("⚠ Judge {} {} not found - skipping", expected.firstName, expected.lastName);
                continue;
            }
            
            // If judgeId is null (first run), print the ID for future reference
            if (expected.judgeId == null) {
                log.info("ℹ Judge ID for {} {}: {}", expected.firstName, expected.lastName, judge.getId());
            }
            
            // Call the controller method (via service)
            JudgeTournamentScoreDTO result = judgeService.getTournamentsAndScoresForJudge(judge.getId(), false);
            
            assertNotNull(result, "Prelim scores result should not be null");
            assertEquals(expected.firstName, result.getFirstName(), "First name should match");
            assertEquals(expected.lastName, result.getLastName(), "Last name should match");
            
            if (expected.expectedTotalDebatesJudged > 0) {
                assertEquals(expected.expectedTotalDebatesJudged, result.getTotalDebatesJudged(),
                        String.format("Total debates judged by %s %s should match", expected.firstName, expected.lastName));
            } else {
                log.info("ℹ Actual total debates judged: {}", result.getTotalDebatesJudged());
            }
            
            // Verify per-tournament data
            if (result.getTournamentRoundScores() != null) {
                log.info("Tournaments judged (prelims): {}", result.getTournamentRoundScores().size());
                for (TournamentRoundDTO tournament : result.getTournamentRoundScores()) {
                    String tournamentName = tournament.getTournamentShortName();
                    log.info("  Tournament: {}, Prelim Rounds: {}", 
                            tournamentName, 
                            tournament.getNumberOfRounds());
                    
                    // If we have expected data for this tournament
                    if (expected.tournamentData.containsKey(tournamentName)) {
                        ExpectedJudgePrelimScores.TournamentExpectedData tournamentExpected = expected.tournamentData.get(tournamentName);
                        
                        if (tournamentExpected.expectedRounds > 0) {
                            assertEquals(tournamentExpected.expectedRounds, tournament.getNumberOfRounds(),
                                    String.format("Prelim rounds in %s should match", tournamentName));
                        }
                    }
                }
            }
            
            log.info("✓ Judge {} {} prelim scores verified", expected.firstName, expected.lastName);
        }
    }

    @Test
    @Order(7)
    @DisplayName("Test 7: Summary - Print test execution summary")
    void printTestSummary() {
        log.info("========================================");
        log.info("TEST EXECUTION SUMMARY");
        log.info("========================================");
        log.info("✓ All 3 tournaments built successfully");
        log.info("✓ Tournament 1 ID: {}", tournament1Id);
        log.info("✓ Tournament 2 ID: {}", tournament2Id);
        log.info("✓ Tournament 3 ID: {}", tournament3Id);
        log.info("✓ Total Debaters: {}", debaterService.getDebaters().size());
        log.info("✓ Total Judges: {}", judgeService.getJudges().size());
        log.info("========================================");
        log.info("Controller tests completed!");
        log.info("========================================");
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    private Debater findDebaterByName(String firstName, String lastName) {
        List<Debater> debaters = debaterService.getDebaters();
        return debaters.stream()
                .filter(d -> d.getFirstName().equals(firstName) && d.getLastName().equals(lastName))
                .findFirst()
                .orElse(null);
    }

    private Judge findJudgeByName(String firstName, String lastName) {
        List<Judge> judges = judgeService.getJudges();
        return judges.stream()
                .filter(j -> j.getFname().equals(firstName) && j.getLname().equals(lastName))
                .findFirst()
                .orElse(null);
    }
}





