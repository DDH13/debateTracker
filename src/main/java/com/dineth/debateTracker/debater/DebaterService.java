package com.dineth.debateTracker.debater;

import com.dineth.debateTracker.ballot.BallotService;
import com.dineth.debateTracker.dtos.DebaterTournamentScoreDTO;
import com.dineth.debateTracker.dtos.RoundScoreDTO;
import com.dineth.debateTracker.dtos.TournamentRoundDTO;
import com.dineth.debateTracker.team.TeamService;
import com.dineth.debateTracker.tournament.TournamentRepository;
import com.dineth.debateTracker.utils.CustomExceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public Debater addDebater(Debater debater) {
        Debater temp = checkIfDebaterExists(debater);
        if (temp != null) {
            return temp;
        }
        return debaterRepository.save(debater);
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

    public void replaceDebaters(Debater oldDebater, Debater newDebater) {
        ballotService.replaceDebater(oldDebater, newDebater);
        teamService.replaceDebater(oldDebater, newDebater);
        debaterRepository.delete(oldDebater);
    }

    /**
     * get all speaks for a debater from each tournament
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
}
