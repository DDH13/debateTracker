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

    @PostMapping
    public Debater addDebater(@RequestBody  Debater debater) {
        return debaterService.addDebater(debater);
    }
    @GetMapping(path = "external")
    public void getDebatersFromAPI() {
        debaterService.getDebatersFromAPI();
    }

}
