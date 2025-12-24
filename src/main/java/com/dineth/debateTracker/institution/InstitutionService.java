package com.dineth.debateTracker.institution;

import com.dineth.debateTracker.team.Team;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
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
    public Institution findInstitutionByName(String name) {
//        turn to lowercase, strip spaces and special characters
        List<String> similarNames = getInstitutionsWithSimilarNames(name);
        name = name.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
        for (String s : similarNames) {
            String[] parts = s.split(",");
            String temp = parts[1].toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
            if (temp.equals(name)) {
                return institutionRepository.findById(Long.parseLong(parts[0])).orElse(null);
            }
        }
        return null;
    }
    
    public void deleteInstitution(Long id) {
        institutionRepository.deleteById(id);
    }

    public Institution findInstitutionById(Long id) {
        return institutionRepository.findById(id).orElse(null);
    }

    public Institution addInstitution(Institution institution) {
        return institutionRepository.save(institution);
    }
    public void updateInstitution(Institution institution) {
        institutionRepository.save(institution);
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

    public List<String> getInstitutionsWithSimilarNames(String name) {
        List<String> l1 = institutionRepository.findSimilarInstitutions(name);
        List<Institution> l2 = institutionRepository.findByNameContaining(name);
        for (Institution i : l2) {
            String temp =  i.getId() + "," + i.getName();
            if (!l1.contains(temp)) {
                l1.add(temp);
            }
        }
        return l1;
    }
}
