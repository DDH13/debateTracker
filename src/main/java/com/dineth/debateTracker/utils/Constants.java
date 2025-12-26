package com.dineth.debateTracker.utils;

import java.util.List;
import java.util.Map;

public class Constants {
    public static final String ROUND1 = "Round 1";
    public static final String ROUND2 = "Round 2";
    public static final String ROUND3 = "Round 3";
    public static final String ROUND4 = "Round 4";
    public static final String ROUND5 = "Round 5";
    public static final String ROUND6 = "Round 6";
    public static final String PARTIAL_OCTOS =  "Partial Octofinals";
    public static final String OCTOS =  "Octofinals";
    public static final String QUARTERS =  "Quarterfinals";
    public static final String SEMIS =  "Semifinals";
    public static final String FINALS =  "Grand Final";
    public static final String NOVICE_SEMIS =  "Novice Semifinals";
    public static final String NOVICE_FINALS =  "Novice Finals";
    
    public static final String CHAMPIONS = "Champions";
    public static final String RUNNERS_UP = "Runners-Up";
    public static final String SEMI_FINALISTS = "Semi-Finalists";
    public static final String QUARTER_FINALISTS = "Quarter-Finalists";
    public static final String OCTO_FINALISTS = "Octo-Finalists";
    public static final String PARTIAL_OCTO_FINALISTS = "Partial Octo-Finalists";
    public static final String NOVICE_CHAMPIONS = "Novice Champions";
    public static final String NOVICE_RUNNERS_UP = "Novice Runners-Up";
    public static final String NOVICE_SEMI_FINALISTS = "Novice Semi-Finalists";
    
    public static final List<String> PRELIM_ROUNDS = List.of(
            ROUND1,
            ROUND2,
            ROUND3,
            ROUND4,
            ROUND5,
            ROUND6
    );
    public static final List<String> BREAK_ROUNDS = List.of(
            PARTIAL_OCTOS,
            OCTOS,
            QUARTERS,
            SEMIS,
            FINALS,
            NOVICE_SEMIS,
            NOVICE_FINALS
    );
    
    public static final Map<String,Integer> OUTROUND_PRIORITY = Map.of(
            FINALS, 1,
            SEMIS, 2,
            QUARTERS, 3,
            PARTIAL_OCTOS, 4,
            OCTOS, 5,
            NOVICE_FINALS, 6,
            NOVICE_SEMIS, 7
    );
    
}
