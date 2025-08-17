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
import com.dineth.debateTracker.feedback.Feedback;
import com.dineth.debateTracker.feedback.FeedbackService;
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
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@Slf4j
@CrossOrigin(origins = "*")
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
    private final FeedbackService feedbackService;
    private final EliminationBallotService eliminationBallotService;


    @Autowired
    public TournamentBuilder(JudgeService judgeService, TeamService teamService, DebaterService debaterService, DebateService debateService, InstitutionService institutionService, MotionService motionService, TournamentService tournamentService, BreakCategoryService breakCategoryService, BallotService ballotService, RoundService roundService, FeedbackService feedbackService, EliminationBallotService eliminationBallotService) {
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
        this.feedbackService = feedbackService;
        this.eliminationBallotService = eliminationBallotService;
    }


    @GetMapping("/build")
    @Transactional
    public TournamentDataDTO buildTournament(@RequestParam String fileName) {
        return buildMyTournament("src/main/resources/static/speaksXML/2024/" + fileName);
    }

    @GetMapping("/buildall")
    @Transactional
    public List<TournamentDataDTO> buildAllTournaments() {
        List<String> fileNames = new ArrayList<>();
        List<TournamentDataDTO> tournamentDataList = new ArrayList<>();
        
        try {
            Path folderPath = Paths.get("src/main/resources/static/speaksXML/2024/");
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath, "*.xml")) {
                for (Path entry : stream) {
                    fileNames.add(entry.getFileName().toString());
                }
            }
        } catch (IOException e) {
            log.error("Error in reading files : " + e.getMessage());
            return tournamentDataList; // Return empty list on error
        }

        for (String fileName : fileNames) {
            try {
                TournamentDataDTO tournamentData = buildMyTournament("src/main/resources/static/speaksXML/2024/" + fileName);
                if (tournamentData != null) {
                    tournamentDataList.add(tournamentData);
                }
                log.info("Built tournament : " + fileName);
            } catch (Exception e) {
                log.error("Error building tournament " + fileName + ": " + e.getMessage());
            }
        }
        return tournamentDataList;
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

    private TournamentDataDTO buildMyTournament(String filePath) {
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
                    //  check if institution exists
                    Institution tempInstitution = institutionService.findInstitutionByName(institutionDTO.name.strip());
                    if (tempInstitution == null) {
                        log.debug("Adding institution : " + institutionDTO.name);
                        Institution institution = new Institution(institutionDTO.name.strip(), institutionDTO.reference);
                        institution = institutionService.addInstitution(institution);
                        institutionDTO.dbId = institution.getId();
                    } else {
                        log.debug("Institution already exists : " + institutionDTO.name);
                        institutionDTO.dbId = tempInstitution.getId();
                    }
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
                            // check if elimination debate
                            List<EliminationBallot> eliminationBallots = new ArrayList<>();
                            if (roundDTO.isElimination()) {
                                try {
                                    Team tempTeam1 = teamService.findTeamById(teamDTOMap.get(debateDTO.getSides().get(0).getTeamId()).getDbId());
                                    Team tempTeam2 = teamService.findTeamById(teamDTOMap.get(debateDTO.getSides().get(1).getTeamId()).getDbId());
                                    for (SideDTO sideDTO : debateDTO.getSides()) {
                                        Team currentTeam = teamService.findTeamById(teamDTOMap.get(sideDTO.getTeamId()).getDbId());
                                        for (FinalTeamBallotDTO finalTeamBallotDTO : sideDTO.getFinalTeamBallots()) {
                                            String judgeId = finalTeamBallotDTO.getAdjudicatorIds().get(0);
                                            Judge judge = judgeService.findJudgeById(judgeDTOMap.get(judgeId).getDbId());
                                            if (finalTeamBallotDTO.getRank() == 1 && currentTeam.getId().equals(tempTeam1.getId())) {
                                                eliminationBallots.add(new EliminationBallot(judge, tempTeam1, tempTeam2));
                                            } else if (finalTeamBallotDTO.getRank() == 1 && currentTeam.getId().equals(tempTeam2.getId())) {
                                                eliminationBallots.add(new EliminationBallot(judge, tempTeam2, tempTeam1));
                                            }
                                        }
                                    }
                                    for (EliminationBallot eliminationBallot : eliminationBallots) {
                                        eliminationBallotService.addEliminationBallot(eliminationBallot);
                                    }
                                } catch (Exception e) {
                                    log.error("Error in adding elimination ballot : " + e.getMessage());
                                }

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
                                if (!roundDTO.isElimination()) {
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
                                                    Ballot ballot = new Ballot(judge, debater, (float) score, speechDTO.getSpeakerPosition());
                                                    ballotService.addBallot(ballot);
                                                    ballots.add(ballot);
                                                }
                                            }
                                        }
                                    }
                                }
                                Debate debate;
                                if (roundDTO.isElimination()) {
                                    debate = new Debate(prop, opp, null, null, motion);
                                    debate.setEliminationBallots(eliminationBallots);
//                                    Check winner
                                    if (eliminationBallots.size() ==1 ) {
                                        Team winner = eliminationBallots.get(0).getWinner();
                                        debate.setWinner(winner);
                                    } else{
                                        List<Team> propVotes = new ArrayList<>();
                                        List<Team> oppVotes = new ArrayList<>();
                                        for (EliminationBallot eliminationBallot : eliminationBallots) {
                                            if (eliminationBallot.getWinner().equals(prop)) {
                                                propVotes.add(prop);
                                            } else {
                                                oppVotes.add(opp);
                                            }
                                        }
                                        if (propVotes.size() > oppVotes.size()) {
                                            debate.setWinner(prop);
                                        } else {
                                            debate.setWinner(opp);
                                        }
                                    }

                                } else {
                                    debate = new Debate(prop, opp, null, ballots, motion);
                                    String side0TeamId = debateDTO.getSides().get(0).getTeamId();
                                    String side1TeamId = debateDTO.getSides().get(1).getTeamId();
                                    Team side0Team = teamService.findTeamById(teamDTOMap.get(side0TeamId).getDbId());
                                    Team side1Team = teamService.findTeamById(teamDTOMap.get(side1TeamId).getDbId());
                                    List<Integer> side0Votes = new ArrayList<>();
                                    List<Integer> side1Votes = new ArrayList<>();
                                    for (FinalTeamBallotDTO finalTeamBallotDTO : debateDTO.getSides().get(0).getFinalTeamBallots()) {
                                        if (finalTeamBallotDTO.getRank() == 1) {
                                            side0Votes.add(1);
                                        } else {
                                            side0Votes.add(0);
                                        }
                                    }
                                    for (FinalTeamBallotDTO finalTeamBallotDTO : debateDTO.getSides().get(1).getFinalTeamBallots()) {
                                        if (finalTeamBallotDTO.getRank() == 1) {
                                            side1Votes.add(1);
                                        } else {
                                            side1Votes.add(0);
                                        }
                                    }
                                    int side0Sum = side0Votes.stream().mapToInt(Integer::intValue).sum();
                                    int side1Sum = side1Votes.stream().mapToInt(Integer::intValue).sum();
                                    if (side0Sum > side1Sum) {
                                        debate.setWinner(side0Team);
                                    } else if (side0Sum < side1Sum) {
                                        debate.setWinner(side1Team);
                                    }

                                }
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
            try {
                for (JudgeDTO judgeDTO : judgeDTOs) {
                    Judge judge = judgeService.findJudgeById(judgeDTO.getDbId());
                    List<FeedbackDTO> feedbackDTOs = judgeDTO.getFeedback();
                    for (FeedbackDTO feedbackDTO : feedbackDTOs) {
                        try {
                            Team sourceTeam = null;
                            Judge sourceJudge = null;
                            if (feedbackDTO.getSourceJudgeId() == null)
                                sourceTeam = teamService.findTeamById(teamDTOMap.get(feedbackDTO.getSourceTeamId()).getDbId());
                            else
                                sourceJudge = judgeService.findJudgeById(judgeDTOMap.get(feedbackDTO.getSourceJudgeId()).getDbId());
                            Float clashEvaluation = feedbackDTO.getClashEvaluation();
                            Float clashOrganization = feedbackDTO.getClashOrganization();
                            Float trackingArguments = feedbackDTO.getTrackingArguments();
                            String comments = feedbackDTO.getComments();
                            Float overallRating = feedbackDTO.getOverallRating();
                            String agree = feedbackDTO.getAgree();
                            Feedback feedback = new Feedback(judge, overallRating, clashEvaluation, clashOrganization, trackingArguments, agree, comments);
                            if (sourceTeam != null) {
                                feedback.setSourceTeam(sourceTeam);
                            } else if (sourceJudge != null) {
                                feedback.setSourceJudge(sourceJudge);
                            }
//                            feedbackService.addFeedback(feedback);
                        } catch (Exception e) {
                            log.error("Error in adding feedback to judge : " + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error in adding feedback : " + e.getMessage());
            }

            // Create and return comprehensive tournament data DTO
            TournamentDataDTO tournamentDataDTO = new TournamentDataDTO(
                tournamentDTO,
                new ArrayList<>(debaterDTOMap.values()),
                teamDTOs,
                judgeDTOs,
                institutionDTOs,
                motionDTOs,
                breakCategoryDTOs,
                roundsDTOs,
                debaterDTOMap,
                teamDTOMap,
                judgeDTOMap
            );
            
            return tournamentDataDTO;
        } catch (Exception e) {
            log.error("Error in building tournament : " + e.getMessage());
            return null;
        }
    }

    /**
     * Get tournament data summary - useful for checking what data was parsed
     */
    @GetMapping("/summary")
    public String getTournamentSummary(@RequestParam String fileName) {
        TournamentDataDTO tournamentData = buildMyTournament("src/main/resources/static/speaksXML/2024/" + fileName);
        
        if (tournamentData == null) {
            return "Failed to build tournament data";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Tournament: ").append(tournamentData.getTournament().getFullName()).append("\n");
        summary.append("Short Name: ").append(tournamentData.getTournament().getShortName()).append("\n");
        summary.append("Metadata: ").append(tournamentData.getMetadata().toString()).append("\n");
        
        summary.append("\nRound Summary:\n");
        for (RoundDTO round : tournamentData.getRounds()) {
            summary.append("- ").append(round.getName())
                   .append(" (").append(round.isElimination() ? "Elimination" : "Preliminary").append(")")
                   .append(" - ").append(round.getDebates().size()).append(" debates\n");
        }
        
        return summary.toString();
    }
}
