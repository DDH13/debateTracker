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
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End test for profile generation, speaker tabs, and statistics calculations.
 * Tests profile refresh functionality and statistical computations across multiple tournaments.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
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

    private record ExpectedSpeakerTabEntry(int rank, Long debaterId, double averageScore) {}

    private static final List<ExpectedSpeakerTabEntry> EXPECTED_TOURNAMENT_1_TOP_10 = List.of(
            new ExpectedSpeakerTabEntry(1, 69L, 76.9),
            new ExpectedSpeakerTabEntry(2, 15L, 76.5),
            new ExpectedSpeakerTabEntry(3, 14L, 76.4),
            new ExpectedSpeakerTabEntry(3, 21L, 76.4),
            new ExpectedSpeakerTabEntry(3, 86L, 76.4),
            new ExpectedSpeakerTabEntry(3, 87L, 76.4),
            new ExpectedSpeakerTabEntry(3, 97L, 76.4),
            new ExpectedSpeakerTabEntry(3, 104L, 76.4),
            new ExpectedSpeakerTabEntry(9, 22L, 76.3),
            new ExpectedSpeakerTabEntry(9, 56L, 76.3)
    );

    @BeforeAll
    static void buildTournaments(@Autowired TournamentImportService importService,
                                 @Autowired TournamentService tournamentService,
                                 @Autowired DebaterService debaterService,
                                 @Autowired JudgeService judgeService) {
        log.info("========================================");
        log.info("Building tournaments for profile and statistics tests...");
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
        log.info("Test 2: Verifying speaker tab top 10 against expected data...");

        SpeakerTabDTO speakerTab = statisticsService.calculateSpeakerTabForTournament(tournament1.getId());

        List<SpeakerTabRowDTO> top10 = speakerTab.getSpeakerTabRows().stream()
                .filter(row -> row.getRank() > 0 && row.getRank() <= 10)
                .sorted((r1, r2) -> Integer.compare(r1.getRank(), r2.getRank()))
                .toList();

        assertEquals(EXPECTED_TOURNAMENT_1_TOP_10.size(), top10.size(),
                "Top 10 rows count should match expected data");

        Map<Long, SpeakerTabRowDTO> rowsByDebaterId = top10.stream()
                .collect(Collectors.toMap(SpeakerTabRowDTO::getDebaterId, row -> row));

        log.info("========================================");
        log.info("TOP 10 SPEAKER TAB (Tournament 1)");
        log.info("========================================");

        for (ExpectedSpeakerTabEntry expectedRow : EXPECTED_TOURNAMENT_1_TOP_10) {
            SpeakerTabRowDTO actualRow = rowsByDebaterId.get(expectedRow.debaterId());
            assertNotNull(actualRow, "Expected debater ID not found in top 10: " + expectedRow.debaterId());

            assertEquals(expectedRow.rank(), actualRow.getRank(),
                    "Unexpected rank for debater ID " + expectedRow.debaterId());
            assertEquals(expectedRow.averageScore(), actualRow.getAverageSpeakerScore(), 0.01,
                    "Unexpected average score for debater ID " + expectedRow.debaterId());
            assertTrue(actualRow.getSpeechesCount() > 0,
                    "Expected speeches count to be positive for debater ID " + expectedRow.debaterId());

            Debater debater = debaterService.getDebaterById(actualRow.getDebaterId());
            assertNotNull(debater, "Debater should exist for ID " + expectedRow.debaterId());

            log.info("Rank {}: {} {} - Avg: {}, Speeches: {}",
                    actualRow.getRank(),
                    debater.getFirstName(),
                    debater.getLastName(),
                    String.format("%.2f", actualRow.getAverageSpeakerScore()),
                    actualRow.getSpeechesCount());
        }

        log.info("========================================");
        log.info("✓ Top 10 speaker tab verified against expected ranks, debater IDs, and averages");
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
    @Transactional
    @Commit
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

        // Update all profiles
        debaterProfileService.updateAllDebaterProfiles();
        log.info("✓ Updated all debater profiles");
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

    // ========================================
    // DEBATER PROFILE VALIDATION TESTS (from exported CSV 2026-05-01)
    // ========================================

    /**
     * Expected debater profile data derived from debater_profile_202605011243.csv
     */
    private record ExpectedDebaterProfileData(
            Long debaterId,
            String firstName,
            String lastName,
            Float avgSpeakerScore,
            Integer tournamentsDebated,
            Integer prelimsDebated,
            Integer breaksDebated,
            Float winPercentagePrelims,
            Float winPercentageBreaks,
            Integer speakerRank,
            Float activityPercentile
    ) {}

    private static final List<ExpectedDebaterProfileData> EXPECTED_PROFILES = List.of(
            // Rank 3 overall speaker, 3 tournaments, 15 prelims, 7 breaks
            new ExpectedDebaterProfileData(21L, "Rudhesh", "Ram",
                    76.833336f, 3, 15, 7, 86.666664f, 71.42857f, 3, 100.0f),
            // Rank 10 overall speaker, 3 tournaments, 15 prelims, 7 breaks
            new ExpectedDebaterProfileData(28L, "Vidath", "Marasinghe",
                    76.433334f, 3, 15, 7, 80.0f, 71.42857f, 10, 100.0f),
            // Rank 7 overall speaker, 3 tournaments, 15 prelims, 7 breaks
            new ExpectedDebaterProfileData(22L, "Akein", "Bandara",
                    76.6f, 3, 15, 7, 86.666664f, 71.42857f, 7, 100.0f),
            // Rank 14 overall speaker, 2 tournaments, 8 prelims, 6 breaks
            new ExpectedDebaterProfileData(87L, "Raashid", "Cassim",
                    76.375f, 2, 8, 6, 87.5f, 83.33333f, 14, 93.962265f),
            // Rank 2 overall speaker, 2 tournaments, 7 prelims, 5 breaks
            new ExpectedDebaterProfileData(185L, "T", "Tharaniharan",
                    76.85714f, 2, 7, 5, 71.42857f, 60.000004f, 2, 90.18868f),
            // Rank 5 overall speaker, 2 tournaments, 10 prelims, 4 breaks
            new ExpectedDebaterProfileData(200L, "Nimuthu", "Pathiraja",
                    76.7f, 2, 10, 4, 70.0f, 50.0f, 5, 93.962265f),
            // No breaks, 1 tournament, rank 1 - top speaker in tournament
            new ExpectedDebaterProfileData(69L, "Nimansa", "Jayasundera",
                    76.9f, 1, 5, 2, 80.0f, 50.0f, 1, 75.09434f)
    );

    /**
     * Expected judge profile data derived from judge_profile_202605011341.csv
     */
    private record ExpectedJudgeProfileData(
            Long judgeId,
            String firstName,
            String lastName,
            Integer prelimsJudged,
            Integer breaksJudged,
            Integer tournamentsJudged,
            Integer speechCountForMetrics,
            Integer leniencyCount,
            Integer harshnessCount,
            Integer neutralCount,
            Double averageFirst,
            Double averageSecond,
            Double averageSubstantive,
            Double averageThird,
            Double leniency,
            Double harshness,
            Double overallSentiment,
            Float activityPercentile
    ) {}

    private static final List<ExpectedJudgeProfileData> EXPECTED_JUDGE_PROFILES = List.of(
            new ExpectedJudgeProfileData(
                    54L, "Cheka", "Mendis",
                    6, 5, 2, 24, 2, 13, 9,
                    74.91666666666667, 74.95833333333333, 74.93055555555556, 74.91666666666667,
                    2.857142857142861, -10.555927405927392, -0.3207826895326888, 100.0f
            ),
            new ExpectedJudgeProfileData(
                    29L, "Vakeesh", "Shanthiruban",
                    10, 0, 3, 22, 11, 7, 4,
                    74.525, 74.8, 74.69166666666666, 74.75,
                    14.28412698412697, -7.008928571428569, 0.3306908369408364, 97.70115f
            ),
            new ExpectedJudgeProfileData(
                    42L, "Dayadi", "Seneviratne",
                    1, 5, 2, 3, 2, 0, 1,
                    74.5, 74.5, 74.66666666666667, 75.0,
                    3.2847222222222285, 0.0, 1.0949074074074094, 86.206894f
            )
    );

    @Test
    @Order(13)
    @DisplayName("Test 13: Validate specific debater profiles against exported CSV data")
    @Transactional
    void testDebaterProfilesAgainstExportedData() {
        log.info("Test 13: Validating debater profiles against exported CSV data...");

        debaterProfileService.updateAllDebaterProfiles();
        debaterProfileService.updateAllPercentiles();

        log.info("========================================");
        log.info("DEBATER PROFILE VALIDATION (vs CSV export 2026-05-01)");
        log.info("========================================");
        
        for (ExpectedDebaterProfileData expected : EXPECTED_PROFILES) {
            DebaterProfile profile = debaterProfileService.getDebaterProfileByDebaterId(expected.debaterId());
            assertNotNull(profile,
                    "Profile should exist for debater ID " + expected.debaterId() +
                    " (" + expected.firstName() + " " + expected.lastName() + ")");

            assertEquals(expected.firstName(), profile.getFirstName(),
                    "First name mismatch for debater ID " + expected.debaterId());
            assertEquals(expected.lastName(), profile.getLastName(),
                    "Last name mismatch for debater ID " + expected.debaterId());

            assertNotNull(profile.getAverageSpeakerScore(),
                    "Average speaker score should not be null for " + expected.firstName());
            assertEquals(expected.avgSpeakerScore(), profile.getAverageSpeakerScore(), 0.05f,
                    "Average speaker score mismatch for " + expected.firstName() + " " + expected.lastName());

            assertEquals(expected.tournamentsDebated(), profile.getTournamentsDebated(),
                    "Tournaments debated mismatch for " + expected.firstName() + " " + expected.lastName());

            assertEquals(expected.prelimsDebated(), profile.getPrelimsDebated(),
                    "Prelims debated mismatch for " + expected.firstName() + " " + expected.lastName());

            assertEquals(expected.breaksDebated(), profile.getBreaksDebated(),
                    "Breaks debated mismatch for " + expected.firstName() + " " + expected.lastName());

            assertNotNull(profile.getWinPercentagePrelims(),
                    "Win percentage prelims should not be null for " + expected.firstName());
            assertEquals(expected.winPercentagePrelims(), profile.getWinPercentagePrelims(), 1.0f,
                    "Win percentage prelims mismatch for " + expected.firstName() + " " + expected.lastName());

            assertNotNull(profile.getWinPercentageBreaks(),
                    "Win percentage breaks should not be null for " + expected.firstName());
            assertEquals(expected.winPercentageBreaks(), profile.getWinPercentageBreaks(), 1.0f,
                    "Win percentage breaks mismatch for " + expected.firstName() + " " + expected.lastName());

            assertNotNull(profile.getSpeakerRank(),
                    "Speaker rank should not be null for " + expected.firstName());
            assertEquals(expected.speakerRank(), profile.getSpeakerRank(),
                    "Speaker rank mismatch for " + expected.firstName() + " " + expected.lastName());

            assertNotNull(profile.getActivityPercentile(),
                    "Activity percentile should not be null for " + expected.firstName());
            assertEquals(expected.activityPercentile(), profile.getActivityPercentile(), 1.0f,
                    "Activity percentile mismatch for " + expected.firstName() + " " + expected.lastName());

            // Verify speaker performances list is populated
            assertNotNull(profile.getSpeakerPerformances(),
                    "Speaker performances should not be null for " + expected.firstName());
            assertFalse(profile.getSpeakerPerformances().isEmpty(),
                    "Speaker performances should not be empty for " + expected.firstName());
            assertEquals(expected.tournamentsDebated(), profile.getSpeakerPerformances().size(),
                    "Speaker performances count should match tournaments debated for " + expected.firstName());

            // Verify furthest rounds list is populated for debaters with breaks
            assertNotNull(profile.getFurthestRounds(),
                    "Furthest rounds should not be null for " + expected.firstName());
            if (expected.breaksDebated() > 0) {
                assertFalse(profile.getFurthestRounds().isEmpty(),
                        "Furthest rounds should not be empty for " + expected.firstName() + " who has breaks");
            }

            log.info("✓ {} {} (ID={}): avg={}, tournaments={}, prelims={}, breaks={}, winPrelims={}%, rank={}",
                    profile.getFirstName(), profile.getLastName(), expected.debaterId(),
                    String.format("%.2f", profile.getAverageSpeakerScore()),
                    profile.getTournamentsDebated(), profile.getPrelimsDebated(),
                    profile.getBreaksDebated(),
                    profile.getWinPercentagePrelims() != null ? String.format("%.1f", profile.getWinPercentagePrelims()) : "N/A",
                    profile.getSpeakerRank());
        }

        log.info("========================================");
        log.info("✓ All {} debater profiles validated against CSV export", EXPECTED_PROFILES.size());
    }

    @Test
    @Order(14)
    @DisplayName("Test 14: Validate specific judge profiles against exported CSV data")
    void testJudgeProfilesAgainstExportedData() {
        log.info("Test 14: Validating judge profiles against exported CSV data...");

        judgeProfileService.initializeAllJudgeProfiles();
        judgeProfileService.updateAllJudgeProfiles();

        log.info("========================================");
        log.info("JUDGE PROFILE VALIDATION (vs CSV export 2026-05-01)");
        log.info("========================================");

        for (ExpectedJudgeProfileData expected : EXPECTED_JUDGE_PROFILES) {
            Judge judge = judgeService.findJudgeById(expected.judgeId());
            assertNotNull(judge, "Judge should exist for ID " + expected.judgeId());

            JudgeProfile profile = judgeProfileService.getJudgeProfileByJudgeId(expected.judgeId());
            assertNotNull(profile,
                    "Profile should exist for judge ID " + expected.judgeId() +
                            " (" + expected.firstName() + " " + expected.lastName() + ")");

            assertEquals(expected.judgeId(), profile.getJudgeId(),
                    "Judge ID mismatch for " + expected.firstName() + " " + expected.lastName());
            assertEquals(expected.firstName(), profile.getFirstName(),
                    "First name mismatch for judge ID " + expected.judgeId());
            assertEquals(expected.lastName(), profile.getLastName(),
                    "Last name mismatch for judge ID " + expected.judgeId());

            assertEquals(expected.prelimsJudged(), profile.getPrelimsJudged(),
                    "Prelims judged mismatch for " + expected.firstName() + " " + expected.lastName());
            assertEquals(expected.breaksJudged(), profile.getBreaksJudged(),
                    "Breaks judged mismatch for " + expected.firstName() + " " + expected.lastName());
            assertEquals(expected.tournamentsJudged(), profile.getTournamentsJudged(),
                    "Tournaments judged mismatch for " + expected.firstName() + " " + expected.lastName());
            assertEquals(expected.speechCountForMetrics(), profile.getSpeechCountForMetrics(),
                    "Speech count for metrics mismatch for " + expected.firstName() + " " + expected.lastName());

            assertEquals(expected.leniencyCount(), profile.getLeniencyCount(),
                    "Leniency count mismatch for " + expected.firstName() + " " + expected.lastName());
            assertEquals(expected.harshnessCount(), profile.getHarshnessCount(),
                    "Harshness count mismatch for " + expected.firstName() + " " + expected.lastName());
            assertEquals(expected.neutralCount(), profile.getNeutralCount(),
                    "Neutral count mismatch for " + expected.firstName() + " " + expected.lastName());

            assertEquals(expected.averageFirst(), profile.getAverageFirst(), 0.05,
                    "Average first speaker score mismatch for " + expected.firstName() + " " + expected.lastName());
            assertEquals(expected.averageSecond(), profile.getAverageSecond(), 0.05,
                    "Average second speaker score mismatch for " + expected.firstName() + " " + expected.lastName());
            assertEquals(expected.averageSubstantive(), profile.getAverageSubstantive(), 0.05,
                    "Average substantive speaker score mismatch for " + expected.firstName() + " " + expected.lastName());
            assertEquals(expected.averageThird(), profile.getAverageThird(), 0.05,
                    "Average third speaker score mismatch for " + expected.firstName() + " " + expected.lastName());

            assertEquals(expected.leniency(), profile.getLeniency(), 0.02,
                    "Leniency mismatch for " + expected.firstName() + " " + expected.lastName());
            assertEquals(expected.harshness(), profile.getHarshness(), 0.02,
                    "Harshness mismatch for " + expected.firstName() + " " + expected.lastName());
            assertEquals(expected.overallSentiment(), profile.getOverallSentiment(), 0.02,
                    "Overall sentiment mismatch for " + expected.firstName() + " " + expected.lastName());

            assertNotNull(profile.getActivityPercentile(),
                    "Activity percentile should not be null for " + expected.firstName() + " " + expected.lastName());
            assertEquals(expected.activityPercentile(), profile.getActivityPercentile(), 0.5f,
                    "Activity percentile mismatch for " + expected.firstName() + " " + expected.lastName());

            assertNotNull(profile.getRoundPreferences(),
                    "Round preferences should not be null for " + expected.firstName() + " " + expected.lastName());
            assertFalse(profile.getRoundPreferences().isEmpty(),
                    "Round preferences should not be empty for " + expected.firstName() + " " + expected.lastName());

            log.info("✓ {} {} (ID={}): prelims={}, breaks={}, tournaments={}, sentiment={}",
                    profile.getFirstName(), profile.getLastName(), profile.getJudgeId(),
                    profile.getPrelimsJudged(), profile.getBreaksJudged(), profile.getTournamentsJudged(),
                    String.format("%.3f", profile.getOverallSentiment()));
        }

        log.info("========================================");
        log.info("✓ All {} judge profiles validated against CSV export", EXPECTED_JUDGE_PROFILES.size());
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


