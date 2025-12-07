package com.dineth.debateTracker.institution;

import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.debater.DebaterRepository;
import com.dineth.debateTracker.team.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InstitutionService {
    private final InstitutionRepository institutionRepository;
    private final DebaterRepository debaterRepository;

    @Autowired
    public InstitutionService(InstitutionRepository institutionRepository, DebaterRepository debaterRepository) {
        this.institutionRepository = institutionRepository;
        this.debaterRepository = debaterRepository;
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
    //Merge multiple institutions
    public Institution mergeMultipleInstitutions(List<Long> institutionIds) throws Exception {
    //pick the first institution as the merged institution
        Institution mergedInstitution = institutionRepository.findById(institutionIds.getFirst()).orElse(null);
        if (mergedInstitution == null) {
            throw new Exception("First Institution not found");
        }
        List<Team> teams = new ArrayList<>();
        for (Long id : institutionIds) {
            Institution institution = institutionRepository.findById(id).orElse(null);
            if (institution != null) {
                teams.addAll(institution.getTeams());
                institutionRepository.delete(institution);
            }
        }
        mergedInstitution.setTeams(teams);
        
        for (Long id : institutionIds) {
            List<Debater> debaters = debaterRepository.findByInstitutionId(id);
            for (Debater debater : debaters) {
                debater.setInstitution(mergedInstitution);
                debaterRepository.save(debater);
            }
        }
        return institutionRepository.save(mergedInstitution);
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
