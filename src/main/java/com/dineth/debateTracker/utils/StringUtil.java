package com.dineth.debateTracker.utils;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Arrays;
import java.util.HashSet;

public class StringUtil {
    /**
     * Splits a name into first name and last name
     *
     * @param name the name to split
     * @return a pair of first name and last name
     */
    public static ImmutablePair<String, String> splitName(String name) {
        String firstName;
        String lastName;
        if (name == null || name.isEmpty()) {
            throw new CustomExceptions.NameSplitException("Debater name cannot be null or empty");
        }
        //split based on spaces or dots
        String[] parts = name.split("[\\s.]");
        //remove all empty strings
        parts = Arrays.stream(parts).filter(s -> !s.isEmpty()).toArray(String[]::new);
        if (parts.length == 1) {
            throw new CustomExceptions.NameSplitException("Debater name must at least have a first name and a last name");
        }

        //check for prefixes in the entire name
        int prefixIndex = -1;
        for (int i = 0; i < parts.length; i++) {
            if (isPrefix(parts[i])) {
                prefixIndex = i;
            }
        }
        firstName = parts[0];
        if (prefixIndex != -1) {
            //if there is a prefix, the first name is the first word and the last name is the prefix and the word after the prefix
            lastName = parts[prefixIndex] + " " + parts[prefixIndex + 1];
        } else {
            //if there is no prefix, the first name is the first word and the last name is the last word
            lastName = parts[parts.length - 1];
        }
        return new ImmutablePair<>(capitalizeName(firstName.trim()), capitalizeName(lastName.trim()));


    }

    /**
     * Checks if a name is a common prefix
     *
     * @param name the name to check
     * @return true if the name is a common prefix, false otherwise
     */
    public static boolean isPrefix(String name) {
        HashSet<String> prefixes = new HashSet<>(Arrays.asList("de", "la", "van", "von"));
        return prefixes.contains(name.toLowerCase());
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

    /**
     * @param gender M or F or other
     * @return Male, Female or throws an exception
     */
    public static String parseGender(String gender) {
        if (gender == null) {
            throw new IllegalArgumentException("Gender is null");
        }
        gender = gender.strip();
        if (gender.equals("M")) {
            return "Male";
        } else if (gender.equals("F")) {
            return "Female";
        } else {
            throw new IllegalArgumentException("Other Gender: " + gender);
        }
    }

    public static boolean isSamePerson(String name1, String name2) {
        return name1.equalsIgnoreCase(name2);
    }

    /**
     * Capitalizes the first letter of each word in a name barring common prefixes that should not be capitalized
     *
     * @param name the name to capitalize
     * @return the capitalized name
     */
    public static String capitalizeName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        // Split the name into words
        String[] words = name.split("\\s+");
        StringBuilder capitalized = new StringBuilder();

        for (String word : words) {
            // Check if the word is a prefix that should not be fully capitalized
            if (isLowercasePrefix(word.toLowerCase())) {
                capitalized.append(word.substring(0, 1).toLowerCase()).append(word.substring(1).toLowerCase());
            } else {
                // Capitalize the first letter and make the rest lowercase
                capitalized.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase());
            }
            capitalized.append(" ");
        }

        // Remove the last unnecessary space and return the result
        return capitalized.toString().trim();
    }

    /**
     * Checks if a word is a common prefix that should not be capitalized
     *
     * @param word the word to check
     * @return true if the word is a common prefix that should not be capitalized, false otherwise
     */
    private static boolean isLowercasePrefix(String word) {
        // List common prefixes that shouldn't be capitalized or need specific handling
        HashSet<String> prefixes = new HashSet<>(Arrays.asList("la", "van", "von"));
        return prefixes.contains(word);
    }
}
