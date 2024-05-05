package com.dineth.debateTracker.team;

import com.dineth.debateTracker.debater.Debater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamService {
    private final TeamRepository teamRepository;

    @Autowired
    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public List<Team> getTeam() {
        return teamRepository.findAll();
    }

    public Team addTeam(Team team) {
        return teamRepository.save(team);
    }

    public Team findTeamById(Long id) {
        return teamRepository.findById(id).orElse(null);
    }

    private List<Team> getTeamsByDebater(Long debaterId) {
        return teamRepository.findByDebaters_Id(debaterId);
    }

    @Transactional
    public void replaceDebater(Debater oldDebater, Debater newDebater) {
        List<Team> teams = getTeamsByDebater(oldDebater.getId());
        for (Team team : teams) {
            team.getDebaters().remove(oldDebater);
            team.getDebaters().add(newDebater);
            teamRepository.save(team);
        }
    }


}
