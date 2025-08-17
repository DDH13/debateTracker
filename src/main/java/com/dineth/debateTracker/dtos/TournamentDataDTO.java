package com.dineth.debateTracker.dtos;

import com.dineth.debateTracker.dtos.xmlparsing.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Comprehensive DTO that contains all the tournament data after XML parsing.
 * This includes debaters, rounds, round results, judges, motions, teams, institutions, and break categories.
 */
@Getter
@Setter
@NoArgsConstructor
public class TournamentDataDTO {
    
    // Basic tournament information
    private TournamentDTO tournament;
    
    // All participants and entities
    private List<DebaterDTO> debaters;
    private List<TeamDTO> teams;
    private List<JudgeDTO> judges;
    private List<InstitutionDTO> institutions;
    private List<MotionDTO> motions;
    private List<BreakCategoryDTO> breakCategories;
    
    // Tournament structure and results
    private List<RoundDTO> rounds;
    
    // Mappings for quick lookup during parsing
    private Map<String, DebaterDTO> debaterMap;
    private Map<String, TeamDTO> teamMap;
    private Map<String, JudgeDTO> judgeMap;
    private Map<String, InstitutionDTO> institutionMap;
    private Map<String, MotionDTO> motionMap;
    
    // Statistics and metadata
    private TournamentMetadata metadata;
    
    /**
     * Constructor with all required data
     */
    public TournamentDataDTO(TournamentDTO tournament,
                           List<DebaterDTO> debaters,
                           List<TeamDTO> teams,
                           List<JudgeDTO> judges,
                           List<InstitutionDTO> institutions,
                           List<MotionDTO> motions,
                           List<BreakCategoryDTO> breakCategories,
                           List<RoundDTO> rounds) {
        this.tournament = tournament;
        this.debaters = debaters;
        this.teams = teams;
        this.judges = judges;
        this.institutions = institutions;
        this.motions = motions;
        this.breakCategories = breakCategories;
        this.rounds = rounds;
        this.metadata = new TournamentMetadata();
        calculateMetadata();
    }
    
    /**
     * Constructor with maps for quick lookup
     */
    public TournamentDataDTO(TournamentDTO tournament,
                           List<DebaterDTO> debaters,
                           List<TeamDTO> teams,
                           List<JudgeDTO> judges,
                           List<InstitutionDTO> institutions,
                           List<MotionDTO> motions,
                           List<BreakCategoryDTO> breakCategories,
                           List<RoundDTO> rounds,
                           Map<String, DebaterDTO> debaterMap,
                           Map<String, TeamDTO> teamMap,
                           Map<String, JudgeDTO> judgeMap) {
        this(tournament, debaters, teams, judges, institutions, motions, breakCategories, rounds);
        this.debaterMap = debaterMap;
        this.teamMap = teamMap;
        this.judgeMap = judgeMap;
    }
    
    /**
     * Calculate tournament metadata and statistics
     */
    private void calculateMetadata() {
        if (metadata == null) {
            metadata = new TournamentMetadata();
        }
        
        metadata.setTotalDebaters(debaters != null ? debaters.size() : 0);
        metadata.setTotalTeams(teams != null ? teams.size() : 0);
        metadata.setTotalJudges(judges != null ? judges.size() : 0);
        metadata.setTotalInstitutions(institutions != null ? institutions.size() : 0);
        metadata.setTotalMotions(motions != null ? motions.size() : 0);
        metadata.setTotalRounds(rounds != null ? rounds.size() : 0);
        metadata.setTotalBreakCategories(breakCategories != null ? breakCategories.size() : 0);
        
        // Calculate total debates across all rounds
        int totalDebates = 0;
        int preliminaryRounds = 0;
        int eliminationRounds = 0;
        
        if (rounds != null) {
            for (RoundDTO round : rounds) {
                if (round.getDebates() != null) {
                    totalDebates += round.getDebates().size();
                }
                if (round.isElimination()) {
                    eliminationRounds++;
                } else {
                    preliminaryRounds++;
                }
            }
        }
        
        metadata.setTotalDebates(totalDebates);
        metadata.setPreliminaryRounds(preliminaryRounds);
        metadata.setEliminationRounds(eliminationRounds);
    }
    
    /**
     * Get a round by its name
     */
    public RoundDTO getRoundByName(String name) {
        if (rounds == null || name == null) {
            return null;
        }
        return rounds.stream()
                .filter(round -> name.equals(round.getName()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Get all preliminary rounds
     */
    public List<RoundDTO> getPreliminaryRounds() {
        if (rounds == null) {
            return List.of();
        }
        return rounds.stream()
                .filter(round -> !round.isElimination())
                .toList();
    }
    
    /**
     * Get all elimination rounds
     */
    public List<RoundDTO> getEliminationRounds() {
        if (rounds == null) {
            return List.of();
        }
        return rounds.stream()
                .filter(RoundDTO::isElimination)
                .toList();
    }
    
    /**
     * Get teams by institution
     */
    public List<TeamDTO> getTeamsByInstitution(String institutionId) {
        if (teams == null || institutionId == null) {
            return List.of();
        }
        return teams.stream()
                .filter(team -> team.getDebaters() != null &&
                        team.getDebaters().stream()
                                .anyMatch(debater -> institutionId.equals(debater.getInstitutionId())))
                .toList();
    }
    
    /**
     * Get debaters by institution
     */
    public List<DebaterDTO> getDebatersByInstitution(String institutionId) {
        if (debaters == null || institutionId == null) {
            return List.of();
        }
        return debaters.stream()
                .filter(debater -> institutionId.equals(debater.getInstitutionId()))
                .toList();
    }
    
    /**
     * Get all debates across all rounds
     */
    public List<DebateDTO> getAllDebates() {
        if (rounds == null) {
            return List.of();
        }
        return rounds.stream()
                .filter(round -> round.getDebates() != null)
                .flatMap(round -> round.getDebates().stream())
                .toList();
    }
    
    /**
     * Get debates for a specific round
     */
    public List<DebateDTO> getDebatesForRound(String roundName) {
        RoundDTO round = getRoundByName(roundName);
        return round != null && round.getDebates() != null ? round.getDebates() : List.of();
    }
    
    /**
     * Get motion by ID
     */
    public MotionDTO getMotionById(String motionId) {
        if (motions == null || motionId == null) {
            return null;
        }
        return motions.stream()
                .filter(motion -> motionId.equals(motion.getId()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Get team by ID (using the team map for efficiency)
     */
    public TeamDTO getTeamById(String teamId) {
        if (teamMap != null) {
            return teamMap.get(teamId);
        }
        // Fallback to linear search if map is not available
        if (teams == null || teamId == null) {
            return null;
        }
        return teams.stream()
                .filter(team -> teamId.equals(team.getId()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Get judge by ID (using the judge map for efficiency)
     */
    public JudgeDTO getJudgeById(String judgeId) {
        if (judgeMap != null) {
            return judgeMap.get(judgeId);
        }
        // Fallback to linear search if map is not available
        if (judges == null || judgeId == null) {
            return null;
        }
        return judges.stream()
                .filter(judge -> judgeId.equals(judge.getId()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Get debater by ID (using the debater map for efficiency)
     */
    public DebaterDTO getDebaterById(String debaterId) {
        if (debaterMap != null) {
            return debaterMap.get(debaterId);
        }
        // Fallback to linear search if map is not available
        if (debaters == null || debaterId == null) {
            return null;
        }
        return debaters.stream()
                .filter(debater -> debaterId.equals(debater.getId()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Nested class for tournament metadata and statistics
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class TournamentMetadata {
        private int totalDebaters;
        private int totalTeams;
        private int totalJudges;
        private int totalInstitutions;
        private int totalMotions;
        private int totalRounds;
        private int totalDebates;
        private int totalBreakCategories;
        private int preliminaryRounds;
        private int eliminationRounds;
        
        @Override
        public String toString() {
            return "TournamentMetadata{" +
                    "totalDebaters=" + totalDebaters +
                    ", totalTeams=" + totalTeams +
                    ", totalJudges=" + totalJudges +
                    ", totalInstitutions=" + totalInstitutions +
                    ", totalMotions=" + totalMotions +
                    ", totalRounds=" + totalRounds +
                    ", totalDebates=" + totalDebates +
                    ", totalBreakCategories=" + totalBreakCategories +
                    ", preliminaryRounds=" + preliminaryRounds +
                    ", eliminationRounds=" + eliminationRounds +
                    '}';
        }
    }
    
    @Override
    public String toString() {
        return "TournamentDataDTO{" +
                "tournament=" + (tournament != null ? tournament.getShortName() : "null") +
                ", metadata=" + metadata +
                '}';
    }
}
