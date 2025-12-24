package com.dineth.debateTracker.debater;


import com.dineth.debateTracker.dtos.DebaterMergeInfoDTO;
import com.dineth.debateTracker.dtos.DebaterTournamentScoreDTO;
import com.dineth.debateTracker.utils.ReplacementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/debater")
public class DebaterController {
    private final DebaterService debaterService;
    private final ReplacementService replacementService;

    @Autowired
    public DebaterController(DebaterService debaterService, ReplacementService replacementService) {
        this.debaterService = debaterService;
        this.replacementService = replacementService;
    }

    @GetMapping
    public List<Debater> getDebaters() {
        return debaterService.getDebaters();
    }

    @GetMapping(path = "{debaterId}")
    public Debater getDebater(@PathVariable("debaterId") Long debaterId) {
        return debaterService.findDebaterById(debaterId);
    }

    /**
     * Get debaters with the same name
     *
     * @param birthdate - if true, also check for same birthdate
     * @return List of debaters with the same name
     */
    @GetMapping(path = "same")
    public List<Debater> getDebatersWithSameName(@RequestParam(required = false) String birthdate) {
        if (birthdate != null && birthdate.equalsIgnoreCase("true")) {
            return debaterService.findDebatersWithDuplicateNamesAndBirthdays();
        }
        return debaterService.findDebatersWithDuplicateNames();
    }

    /**
     * Replace one debater with another in all references in the database
     *
     * @param values - oldDebaterId, newDebaterId
     */
    @PostMapping(path = "replace")
    public Debater replaceDebater(@RequestBody Map<String, String> values) {
        try {
            Long oldDebaterId = Long.parseLong(values.get("oldDebaterId"));
            Long newDebaterId = Long.parseLong(values.get("newDebaterId"));
            Debater oldDebater = debaterService.findDebaterById(oldDebaterId);
            Debater newDebater = debaterService.findDebaterById(newDebaterId);
            return replacementService.replaceDebater(oldDebater, newDebater);
        } catch (Exception e) {
            log.error("Error replacing debater: {}", e.getMessage());
        }
        return null;
    }

    @PostMapping
    public Debater addDebater(@RequestBody Debater debater) {
        return debaterService.addDebater(debater);
    }

    /**
     * Returns a list of all tournaments and the scores & speaker position for each prelim for a debater
     *
     * @param debaterId - the id of the debater
     */
    @GetMapping(path = "speaks/{debaterId}")
    public DebaterTournamentScoreDTO getSpeaks(@PathVariable("debaterId") Long debaterId,
                                               @RequestParam(value = "reply", required = false, defaultValue = "false") Boolean reply) {
        return debaterService.getTournamentsAndScoresForSpeaker(debaterId, reply);
    }

    /**
     * Returns a list of all tournaments and the scores & speaker position for each prelim for all debaters
     */
    @GetMapping(path = "speaks/all")
    public List<DebaterTournamentScoreDTO> getAllSpeaks() {
        List<Debater> debaters = debaterService.getDebaters();
        List<DebaterTournamentScoreDTO> debaterTournamentScoreDTOS = new ArrayList<>();
        for (Debater debater : debaters) {
            debaterTournamentScoreDTOS.add(debaterService.getTournamentsAndScoresForSpeaker(debater.getId(), false));
        }
        return debaterTournamentScoreDTOS;
    }
    
    @GetMapping(path = "teams-speaks-rounds")
    public List<DebaterMergeInfoDTO> getDebatersWithTeamsSpeaksRounds() {
        return debaterService.getDebatersTeamsSpeaksRounds();
    }

}
