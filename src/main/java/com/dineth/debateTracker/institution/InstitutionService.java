package com.dineth.debateTracker.institution;

import com.dineth.debateTracker.team.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InstitutionService {
    private final InstitutionRepository institutionRepository;

    @Autowired
    public InstitutionService(InstitutionRepository institutionRepository) {
        this.institutionRepository = institutionRepository;
    }

    public List<Institution> getInstitutions() {
        return institutionRepository.findAll();
    }

    public Institution findInstitutionById(Long id) {
        return institutionRepository.findById(id).orElse(null);
    }

    public Institution addInstitution(Institution institution) {
        return institutionRepository.save(institution);
    }

    public void addTeamToInstitution(Long institutionId, Team team) {
        Institution institution = institutionRepository.findById(institutionId).orElse(null);
        if (institution != null) {
            List<Team> teams = institution.getTeams();
            if (teams == null) {
                teams = new ArrayList<>();
            }
            teams.add(team);
            institution.setTeams(teams);
            institutionRepository.save(institution);
        }
    }


}
