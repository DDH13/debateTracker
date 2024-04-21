package com.dineth.debateTracker.utils;

import org.apache.commons.lang3.tuple.ImmutablePair;

public class StringUtil {
    /**
     * Splits a name into first name and last name
     * @param name the name to split
     * @return a pair of first name and last name
     */
    public static ImmutablePair<String, String> splitName(String name) {
        String[] parts = name.split(" ");
        String firstName = parts[0];

        String lastName;
        if (parts.length == 1) {
            //if no last name
            lastName = "";
        } else if (parts.length == 3 && parts[1].equalsIgnoreCase("de")) {
            //if last name has a prefix like "de"
            lastName = parts[1] + " " + parts[2];
        } else if (parts.length == 2) {
            //if only two names
            lastName = parts[1];
        } else {
            //if more than two names
            lastName = parts[parts.length - 1];
        }
        return new ImmutablePair<>(firstName, lastName);
    }
}
