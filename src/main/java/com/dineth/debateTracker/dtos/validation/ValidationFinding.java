package com.dineth.debateTracker.dtos.validation;

/**
 * A single issue discovered while validating an uploaded tournament XML file.
 *
 * @param severity how serious the issue is
 * @param code     a stable machine-readable code (e.g. {@code MISSING_LAST_NAME}) for the UI to switch on
 * @param message  a human-readable description
 * @param location a human-readable pointer to where the issue is (e.g. {@code "team 'Foo' debater 'John'"})
 */
public record ValidationFinding(Severity severity, String code, String message, String location) {
}
