package com.dineth.debateTracker.utils;

import org.apache.commons.lang3.tuple.ImmutablePair;

public class StringUtil {
    /**
     * Splits a name into first name and last name
     *
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

    /**
     * Parses a phone number to the international format
     *
     * @param phoneNumber the phone number to parse
     * @return the parsed phone number
     */
    public static String parsePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number cannot be null");
        } else if (phoneNumber.isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        String parsedPhoneNumber;
        //replace all except numbers or + sign in the string
        phoneNumber = phoneNumber.replaceAll("[^0-9+]", "");

        //if it starts with a 0 and has 10 digits
        if (phoneNumber.startsWith("0") && phoneNumber.length() == 10) {
            return "+94" + phoneNumber.substring(1);
        } else if (!phoneNumber.startsWith("0") && phoneNumber.length() == 9) {
            //if it doesn't start with 0 and has 9 digits
            return "+94" + phoneNumber;
        } else if (phoneNumber.startsWith("+") && phoneNumber.length() == 12) {
            //if it starts with + and has 11 digits
            return phoneNumber;
        } else {
            throw new IllegalArgumentException("Invalid phone number : " + phoneNumber);
        }
    }
}
