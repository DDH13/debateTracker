package com.dineth.debateTracker.dtos.validation;

/**
 * Severity of a {@link ValidationFinding}. A report is considered invalid only if it contains
 * at least one {@link #ERROR}.
 */
public enum Severity {
    ERROR,
    WARNING,
    INFO
}
