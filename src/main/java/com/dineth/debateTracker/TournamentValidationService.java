package com.dineth.debateTracker;

import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.debater.DebaterService;
import com.dineth.debateTracker.dtos.validation.DebaterMatch;
import com.dineth.debateTracker.dtos.validation.Severity;
import com.dineth.debateTracker.dtos.validation.ValidationFinding;
import com.dineth.debateTracker.dtos.validation.ValidationReportDTO;
import com.dineth.debateTracker.dtos.xmlparsing.*;
import com.dineth.debateTracker.institution.Institution;
import com.dineth.debateTracker.institution.InstitutionService;
import com.dineth.debateTracker.team.Team;
import com.dineth.debateTracker.team.TeamService;
import com.dineth.debateTracker.tournament.TournamentService;
import com.dineth.debateTracker.utils.CustomExceptions;
import com.dineth.debateTracker.utils.ParseTabbycatXML;
import com.dineth.debateTracker.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Dry-run validator for uploaded Tabbycat tournament XML. Parses the file and runs the same checks
 * and lookups the importer uses, but is strictly <b>read-only</b> — it never persists anything, so it
 * cannot affect existing data and is safe to call before a real import.
 *
 * <p>Returns a {@link ValidationReportDTO}; it never throws for a bad file (a malformed upload is
 * reported as a finding, not a server error).
 */
@Service
@Slf4j
public class TournamentValidationService {

    private final DebaterService debaterService;
    private final InstitutionService institutionService;
    private final TournamentService tournamentService;
    private final TeamService teamService;

    @Autowired
    public TournamentValidationService(DebaterService debaterService, InstitutionService institutionService,
            TournamentService tournamentService, TeamService teamService) {
        this.debaterService = debaterService;
        this.institutionService = institutionService;
        this.tournamentService = tournamentService;
        this.teamService = teamService;
    }

    @Transactional(readOnly = true)
    public ValidationReportDTO validate(MultipartFile file) {
        List<ValidationFinding> findings = new ArrayList<>();
        Map<String, Integer> summary = new LinkedHashMap<>();

        if (file == null || file.isEmpty()) {
            findings.add(new ValidationFinding(Severity.ERROR, "EMPTY_FILE", "No file was uploaded or the file is empty.", "file"));
            return new ValidationReportDTO(findings, summary);
        }

        ParseTabbycatXML parser;
        try {
            parser = new ParseTabbycatXML(file.getInputStream());
            parser.parseXML();
        } catch (Exception e) {
            findings.add(new ValidationFinding(Severity.ERROR, "PARSE_ERROR", "Could not read the uploaded file: " + e.getMessage(), "file"));
            return new ValidationReportDTO(findings, summary);
        }

        if (parser.document == null) {
            findings.add(new ValidationFinding(Severity.ERROR, "PARSE_ERROR", "The file is not well-formed XML or is not a Tabbycat export.", "file"));
            return new ValidationReportDTO(findings, summary);
        }

        TournamentDTO tournamentDTO = safeExtract(parser::getTournamentDTO, "tournament", findings);
        List<TeamDTO> teamDTOs = safeExtractList(parser::getTeamDTOs, "teams", findings);
        List<JudgeDTO> judgeDTOs = safeExtractList(parser::getJudgeDTOs, "adjudicators", findings);
        List<InstitutionDTO> institutionDTOs = safeExtractList(parser::getInstitutionDTOs, "institutions", findings);
        List<MotionDTO> motionDTOs = safeExtractList(parser::getMotionDTOs, "motions", findings);
        List<RoundDTO> roundDTOs = safeExtractList(parser::getRoundsDTO, "rounds", findings);

        buildSummary(summary, teamDTOs, judgeDTOs, institutionDTOs, motionDTOs, roundDTOs);

        validateTeamsAndDebaters(teamDTOs, institutionDTOs, findings);
        validateJudges(judgeDTOs, findings);
        validateDebates(roundDTOs, findings);
        crossCheckTournament(tournamentDTO, findings);

        return new ValidationReportDTO(findings, summary);
    }

    // ---- Section A: XML-intrinsic checks ----------------------------------------------------------

    private void validateTeamsAndDebaters(List<TeamDTO> teamDTOs, List<InstitutionDTO> institutionDTOs,
            List<ValidationFinding> findings) {
        Set<String> seenDebaterNames = new HashSet<>();
        for (TeamDTO teamDTO : teamDTOs) {
            String teamLabel = "team '" + teamDTO.getName() + "'";
            List<DebaterDTO> debaters = teamDTO.getDebaters();
            if (debaters == null || debaters.isEmpty()) {
                findings.add(new ValidationFinding(Severity.WARNING, "EMPTY_TEAM", "Team has no speakers.", teamLabel));
                continue;
            }
            checkMissingInstitution(teamDTO, debaters, institutionDTOs, findings, teamLabel);
            for (DebaterDTO debaterDTO : debaters) {
                String location = teamLabel + " speaker '" + debaterDTO.getName() + "'";
                checkName(debaterDTO.getName(), location, findings);
                // read-only DB cross-check, once per distinct name
                if (debaterDTO.getName() != null && seenDebaterNames.add(debaterDTO.getName())) {
                    crossCheckDebater(debaterDTO.getName(), location, findings);
                }
            }
        }
    }

    private void checkMissingInstitution(TeamDTO teamDTO, List<DebaterDTO> debaters,
            List<InstitutionDTO> institutionDTOs, List<ValidationFinding> findings, String teamLabel) {
        String institutionId = debaters.get(0).getInstitutionId();
        if (institutionId == null || institutionId.isEmpty()) {
            findings.add(new ValidationFinding(Severity.INFO, "MISSING_INSTITUTION", "Team has no institution; it will not be linked to one.", teamLabel));
            return;
        }
        boolean known = institutionDTOs.stream().anyMatch(inst -> institutionId.equals(inst.getId()));
        if (!known) {
            findings.add(new ValidationFinding(Severity.WARNING, "MISSING_INSTITUTION",
                    "Team references institution id '" + institutionId + "' that is not declared in the file.", teamLabel));
        }
    }

    private void validateJudges(List<JudgeDTO> judgeDTOs, List<ValidationFinding> findings) {
        for (JudgeDTO judgeDTO : judgeDTOs) {
            checkName(judgeDTO.getName(), "adjudicator '" + judgeDTO.getName() + "'", findings);
        }
    }

    private void validateDebates(List<RoundDTO> roundDTOs, List<ValidationFinding> findings) {
        for (RoundDTO roundDTO : roundDTOs) {
            List<DebateDTO> debates = roundDTO.getDebates();
            if (debates == null) {
                continue;
            }
            for (DebateDTO debateDTO : debates) {
                List<SideDTO> sides = debateDTO.getSides();
                if (sides == null || sides.size() < 2) {
                    findings.add(new ValidationFinding(Severity.WARNING, "INCOMPLETE_DEBATE",
                            "Debate does not have two sides; it will be skipped on import.", "round '" + roundDTO.getName() + "'"));
                    continue;
                }
                int propBallots = sides.get(0).getFinalTeamBallots().size();
                int oppBallots = sides.get(1).getFinalTeamBallots().size();
                if (propBallots != oppBallots) {
                    findings.add(new ValidationFinding(Severity.WARNING, "BALLOT_COUNT_MISMATCH",
                            "Ballot count mismatch (prop=" + propBallots + ", opp=" + oppBallots + "); this debate will be skipped on import.",
                            "round '" + roundDTO.getName() + "'"));
                }
            }
        }
    }

    /**
     * Reuses {@link StringUtil#splitName} so name rules stay single-sourced: an empty/null name throws
     * (reported as an error), and a single-token name yields an empty last name (reported as a warning).
     */
    private void checkName(String name, String location, List<ValidationFinding> findings) {
        try {
            ImmutablePair<String, String> names = StringUtil.splitName(name);
            if (names.getRight() == null || names.getRight().isEmpty()) {
                findings.add(new ValidationFinding(Severity.WARNING, "MISSING_LAST_NAME",
                        "Name '" + name + "' has no last name.", location));
            }
        } catch (CustomExceptions.NameSplitException e) {
            findings.add(new ValidationFinding(Severity.ERROR, "MISSING_NAME", "Name is missing or empty.", location));
        }
    }

    // ---- Section B: read-only DB cross-checks -----------------------------------------------------

    private void crossCheckDebater(String name, String location, List<ValidationFinding> findings) {
        ImmutablePair<String, String> names;
        try {
            names = StringUtil.splitName(name);
        } catch (CustomExceptions.NameSplitException e) {
            return; // already reported as MISSING_NAME
        }
        String first = StringUtil.capitalizeName(names.getLeft());
        String last = StringUtil.capitalizeName(names.getRight());
        List<Debater> existing = debaterService.findDebatersByName(first, last);
        if (existing.isEmpty()) {
            return;
        }
        // Attach each existing debater's teams + institution so a human can tell apart speakers who
        // share a name (or whose names are misspelled / missing a last name).
        List<DebaterMatch> matches = existing.stream().map(this::toMatch).collect(Collectors.toList());
        if (existing.size() == 1) {
            findings.add(new ValidationFinding(Severity.INFO, "DEBATER_EXISTS",
                    "A speaker named '" + name + "' already exists (" + describe(matches.get(0))
                            + ") and will be reused, not created.",
                    location, matches));
        } else {
            findings.add(new ValidationFinding(Severity.WARNING, "DEBATER_AMBIGUOUS",
                    "Multiple existing speakers match '" + name + "' — " + summarize(matches)
                            + ". A birthdate is needed to disambiguate on import; compare the teams/institution to identify the right one.",
                    location, matches));
        }
    }

    /** Builds disambiguating context (teams + institution) for an existing debater. */
    private DebaterMatch toMatch(Debater debater) {
        Institution institution = debater.getInstitution();
        String institutionName = institution != null ? institution.getName() : null;
        List<String> teams = teamService.getTeamsByDebater(debater.getId()).stream()
                .map(Team::getTeamName)
                .filter(teamName -> teamName != null && !teamName.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        String storedName = ((debater.getFirstName() != null ? debater.getFirstName() : "") + " "
                + (debater.getLastName() != null ? debater.getLastName() : "")).trim();
        return new DebaterMatch(debater.getId(), storedName, institutionName, teams);
    }

    private String describe(DebaterMatch match) {
        String institution = match.institution() != null ? match.institution() : "no institution";
        String teams = match.teams().isEmpty() ? "no recorded teams" : "teams: " + String.join(", ", match.teams());
        return institution + "; " + teams;
    }

    private String summarize(List<DebaterMatch> matches) {
        return matches.stream()
                .map(match -> "#" + match.debaterId() + " [" + describe(match) + "]")
                .collect(Collectors.joining("; "));
    }

    private void crossCheckTournament(TournamentDTO tournamentDTO, List<ValidationFinding> findings) {
        if (tournamentDTO == null || tournamentDTO.getShortName() == null) {
            return;
        }
        boolean exists = tournamentService.getTournaments().stream()
                .anyMatch(t -> tournamentDTO.getShortName().equals(t.getShortName()));
        if (exists) {
            findings.add(new ValidationFinding(Severity.WARNING, "TOURNAMENT_EXISTS",
                    "A tournament with short name '" + tournamentDTO.getShortName() + "' already exists; importing may create a duplicate.",
                    "tournament '" + tournamentDTO.getShortName() + "'"));
        }
    }

    // ---- helpers ---------------------------------------------------------------------------------

    private void buildSummary(Map<String, Integer> summary, List<TeamDTO> teamDTOs, List<JudgeDTO> judgeDTOs,
            List<InstitutionDTO> institutionDTOs, List<MotionDTO> motionDTOs, List<RoundDTO> roundDTOs) {
        int debaters = teamDTOs.stream().mapToInt(t -> t.getDebaters() != null ? t.getDebaters().size() : 0).sum();
        int debates = roundDTOs.stream().mapToInt(r -> r.getDebates() != null ? r.getDebates().size() : 0).sum();
        summary.put("teams", teamDTOs.size());
        summary.put("debaters", debaters);
        summary.put("judges", judgeDTOs.size());
        summary.put("institutions", institutionDTOs.size());
        summary.put("motions", motionDTOs.size());
        summary.put("rounds", roundDTOs.size());
        summary.put("debates", debates);
    }

    private <T> List<T> safeExtractList(Supplier<List<T>> supplier, String section, List<ValidationFinding> findings) {
        try {
            List<T> result = supplier.get();
            return result != null ? result : List.of();
        } catch (Exception e) {
            findings.add(new ValidationFinding(Severity.ERROR, "PARSE_ERROR",
                    "Failed to parse " + section + ": " + e.getMessage(), section));
            return List.of();
        }
    }

    private <T> T safeExtract(Supplier<T> supplier, String section, List<ValidationFinding> findings) {
        try {
            return supplier.get();
        } catch (Exception e) {
            findings.add(new ValidationFinding(Severity.ERROR, "PARSE_ERROR",
                    "Failed to parse " + section + ": " + e.getMessage(), section));
            return null;
        }
    }
}
