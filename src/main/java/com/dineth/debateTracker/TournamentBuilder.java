package com.dineth.debateTracker;

import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.debater.DebaterService;
import com.dineth.debateTracker.institution.Institution;
import com.dineth.debateTracker.institution.InstitutionService;
import com.dineth.debateTracker.judge.Judge;
import com.dineth.debateTracker.judge.JudgeService;
import com.dineth.debateTracker.motion.Motion;
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
        ParseTabbycatXML parser = new ParseTabbycatXML("src/main/resources/static/Wickys_2024.xml");
        parser.parseXML();

        ImmutablePair<List<Debater>, List<Team>> pair = parser.getTeamsAndSpeakers(parser.document);
        List<Debater> debaters = pair.getLeft();
        List<Team> teams = pair.getRight();
        List<Judge> judges = parser.getJudges(parser.document);
        List<Institution> institutions = parser.getInstitutions(parser.document);
        Tournament tournament = parser.getTournament(parser.document);
        List<Motion> motions = parser.getMotions(parser.document);


        //save debaters, judges, institutions
        for (Debater debater : debaters) {
            debaterService.addDebater(debater);
        }
        for (Judge judge : judges) {
            judgeService.addJudge(judge);
        }
        for (Institution institution : institutions) {
            institutionService.addInstitution(institution);
        }
        tournamentService.addRoundToTournament(1L, new Round());

        //add teams to institutions
        Map<String, Institution> institutionMap = institutions.stream()
                .collect(Collectors.toMap(Institution::getTempId, Function.identity()));

        for (Team team : teams) {
            String instID = team.getInstitutionId();
            if (instID != null) {
                Institution institution = institutionMap.get(instID);
                if (institution != null) {
                    team.setInstitution(institution);
                    team = teamService.addTeam(team);
                    institutionService.addTeamToInstitution(institution.getId(), team);
                } else {
                    System.err.println("No matching institution found for Team with ID: " + instID);
                }
            }
        }


        return tournamentService.addTournament(tournament);


    }

}
