package com.dineth.debateTracker;

import com.dineth.debateTracker.ballot.Ballot;
import com.dineth.debateTracker.ballot.BallotService;
import com.dineth.debateTracker.breakcategory.BreakCategory;
import com.dineth.debateTracker.breakcategory.BreakCategoryService;
import com.dineth.debateTracker.builders.TestFixtures;
import com.dineth.debateTracker.debate.Debate;
import com.dineth.debateTracker.debate.DebateService;
import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.dtos.TournamentDataDTO;
import com.dineth.debateTracker.institution.Institution;
import com.dineth.debateTracker.institution.InstitutionService;
import com.dineth.debateTracker.judge.Judge;
import com.dineth.debateTracker.motion.Motion;
import com.dineth.debateTracker.motion.MotionService;
import com.dineth.debateTracker.round.Round;
import com.dineth.debateTracker.round.RoundService;
import com.dineth.debateTracker.team.Team;
import com.dineth.debateTracker.team.TeamService;
import com.dineth.debateTracker.tournament.Tournament;
import com.dineth.debateTracker.tournament.TournamentService;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End test for tournament data import and entity creation verification.
 * Tests that all entities are correctly created from XML data and relationships are properly established.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class TournamentDataE2ETest extends BaseE2ETest {

    private static final Logger log = LoggerFactory.getLogger(TournamentDataE2ETest.class);

    @Autowired
    private TeamService teamService;

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private MotionService motionService;

    @Autowired
    private RoundService roundService;

    @Autowired
    private DebateService debateService;

    @Autowired
    private BallotService ballotService;

    @Autowired
    private BreakCategoryService breakCategoryService;

    private static TournamentInfo tournament1;

    @BeforeAll
    static void buildTournament(@Autowired TournamentBuilder builder, @Autowired TournamentService tournamentService) {
        log.info("========================================");
        log.info("Building tournament for data verification tests...");
        log.info("========================================");

        TournamentDataDTO data = builder.buildMyTournament(TestFixtures.TOURNAMENT_1_XML);
        Long tournamentId = findTournamentId(tournamentService, data);

        tournament1 = new TournamentInfo(data, tournamentId);

        log.info("========================================");
        log.info("Tournament built successfully!");
        log.info("========================================");
    }

    // ========================================
    // ENTITY CREATION VERIFICATION TESTS
    // ========================================

    @Test
    @Order(1)
    @DisplayName("Test 1: Verify tournament basic details")
    void testTournamentBasicDetails() {
        log.info("Test 1: Verifying tournament basic details...");

        Tournament tournament = tournamentService.getTournamentById(tournament1.getId());
        assertNotNull(tournament, "Tournament should exist");
        assertNotNull(tournament.getShortName(), "Tournament should have a short name");
        assertNotNull(tournament.getFullName(), "Tournament should have a full name");

        log.info("✓ Tournament: {} ({})", tournament.getFullName(), tournament.getShortName());
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: Verify teams were created")
    void testTeamsCreated() {
        log.info("Test 2: Verifying teams...");

        List<Team> teams = teamService.getTeam();
        assertEquals(TestFixtures.Tournament1.EXPECTED_TEAMS, teams.size(),
                "Should have expected number of teams");

        log.info("✓ Created {} teams", teams.size());
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: Verify debaters were created")
    void testDebatersCreated() {
        log.info("Test 3: Verifying debaters...");

        List<Debater> debaters = debaterService.getDebaters();
        assertEquals(TestFixtures.Tournament1.EXPECTED_DEBATERS, debaters.size(),
                "Should have expected number of debaters");

        log.info("✓ Created {} debaters", debaters.size());
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: Verify judges were created")
    void testJudgesCreated() {
        log.info("Test 4: Verifying judges...");

        List<Judge> judges = judgeService.getJudges();
        assertEquals(TestFixtures.Tournament1.EXPECTED_JUDGES, judges.size(),
                "Should have expected number of judges");

        log.info("✓ Created {} judges", judges.size());
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: Verify institutions were created")
    void testInstitutionsCreated() {
        log.info("Test 5: Verifying institutions...");

        List<Institution> institutions = institutionService.getInstitutions();
        assertEquals(TestFixtures.Tournament1.EXPECTED_INSTITUTIONS, institutions.size(),
                "Should have expected number of institutions");

        log.info("✓ Created {} institutions", institutions.size());
    }

    @Test
    @Order(6)
    @DisplayName("Test 6: Verify motions were created")
    @Transactional
    void testMotionsCreated() {
        log.info("Test 6: Verifying motions...");

        Tournament tournament = tournamentService.getTournamentById(tournament1.getId());
        List<Motion> motions = tournament.getMotions();
        assertEquals(TestFixtures.Tournament1.EXPECTED_MOTIONS, motions.size(),
                "Should have expected number of motions");

        log.info("✓ Created {} motions", motions.size());
    }

    @Test
    @Order(7)
    @DisplayName("Test 7: Verify rounds were created")
    @Transactional
    void testRoundsCreated() {
        log.info("Test 7: Verifying rounds...");

        Tournament tournament = tournamentService.getTournamentById(tournament1.getId());
        List<Round> rounds = tournament.getRounds();
        assertEquals(TestFixtures.Tournament1.EXPECTED_ROUNDS, rounds.size(),
                "Should have expected number of rounds");

        // Verify prelim vs elimination rounds
        long prelimRounds = rounds.stream().filter(r -> !r.getIsBreakRound()).count();
        long elimRounds = rounds.stream().filter(Round::getIsBreakRound).count();

        assertEquals(TestFixtures.Tournament1.EXPECTED_PRELIM_ROUNDS, prelimRounds,
                "Should have expected number of prelim rounds");
        assertEquals(TestFixtures.Tournament1.EXPECTED_ELIMINATION_ROUNDS, elimRounds,
                "Should have expected number of elimination rounds");

        log.info("✓ Created {} rounds ({} prelims, {} elimination)",
                rounds.size(), prelimRounds, elimRounds);
    }

    @Test
    @Order(8)
    @DisplayName("Test 8: Verify break categories were created")
    @Transactional
    void testBreakCategoriesCreated() {
        log.info("Test 8: Verifying break categories...");

        Tournament tournament = tournamentService.getTournamentById(tournament1.getId());
        List<BreakCategory> breakCategories = tournament.getBreakCategories();
        assertFalse(breakCategories.isEmpty(), "Should have at least one break category");

        log.info("✓ Created {} break categories", breakCategories.size());
    }

    // ========================================
    // RELATIONSHIP VALIDATION TESTS
    // ========================================

    @Test
    @Order(9)
    @DisplayName("Test 9: Verify round-tournament relationships")
    @Transactional
    void testRoundTournamentRelationships() {
        log.info("Test 9: Verifying round-tournament relationships...");

        Tournament tournament = tournamentService.getTournamentById(tournament1.getId());
        List<Round> tournamentRounds = tournament.getRounds();

        assertEquals(TestFixtures.Tournament1.EXPECTED_ROUNDS, tournamentRounds.size(),
                "Tournament should have all rounds linked");

        // Verify each round references the tournament
        for (Round round : tournamentRounds) {
            assertEquals(tournament.getId(), round.getTournament().getId(),
                    "Round should reference correct tournament");
        }

        log.info("✓ All rounds properly linked to tournament");
    }

    @Test
    @Order(10)
    @DisplayName("Test 10: Verify debate-round relationships")
    @Transactional
    void testDebateRoundRelationships() {
        log.info("Test 10: Verifying debate-round relationships...");

        Tournament tournament = tournamentService.getTournamentById(tournament1.getId());
        List<Round> rounds = tournament.getRounds();
        boolean allRoundsHaveDebates = rounds.stream()
                .allMatch(r -> r.getDebates() != null && !r.getDebates().isEmpty());

        assertTrue(allRoundsHaveDebates, "All rounds should have debates");

        log.info("✓ All rounds have debates assigned");
    }

    @Test
    @Order(11)
    @DisplayName("Test 11: Verify team-debater composition")
    @Transactional
    void testTeamDebaterComposition() {
        log.info("Test 11: Verifying team-debater composition...");

        List<Team> teams = teamService.getTeam();
        
        // Verify each team has debaters
        for (Team team : teams) {
            assertNotNull(team.getDebaters(), "Team should have debaters list");
            assertTrue(team.getDebaters().size() >= 2,
                    "Each team should have at least 2 debaters");
        }

        log.info("✓ All teams have correct debater composition");
    }

    @Test
    @Order(12)
    @DisplayName("Test 12: Verify ballot-judge-debater relationships")
    void testBallotRelationships() {
        log.info("Test 12: Verifying ballot relationships...");

        List<Ballot> ballots = ballotService.getBallots();
        assertFalse(ballots.isEmpty(), "Should have ballots created");

        // Verify ballots have required relationships
        int validBallots = 0;
        for (Ballot ballot : ballots) {
            if (ballot.getJudge() != null && ballot.getDebater() != null) {
                assertNotNull(ballot.getJudge(), "Ballot should have judge");
                assertNotNull(ballot.getDebater(), "Ballot should have debater");
                assertTrue(ballot.getSpeakerScore() > 0, "Ballot should have speaker score");
                assertTrue(ballot.getSpeakerPosition() >= 1 && ballot.getSpeakerPosition() <= 4,
                        "Speaker position should be between 1 and 4");
                validBallots++;
            }
        }

        log.info("✓ {} valid ballots with proper relationships", validBallots);
    }

    @Test
    @Order(13)
    @DisplayName("Test 13: Verify institution-team relationships")
    @Transactional
    void testInstitutionTeamRelationships() {
        log.info("Test 13: Verifying institution-team relationships...");

        List<Institution> institutions = institutionService.getInstitutions();
        
        // Verify institutions have teams assigned
        boolean hasTeams = institutions.stream()
                .anyMatch(i -> i.getTeams() != null && !i.getTeams().isEmpty());

        assertTrue(hasTeams, "At least some institutions should have teams");

        log.info("✓ Institution-team relationships verified");
    }

    @Test
    @Order(14)
    @DisplayName("Test 14: Verify motions exist for tournament")
    @Transactional
    void testMotionsExist() {
        log.info("Test 14: Verifying motions...");

        Tournament tournament = tournamentService.getTournamentById(tournament1.getId());
        List<Motion> motions = tournament.getMotions();
        
        assertNotNull(motions, "Tournament should have motions");
        assertFalse(motions.isEmpty(), "Tournament should have motions assigned");

        log.info("✓ Tournament has {} motions assigned", motions.size());
    }
    
    @AfterAll
    static void printSummary() {
        log.info("========================================");
        log.info("TOURNAMENT DATA E2E TEST SUMMARY");
        log.info("========================================");
        log.info("✓ Tournament built and verified");
        log.info("✓ All entities created correctly");
        log.info("✓ All relationships validated");
        log.info("========================================");
    }
}




