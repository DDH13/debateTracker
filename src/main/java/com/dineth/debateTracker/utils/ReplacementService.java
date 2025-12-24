package com.dineth.debateTracker.utils;

import com.dineth.debateTracker.ballot.BallotService;
import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.debater.DebaterService;
import com.dineth.debateTracker.institution.InstitutionService;
import com.dineth.debateTracker.team.TeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReplacementService {
    private final TeamService teamService;
    private final DebaterService debaterService;
    private final InstitutionService institutionService;
    private final BallotService ballotService;

    public ReplacementService(TeamService teamService, DebaterService debaterService,
            InstitutionService institutionService, BallotService ballotService) {
        this.teamService = teamService;
        this.debaterService = debaterService;
        this.institutionService = institutionService;
        this.ballotService = ballotService;
    }

    /**
     * Replace all references of oldDebater with newDebater in the database
     * @param oldDebater the debater to be replaced
     * @param newDebater the debater to replace with
     * @return the newDebater after replacement
     */

    public Debater replaceDebater(Debater oldDebater, Debater newDebater) {
        teamService.replaceDebater(oldDebater, newDebater);
        ballotService.replaceDebater(oldDebater, newDebater);

        if (newDebater.getFullName() == null && oldDebater.getFullName() != null) {
            newDebater.setFullName(oldDebater.getFullName());
        }
        if (newDebater.getGender() == null && oldDebater.getGender() != null) {
            newDebater.setGender(oldDebater.getGender());
        }
        if (newDebater.getEmail() == null && oldDebater.getEmail() != null) {
            newDebater.setEmail(oldDebater.getEmail());
        }
        if (newDebater.getPhone() == null && oldDebater.getPhone() != null) {
            newDebater.setPhone(oldDebater.getPhone());
        }
        if (newDebater.getDistrict() == null && oldDebater.getDistrict() != null) {
            newDebater.setDistrict(oldDebater.getDistrict());
        }
        if (newDebater.getBirthdate() == null && oldDebater.getBirthdate() != null) {
            newDebater.setBirthdate(oldDebater.getBirthdate());
        }
        if (newDebater.getInstitution() == null && oldDebater.getInstitution() != null) {
            newDebater.setInstitution(oldDebater.getInstitution());
        }
        
        debaterService.updateDebater(newDebater);
        debaterService.deleteDebater(oldDebater.getId());

        log.info("Replaced debater {} {} {} with {} {} {}", oldDebater.getId(), oldDebater.getFirstName(),
                oldDebater.getLastName(), +newDebater.getId(), newDebater.getFirstName(), newDebater.getLastName());
        return newDebater;
    }
}
