package com.dineth.debateTracker.utils;

import com.dineth.debateTracker.ballot.BallotService;
import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.debater.DebaterService;
import com.dineth.debateTracker.eliminationballot.EliminationBallotService;
import com.dineth.debateTracker.institution.Institution;
import com.dineth.debateTracker.institution.InstitutionService;
import com.dineth.debateTracker.judge.Judge;
import com.dineth.debateTracker.judge.JudgeService;
import com.dineth.debateTracker.team.Team;
import com.dineth.debateTracker.team.TeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ReplacementService {
    private final TeamService teamService;
    private final DebaterService debaterService;
    private final InstitutionService institutionService;
    private final BallotService ballotService;
    private final EliminationBallotService eliminationBallotService;
    private final JudgeService judgeService;

    public ReplacementService(TeamService teamService, DebaterService debaterService,
            InstitutionService institutionService, BallotService ballotService,
            EliminationBallotService eliminationBallotService, JudgeService judgeService) {
        this.teamService = teamService;
        this.debaterService = debaterService;
        this.institutionService = institutionService;
        this.ballotService = ballotService;
        this.eliminationBallotService = eliminationBallotService;
        this.judgeService = judgeService;
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

    /**
     * Merge multiple institutions into one institution
     * @param institutionIds List of institution IDs to be merged
     * @return the merged Institution
     */
    public Institution mergeMultipleInstitutions(List<Long> institutionIds) {
        //pick the first institution as the institution to persist
        Institution mergedInstitution = institutionService.findInstitutionById(institutionIds.getFirst());
        if (mergedInstitution == null) {
            log.error("Institution ID: {} not found for merging institutions", institutionIds.getFirst());
            return null;
        }
        List<Team> teams = new ArrayList<>(mergedInstitution.getTeams());
        for (Long id : institutionIds) {
            Institution institution = institutionService.findInstitutionById(id);
            if (institution != null) {
                teams.addAll(institution.getTeams());
                institutionService.deleteInstitution(id);
            }
        }
        mergedInstitution.setTeams(teams);

        for (Long id : institutionIds) {
            List<Debater> debaters = debaterService.findDebatersByInstitutionId(id);
            for (Debater debater : debaters) {
                debater.setInstitution(mergedInstitution);
                debaterService.updateDebater(debater);
            }
        }
        log.info("Merged Institutions: {} into Institution ID: {}", institutionIds,
                mergedInstitution.getId());
        institutionService.updateInstitution(mergedInstitution);
        return mergedInstitution;
    }

    /**
     * Replace an old judge by a new judge along with all references
     * @param oldJudgeId the id of the judge to be replaced
     * @param newJudgeId the id of the judge to replace with
     */
    public Judge replaceJudge(Long oldJudgeId, Long newJudgeId) {
        Judge oldJudge = judgeService.findJudgeById(oldJudgeId);
        Judge newJudge = judgeService.findJudgeById(newJudgeId);
        if (newJudge.getFname() == null) {
            newJudge.setFname(oldJudge.getFname());
        }
        if (newJudge.getLname() == null) {
            newJudge.setLname(oldJudge.getLname());
        }
        if (newJudge.getGender() == null) {
            newJudge.setGender(oldJudge.getGender());
        }
        if (newJudge.getEmail() == null) {
            newJudge.setEmail(oldJudge.getEmail());
        }
        if (newJudge.getPhone() == null) {
            newJudge.setPhone(oldJudge.getPhone());
        }
        if (newJudge.getBirthdate() == null) {
            newJudge.setBirthdate(oldJudge.getBirthdate());
        }
        ballotService.replaceJudge(oldJudge, newJudge);
        eliminationBallotService.replaceJudge(oldJudge, newJudge);
        //TODO replace in feedback once it's implemented

        log.info("Replaced judge {} {} with judge {} {}", oldJudge.getFname(), oldJudge.getLname(),
                newJudge.getFname(), newJudge.getLname());
        judgeService.updateJudge(newJudge);
        judgeService.deleteJudge(oldJudgeId);
        return newJudge;
    }
}
