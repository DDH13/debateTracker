package com.dineth.debateTracker.utils;

public class CustomExceptions {
    public static class MultipleDebatersFoundException extends RuntimeException {
        public MultipleDebatersFoundException(String message) {
            super(message);
        }
    }
    public static class NameSplitException extends RuntimeException {
        public NameSplitException(String message) {
            super(message);
        }
    }
}
