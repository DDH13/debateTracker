package com.dineth.debateTracker.debater;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController @Slf4j
@RequestMapping(path = "api/v1/debater")
public class DebaterController {
    private final DebaterService debaterService;

    @Autowired
    public DebaterController(DebaterService debaterService) {
        this.debaterService = debaterService;
    }

    @GetMapping
    public List<Debater> getDebaters() {
        return debaterService.getDebaters();
    }

    @GetMapping(path = "same")
    public List<Debater> getDebatersWithSameName(@RequestParam(required = false) String birthdate) {
        if (birthdate != null && birthdate.equalsIgnoreCase("true")) {
            return debaterService.findDebatersWithDuplicateNamesAndBirthdays();
        }
        return debaterService.findDebatersWithDuplicateNames();
    }

    @PostMapping(path = "replace")
    public void replaceDebater(@RequestBody Map<String, String> values) {
        try {

            Long oldDebaterId = Long.parseLong(values.get("oldDebaterId"));
            Long newDebaterId = Long.parseLong(values.get("newDebaterId"));
            Debater oldDebater = debaterService.findDebaterById(oldDebaterId);
            Debater newDebater = debaterService.findDebaterById(newDebaterId);
            debaterService.replaceDebaters(oldDebater, newDebater);
        } catch (Exception e) {
            log.error("Error replacing debater: " + e.getMessage());
        }
    }

    @PostMapping
    public Debater addDebater(@RequestBody Debater debater) {
        return debaterService.addDebater(debater);
    }


}
