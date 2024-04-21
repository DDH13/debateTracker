package com.dineth.debateTracker;

import com.dineth.debateTracker.breakcategory.BreakCategory;
import com.dineth.debateTracker.breakcategory.BreakCategoryService;
import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.debater.DebaterService;
import com.dineth.debateTracker.dtos.*;
import com.dineth.debateTracker.institution.Institution;
import com.dineth.debateTracker.institution.InstitutionService;
import com.dineth.debateTracker.judge.Judge;
import com.dineth.debateTracker.judge.JudgeService;
import com.dineth.debateTracker.motion.MotionService;
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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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


    @Autowired
    public TournamentBuilder(JudgeService judgeService, TeamService teamService, DebaterService debaterService, InstitutionService institutionService, MotionService motionService, TournamentService tournamentService, BreakCategoryService breakCategoryService) {
        this.judgeService = judgeService;
        this.teamService = teamService;
        this.debaterService = debaterService;
        this.institutionService = institutionService;
        this.motionService = motionService;
        this.tournamentService = tournamentService;
        this.breakCategoryService = breakCategoryService;
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
        List<DebaterDTO> allDebaterDTOs = new ArrayList<>();



        //save debaters, institutions, judges
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
        }

        for (TeamDTO teamDTO: teamDTOs){
            List<DebaterDTO> debaterDTOs = teamDTO.getDebaters();
            List<Debater> debaters = debaterDTOs.stream().map(debaterDTO -> {
                ImmutablePair<String, String> names = StringUtil.splitName(debaterDTO.getName());
                Debater debater = new Debater(names.getLeft(), names.getRight());
                debater = debaterService.addDebater(debater);
                debaterDTO.setDbId(debater.getId());
                allDebaterDTOs.add(debaterDTO);
                return debater;
            }).collect(Collectors.toList());
            System.out.println(debaterDTOs);
            Team team = new Team(teamDTO.getName(), teamDTO.getCode(), debaters);
            team = teamService.addTeam(team);
            teamDTO.setDbId(team.getId());
        }



        Tournament tournament = new Tournament(tournamentDTO.getFullName(), tournamentDTO.getShortName());
        tournament = tournamentService.addTournament(tournament);


//        }

        return tournament;
    }

}
