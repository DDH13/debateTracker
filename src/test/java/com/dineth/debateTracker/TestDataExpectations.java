package com.dineth.debateTracker;

import java.util.HashMap;
import java.util.Map;

/**
 * Expected data values from testTourney.xml for precise E2E test assertions.
 * Values are calculated/extracted from the test tournament data.
 * 
 * TODO: Fill in actual values after running the tournament builder and inspecting database
 */
public class TestDataExpectations {
    
    // ========================================
    // SPEAKER TAB EXPECTATIONS
    // ========================================
    
    /**
     * Expected speaker scores for specific debaters across all preliminary rounds.
     * Key: Debater name, Value: ExpectedSpeakerData
     */
    public static class ExpectedSpeakerData {
        public final Long debaterId; // TODO: Fill from database
        public final String firstName;
        public final String lastName;
        public final int speechesCount;
        public final float averageSpeakerScore;
        public final int expectedRank;
        public final double standardDeviation;
        
        public ExpectedSpeakerData(Long debaterId, String firstName, String lastName, 
                                   int speechesCount, float averageSpeakerScore, 
                                   int expectedRank, double standardDeviation) {
            this.debaterId = debaterId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.speechesCount = speechesCount;
            this.averageSpeakerScore = averageSpeakerScore;
            this.expectedRank = expectedRank;
            this.standardDeviation = standardDeviation;
        }
    }
    
    public static final Map<String, ExpectedSpeakerData> SPEAKER_TAB_DATA = new HashMap<>() {{
        // Top speakers from testTourney.xml (calculated from XML ballot scores)
        // TODO: Fill debaterId and verify calculations after tournament build
        
        // Kulith Wickramasinghe (S5) - AC A
        // R1: 76.5, R2: 76.0, R3: (no data visible), R4: 76.0, R5: 75.0
        put("Kulith_Wickramasinghe", new ExpectedSpeakerData(
            null, // TODO: Fill debaterId from database
            "Kulith", "Wickramasinghe",
            5, // speeches count
            75.7f, // TODO: Calculate actual average from all rounds
            1, // TODO: Verify rank after full calculation
            0.6f // TODO: Calculate standard deviation
        ));
        
        // Nimansa Jayasundera (S72) - LC A
        // High scoring speaker - multiple 77+ scores visible in XML
        put("Nimansa_Jayasundera", new ExpectedSpeakerData(
            null, // TODO: Fill debaterId
            "Nimansa", "Jayasundera",
            5,
            76.5f, // TODO: Calculate actual average
            2, // TODO: Verify rank
            0.5f // TODO: Calculate standard deviation
        ));
        
        // Rudhesh Ram (S25) - CIS Quarters
        // High scorer in multiple rounds
        put("Rudhesh_Ram", new ExpectedSpeakerData(
            null, // TODO: Fill debaterId
            "Rudhesh", "Ram",
            5,
            76.3f, // TODO: Calculate actual average
            3, // TODO: Verify rank
            0.7f // TODO: Calculate standard deviation
        ));
        
        // Add more top speakers as needed for comprehensive testing
    }};
    
    // ========================================
    // WIN-LOSS EXPECTATIONS
    // ========================================
    
    public static class ExpectedWinLoss {
        public final String firstName;
        public final String lastName;
        public final int prelimWins;
        public final int prelimLosses;
        public final int breakWins;
        public final int breakLosses;
        
        public ExpectedWinLoss(String firstName, String lastName, 
                               int prelimWins, int prelimLosses, 
                               int breakWins, int breakLosses) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.prelimWins = prelimWins;
            this.prelimLosses = prelimLosses;
            this.breakWins = breakWins;
            this.breakLosses = breakLosses;
        }
    }
    
    public static final Map<String, ExpectedWinLoss> WIN_LOSS_DATA = new HashMap<>() {{
        // Teams that made it to finals/semifinals will have specific records
        // TODO: Analyze XML to determine which teams broke and their records
        
        // Example: A finalist team's debater
        put("Finalist_Example", new ExpectedWinLoss(
            "TODO", "TODO",
            4, // prelim wins
            1, // prelim losses
            3, // break wins (QF, SF, F)
            0  // break losses (undefeated in break)
        ));
        
        // Example: A quarterfinalist team's debater
        put("Quarterfinalist_Example", new ExpectedWinLoss(
            "TODO", "TODO",
            3, // prelim wins
            2, // prelim losses
            0, // break wins (lost in QF)
            1  // break losses
        ));
        
        // Add more specific team records
    }};
    
    // ========================================
    // DEBATE OUTCOME EXPECTATIONS
    // ========================================
    
    public static class ExpectedDebateOutcome {
        public final String roundName;
        public final String debateId;
        public final String propositionTeam;
        public final String oppositionTeam;
        public final String winner;
        public final float propositionScore;
        public final float oppositionScore;
        
        public ExpectedDebateOutcome(String roundName, String debateId,
                                     String propositionTeam, String oppositionTeam,
                                     String winner, float propositionScore, float oppositionScore) {
            this.roundName = roundName;
            this.debateId = debateId;
            this.propositionTeam = propositionTeam;
            this.oppositionTeam = oppositionTeam;
            this.winner = winner;
            this.propositionScore = propositionScore;
            this.oppositionScore = oppositionScore;
        }
    }
    
    public static final Map<String, ExpectedDebateOutcome> DEBATE_OUTCOMES = new HashMap<>() {{
        // From XML: Round 1, Debate D12
        // T17 (HFC - Stunned) vs T16 (HCK - Car Sick Face)
        // T16 wins with 264.5 vs 260.5
        put("R1_D12", new ExpectedDebateOutcome(
            "Round 1", "D12",
            "HFC", "HCK",
            "HCK", 260.5f, 264.5f
        ));
        
        // From XML: Round 1, Debate D9
        // T27 (SBC B - Potato) vs T9 (DS A - Chick)
        // T9 wins with 262.0 vs 257.0
        put("R1_D9", new ExpectedDebateOutcome(
            "Round 1", "D9",
            "SBC B", "DS A",
            "DS A", 257.0f, 262.0f
        ));
        
        // From XML: Round 1, Debate D8
        // T12 (Moir B - Puzzle Piece) vs T11 (Moir A - Wastebasket)
        // T11 wins with 262.5 vs 259.0
        put("R1_D8", new ExpectedDebateOutcome(
            "Round 1", "D8",
            "Moir B", "Moir A",
            "Moir A", 259.0f, 262.5f
        ));
        
        // Add more key debates, especially from elimination rounds
        // TODO: Add quarterfinals, semifinals, finals debates
    }};
    
    // ========================================
    // BALLOT SCORE VALIDATION
    // ========================================
    
    public static class ExpectedBallot {
        public final String debaterName;
        public final String roundName;
        public final float score;
        public final int position;
        public final boolean isReplyScore;
        
        public ExpectedBallot(String debaterName, String roundName, 
                             float score, int position, boolean isReplyScore) {
            this.debaterName = debaterName;
            this.roundName = roundName;
            this.score = score;
            this.position = position;
            this.isReplyScore = isReplyScore;
        }
    }
    
    public static final Map<String, ExpectedBallot> BALLOT_SCORES = new HashMap<>() {{
        // Kulith Wickramasinghe (S5) in Round 1 debate D1
        put("S5_R1_Speech1", new ExpectedBallot(
            "Kulith Wickramasinghe", "Round 1",
            76.5f, 1, false
        ));
        
        put("S5_R1_Reply", new ExpectedBallot(
            "Kulith Wickramasinghe", "Round 1",
            37.0f, 1, true
        ));
        
        // Add more specific ballot validations
        // TODO: Select key ballots to validate score calculation
    }};
    
    // ========================================
    // BREAK/ELIMINATION EXPECTATIONS
    // ========================================
    
    public static class ExpectedBreakTeam {
        public final String teamName;
        public final String furthestRound;
        public final int prelimWins;
        public final int prelimLosses;
        
        public ExpectedBreakTeam(String teamName, String furthestRound, 
                                int prelimWins, int prelimLosses) {
            this.teamName = teamName;
            this.furthestRound = furthestRound;
            this.prelimWins = prelimWins;
            this.prelimLosses = prelimLosses;
        }
    }
    
    public static final Map<String, ExpectedBreakTeam> BREAK_TEAMS = new HashMap<>() {{
        // Teams that broke to elimination rounds
        // TODO: Analyze XML elimination rounds to determine breaking teams
        
        put("Champions", new ExpectedBreakTeam(
            "TODO_TEAM_NAME", "Grand Final",
            4, 1 // TODO: Verify prelim record
        ));
        
        put("Runners_Up", new ExpectedBreakTeam(
            "TODO_TEAM_NAME", "Grand Final",
            4, 1 // TODO: Verify prelim record
        ));
        
        put("Semifinalist_1", new ExpectedBreakTeam(
            "TODO_TEAM_NAME", "Semifinals",
            3, 2 // TODO: Verify prelim record
        ));
        
        put("Semifinalist_2", new ExpectedBreakTeam(
            "TODO_TEAM_NAME", "Semifinals",
            3, 2 // TODO: Verify prelim record
        ));
        
        // Add all 8 quarterfinalists
        // Typically top 8 teams break to quarterfinals in British Parliamentary
    }};
    
    // ========================================
    // TEAM STANDINGS EXPECTATIONS
    // ========================================
    
    public static class ExpectedTeamStanding {
        public final String teamName;
        public final int wins;
        public final int losses;
        public final float totalSpeakerPoints;
        public final int expectedRank;
        
        public ExpectedTeamStanding(String teamName, int wins, int losses, 
                                   float totalSpeakerPoints, int expectedRank) {
            this.teamName = teamName;
            this.wins = wins;
            this.losses = losses;
            this.totalSpeakerPoints = totalSpeakerPoints;
            this.expectedRank = expectedRank;
        }
    }
    
    public static final Map<String, ExpectedTeamStanding> TEAM_STANDINGS = new HashMap<>() {{
        // TODO: Calculate team standings from XML prelim rounds
        // Track total speaker points across all prelims for each team
    }};
    
    // ========================================
    // CONSTANTS
    // ========================================
    
    public static final int EXPECTED_MINIMUM_SPEECHES = 3; // TODO: Verify minimum speeches threshold
    public static final int EXPECTED_PRELIM_ROUNDS = 5;
    public static final int EXPECTED_BREAK_ROUNDS = 4; // QF, SF, 3rd Place, GF
    public static final int EXPECTED_BREAKING_TEAMS = 8; // Quarterfinalists
    
    public static final float SCORE_COMPARISON_DELTA = 0.1f; // Tolerance for float comparisons
    public static final double STDDEV_COMPARISON_DELTA = 0.1; // Tolerance for standard deviation
}

