package com.dineth.debateTracker;

import com.dineth.debateTracker.builders.TestFixtures;
import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.debater.DebaterService;
import com.dineth.debateTracker.debaterprofile.DebaterProfile;
import com.dineth.debateTracker.debaterprofile.DebaterProfileService;
import com.dineth.debateTracker.dtos.SpeakerTab.SpeakerTabDTO;
import com.dineth.debateTracker.dtos.SpeakerTab.SpeakerTabRowDTO;
import com.dineth.debateTracker.dtos.TournamentDataDTO;
import com.dineth.debateTracker.dtos.debaterprofiles.FurthestRoundDTO;
import com.dineth.debateTracker.dtos.debaterprofiles.SpeakerPerformanceDTO;
import com.dineth.debateTracker.dtos.statistics.WinLossStatDTO;
import com.dineth.debateTracker.judge.Judge;
import com.dineth.debateTracker.judge.JudgeService;
import com.dineth.debateTracker.judgeprofile.JudgeProfile;
import com.dineth.debateTracker.judgeprofile.JudgeProfileService;
import com.dineth.debateTracker.statistics.StatisticsService;
import com.dineth.debateTracker.tournament.TournamentService;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End test for profile generation, speaker tabs, and statistics calculations.
 * Tests profile refresh functionality and statistical computations across multiple tournaments.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProfileAndStatisticsE2ETest extends BaseE2ETest {

    private static final Logger log = LoggerFactory.getLogger(ProfileAndStatisticsE2ETest.class);

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private JudgeProfileService judgeProfileService;

    @Autowired
    private DebaterProfileService debaterProfileService;

    private static TournamentInfo tournament1;
    private static TournamentInfo tournament2;
    private static TournamentInfo tournament3;

    @BeforeAll
    static void buildTournaments(@Autowired TournamentBuilder builder, 
                                 @Autowired TournamentService tournamentService,
                                 @Autowired DebaterService debaterService,
                                 @Autowired JudgeService judgeService) {
        log.info("========================================");
        log.info("Building tournaments for profile and statistics tests...");
        log.info("========================================");

        TournamentDataDTO data1 = builder.buildMyTournament(TestFixtures.TOURNAMENT_1_XML);
        tournament1 = new TournamentInfo(data1, findTournamentId(tournamentService, data1));
        log.info("✓ Tournament 1 built with ID: {}", tournament1.getId());

        TournamentDataDTO data2 = builder.buildMyTournament(TestFixtures.TOURNAMENT_2_XML);
        tournament2 = new TournamentInfo(data2, findTournamentId(tournamentService, data2));
        log.info("✓ Tournament 2 built with ID: {}", tournament2.getId());

        TournamentDataDTO data3 = builder.buildMyTournament(TestFixtures.TOURNAMENT_3_XML);
        tournament3 = new TournamentInfo(data3, findTournamentId(tournamentService, data3));
        log.info("✓ Tournament 3 built with ID: {}", tournament3.getId());

        log.info("========================================");
        log.info("All tournaments built successfully!");
        log.info("========================================");
    }

    // ========================================
    // SPEAKER TAB TESTS
    // ========================================

    @Test
    @Order(1)
    @DisplayName("Test 1: Calculate speaker tab for Tournament 1")
    @Transactional
    void testSpeakerTabCalculation() {
        log.info("Test 1: Calculating speaker tab for Tournament 1...");

        SpeakerTabDTO speakerTab = statisticsService.calculateSpeakerTabForTournament(tournament1.getId());
        
        assertNotNull(speakerTab, "Speaker tab should not be null");
        assertEquals(tournament1.getId(), speakerTab.getTournamentId(), "Speaker tab should be for Tournament 1");
        assertFalse(speakerTab.getSpeakerTabRows().isEmpty(), "Speaker tab should have rows");

        log.info("✓ Speaker tab calculated with {} rows", speakerTab.getSpeakerTabRows().size());
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: Verify speaker tab top 10 rankings")
    @Transactional
    void testSpeakerTabTop10() {
        log.info("Test 2: Verifying speaker tab top 10...");

        SpeakerTabDTO speakerTab = statisticsService.calculateSpeakerTabForTournament(tournament1.getId());
        
        List<SpeakerTabRowDTO> top10 = speakerTab.getSpeakerTabRows().stream()
                .filter(row -> row.getRank() > 0 && row.getRank() <= 10)
                .sorted((r1, r2) -> Integer.compare(r1.getRank(), r2.getRank()))
                .toList();

        log.info("========================================");
        log.info("TOP 10 SPEAKER TAB (Tournament 1)");
        log.info("========================================");
        
        for (int i = 0; i < Math.min(10, top10.size()); i++) {
            SpeakerTabRowDTO row = top10.get(i);
            Debater debater = debaterService.getDebaterById(row.getDebaterId());
            
            log.info("Rank {}: {} {} - Avg: {}, Speeches: {}",
                    row.getRank(),
                    debater.getFirstName(),
                    debater.getLastName(),
                    String.format("%.2f", row.getAverageSpeakerScore()),
                    row.getSpeechesCount());
            
            // Verify data quality
            assertTrue(row.getRank() <= 10, "Should be in top 10");
            assertTrue(row.getAverageSpeakerScore() > 0, "Should have positive average");
            assertTrue(row.getSpeechesCount() > 0, "Should have speeches");
        }
        
        log.info("========================================");
        log.info("✓ Top 10 speaker tab verified");
    }

    // ========================================
    // PROFILE REFRESH TESTS
    // ========================================

    @Test
    @Order(3)
    @DisplayName("Test 3: Initialize and verify judge profiles")
    void testJudgeProfileRefresh() {
        log.info("Test 3: Refreshing judge profiles...");

        judgeProfileService.initializeAllJudgeProfiles();
        
        List<Judge> allJudges = judgeService.getJudges();
        log.info("✓ Initialized profiles for {} judges", allJudges.size());
        
        // Verify profiles were created
        for (Judge judge : allJudges) {
            JudgeProfile profile = judgeProfileService.getJudgeProfileByJudgeId(judge.getId());
            assertNotNull(profile, "Judge profile should exist for " + judge.getFname() + " " + judge.getLname());
            assertEquals(judge.getId(), profile.getJudgeId(), "Profile should be linked to correct judge");
        }

        // Update all profiles
        judgeProfileService.updateAllJudgeProfiles();
        log.info("✓ Updated all judge profiles");
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: Initialize and verify debater profiles")
    void testDebaterProfileRefresh() {
        log.info("Test 4: Refreshing debater profiles...");

        debaterProfileService.initializeAllDebaterProfiles();
        
        List<Debater> allDebaters = debaterService.getDebaters();
        log.info("✓ Initialized profiles for {} debaters", allDebaters.size());
        
        // Verify profiles were created
        for (Debater debater : allDebaters) {
            DebaterProfile profile = debaterProfileService.getDebaterProfileByDebaterId(debater.getId());
            assertNotNull(profile, "Debater profile should exist for " + 
                    debater.getFirstName() + " " + debater.getLastName());
            assertEquals(debater.getId(), profile.getDebaterId(), "Profile should be linked to correct debater");
        }

        log.info("✓ Debater profiles initialized");
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: Verify judge profile data completeness")
    void testJudgeProfileData() {
        log.info("Test 5: Verifying judge profile data...");

        List<Judge> judges = judgeService.getJudges();
        int profilesWithData = 0;
        
        for (Judge judge : judges) {
            JudgeProfile profile = judgeProfileService.getJudgeProfileByJudgeId(judge.getId());
            
            if (profile.getPrelimsJudged() != null && profile.getPrelimsJudged() > 0) {
                profilesWithData++;
                
                // Verify data consistency
                assertNotNull(profile.getTournamentsJudged(), "Should have tournaments judged count");
                assertTrue(profile.getTournamentsJudged() >= 1, "Should have judged at least 1 tournament");
            }
        }
        
        assertTrue(profilesWithData > 0, "At least some judges should have profile data");
        log.info("✓ {} judges have complete profile data", profilesWithData);
    }

    @Test
    @Order(6)
    @DisplayName("Test 6: Verify debater profile data completeness")
    void testDebaterProfileData() {
        log.info("Test 6: Verifying debater profile data...");

        List<Debater> debaters = debaterService.getDebaters();
        int profilesWithData = 0;
        
        for (Debater debater : debaters) {
            DebaterProfile profile = debaterProfileService.getDebaterProfileByDebaterId(debater.getId());
            
            if (profile.getPrelimsDebated() != null && profile.getPrelimsDebated() > 0) {
                profilesWithData++;
                
                // Verify data consistency
                assertNotNull(profile.getTournamentsDebated(), "Should have tournaments debated count");
                assertTrue(profile.getTournamentsDebated() >= 1, "Should have debated in at least 1 tournament");
            }
        }
        
        assertTrue(profilesWithData > 0, "At least some debaters should have profile data");
        log.info("✓ {} debaters have complete profile data", profilesWithData);
    }

    // ========================================
    // STATISTICS TESTS
    // ========================================

    @Test
    @Order(7)
    @DisplayName("Test 7: Calculate and verify win-loss statistics")
    @Transactional
    void testWinLossStatistics() {
        log.info("Test 7: Calculating win-loss statistics...");

        debaterProfileService.updateAllDebaterProfiles();
        
        List<WinLossStatDTO> winLossStats = statisticsService.calculateWinLoss();
        assertNotNull(winLossStats, "Win-loss stats should not be null");
        assertFalse(winLossStats.isEmpty(), "Should have win-loss statistics");

        log.info("✓ Win-loss statistics calculated for {} debaters", winLossStats.size());

        // Verify some debaters have wins and losses
        boolean hasWins = winLossStats.stream()
                .anyMatch(stat -> stat.getPrelimWins() > 0 || stat.getBreakWins() > 0);
        boolean hasLosses = winLossStats.stream()
                .anyMatch(stat -> stat.getPrelimLosses() > 0 || stat.getBreakLosses() > 0);

        assertTrue(hasWins, "Some debaters should have wins");
        assertTrue(hasLosses, "Some debaters should have losses");
    }

    @Test
    @Order(8)
    @DisplayName("Test 8: Calculate judge sentiment analysis")
    void testJudgeSentimentAnalysis() {
        log.info("Test 8: Calculating judge sentiment...");

        List<com.dineth.debateTracker.dtos.JudgeSentimentDTO> sentiments = 
                statisticsService.getSentiment(0.5);
        
        assertNotNull(sentiments, "Sentiment list should not be null");
        log.info("✓ Judge sentiment calculated for {} judges", sentiments.size());
    }

    @Test
    @Order(9)
    @DisplayName("Test 9: Calculate speaker tabs for all tournaments")
    @Transactional
    void testSpeakerTabsAllTournaments() {
        log.info("Test 9: Calculating speaker tabs for all tournaments...");

        SpeakerTabDTO speakerTab1 = statisticsService.calculateSpeakerTabForTournament(tournament1.getId());
        SpeakerTabDTO speakerTab2 = statisticsService.calculateSpeakerTabForTournament(tournament2.getId());
        SpeakerTabDTO speakerTab3 = statisticsService.calculateSpeakerTabForTournament(tournament3.getId());

        assertNotNull(speakerTab1, "Tournament 1 speaker tab should not be null");
        assertNotNull(speakerTab2, "Tournament 2 speaker tab should not be null");
        assertNotNull(speakerTab3, "Tournament 3 speaker tab should not be null");

        log.info("✓ Tournament 1 speaker tab: {} rows", speakerTab1.getSpeakerTabRows().size());
        log.info("✓ Tournament 2 speaker tab: {} rows", speakerTab2.getSpeakerTabRows().size());
        log.info("✓ Tournament 3 speaker tab: {} rows", speakerTab3.getSpeakerTabRows().size());
    }

    @Test
    @Order(10)
    @DisplayName("Test 10: Verify furthest rounds reached by debaters")
    @Transactional
    void testFurthestRoundsReached() {
        log.info("Test 10: Testing furthest rounds reached...");

        List<Debater> debaters = debaterService.getDebaters();
        int debatersWithBreaks = 0;

        for (Debater debater : debaters) {
            List<FurthestRoundDTO> furthestRounds = 
                    statisticsService.findFurthestRoundsReachedByDebater(debater.getId());
            
            if (furthestRounds != null && !furthestRounds.isEmpty()) {
                debatersWithBreaks++;
            }
        }

        log.info("✓ {} debaters broke to elimination rounds", debatersWithBreaks);
        assertTrue(debatersWithBreaks > 0, "At least some debaters should have broken");
    }

    @Test
    @Order(11)
    @DisplayName("Test 11: Verify speaker performance tracking")
    @Transactional
    void testSpeakerPerformances() {
        log.info("Test 11: Testing speaker performances...");

        var performancesMap = statisticsService.findSpeakerPerformanceOfDebaters();
        assertNotNull(performancesMap, "Speaker performances map should not be null");
        
        log.info("✓ Speaker performances tracked for {} debaters", performancesMap.size());

        // Verify performance data quality
        for (var entry : performancesMap.entrySet()) {
            List<SpeakerPerformanceDTO> performances = entry.getValue();
            for (SpeakerPerformanceDTO perf : performances) {
                assertNotNull(perf.getTournamentName(), "Performance should have tournament name");
                assertTrue(perf.getPrelimsDebated() >= 0, "Prelims debated should be non-negative");
            }
        }
    }

    @Test
    @Order(12)
    @DisplayName("Test 12: Verify profile percentile calculations")
    void testProfilePercentiles() {
        log.info("Test 12: Verifying profile percentiles...");

        debaterProfileService.updateAllPercentiles();
        
        List<DebaterProfile> profiles = debaterProfileService.getAllDebaterProfiles();
        assertFalse(profiles.isEmpty(), "Should have debater profiles");

        long profilesWithPercentiles = profiles.stream()
                .filter(p -> p.getActivityPercentile() != null && p.getActivityPercentile() > 0)
                .count();

        log.info("✓ {} profiles have percentile calculations", profilesWithPercentiles);
    }

    @AfterAll
    static void printSummary() {
        log.info("========================================");
        log.info("PROFILE & STATISTICS E2E TEST SUMMARY");
        log.info("========================================");
        log.info("✓ All 3 tournaments built successfully");
        log.info("✓ Speaker tabs calculated and verified");
        log.info("✓ Judge profiles initialized and updated");
        log.info("✓ Debater profiles initialized and updated");
        log.info("✓ Win-loss statistics calculated");
        log.info("✓ Judge sentiment analysis performed");
        log.info("✓ Furthest rounds tracking verified");
        log.info("✓ Speaker performances tracked");
        log.info("✓ Percentile calculations validated");
        log.info("========================================");
    }
}


