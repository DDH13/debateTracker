package com.dineth.debateTracker.debater;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
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

    @PostMapping
    public Debater addDebater(@RequestBody Debater debater) {
        return debaterService.addDebater(debater);
    }


}
