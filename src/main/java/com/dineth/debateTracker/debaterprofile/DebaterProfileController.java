package com.dineth.debateTracker.debaterprofile;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/debaterprofile")
public class DebaterProfileController {
    private final DebaterProfileService debaterProfileService;

    @Autowired
    public DebaterProfileController(DebaterProfileService debaterProfileService) {
        this.debaterProfileService = debaterProfileService;
    }
    
    @GetMapping("/refresh")
    public void updateDebaterProfiles() {
        debaterProfileService.initializeAllDebaterProfiles();
        debaterProfileService.updateAllDebaterProfiles();
    }
    
}
