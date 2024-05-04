package com.dineth.debateTracker.utils;

public class CustomExceptions {
    public static class MultipleDebatersFoundException extends RuntimeException {
        public MultipleDebatersFoundException(String message) {
            super(message);
        }
    }
}
