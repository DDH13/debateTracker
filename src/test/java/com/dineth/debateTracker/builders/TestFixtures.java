package com.dineth.debateTracker.builders;

/**
 * Centralized test data fixtures and expected values for E2E tests.
 * This class manages test tournament data and expected outcomes.
 */
public class TestFixtures {

    // ========================================
    // TEST TOURNAMENT FILES
    // ========================================
    
    public static final String TOURNAMENT_1_XML = "src/test/resources/testTourney.xml";
    public static final String TOURNAMENT_2_XML = "src/test/resources/testTourney2.xml";
    public static final String TOURNAMENT_3_XML = "src/test/resources/testTourney3.xml";

    // ========================================
    // TOURNAMENT 1 EXPECTED COUNTS
    // ========================================
    
    public static class Tournament1 {
        public static final String NAME = "Tournament 1";
        public static final int EXPECTED_TEAMS = 33;
        public static final int EXPECTED_DEBATERS = 126;
        public static final int EXPECTED_JUDGES = 47;
        public static final int EXPECTED_INSTITUTIONS = 24;
        public static final int EXPECTED_MOTIONS = 10;
        public static final int EXPECTED_ROUNDS = 9;
        public static final int EXPECTED_PRELIM_ROUNDS = 6;
        public static final int EXPECTED_ELIMINATION_ROUNDS = 3;
    }

    // ========================================
    // EXPECTED DATA STRUCTURES
    // ========================================

    /**
     * Expected data for debater speaks tests
     */
    public static class ExpectedDebaterSpeaks {
        public String firstName;
        public String lastName;
        public Long debaterId;
        public Integer expectedTotalDebates;

        public ExpectedDebaterSpeaks(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.debaterId = null;
            this.expectedTotalDebates = 0;
        }

        public ExpectedDebaterSpeaks withDebaterId(Long id) {
            this.debaterId = id;
            return this;
        }

        public ExpectedDebaterSpeaks withTotalDebates(int total) {
            this.expectedTotalDebates = total;
            return this;
        }
    }

    /**
     * Expected data for judge tournaments tests
     */
    public static class ExpectedJudgeTournaments {
        public String firstName;
        public String lastName;
        public Long judgeId;
        public java.util.List<String> expectedTournamentNames;

        public ExpectedJudgeTournaments(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.judgeId = null;
            this.expectedTournamentNames = new java.util.ArrayList<>();
        }

        public ExpectedJudgeTournaments withJudgeId(Long id) {
            this.judgeId = id;
            return this;
        }

        public ExpectedJudgeTournaments withTournaments(String... tournaments) {
            this.expectedTournamentNames = java.util.Arrays.asList(tournaments);
            return this;
        }
    }

    /**
     * Expected data for debater profile tests
     */
    public static class ExpectedDebaterProfile {
        public String firstName;
        public String lastName;
        public Long debaterId;
        public Integer expectedPrelimsDebated;
        public Integer expectedBreaksDebated;
        public Integer expectedTournamentsDebated;
        public Float expectedWinPercentagePrelims;
        public Float expectedAverageSpeakerScore;

        public ExpectedDebaterProfile(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.debaterId = null;
            this.expectedPrelimsDebated = 0;
            this.expectedBreaksDebated = 0;
            this.expectedTournamentsDebated = 0;
            this.expectedWinPercentagePrelims = 0.0f;
            this.expectedAverageSpeakerScore = 0.0f;
        }

        public ExpectedDebaterProfile withDebaterId(Long id) {
            this.debaterId = id;
            return this;
        }

        public ExpectedDebaterProfile withPrelims(int prelims) {
            this.expectedPrelimsDebated = prelims;
            return this;
        }

        public ExpectedDebaterProfile withBreaks(int breaks) {
            this.expectedBreaksDebated = breaks;
            return this;
        }

        public ExpectedDebaterProfile withTournaments(int tournaments) {
            this.expectedTournamentsDebated = tournaments;
            return this;
        }

        public ExpectedDebaterProfile withWinPercentage(float percentage) {
            this.expectedWinPercentagePrelims = percentage;
            return this;
        }

        public ExpectedDebaterProfile withAverageSpeakerScore(float score) {
            this.expectedAverageSpeakerScore = score;
            return this;
        }
    }

    /**
     * Expected data for judge profile tests
     */
    public static class ExpectedJudgeProfile {
        public String firstName;
        public String lastName;
        public Long judgeId;
        public Integer expectedPrelimsJudged;
        public Integer expectedBreaksJudged;
        public Integer expectedTournamentsJudged;

        public ExpectedJudgeProfile(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.judgeId = null;
            this.expectedPrelimsJudged = 0;
            this.expectedBreaksJudged = 0;
            this.expectedTournamentsJudged = 0;
        }

        public ExpectedJudgeProfile withJudgeId(Long id) {
            this.judgeId = id;
            return this;
        }

        public ExpectedJudgeProfile withPrelims(int prelims) {
            this.expectedPrelimsJudged = prelims;
            return this;
        }

        public ExpectedJudgeProfile withBreaks(int breaks) {
            this.expectedBreaksJudged = breaks;
            return this;
        }

        public ExpectedJudgeProfile withTournaments(int tournaments) {
            this.expectedTournamentsJudged = tournaments;
            return this;
        }
    }
}

