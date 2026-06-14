package com.dineth.debateTracker.imports.tabbycat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Jackson bindings for the subset of the Tabbycat REST API (v1.3.0) that {@link ApiTournamentSource}
 * reads. Only the fields the importer needs are mapped; everything else is ignored. Nested records are
 * used purely to namespace these response shapes under one type.
 *
 * <p>Reference: the bundled {@code docs/TabbycatAPI.yaml} OpenAPI document.
 */
public final class Tabbycat {

    private Tabbycat() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Tournament(String name, @JsonProperty("short_name") String shortName, String slug) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Institution(Integer id, String name, String code) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Adjudicator(Integer id, String name, @JsonProperty("base_score") Double baseScore,
                              @JsonProperty("adj_core") Boolean adjCore, Boolean independent) {
    }

    /** A team's speaker as nested in the Team payload (Tabbycat's {@code TeamSpeaker}). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Speaker(Integer id, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Team(Integer id, String institution, @JsonProperty("long_name") String longName,
                       @JsonProperty("short_name") String shortName, @JsonProperty("code_name") String codeName,
                       String reference, @JsonProperty("break_categories") List<String> breakCategories,
                       List<Speaker> speakers) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Motion(Integer id, String text, String reference, @JsonProperty("info_slide") String infoSlide) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BreakCategory(Integer id, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Round(Integer id, Integer seq, String name, String abbreviation, String stage,
                        @JsonProperty("feedback_weight") Double feedbackWeight) {
    }

    /** {@code side} is {@code anyOf} number|string in the schema, so it is bound loosely as Object. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DebateTeam(String team, Object side) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DebateAdjudicator(String chair, List<String> panellists, List<String> trainees) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pairing(Integer id, List<DebateTeam> teams, DebateAdjudicator adjudicators) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Speech(String speaker, Double score, Integer rank, Boolean ghost) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamResult(String team, Object side, Boolean win, Double score, List<Speech> speeches) {
    }

    /** One adjudicator's scoresheet; {@code adjudicator} is null for a consensus (whole-panel) ballot. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Sheet(String adjudicator, List<TeamResult> teams) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(List<Sheet> sheets) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Ballot(Integer id, Result result, String motion, Boolean confirmed, Boolean discarded) {
    }
}
