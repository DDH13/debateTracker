package com.dineth.debateTracker.dtos.validation;

import java.util.List;

/**
 * A single issue discovered while validating an uploaded tournament XML file.
 *
 * @param severity how serious the issue is
 * @param code     a stable machine-readable code (e.g. {@code MISSING_LAST_NAME}) for the UI to switch on
 * @param message  a human-readable description
 * @param location a human-readable pointer to where the issue is (e.g. {@code "team 'Foo' debater 'John'"})
 * @param matches  for debater cross-checks, the existing DB debaters this name matched, with the teams
 *                 and institution that disambiguate them; empty for findings that carry no candidates
 */
public record ValidationFinding(Severity severity, String code, String message, String location,
        List<DebaterMatch> matches) {

    /** Convenience constructor for findings that carry no debater candidates. */
    public ValidationFinding(Severity severity, String code, String message, String location) {
        this(severity, code, message, location, List.of());
    }
}
