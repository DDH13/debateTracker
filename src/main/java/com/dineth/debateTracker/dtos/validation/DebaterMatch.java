package com.dineth.debateTracker.dtos.validation;

import java.util.List;

/**
 * Identifying context for an existing debater in the DB that an uploaded name matched. Surfaced on
 * {@code DEBATER_EXISTS}/{@code DEBATER_AMBIGUOUS} findings so a human can tell apart speakers who
 * share a name (or whose names are misspelled / missing a last name) by the teams and institution
 * they have previously been part of.
 *
 * @param debaterId   the existing debater's id
 * @param name        the existing debater's stored name
 * @param institution the existing debater's institution name, or {@code null} if none is recorded
 * @param teams       the names of teams this debater has been part of (may be empty)
 */
public record DebaterMatch(Long debaterId, String name, String institution, List<String> teams) {
}
