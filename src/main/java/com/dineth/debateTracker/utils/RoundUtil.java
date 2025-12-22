package com.dineth.debateTracker.utils;

public class RoundUtil {
    /**
     * Compare two round names and returns the furthest round based on out-round priority
     */
    public static String compareRoundAndGetHigherPriorityRound(String roundName1, String roundName2) {
        if (roundName1 == null)
            return roundName2;
        if (roundName2 == null)
            return roundName1;
        Integer priority1 = com.dineth.debateTracker.utils.Constants.OUTROUND_PRIORITY.getOrDefault(
                roundName1, Integer.MAX_VALUE);
        Integer priority2 = com.dineth.debateTracker.utils.Constants.OUTROUND_PRIORITY.getOrDefault(
                roundName2, Integer.MAX_VALUE);
        return priority1 < priority2 ? roundName1 : roundName2;
    }
}
