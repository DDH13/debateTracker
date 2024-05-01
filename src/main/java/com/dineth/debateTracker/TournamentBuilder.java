package com.dineth.debateTracker;

import com.dineth.debateTracker.ballot.Ballot;
import com.dineth.debateTracker.breakcategory.BreakCategory;
import com.dineth.debateTracker.breakcategory.BreakCategoryService;
import com.dineth.debateTracker.debate.Debate;
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
import com.dineth.debateTracker.utils.ParseTabbycatXML;
import com.dineth.debateTracker.utils.StringUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/tournament")
public class TournamentBuilder {

    private final JudgeService judgeService;
    private final TeamService teamService;
    private final DebaterService debaterService;
    private final InstitutionService institutionService;
    private final MotionService motionService;
    private final TournamentService tournamentService;
    private final BreakCategoryService breakCategoryService;

    private final RoundService roundService;


    @Autowired
    public TournamentBuilder(JudgeService judgeService, TeamService teamService, DebaterService debaterService, InstitutionService institutionService, MotionService motionService, TournamentService tournamentService, BreakCategoryService breakCategoryService, RoundService roundService) {
        this.judgeService = judgeService;
        this.teamService = teamService;
        this.debaterService = debaterService;
        this.institutionService = institutionService;
        this.motionService = motionService;
        this.tournamentService = tournamentService;
        this.breakCategoryService = breakCategoryService;
        this.roundService = roundService;
    }


    @GetMapping("/build")
    public Object buildTournament() {
        ParseTabbycatXML parser = new ParseTabbycatXML("src/main/resources/static/SLSDC'23.xml");
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
        for (InstitutionDTO institutionDTO : institutionDTOs) {
            Institution institution = new Institution(institutionDTO.name, institutionDTO.reference);
            institution = institutionService.addInstitution(institution);
            institutionDTO.dbId = institution.getId();
        }

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

        for (TeamDTO teamDTO : teamDTOs) {
            List<DebaterDTO> debaterDTOs = teamDTO.getDebaters();
            List<Debater> debaters = debaterDTOs.stream().map(debaterDTO -> {
                ImmutablePair<String, String> names = StringUtil.splitName(debaterDTO.getName());
                Debater debater = new Debater(names.getLeft(), names.getRight());
                debater = debaterService.addDebater(debater);
                debaterDTO.setDbId(debater.getId());
                debaterDTOMap.put(debaterDTO.getId(), debaterDTO);
                return debater;
            }).collect(Collectors.toList());
            Team team = new Team(teamDTO.getName(), teamDTO.getCode(), debaters);
            team = teamService.addTeam(team);
            teamDTO.setDbId(team.getId());
            teamDTOMap.put(teamDTO.getId(), teamDTO);
        }

        Tournament tournament = new Tournament(tournamentDTO.getFullName(), tournamentDTO.getShortName());
        tournament = tournamentService.addTournament(tournament);

        for (BreakCategoryDTO breakCategoryDTO : breakCategoryDTOs) {
            BreakCategory breakCategory = new BreakCategory(breakCategoryDTO.getName());
            breakCategory = breakCategoryService.addBreakCategory(breakCategory);
            breakCategoryDTO.setDbId(breakCategory.getId());
            tournamentService.addBreakCategoryToTournament(tournament.getId(), breakCategory);
        }
        for (MotionDTO motionDTO : motionDTOs) {
            Motion motion = new Motion(motionDTO.getMotion(), motionDTO.getInfoSlide(), motionDTO.getReference());
            motion = motionService.addMotion(motion);
            motionDTO.setDbId(motion.getId());
            tournamentService.addMotionToTournament(tournament.getId(), motion);
        }


        for (RoundDTO roundDTO : roundsDTOs) {
            //For now, ignore elimination rounds
            if (roundDTO.isElimination()) {
                continue;
            }

            Round round = new Round(roundDTO.getName(), null, roundDTO.isElimination());
            round = roundService.addRound(round);
            roundDTO.setDbId(round.getId());
            tournamentService.addRoundToTournament(tournament.getId(), round);

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
                List <String> ignoredBallotAdjIds = new ArrayList<>();
                List<FinalTeamBallotDTO> adjBallots = debateDTO.getSides().get(0).getFinalTeamBallots();
                for (FinalTeamBallotDTO adjBallot : adjBallots) {
                    if (adjBallot.isIgnored()) {
                        ignoredBallotAdjIds.addAll(adjBallot.getAdjudicatorIds());
                    }
                }


//                        <debate id="D72" adjudicators="A314 A242 A311" chair="A314" venue="V22" motion="M1">
//            <side team="T40">
//                <ballot adjudicators="A242" minority="false" ignored="false" rank="1">254.5</ballot>
//                <ballot adjudicators="A311" minority="true" ignored="true" rank="2">261.5</ballot>
//                <ballot adjudicators="A314" minority="false" ignored="false" rank="1">268.0</ballot>

//                for (SideDTO sideDTO : debateDTO.getSides()) {
//                    List<Ballot> ballots = new ArrayList<>();
//                    for (FinalTeamBallotDTO finalTeamBallotDTO : sideDTO.getFinalTeamBallots()) {
//                        Judge judge = judgeService.findJudgeById(judgeDTOMap.get(finalTeamBallotDTO.getJudgeId()).getDbId());
//
//                    }
//                }

                //Create ballots
                List<Ballot> ballots = new ArrayList<>();


                Debate debate = new Debate(prop, opp, null, ballots, motion);
                roundService.addDebateToRound(round.getId(), debate);

            }
        }

        return tournament;
    }

}
