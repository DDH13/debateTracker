package com.dineth.debateTracker.debaterprofile;

import com.dineth.debateTracker.ballot.BallotService;
import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.debater.DebaterService;
import com.dineth.debateTracker.dtos.statistics.WinLossStatDTO;
import com.dineth.debateTracker.statistics.StatisticsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DebaterProfileService {
    private final BallotService ballotService;
    private final DebaterService debaterService;
    private final StatisticsService statisticsService;
    private final DebaterProfileRepository debaterProfileRepository;

    public DebaterProfileService(BallotService ballotService, DebaterService debaterService,
            StatisticsService statisticsService, DebaterProfileRepository debaterProfileRepository) {
        this.ballotService = ballotService;
        this.debaterService = debaterService;
        this.statisticsService = statisticsService;
        this.debaterProfileRepository = debaterProfileRepository;
    }

    public DebaterProfile addDebaterProfile(DebaterProfile debaterProfile) {
        return debaterProfileRepository.save(debaterProfile);
    }

    public DebaterProfile getDebaterProfileByDebaterId(Long debaterId) {
        return debaterProfileRepository.getDebaterProfileByDebaterId(debaterId);
    }

    public List<DebaterProfile> getAllDebaterProfiles() {
        return debaterProfileRepository.findAll();
    }

    public void updateDebaterProfile(DebaterProfile debaterProfile) {
        debaterProfileRepository.save(debaterProfile);
    }

    public void deleteDebaterProfile(Long id) {
        debaterProfileRepository.deleteById(id);
    }

    /**
     * Updates the win percentages for prelims and breaks along with the number of speeches for a given debater profile
     */
    public void updateWinLoss(WinLossStatDTO winLossStatDTO) {
        DebaterProfile debaterProfile = getDebaterProfileByDebaterId(winLossStatDTO.getId());
        if (debaterProfile != null) {
            int prelimsDebated = winLossStatDTO.getPrelimWins() + winLossStatDTO.getPrelimLosses();
            int breaksDebated = winLossStatDTO.getBreakWins() + winLossStatDTO.getBreakLosses();
            float prelimWinRate = prelimsDebated > 0 ? (float) winLossStatDTO.getPrelimWins() / prelimsDebated : 0f;
            float breakWinRate = breaksDebated > 0 ? (float) winLossStatDTO.getBreakWins() / breaksDebated : 0f;

            debaterProfile.setBreaksDebated(breaksDebated);
            debaterProfile.setPrelimsDebated(prelimsDebated);
            debaterProfile.setWinPercentageBreaks(breakWinRate * 100);
            debaterProfile.setWinPercentagePrelims(prelimWinRate * 100);
            updateDebaterProfile(debaterProfile);
        }
    }
    
    public void updateEmail(Long debaterId, String email) {
        DebaterProfile debaterProfile = getDebaterProfileByDebaterId(debaterId);
        if (debaterProfile != null) {
            debaterProfile.setEmail(email);
            updateDebaterProfile(debaterProfile);
        }
    }

}
