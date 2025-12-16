package com.dineth.debateTracker.debaterprofile;

import com.dineth.debateTracker.ballot.BallotService;
import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.debater.DebaterService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DebaterProfileService {
    private final BallotService ballotService;
    private final DebaterService debaterService;
    

    public DebaterProfileService(BallotService ballotService, DebaterService debaterService) {
        this.ballotService = ballotService;
        this.debaterService = debaterService;
    }
    
}
