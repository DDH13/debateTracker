package com.dineth.debateTracker;

import com.dineth.debateTracker.ballot.Ballot;
import com.dineth.debateTracker.ballot.BallotService;
import com.dineth.debateTracker.breakcategory.BreakCategory;
import com.dineth.debateTracker.breakcategory.BreakCategoryService;
import com.dineth.debateTracker.debate.Debate;
import com.dineth.debateTracker.debate.DebateService;
import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.debater.DebaterService;
import com.dineth.debateTracker.dtos.TournamentDataDTO;
import com.dineth.debateTracker.dtos.xmlparsing.*;
import com.dineth.debateTracker.eliminationballot.EliminationBallot;
import com.dineth.debateTracker.eliminationballot.EliminationBallotService;
import com.dineth.debateTracker.institution.Institution;
import com.dineth.debateTracker.institution.InstitutionService;
import com.dineth.debateTracker.judge.Judge;
import com.dineth.debateTracker.judge.JudgeService;
import com.dineth.debateTracker.motion.Motion;
import com.dineth.debateTracker.motion.MotionService;
import com.dineth.debateTracker.round.Round;
import com.dineth.debateTracker.round.RoundService;
import com.dineth.debateTracker.team.Team;
import com.dineth.debateTracker.team.TeamService;
import com.dineth.debateTracker.tournament.Tournament;
import com.dineth.debateTracker.tournament.TournamentService;
import com.dineth.debateTracker.utils.ParseTabbycatXML;
import com.dineth.debateTracker.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dineth.debateTracker.utils.StringUtil.normalizeName;

/**
 * Imports a Tabbycat XML export into the database. Parses the XML into DTOs, persists every
 * entity (institutions, judges, teams/debaters, break categories, motions, rounds, debates,
 * ballots), and returns a summary {@link TournamentDataDTO}.
 *
 * <p>The whole import runs in a single transaction: any failure rolls back the entire tournament
 * so partial imports never reach the database.
 */
@Service
@Slf4j
public class TournamentImportService {

    private final JudgeService judgeService;
    private final TeamService teamService;
    private final DebaterService debaterService;
    private final DebateService debateService;
    private final InstitutionService institutionService;
    private final MotionService motionService;
    private final TournamentService tournamentService;
    private final BreakCategoryService breakCategoryService;
    private final BallotService ballotService;
    private final RoundService roundService;
    private final EliminationBallotService eliminationBallotService;

    @Autowired
    public TournamentImportService(JudgeService judgeService, TeamService teamService, DebaterService debaterService,
            DebateService debateService, InstitutionService institutionService, MotionService motionService,
            TournamentService tournamentService, BreakCategoryService breakCategoryService, BallotService ballotService,
            RoundService roundService, EliminationBallotService eliminationBallotService) {
        this.judgeService = judgeService;
        this.teamService = teamService;
        this.debaterService = debaterService;
        this.debateService = debateService;
        this.institutionService = institutionService;
        this.motionService = motionService;
        this.tournamentService = tournamentService;
        this.breakCategoryService = breakCategoryService;
        this.ballotService = ballotService;
        this.roundService = roundService;
        this.eliminationBallotService = eliminationBallotService;
    }

    /**
     * In-memory caches of persisted entities keyed by their database id, built once before the
     * rounds loop so debate processing never queries the database per debate.
     */
    private record LookupCaches(Map<Long, Judge> judges, Map<Long, Team> teams,
                                Map<Long, Motion> motions, Map<Long, Debater> debaters) {
    }

    @Transactional
    public TournamentDataDTO importTournament(String filePath) {
        try {
            ParseTabbycatXML parser = new ParseTabbycatXML(filePath);
            parser.parseXML();

            TournamentDTO tournamentDTO = parser.getTournamentDTO();
            List<TeamDTO> teamDTOs = parser.getTeamDTOs();
            List<JudgeDTO> judgeDTOs = parser.getJudgeDTOs();
            List<InstitutionDTO> institutionDTOs = parser.getInstitutionDTOs();
            List<MotionDTO> motionDTOs = parser.getMotionDTOs();
            List<RoundDTO> roundDTOs = parser.getRoundsDTO();
            List<BreakCategoryDTO> breakCategoryDTOs = parser.getBreakCategoryDTOs();

            // XML-id -> DTO maps, populated as entities are persisted and used to resolve references
            Map<String, DebaterDTO> debaterDTOMap = new HashMap<>();
            Map<String, TeamDTO> teamDTOMap = new HashMap<>();
            Map<String, JudgeDTO> judgeDTOMap = new HashMap<>();

            saveInstitutions(institutionDTOs);
            saveJudges(judgeDTOs, judgeDTOMap);
            saveTeams(teamDTOs, institutionDTOs, debaterDTOMap, teamDTOMap);

            Tournament tournament = tournamentService.addTournament(
                    new Tournament(tournamentDTO.getFullName(), tournamentDTO.getShortName()));
            saveBreakCategories(breakCategoryDTOs, tournament);
            saveMotions(motionDTOs, tournament);

            LookupCaches caches = buildLookupCaches(judgeDTOMap, teamDTOMap, motionDTOs, debaterDTOMap);
            Map<String, MotionDTO> motionDTOMap = motionDTOs.stream()
                    .collect(Collectors.toMap(MotionDTO::getId, m -> m, (a, b) -> a));

            for (RoundDTO roundDTO : roundDTOs) {
                processRound(roundDTO, tournament, judgeDTOMap, teamDTOMap, motionDTOMap, debaterDTOMap, caches);
            }

            return new TournamentDataDTO(tournamentDTO, new ArrayList<>(debaterDTOMap.values()),
                    teamDTOs, judgeDTOs, institutionDTOs, motionDTOs, breakCategoryDTOs, roundDTOs);
        } catch (Exception e) {
            log.error("Error in building tournament : " + e.getMessage(), e);
            throw new RuntimeException("Tournament build failed: " + e.getMessage(), e);
        }
    }

    private void saveInstitutions(List<InstitutionDTO> institutionDTOs) {
        for (InstitutionDTO institutionDTO : institutionDTOs) {
            Institution existing = institutionService.findInstitutionByName(institutionDTO.getName().strip());
            if (existing == null) {
                log.debug("Adding institution : " + institutionDTO.getName());
                Institution institution = institutionService.addInstitution(
                        new Institution(institutionDTO.getName().strip(), institutionDTO.getReference()));
                institutionDTO.setDbId(institution.getId());
            } else {
                log.debug("Institution already exists : " + institutionDTO.getName());
                institutionDTO.setDbId(existing.getId());
            }
        }
    }

    private void saveJudges(List<JudgeDTO> judgeDTOs, Map<String, JudgeDTO> judgeDTOMap) {
        for (JudgeDTO judgeDTO : judgeDTOs) {
            ImmutablePair<String, String> names = StringUtil.splitName(judgeDTO.getName());
            Judge judge = new Judge(judgeDTO.getScore(), names.getLeft(), names.getRight());
            Judge existingJudge = judgeService.checkJudgeExists(judge);
            judge = existingJudge != null ? existingJudge : judgeService.addJudge(judge);
            judgeDTO.setDbId(judge.getId());
            judgeDTOMap.put(judgeDTO.getId(), judgeDTO);
        }
    }

    private void saveTeams(List<TeamDTO> teamDTOs, List<InstitutionDTO> institutionDTOs,
            Map<String, DebaterDTO> debaterDTOMap, Map<String, TeamDTO> teamDTOMap) {
        for (TeamDTO teamDTO : teamDTOs) {
            List<DebaterDTO> debaterDTOs = teamDTO.getDebaters();
            List<Debater> debaters = saveDebaters(debaterDTOs, debaterDTOMap);
            Team team = teamService.addTeam(new Team(teamDTO.getName(), teamDTO.getCode(), debaters));
            teamDTO.setDbId(team.getId());
            teamDTOMap.put(teamDTO.getId(), teamDTO);
            linkTeamToInstitution(teamDTO, debaterDTOs, institutionDTOs, team);
        }
    }

    private List<Debater> saveDebaters(List<DebaterDTO> debaterDTOs, Map<String, DebaterDTO> debaterDTOMap) {
        List<Debater> debaters = new ArrayList<>();
        for (DebaterDTO debaterDTO : debaterDTOs) {
            ImmutablePair<String, String> names = StringUtil.splitName(debaterDTO.getName());
            Debater debater = new Debater(StringUtil.capitalizeName(names.getLeft()),
                    StringUtil.capitalizeName(names.getRight()));
            Debater existing = debaterService.checkIfDebaterExists(debater);
            debater = existing != null ? existing : debaterService.addDebater(debater);
            debaterDTO.setDbId(debater.getId());
            debaterDTOMap.put(debaterDTO.getId(), debaterDTO);
            debaters.add(debater);
        }
        return debaters;
    }

    private void linkTeamToInstitution(TeamDTO teamDTO, List<DebaterDTO> debaterDTOs,
            List<InstitutionDTO> institutionDTOs, Team team) {
        if (debaterDTOs.isEmpty()) {
            log.error("No debaters found for team : " + teamDTO.getName());
            return;
        }
        String institutionId = debaterDTOs.get(0).getInstitutionId();
        if (institutionId == null || institutionId.isEmpty()) {
            log.debug("No institution ID found for team : " + teamDTO.getName());
            return;
        }
        Institution institution = institutionDTOs.stream()
                .filter(inst -> inst.getId().equals(institutionId)).findFirst()
                .map(inst -> institutionService.findInstitutionById(inst.getDbId())).orElse(null);
        if (institution != null) {
            institutionService.addTeamToInstitution(institution.getId(), team);
        } else {
            log.debug("Institution not found for team : " + teamDTO.getName());
        }
    }

    private void saveBreakCategories(List<BreakCategoryDTO> breakCategoryDTOs, Tournament tournament) {
        for (BreakCategoryDTO breakCategoryDTO : breakCategoryDTOs) {
            BreakCategory breakCategory = breakCategoryService.addBreakCategory(
                    new BreakCategory(breakCategoryDTO.getName()));
            breakCategoryDTO.setDbId(breakCategory.getId());
            tournamentService.addBreakCategoryToTournament(tournament.getId(), breakCategory);
        }
    }

    private void saveMotions(List<MotionDTO> motionDTOs, Tournament tournament) {
        for (MotionDTO motionDTO : motionDTOs) {
            Motion motion = motionService.addMotion(new Motion(normalizeName(motionDTO.getMotion()),
                    normalizeName(motionDTO.getInfoSlide()), motionDTO.getReference()));
            motionDTO.setDbId(motion.getId());
            tournamentService.addMotionToTournament(tournament.getId(), motion);
        }
    }

    private LookupCaches buildLookupCaches(Map<String, JudgeDTO> judgeDTOMap, Map<String, TeamDTO> teamDTOMap,
            List<MotionDTO> motionDTOs, Map<String, DebaterDTO> debaterDTOMap) {
        Map<Long, Judge> judges = judgeService.findAllJudgesByIds(dbIds(judgeDTOMap.values(), JudgeDTO::getDbId))
                .stream().collect(Collectors.toMap(Judge::getId, j -> j));
        Map<Long, Team> teams = teamService.findAllTeamsByIds(dbIds(teamDTOMap.values(), TeamDTO::getDbId))
                .stream().collect(Collectors.toMap(Team::getId, t -> t));
        Map<Long, Motion> motions = motionService.findAllMotionsByIds(dbIds(motionDTOs, MotionDTO::getDbId))
                .stream().collect(Collectors.toMap(Motion::getId, m -> m));
        Map<Long, Debater> debaters = debaterService.findAllDebatersByIds(dbIds(debaterDTOMap.values(), DebaterDTO::getDbId))
                .stream().collect(Collectors.toMap(Debater::getId, d -> d));
        return new LookupCaches(judges, teams, motions, debaters);
    }

    private static <T> Set<Long> dbIds(Collection<T> dtos, Function<T, Long> idGetter) {
        return dtos.stream().map(idGetter).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private void processRound(RoundDTO roundDTO, Tournament tournament,
            Map<String, JudgeDTO> judgeDTOMap, Map<String, TeamDTO> teamDTOMap,
            Map<String, MotionDTO> motionDTOMap, Map<String, DebaterDTO> debaterDTOMap, LookupCaches caches)
            throws Exception {
        Round round = roundService.addRound(new Round(roundDTO.getName(), null, roundDTO.isElimination()));
        roundDTO.setDbId(round.getId());

        for (DebateDTO debateDTO : roundDTO.getDebates()) {
            Debate debate = buildDebate(debateDTO, roundDTO, judgeDTOMap, teamDTOMap, motionDTOMap, debaterDTOMap, caches);
            if (debate == null) {
                continue;
            }
            debate = debateService.addDebate(debate);
            roundService.addDebateToRound(round.getId(), debate);
        }
        tournamentService.addRoundToTournament(tournament.getId(), round);
    }

    /**
     * Build a single debate (persisting its ballots as a side effect), or return {@code null} if the
     * debate must be skipped (unresolved teams or a ballot-count mismatch).
     */
    private Debate buildDebate(DebateDTO debateDTO, RoundDTO roundDTO,
            Map<String, JudgeDTO> judgeDTOMap, Map<String, TeamDTO> teamDTOMap,
            Map<String, MotionDTO> motionDTOMap, Map<String, DebaterDTO> debaterDTOMap, LookupCaches caches) {
        Team prop = caches.teams().get(teamDTOMap.get(debateDTO.getSides().get(0).getTeamId()).getDbId());
        Team opp = caches.teams().get(teamDTOMap.get(debateDTO.getSides().get(1).getTeamId()).getDbId());
        if (prop == null || opp == null) {
            log.error("Skipping debate in round '{}': could not resolve proposition ({}) or opposition ({}) team",
                    roundDTO.getName(),
                    debateDTO.getSides().get(0).getTeamId(),
                    debateDTO.getSides().get(1).getTeamId());
            return null;
        }

        MotionDTO motionDTO = motionDTOMap.get(debateDTO.getMotionId());
        Motion motion = motionDTO != null ? caches.motions().get(motionDTO.getDbId()) : null;

        int propBallots = debateDTO.getSides().get(0).getFinalTeamBallots().size();
        int oppBallots = debateDTO.getSides().get(1).getFinalTeamBallots().size();
        if (propBallots != oppBallots) {
            log.error("Skipping debate in round '{}': ballot count mismatch (prop={}, opp={})",
                    roundDTO.getName(), propBallots, oppBallots);
            return null;
        }

        if (roundDTO.isElimination()) {
            List<EliminationBallot> eliminationBallots =
                    buildEliminationBallots(debateDTO, teamDTOMap, judgeDTOMap, caches, prop, opp);
            Debate debate = new Debate(prop, opp, null, null, motion);
            debate.setEliminationBallots(eliminationBallots);
            debate.setWinner(determineEliminationWinner(eliminationBallots, prop, opp));
            return debate;
        }

        List<Ballot> ballots = buildBallots(debateDTO, judgeDTOMap, debaterDTOMap, caches);
        Debate debate = new Debate(prop, opp, null, ballots, motion);
        debate.setWinner(determinePrelimWinner(debateDTO, prop, opp));
        return debate;
    }

    private List<EliminationBallot> buildEliminationBallots(DebateDTO debateDTO,
            Map<String, TeamDTO> teamDTOMap, Map<String, JudgeDTO> judgeDTOMap, LookupCaches caches,
            Team prop, Team opp) {
        List<EliminationBallot> eliminationBallots = new ArrayList<>();
        for (SideDTO sideDTO : debateDTO.getSides()) {
            Team currentTeam = caches.teams().get(teamDTOMap.get(sideDTO.getTeamId()).getDbId());
            for (FinalTeamBallotDTO finalTeamBallotDTO : sideDTO.getFinalTeamBallots()) {
                String judgeId = finalTeamBallotDTO.getAdjudicatorIds().get(0);
                Judge judge = caches.judges().get(judgeDTOMap.get(judgeId).getDbId());
                if (finalTeamBallotDTO.getRank() == 1 && currentTeam.getId().equals(prop.getId())) {
                    eliminationBallots.add(new EliminationBallot(judge, prop, opp));
                } else if (finalTeamBallotDTO.getRank() == 1 && currentTeam.getId().equals(opp.getId())) {
                    eliminationBallots.add(new EliminationBallot(judge, opp, prop));
                }
            }
        }
        for (EliminationBallot eliminationBallot : eliminationBallots) {
            eliminationBallotService.addEliminationBallot(eliminationBallot);
        }
        return eliminationBallots;
    }

    private List<Ballot> buildBallots(DebateDTO debateDTO, Map<String, JudgeDTO> judgeDTOMap,
            Map<String, DebaterDTO> debaterDTOMap, LookupCaches caches) {
        List<String> ignoredBallotAdjIds = new ArrayList<>();
        for (FinalTeamBallotDTO adjBallot : debateDTO.getSides().get(0).getFinalTeamBallots()) {
            if (adjBallot.isIgnored()) {
                ignoredBallotAdjIds.addAll(adjBallot.getAdjudicatorIds());
            }
        }

        List<Ballot> ballots = new ArrayList<>();
        for (SideDTO sideDTO : debateDTO.getSides()) {
            for (SpeechDTO speechDTO : sideDTO.getSpeeches()) {
                for (IndividualSpeechBallotDTO speechBallot : speechDTO.getIndividualSpeechBallots()) {
                    String judgeId = speechBallot.getAdjudicatorId();
                    if (ignoredBallotAdjIds.contains(judgeId)) {
                        continue;
                    }
                    // judgeId may contain multiple adjudicators, take the first one (chair)
                    judgeId = judgeId.split(" ")[0];
                    Judge judge = caches.judges().get(judgeDTOMap.get(judgeId).getDbId());
                    Debater debater = caches.debaters().get(debaterDTOMap.get(speechDTO.getSpeakerId()).getDbId());
                    if (judge != null && debater != null) {
                        Ballot ballot = new Ballot(judge, debater, (float) speechBallot.getScore(),
                                speechDTO.getSpeakerPosition());
                        ballotService.addBallot(ballot);
                        ballots.add(ballot);
                    }
                }
            }
        }
        return ballots;
    }

    private Team determineEliminationWinner(List<EliminationBallot> eliminationBallots, Team prop, Team opp) {
        if (eliminationBallots.size() == 1) {
            return eliminationBallots.get(0).getWinner();
        }
        long propWins = eliminationBallots.stream().filter(b -> b.getWinner().equals(prop)).count();
        long oppWins = eliminationBallots.size() - propWins;
        return propWins > oppWins ? prop : opp;
    }

    private Team determinePrelimWinner(DebateDTO debateDTO, Team prop, Team opp) {
        long propWins = debateDTO.getSides().get(0).getFinalTeamBallots().stream()
                .filter(b -> b.getRank() == 1).count();
        long oppWins = debateDTO.getSides().get(1).getFinalTeamBallots().stream()
                .filter(b -> b.getRank() == 1).count();
        if (propWins > oppWins) {
            return prop;
        }
        if (propWins < oppWins) {
            return opp;
        }
        return null;
    }
}
