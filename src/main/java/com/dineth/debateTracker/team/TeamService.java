package com.dineth.debateTracker.team;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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


}
