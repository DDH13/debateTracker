package com.dineth.debateTracker.imports;

import com.dineth.debateTracker.dtos.xmlparsing.BreakCategoryDTO;
import com.dineth.debateTracker.dtos.xmlparsing.DebateDTO;
import com.dineth.debateTracker.dtos.xmlparsing.DebaterDTO;
import com.dineth.debateTracker.dtos.xmlparsing.FinalTeamBallotDTO;
import com.dineth.debateTracker.dtos.xmlparsing.IndividualSpeechBallotDTO;
import com.dineth.debateTracker.dtos.xmlparsing.InstitutionDTO;
import com.dineth.debateTracker.dtos.xmlparsing.JudgeDTO;
import com.dineth.debateTracker.dtos.xmlparsing.MotionDTO;
import com.dineth.debateTracker.dtos.xmlparsing.RoundDTO;
import com.dineth.debateTracker.dtos.xmlparsing.SideDTO;
import com.dineth.debateTracker.dtos.xmlparsing.SpeechDTO;
import com.dineth.debateTracker.dtos.xmlparsing.TeamDTO;
import com.dineth.debateTracker.dtos.xmlparsing.TournamentDTO;
import com.dineth.debateTracker.imports.tabbycat.Tabbycat;
import com.dineth.debateTracker.imports.tabbycat.TabbycatApiClient;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A {@link TournamentSource} that fetches a tournament from the Tabbycat REST API and maps it into the
 * same {@code dtos/xmlparsing} DTOs the importer consumes — so the existing
 * {@link com.dineth.debateTracker.TournamentImportService} pipeline runs unchanged.
 *
 * <p>Mapping notes (where the API model differs from the XML export):
 * <ul>
 *   <li><b>IDs:</b> the API uses integer PKs / hyperlinked URIs; the DTOs and the importer's lookup
 *       maps are keyed by <em>string</em> ids. PKs are stringified and URIs reduced to their trailing
 *       id so every cross-reference (team/judge/debater/motion/institution) stays consistent.</li>
 *   <li><b>Confirmed results only:</b> ballots are fetched with {@code ?confirmed=true} and any
 *       {@code discarded} ballot is dropped, mirroring the XML export which only contains the
 *       confirmed result.</li>
 *   <li><b>Sheets → speeches:</b> the API nests speeches per adjudicator scoresheet; the XML (and the
 *       importer) expect speeches at the side level with one ballot per adjudicator. This class
 *       inverts that: it groups by speaker position and attaches one
 *       {@link IndividualSpeechBallotDTO} per sheet.</li>
 *   <li><b>Consensus ballots:</b> a sheet with no adjudicator (whole-panel consensus) is attributed to
 *       the debate chair.</li>
 * </ul>
 */
@Slf4j
public class ApiTournamentSource implements TournamentSource {

    private final TabbycatApiClient client;
    private final String slug;

    public ApiTournamentSource(TabbycatApiClient client, String slug) {
        this.client = client;
        this.slug = slug;
    }

    @Override
    public TournamentImportData load() {
        Tabbycat.Tournament tournament = client.getTournament(slug);
        List<Tabbycat.Institution> institutions = client.listInstitutions(slug);
        List<Tabbycat.Adjudicator> adjudicators = client.listAdjudicators(slug);
        List<Tabbycat.Team> teams = client.listTeams(slug);
        List<Tabbycat.Motion> motions = client.listMotions(slug);
        List<Tabbycat.BreakCategory> breakCategories = client.listBreakCategories(slug);
        List<Tabbycat.Round> rounds = client.listRounds(slug);

        TournamentDTO tournamentDTO = new TournamentDTO(tournament.name(), tournament.shortName());

        List<InstitutionDTO> institutionDTOs = institutions.stream()
                .map(i -> new InstitutionDTO(str(i.id()), i.name(), i.code()))
                .collect(Collectors.toCollection(ArrayList::new));

        List<JudgeDTO> judgeDTOs = adjudicators.stream()
                .map(a -> new JudgeDTO(str(a.id()), a.name(),
                        a.baseScore() != null ? a.baseScore().floatValue() : 0f,
                        Boolean.TRUE.equals(a.adjCore()), Boolean.TRUE.equals(a.independent())))
                .collect(Collectors.toCollection(ArrayList::new));

        List<TeamDTO> teamDTOs = teams.stream()
                .map(this::toTeamDTO)
                .collect(Collectors.toCollection(ArrayList::new));

        List<MotionDTO> motionDTOs = motions.stream()
                .map(m -> new MotionDTO(str(m.id()), m.text(), m.infoSlide(), m.reference()))
                .collect(Collectors.toCollection(ArrayList::new));

        List<BreakCategoryDTO> breakCategoryDTOs = breakCategories.stream()
                .map(b -> new BreakCategoryDTO(str(b.id()), b.name()))
                .collect(Collectors.toCollection(ArrayList::new));

        List<RoundDTO> roundDTOs = rounds.stream()
                .map(this::toRoundDTO)
                .collect(Collectors.toCollection(ArrayList::new));

        return new TournamentImportData(tournamentDTO, institutionDTOs, judgeDTOs, teamDTOs,
                motionDTOs, roundDTOs, breakCategoryDTOs);
    }

    private TeamDTO toTeamDTO(Tabbycat.Team team) {
        String institutionId = idFromUri(team.institution());
        List<DebaterDTO> debaters = (team.speakers() != null ? team.speakers() : List.<Tabbycat.Speaker>of())
                .stream()
                .map(s -> new DebaterDTO(str(s.id()), s.name(), institutionId != null ? institutionId : "", ""))
                .collect(Collectors.toCollection(ArrayList::new));
        String breakEligibilities = (team.breakCategories() != null ? team.breakCategories() : List.<String>of())
                .stream().map(this::idFromUri).filter(Objects::nonNull).collect(Collectors.joining(" "));
        String name = firstNonBlank(team.longName(), team.shortName(), team.reference());
        String code = firstNonBlank(team.codeName(), team.shortName(), name);
        return new TeamDTO(str(team.id()), name, code, debaters, breakEligibilities);
    }

    private RoundDTO toRoundDTO(Tabbycat.Round round) {
        boolean elimination = "E".equalsIgnoreCase(round.stage());
        double feedbackWeight = round.feedbackWeight() != null ? round.feedbackWeight() : 1.0;
        List<DebateDTO> debates = new ArrayList<>();
        if (round.seq() != null) {
            for (Tabbycat.Pairing pairing : client.listPairings(slug, round.seq())) {
                DebateDTO debate = toDebateDTO(pairing, round.seq());
                if (debate != null) {
                    debates.add(debate);
                }
            }
        }
        return new RoundDTO(round.name(), round.abbreviation(), elimination, feedbackWeight, debates);
    }

    private DebateDTO toDebateDTO(Tabbycat.Pairing pairing, int roundSeq) {
        List<Tabbycat.DebateTeam> orderedTeams = orderSides(pairing.teams());
        if (orderedTeams.size() < 2) {
            // A bye / single-team slot — the importer assumes a two-sided debate, so skip it (the XML
            // export likewise omits such debates from the rounds it emits).
            log.debug("Skipping pairing {} in round {}: fewer than two sides", pairing.id(), roundSeq);
            return null;
        }

        String chairId = pairing.adjudicators() != null ? idFromUri(pairing.adjudicators().chair()) : null;
        String adjudicatorIds = String.join(" ", panelAdjudicatorIds(pairing.adjudicators()));

        List<Tabbycat.Sheet> sheets = new ArrayList<>();
        String motionUri = null;
        for (Tabbycat.Ballot ballot : client.listConfirmedBallots(slug, roundSeq, pairing.id())) {
            if (Boolean.TRUE.equals(ballot.discarded())) {
                continue;
            }
            if (Boolean.FALSE.equals(ballot.confirmed())) {
                continue;
            }
            if (ballot.result() != null && ballot.result().sheets() != null) {
                sheets.addAll(ballot.result().sheets());
            }
            if (motionUri == null) {
                motionUri = ballot.motion();
            }
        }

        List<SideDTO> sides = new ArrayList<>();
        for (Tabbycat.DebateTeam debateTeam : orderedTeams) {
            String teamId = idFromUri(debateTeam.team());
            sides.add(buildSide(teamId, sheets, chairId));
        }
        return new DebateDTO(str(pairing.id()), adjudicatorIds, chairId, "", idFromUri(motionUri), sides);
    }

    /**
     * Build one side from the confirmed scoresheets: a {@link FinalTeamBallotDTO} per sheet (rank 1 if
     * that adjudicator awarded the win, else 2) and speeches inverted from per-sheet to per-position,
     * each carrying one {@link IndividualSpeechBallotDTO} per sheet.
     */
    private SideDTO buildSide(String teamId, List<Tabbycat.Sheet> sheets, String chairId) {
        List<FinalTeamBallotDTO> finalTeamBallots = new ArrayList<>();
        Map<Integer, SpeechDTO> speechesByPosition = new LinkedHashMap<>();

        for (Tabbycat.Sheet sheet : sheets) {
            String adjId = sheet.adjudicator() != null ? idFromUri(sheet.adjudicator()) : chairId;
            if (adjId == null) {
                continue;
            }
            Tabbycat.TeamResult result = findTeamResult(sheet, teamId);
            if (result == null) {
                continue;
            }
            int rank = Boolean.TRUE.equals(result.win()) ? 1 : 2;
            double score = result.score() != null ? result.score() : 0d;
            finalTeamBallots.add(new FinalTeamBallotDTO(List.of(adjId), false, false, rank, score));

            List<Tabbycat.Speech> speeches = result.speeches() != null ? result.speeches() : List.of();
            for (int i = 0; i < speeches.size(); i++) {
                Tabbycat.Speech speech = speeches.get(i);
                int position = i + 1;
                SpeechDTO speechDTO = speechesByPosition.computeIfAbsent(position,
                        p -> new SpeechDTO(idFromUri(speech.speaker()), false, new ArrayList<>(), p));
                double speechScore = speech.score() != null ? speech.score() : 0d;
                speechDTO.getIndividualSpeechBallots().add(new IndividualSpeechBallotDTO(adjId, speechScore));
            }
        }
        return new SideDTO(teamId, finalTeamBallots, new ArrayList<>(speechesByPosition.values()));
    }

    private Tabbycat.TeamResult findTeamResult(Tabbycat.Sheet sheet, String teamId) {
        if (sheet.teams() == null || teamId == null) {
            return null;
        }
        return sheet.teams().stream()
                .filter(tr -> teamId.equals(idFromUri(tr.team())))
                .findFirst()
                .orElse(null);
    }

    private List<String> panelAdjudicatorIds(Tabbycat.DebateAdjudicator adjudicators) {
        List<String> ids = new ArrayList<>();
        if (adjudicators == null) {
            return ids;
        }
        String chair = idFromUri(adjudicators.chair());
        if (chair != null) {
            ids.add(chair);
        }
        if (adjudicators.panellists() != null) {
            adjudicators.panellists().stream().map(this::idFromUri).filter(Objects::nonNull).forEach(ids::add);
        }
        return ids;
    }

    /** Order sides so the proposition/affirmative comes first, matching the importer's prop=0, opp=1. */
    private List<Tabbycat.DebateTeam> orderSides(List<Tabbycat.DebateTeam> teams) {
        if (teams == null) {
            return List.of();
        }
        List<Tabbycat.DebateTeam> copy = new ArrayList<>(teams);
        copy.sort(Comparator.comparingInt(t -> sideOrder(t.side())));
        return copy;
    }

    private int sideOrder(Object side) {
        if (side == null) {
            return 99;
        }
        String s = String.valueOf(side).toLowerCase();
        return switch (s) {
            case "aff", "cg", "0", "0.0" -> 0;
            case "neg", "co", "1", "1.0" -> 1;
            default -> {
                try {
                    yield (int) Double.parseDouble(s);
                } catch (NumberFormatException e) {
                    yield 50;
                }
            }
        };
    }

    /** Reduce a hyperlinked resource URI (e.g. {@code .../adjudicators/12/}) to its trailing id. */
    private String idFromUri(String uri) {
        if (uri == null || uri.isBlank()) {
            return null;
        }
        String trimmed = uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;
        int slash = trimmed.lastIndexOf('/');
        return slash >= 0 ? trimmed.substring(slash + 1) : trimmed;
    }

    private static String str(Integer value) {
        return value != null ? String.valueOf(value) : null;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
