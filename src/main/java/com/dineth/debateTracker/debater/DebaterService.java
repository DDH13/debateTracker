package com.dineth.debateTracker.debater;

import com.dineth.debateTracker.ballot.BallotService;
import com.dineth.debateTracker.dtos.DebaterTournamentScoreDTO;
import com.dineth.debateTracker.dtos.RoundScoreDTO;
import com.dineth.debateTracker.dtos.TournamentRoundDTO;

import com.dineth.debateTracker.team.TeamService;
import com.dineth.debateTracker.tournament.TournamentRepository;
import com.dineth.debateTracker.utils.CustomExceptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class DebaterService {
    private final DebaterRepository debaterRepository;
    private final TournamentRepository tournamentRepository;
    private final BallotService ballotService;
    private final TeamService teamService;

    @Autowired
    public DebaterService(DebaterRepository debaterRepository, TournamentRepository tournamentRepository, BallotService ballotService, TeamService teamService) {
        this.debaterRepository = debaterRepository;
        this.tournamentRepository = tournamentRepository;
        this.ballotService = ballotService;
        this.teamService = teamService;
    }

    public List<Debater> getDebaters() {
        return debaterRepository.findAll();
    }
    public Debater getDebaterById(Long debaterId) {
        return debaterRepository.findById(debaterId).orElse(null);
    }

    public Debater addDebater(Debater debater) {
        Debater temp = checkIfDebaterExists(debater);
        if (temp != null) {
            return temp;
        }
        return debaterRepository.save(debater);
    }
    
    public void updateDebater(Debater debater) {
        debaterRepository.save(debater);
    }

    public Debater findDebaterById(Long id) {
        return debaterRepository.findById(id).orElse(null);
    }

    public Debater checkIfDebaterExists(Debater debater) {
        List<Debater> debaters;
        if (debater.getBirthdate() != null) {
            debaters = debaterRepository.findDebatersByFirstNameEqualsIgnoreCaseAndLastNameEqualsIgnoreCaseAndBirthdate(debater.getFirstName(), debater.getLastName(), debater.getBirthdate());
        } else {
            debaters = debaterRepository.findDebatersByFirstNameEqualsIgnoreCaseAndLastNameEqualsIgnoreCase(debater.getFirstName(), debater.getLastName());
        }
        //TODO adjust code to accommodate debaters with same first name and last name
        if (debaters.size() == 1) {
            return debaters.get(0);
        } else if (debaters.size() > 1) {
            throw new CustomExceptions.MultipleDebatersFoundException("Multiple debaters found with the same name. Birthdate is required to identify the debater.");
        } else {
            return null;
        }
    }

    public List<Debater> findDebatersWithDuplicateNames() {
        // First get the names that have duplicates
        List<Object[]> nameDuplicates = debaterRepository.findDebaterNameDuplicates();
        // Now fetch full details for each name pair with duplicates
        return getDuplicateDebaters(nameDuplicates);
    }

    public List<Debater> findDebatersWithDuplicateNamesAndBirthdays() {
        // First get the names that have duplicates
        List<Object[]> nameDuplicates = debaterRepository.findDebaterNameAndBirthdayDuplicates();
        // Now fetch full details for each name pair with duplicates
        return getDuplicateDebaters(nameDuplicates);
    }

    private List<Debater> getDuplicateDebaters(List<Object[]> nameDuplicates) {
        List<Debater> duplicateDebaters = new ArrayList<>();
        for (Object[] result : nameDuplicates) {
            String firstName = (String) result[0];
            String lastName = (String) result[1];
            List<Debater> debaters = debaterRepository.findByFirstNameAndLastNameAllIgnoreCase(firstName, lastName);
            duplicateDebaters.addAll(debaters);
        }
        return duplicateDebaters;
    }

    /**
     * Replace all references of oldDebater with newDebater in the database
     * @param oldDebater
     * @param newDebater
     */
    public void replaceDebaters(Debater oldDebater, Debater newDebater) {
        ballotService.replaceDebater(oldDebater, newDebater);
        teamService.replaceDebater(oldDebater, newDebater);
        
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
        
        debaterRepository.save(newDebater);
        debaterRepository.delete(oldDebater);
        log.info("Replaced debater {} {} {} with {} {} {}", oldDebater.getId(), oldDebater.getFirstName(),
                oldDebater.getLastName(), +newDebater.getId(), newDebater.getFirstName(), newDebater.getLastName());
    }

    //TODO this method should be in the statistics service
    /**
     * Get all speaks for a debater from each tournament
     * This will include multiple ballots given by different judges in the same round
     * This will include iron-personed speeches
     */
    public DebaterTournamentScoreDTO getTournamentsAndScoresForSpeaker(Long debaterID, Boolean reply) {
//        Get the result set from the db
        List<Object> temp = debaterRepository.findTournamentsAndScoresForSpeaker(debaterID);
        Debater debater = findDebaterById(debaterID);
        DebaterTournamentScoreDTO x = new DebaterTournamentScoreDTO(debater.getFirstName(), debater.getLastName(), debater.getId(), null);

        HashMap<Long, TournamentRoundDTO> tournamentMap = new HashMap<>();

//        iterate through the result set and separate the data tournament wise
        for (Object o : temp) {
            Object[] obj = (Object[]) o;
            Long tid = (Long) obj[0];
            if (!tournamentMap.containsKey(tid)) {
                TournamentRoundDTO tr = new TournamentRoundDTO((String) obj[1], tid, null);
//                find date of the tournament
                tr.setDate(tournamentRepository.findById(tid).get().getDate());

                tournamentMap.put(tid, tr);
            }
        }
//        iterate through the result set and separate the data round wise
        for (Object o : temp) {
            Object[] obj = (Object[]) o;
            Long tid = (Long) obj[0];
            TournamentRoundDTO tr = tournamentMap.get(tid);
            RoundScoreDTO rs = new RoundScoreDTO((String) obj[3], (Long) obj[2], ((Float) obj[4]).doubleValue(), (Integer) obj[5]);
//            skip reply rounds if required
            if (!reply && rs.getSpeakerPosition() == 4) continue;
            tr.addRoundScore(rs);
            tournamentMap.put(tid, tr);
        }
        x.setTournamentRoundScores(new ArrayList<>(tournamentMap.values()));
        return x;
    }

    public Integer getIronPersonCount(Long debaterId) {
        DebaterTournamentScoreDTO scores = getTournamentsAndScoresForSpeaker(debaterId, false);
        int totalIronSpeeches = 0;

        for (TournamentRoundDTO tr : scores.getTournamentRoundScores()) {
            int tournamentIronSpeeches = 0;
            Map<Long, Set<Integer>> speechCount = new HashMap<>();
            for (RoundScoreDTO rs : tr.getRoundScores()) {
                Set<Integer> count = speechCount.getOrDefault(rs.getRoundId(), new HashSet<>() {
                });
                count.add(rs.getSpeakerPosition());
                speechCount.put(rs.getRoundId(), count);
            }
            for (Map.Entry<Long, Set<Integer>> entry : speechCount.entrySet()) {
                Set<Integer> positions = entry.getValue();
                if (positions.size() > 1) {
                    tournamentIronSpeeches += 1;
                }
            }

            totalIronSpeeches += tournamentIronSpeeches;
        }
        return totalIronSpeeches;
    }
}
