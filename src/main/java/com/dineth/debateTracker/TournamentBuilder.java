package com.dineth.debateTracker;

import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.debater.DebaterService;
import com.dineth.debateTracker.dtos.InstitutionDTO;
import com.dineth.debateTracker.dtos.MotionDTO;
import com.dineth.debateTracker.dtos.RoundDTO;
import com.dineth.debateTracker.dtos.TournamentDTO;
import com.dineth.debateTracker.institution.Institution;
import com.dineth.debateTracker.institution.InstitutionService;
import com.dineth.debateTracker.judge.Judge;
import com.dineth.debateTracker.judge.JudgeService;
import com.dineth.debateTracker.motion.MotionService;
import com.dineth.debateTracker.round.Round;
import com.dineth.debateTracker.team.Team;
import com.dineth.debateTracker.team.TeamService;
import com.dineth.debateTracker.tournament.Tournament;
import com.dineth.debateTracker.tournament.TournamentService;
import com.dineth.debateTracker.utils.ParseTabbycatXML;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


    @Autowired
    public TournamentBuilder(JudgeService judgeService, TeamService teamService, DebaterService debaterService, InstitutionService institutionService, MotionService motionService, TournamentService tournamentService) {
        this.judgeService = judgeService;
        this.teamService = teamService;
        this.debaterService = debaterService;
        this.institutionService = institutionService;
        this.motionService = motionService;
        this.tournamentService = tournamentService;
    }


    @GetMapping("/build")
    public Object buildTournament() {
        ParseTabbycatXML parser = new ParseTabbycatXML("src/main/resources/static/SLSDC'23.xml");
        parser.parseXML();

        ImmutablePair<List<Debater>, List<Team>> pair = parser.getTeamsAndSpeakers(parser.document);
        List<Debater> debaters = pair.getLeft();
        List<Team> teams = pair.getRight();
        List<Judge> judges = parser.getJudges(parser.document);
        List<InstitutionDTO> institutionDTOs = parser.getInstitutionDTOs(parser.document);
        TournamentDTO tournamentDTO = parser.getTournamentDTO(parser.document);
        List<MotionDTO> motionDTOs = parser.getMotionDTOs(parser.document);
        List<RoundDTO> rounds = parser.getRoundsDTO(parser.document);

        System.out.println("RoundsDTO: " + rounds);

        //save debaters, judges, institutions, tournament
        for (Debater debater : debaters) {
            debaterService.addDebater(debater);
        }
        for (Judge judge : judges) {
            judgeService.addJudge(judge);
        }
        for (InstitutionDTO institutionDTO : institutionDTOs) {
            Institution institution = new Institution(institutionDTO.name, institutionDTO.reference);
            institution = institutionService.addInstitution(institution);
            institutionDTO.dbId = institution.getId();
        }

        Tournament tournament = new Tournament(tournamentDTO.getFullName(), tournamentDTO.getShortName());
        tournament = tournamentService.addTournament(tournament);

        //add teams to institutions
        Map<String, InstitutionDTO> institutionDTOMap = institutionDTOs.stream().collect(Collectors.toMap(InstitutionDTO::getId, Function.identity()));

        for (Team team : teams) {
            String instID = team.getInstitutionId();
            if (instID != null) {
                InstitutionDTO institutionDTO = institutionDTOMap.get(instID);
                if (institutionDTO != null) {
                    team.setInstitution(institutionService.findInstitutionById(institutionDTO.dbId));
                    team = teamService.addTeam(team);
                    institutionService.addTeamToInstitution(institutionDTO.dbId, team);
                } else {
                    System.err.println("No matching institutionDTO found for Team with ID: " + instID);
                }
            }
        }

        return tournament;
    }

}
