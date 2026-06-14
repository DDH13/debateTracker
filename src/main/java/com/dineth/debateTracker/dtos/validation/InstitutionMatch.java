package com.dineth.debateTracker.dtos.validation;

/**
 * Identifying context for an existing institution in the DB that an uploaded institution matched.
 * Surfaced on {@code INSTITUTION_MATCH} findings so a human can confirm that an uploaded institution
 * will be reused (the import will link teams to this existing institution rather than create a new
 * one), and catch an unintended merge when the stored name differs from the uploaded spelling.
 *
 * @param institutionId the existing institution's id
 * @param name          the existing institution's stored name
 * @param abbreviation  the existing institution's abbreviation, or {@code null} if none is recorded
 */
public record InstitutionMatch(Long institutionId, String name, String abbreviation) {
}
