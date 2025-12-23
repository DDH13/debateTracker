package com.dineth.debateTracker.utils;

import java.util.Comparator;
import java.util.List;

public class ProfileUtil {
    
    public static final double EPS = 0.01;
    public static int competitionRank(double score, List<Double> scores) {
        List<Double> sorted = scores.stream()
                .sorted(Comparator.reverseOrder())
                .toList();

        int rank = 1;
        int i = 0;

        while (i < sorted.size()) {
            double current = sorted.get(i);

            // If equal within tolerance, this is the rank
            if (Math.abs(current - score) < EPS) {
                return rank;
            }

            // Count how many are effectively equal
            int count = 1;
            int j = i + 1;
            while (j < sorted.size() &&
                    Math.abs(sorted.get(j) - current) < EPS) {
                count++;
                j++;
            }

            rank += count;
            i = j;
        }

        return -1; // score not found
    }

    /**
     * Calculates a percentile
     */
    public static double percentileRank(double val, List<Double> values, double delta) {
        if (values == null || values.isEmpty()) {
            return Double.NaN;
        }

        int less = 0;
        int equal = 0;

        for (double v : values) {
            double diff = v - val;

            if (diff < -delta) {
                less++;
            } else if (Math.abs(diff) <= delta) {
                equal++;
            }
            // else: greater, ignore
        }

        int n = values.size();

        if (equal == 0) {
            return (less * 100.0) / n;
        }

        // midrank for ties
        return ((less + equal / 2.0) * 100.0) / n;
    }

}
