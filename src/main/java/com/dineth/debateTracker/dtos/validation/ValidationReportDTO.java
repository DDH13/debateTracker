package com.dineth.debateTracker.dtos.validation;

import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Result of a dry-run validation of an uploaded tournament XML file. Produced without persisting
 * anything. {@code valid} is true only when there are no {@link Severity#ERROR} findings.
 */
@Getter
public class ValidationReportDTO {

    private final boolean valid;
    private final int errorCount;
    private final int warningCount;
    private final int infoCount;
    /** Parsed entity counts (teams, debaters, judges, rounds, debates, motions), best-effort. */
    private final Map<String, Integer> summary;
    private final List<ValidationFinding> findings;

    public ValidationReportDTO(List<ValidationFinding> findings, Map<String, Integer> summary) {
        this.findings = findings;
        this.summary = summary;
        this.errorCount = (int) findings.stream().filter(f -> f.severity() == Severity.ERROR).count();
        this.warningCount = (int) findings.stream().filter(f -> f.severity() == Severity.WARNING).count();
        this.infoCount = (int) findings.stream().filter(f -> f.severity() == Severity.INFO).count();
        this.valid = this.errorCount == 0;
    }
}
