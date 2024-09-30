package com.dineth.debateTracker;

import com.dineth.debateTracker.ballot.Ballot;
import com.dineth.debateTracker.ballot.BallotService;
import com.dineth.debateTracker.breakcategory.BreakCategory;
import com.dineth.debateTracker.breakcategory.BreakCategoryService;
import com.dineth.debateTracker.debate.Debate;
import com.dineth.debateTracker.debate.DebateService;
import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.debater.DebaterService;
import com.dineth.debateTracker.dtos.*;
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
import com.dineth.debateTracker.utils.CustomExceptions;
import com.dineth.debateTracker.utils.ParseCV;
import com.dineth.debateTracker.utils.ParseTabbycatXML;
import com.dineth.debateTracker.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@Slf4j
@RequestMapping("/api/v1/tournament")
public class TournamentBuilder {

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


    @Autowired
    public TournamentBuilder(JudgeService judgeService, TeamService teamService, DebaterService debaterService, DebateService debateService, InstitutionService institutionService, MotionService motionService, TournamentService tournamentService, BreakCategoryService breakCategoryService, BallotService ballotService, RoundService roundService) {
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
    }


    @GetMapping("/build")
    @Transactional
    public Object buildTournament(@RequestParam String fileName) {
        return buildMyTournament("src/main/resources/static/speaksXML/" + fileName);
    }

    @GetMapping("/buildall")
    @Transactional
    public Object buildAllTournaments() {
        ArrayList<String> fileNames = new ArrayList<>();
        fileNames.add("APIIT24.xml");
        fileNames.add("DS24.xml");
        fileNames.add("NMPs2024.xml");
        fileNames.add("SLSDC23.xml");
        fileNames.add("Wickys24.xml");
        fileNames.add("BC24.xml");
        fileNames.add("Henley24.xml");
        fileNames.add("RI2024.xml");
        fileNames.add("Vijis2024.xml");

        for (String fileName : fileNames) {
            buildMyTournament("src/main/resources/static/speaksXML/" + fileName);
        }
        return "Done";


    }

    @GetMapping("/parsecsv")
    public Object parseCSV() {

        List<Debater> debaters = new ParseCV("src/main/resources/static/Debater_Information.csv").parseDebaterInfo();
        for (Debater debater : debaters) {
            try {
                Debater existingDebater = debaterService.checkIfDebaterExists(debater);
                if (existingDebater != null) {
                    log.debug("Debater already exists");
                } else {
                    log.debug("Adding debater");
                    debaterService.addDebater(debater);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return debaters;
    }

    private Object buildMyTournament(String filePath) {
        try {
            ParseTabbycatXML parser = new ParseTabbycatXML(filePath);
            parser.parseXML();

            TournamentDTO tournamentDTO = parser.getTournamentDTO(parser.document);
            List<TeamDTO> teamDTOs = parser.getTeamDTOs(parser.document);
            List<JudgeDTO> judgeDTOs = parser.getJudgeDTOs(parser.document);
            List<InstitutionDTO> institutionDTOs = parser.getInstitutionDTOs(parser.document);
            List<MotionDTO> motionDTOs = parser.getMotionDTOs(parser.document);
            List<RoundDTO> roundsDTOs = parser.getRoundsDTO(parser.document);
            List<BreakCategoryDTO> breakCategoryDTOs = parser.getBreakCategoryDTOs(parser.document);
            HashMap<String, DebaterDTO> debaterDTOMap = new HashMap<>();
            HashMap<String, TeamDTO> teamDTOMap = new HashMap<>();
            HashMap<String, JudgeDTO> judgeDTOMap = new HashMap<>();


            //save debaters, institutions, judges, motions, teams

            try {
                for (InstitutionDTO institutionDTO : institutionDTOs) {
                    Institution institution = new Institution(institutionDTO.name, institutionDTO.reference);
                    institution = institutionService.addInstitution(institution);
                    institutionDTO.dbId = institution.getId();
                }
            } catch (Exception e) {
                log.error("Error in adding institution : " + e.getMessage());
            }

            try {
                for (JudgeDTO judgeDTO : judgeDTOs) {
                    ImmutablePair<String, String> names = StringUtil.splitName(judgeDTO.getName());
                    Judge judge = new Judge(judgeDTO.getScore(), names.getLeft(), names.getRight());
                    Judge existingJudge = judgeService.checkJudgeExists(judge);
                    if (existingJudge == null) {
                        judge = judgeService.addJudge(judge);
                    } else {
                        judge = existingJudge;
                    }
                    judgeDTO.setDbId(judge.getId());
                    judgeDTOMap.put(judgeDTO.getId(), judgeDTO);
                }
            } catch (CustomExceptions.NameSplitException e) {
                log.error("Error in splitting name : " + e.getMessage());
            } catch (Exception e) {
                log.error("Error in adding judge : " + e.getMessage());
            }

            try {
                for (TeamDTO teamDTO : teamDTOs) {
                    List<DebaterDTO> debaterDTOs = teamDTO.getDebaters();
                    List<Debater> debaters = debaterDTOs.stream().map(debaterDTO -> {
                        ImmutablePair<String, String> names = StringUtil.splitName(debaterDTO.getName());
                        Debater debater = new Debater(StringUtil.capitalizeName(names.getLeft()), StringUtil.capitalizeName(names.getRight()));
                        //TODO check for user confirmation
                        Debater temp = debaterService.checkIfDebaterExists(debater);
                        if (temp != null) {
                            debater = temp;
                        } else {
                            debater = debaterService.addDebater(debater);
                        }
                        debaterDTO.setDbId(debater.getId());
                        debaterDTOMap.put(debaterDTO.getId(), debaterDTO);
                        return debater;
                    }).collect(Collectors.toList());
                    Team team = new Team(teamDTO.getName(), teamDTO.getCode(), debaters);
                    team = teamService.addTeam(team);
                    teamDTO.setDbId(team.getId());
                    teamDTOMap.put(teamDTO.getId(), teamDTO);
                }
            } catch (Exception e) {
                log.error("Error in adding team : " + e.getMessage());
            }

            Tournament tournament = new Tournament(tournamentDTO.getFullName(), tournamentDTO.getShortName());
            tournament = tournamentService.addTournament(tournament);

            try {
                for (BreakCategoryDTO breakCategoryDTO : breakCategoryDTOs) {
                    BreakCategory breakCategory = new BreakCategory(breakCategoryDTO.getName());
                    breakCategory = breakCategoryService.addBreakCategory(breakCategory);
                    breakCategoryDTO.setDbId(breakCategory.getId());
                    tournamentService.addBreakCategoryToTournament(tournament.getId(), breakCategory);
                }
            } catch (Exception e) {
                log.error("Error in adding break category : " + e.getMessage());
            }
            try {
                for (MotionDTO motionDTO : motionDTOs) {
                    Motion motion = new Motion(motionDTO.getMotion(), motionDTO.getInfoSlide(), motionDTO.getReference());
                    motion = motionService.addMotion(motion);
                    motionDTO.setDbId(motion.getId());
                    tournamentService.addMotionToTournament(tournament.getId(), motion);
                }
            } catch (Exception e) {
                log.error("Error in adding motion : " + e.getMessage());
            }
            try {
                for (RoundDTO roundDTO : roundsDTOs) {
                    //For now, ignore elimination rounds
                    if (roundDTO.isElimination()) {
                        continue;
                    }

                    Round round = new Round(roundDTO.getName(), null, roundDTO.isElimination());
                    round = roundService.addRound(round);
                    roundDTO.setDbId(round.getId());
                    try {
                        for (DebateDTO debateDTO : roundDTO.getDebates()) {
                            //get the judges for the debate
                            List<Judge> judges = new ArrayList<>();
                            List<String> judgeIds = List.of(debateDTO.getAdjudicatorIds().split(" "));
                            for (String judgeId : judgeIds) {
                                JudgeDTO judgeDTO = judgeDTOMap.get(judgeId);
                                Judge judge = judgeService.findJudgeById(judgeDTO.getDbId());
                                if (judge != null) {
                                    judges.add(judge);
                                }
                            }

                            //get the teams for the debate
                            Team prop = teamService.findTeamById(teamDTOMap.get(debateDTO.getSides().get(0).getTeamId()).getDbId());
                            Team opp = teamService.findTeamById(teamDTOMap.get(debateDTO.getSides().get(1).getTeamId()).getDbId());

                            //Get the motion for the round
                            String motionId = debateDTO.getMotionId();
                            MotionDTO motionDTO = motionDTOs.stream().filter(m -> m.getId().equals(motionId)).findFirst().orElse(null);
                            Motion motion = null;
                            if (motionDTO != null) {
                                motion = motionService.findMotionById(motionDTO.getDbId());
                            }

                            //check how many ballots are there for each side
                            int propBallots = debateDTO.getSides().get(0).getFinalTeamBallots().size();
                            int oppBallots = debateDTO.getSides().get(1).getFinalTeamBallots().size();

                            if (propBallots != oppBallots) {
                                System.out.println("Ballot count mismatch");
                                continue;
                            }
                            //check if ballots are ignored
                            List<String> ignoredBallotAdjIds = new ArrayList<>();
                            List<FinalTeamBallotDTO> adjBallots = debateDTO.getSides().get(0).getFinalTeamBallots();
                            for (FinalTeamBallotDTO adjBallot : adjBallots) {
                                if (adjBallot.isIgnored()) {
                                    ignoredBallotAdjIds.addAll(adjBallot.getAdjudicatorIds());
                                }
                            }


                            try {
                                List<Ballot> ballots = new ArrayList<>();
                                for (SideDTO sideDTO : debateDTO.getSides()) {
                                    for (SpeechDTO speechDTO : sideDTO.getSpeeches()) {
                                        for (IndividualSpeechBallotDTO individualSpeechBallotDTO : speechDTO.getIndividualSpeechBallots()) {
                                            String judgeId = individualSpeechBallotDTO.getAdjudicatorId();
                                            String debaterId = speechDTO.getSpeakerId();
                                            double score = individualSpeechBallotDTO.getScore();

                                            if (ignoredBallotAdjIds.contains(judgeId)) {
                                                continue;
                                            }
                                            Judge judge = judgeService.findJudgeById(judgeDTOMap.get(judgeId).getDbId());
                                            Debater debater = debaterService.findDebaterById(debaterDTOMap.get(debaterId).getDbId());
                                            if (judge != null && debater != null) {
                                                Ballot ballot = new Ballot(judge, debater, (float) score);
                                                ballotService.addBallot(ballot);
                                                ballots.add(ballot);
                                            }
                                        }
                                    }
                                }


                                Debate debate = new Debate(prop, opp, null, ballots, motion);
                                debate = debateService.addDebate(debate);
                                roundService.addDebateToRound(round.getId(), debate);
                            } catch (Exception e) {
                                log.error("Error in adding debate to round : " + e.getMessage());
                            }


                        }
                    } catch (Exception e) {
                        log.error("Error in adding debate to round : " + e.getMessage());
                    }
                    tournamentService.addRoundToTournament(tournament.getId(), round);
                }
            } catch (Exception e) {
                System.out.println("Error in adding round");
                log.error("Error in adding round : " + e.getMessage());
            }

            return tournament;
        } catch (Exception e) {
            log.error("Error in building tournament : " + e.getMessage());
            return null;
        }
    }
}
