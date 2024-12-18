package com.dineth.debateTracker.utils;

import java.lang.reflect.Field;
import java.util.List;

public class CSVPrinter {

    public static <T> String CsvToString(List<T> objects) {
        if (objects == null || objects.isEmpty()) {
            return "";
        }

        StringBuilder csvString = new StringBuilder();
        Class<?> clazz = objects.get(0).getClass();
        Field[] fields = clazz.getDeclaredFields();

        // Add headers
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            csvString.append(CamelCaseToNormalCase(fields[i].getName()));
            if (i < fields.length - 1) {
                csvString.append(",");
            }
        }
        csvString.append("\n");

        // Add data
        for (T obj : objects) {
            for (int i = 0; i < fields.length; i++) {
                try {
                    Object value = fields[i].get(obj);
                    csvString.append(escapeSpecialCharacters(value != null ? value.toString() : ""));
                } catch (IllegalAccessException e) {
                    csvString.append("ERROR");
                }
                if (i < fields.length - 1) {
                    csvString.append(",");
                }
            }
            csvString.append("\n");
        }

        return csvString.toString();
    }

    private static String escapeSpecialCharacters(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\""); // Escape double quotes
            return "\"" + value + "\"";         // Enclose in double quotes
        }
        return value;
    }
    private static String CamelCaseToNormalCase(String camelCase) {
        StringBuilder sb = new StringBuilder();
        for (char c : camelCase.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append(" ");
            }
            sb.append(c);
        }
//        Make first letter uppercase
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }
}
