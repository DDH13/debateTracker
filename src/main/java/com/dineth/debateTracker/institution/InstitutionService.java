package com.dineth.debateTracker.institution;

import com.dineth.debateTracker.dtos.InstitutionMergeInfoDTO;
import com.dineth.debateTracker.team.Team;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
        JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();
        double threshold = 0.8; // similarity threshold (0.0 to 1.0)
        
        // Get all institutions
        List<Institution> allInstitutions = institutionRepository.findAll();
        
        // Filter and sort institutions by similarity score
        List<String> result = allInstitutions.stream()
                .filter(institution -> {
                    double score = similarity.apply(
                            name.toLowerCase(), 
                            institution.getName().toLowerCase()
                    );
                    return score >= threshold || 
                           institution.getName().toLowerCase().contains(name.toLowerCase());
                })
                .sorted((i1, i2) -> {
                    double score1 = similarity.apply(name.toLowerCase(), i1.getName().toLowerCase());
                    double score2 = similarity.apply(name.toLowerCase(), i2.getName().toLowerCase());
                    return Double.compare(score2, score1); // descending order
                })
                .map(institution -> institution.getId() + "," + institution.getName())
                .distinct()
                .collect(Collectors.toList());
        
        return result;
    }
    
    public List<InstitutionMergeInfoDTO> getInstitutionsWithTeamsCounts() {
        List<Object> temp = institutionRepository.findInstitutionsWithTeamsCounts();
        List<InstitutionMergeInfoDTO> result = new ArrayList<>();
        for (Object obj : temp) {
            Object[] arr = (Object[]) obj;
            Long id = ((Number) arr[0]).longValue();
            String name = (String) arr[1];
            String abbreviation = (String) arr[2];
            String teamCount = String.valueOf(((Number) arr[3]).longValue());
            String[] teamsArray = (String[]) arr[4];
            InstitutionMergeInfoDTO dto = new InstitutionMergeInfoDTO(id, name, abbreviation, teamCount, List.of(teamsArray));
            result.add(dto);
        }
        return result;
    }
}
