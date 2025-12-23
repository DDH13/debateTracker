package com.dineth.debateTracker.judgeprofile;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/judgeprofile")
public class JudgeProfileController {
    private final JudgeProfileService judgeProfileService;

    public JudgeProfileController(JudgeProfileService judgeProfileService) {
        this.judgeProfileService = judgeProfileService;
    }
    
    @RequestMapping("/refresh")
    public void updateJudgeProfiles() {
        judgeProfileService.initializeAllJudgeProfiles();
        judgeProfileService.updateAllJudgeProfiles();
    }
}
