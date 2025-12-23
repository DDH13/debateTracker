package com.dineth.debateTracker.debaterprofile;

import com.dineth.debateTracker.ballot.BallotService;
import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.debater.DebaterService;
import com.dineth.debateTracker.dtos.DebaterTournamentScoreDTO;
import com.dineth.debateTracker.dtos.debaterprofiles.FurthestRoundDTO;
import com.dineth.debateTracker.dtos.debaterprofiles.SpeakerPerformanceDTO;
import com.dineth.debateTracker.dtos.statistics.WinLossStatDTO;
import com.dineth.debateTracker.statistics.StatisticsService;
import com.dineth.debateTracker.tournament.TournamentService;
import com.dineth.debateTracker.utils.ProfileUtil;
import org.apache.commons.math3.stat.Frequency;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

import static com.dineth.debateTracker.utils.ProfileUtil.percentileRank;

@Service
public class DebaterProfileService {
    private static final double SPEAKER_SCORE_DELTA = 0.01;
    private static final double WIN_RATE_DELTA = 1;
    private final BallotService ballotService;
    private final DebaterService debaterService;
    private final TournamentService tournamentService;
    private final StatisticsService statisticsService;
    private final DebaterProfileRepository debaterProfileRepository;

    public DebaterProfileService(BallotService ballotService, DebaterService debaterService,
            TournamentService tournamentService, StatisticsService statisticsService,
            DebaterProfileRepository debaterProfileRepository) {
        this.ballotService = ballotService;
        this.debaterService = debaterService;
        this.tournamentService = tournamentService;
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

    public void deleteAllDebaterProfiles() {
        debaterProfileRepository.deleteAll();
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

    public void updateFurthestRounds(Long debaterId, List<FurthestRoundDTO> furthestRounds) {
        DebaterProfile debaterProfile = getDebaterProfileByDebaterId(debaterId);
        if (debaterProfile != null) {
            debaterProfile.setFurthestRounds(furthestRounds);
            updateDebaterProfile(debaterProfile);
        }
    }

    /**
     * Updates the following for a given debater profile - Number of tournaments debated - Average speaker score (over
     * all tournaments)
     */
    public void updateScoreStats(Long debaterId) {
        DebaterProfile debaterProfile = getDebaterProfileByDebaterId(debaterId);
        DebaterTournamentScoreDTO speakerPerformance = null;
        if (debaterProfile == null) {
            return;
        }
        speakerPerformance = statisticsService.getSpeakerTabScoresForDebater(debaterId);
        debaterProfile.setTournamentsDebated((int) speakerPerformance.getTournamentsDebated());
        debaterProfile.setAverageSpeakerScore(speakerPerformance.getAverageSpeakerScore().floatValue());
        updateDebaterProfile(debaterProfile);
    }

    public void updateSpeakerPerformances(Map<Long, List<SpeakerPerformanceDTO>> debaterPerformances) {
        List<DebaterProfile> profiles = new ArrayList<>();
        for (Long debaterId : debaterPerformances.keySet()) {
            DebaterProfile debaterProfile = getDebaterProfileByDebaterId(debaterId);
            if (debaterProfile != null) {
                List<SpeakerPerformanceDTO> performances = debaterPerformances.get(debaterId);
                debaterProfile.setSpeakerPerformances(performances);
                profiles.add(debaterProfile);
            }
        }
        debaterProfileRepository.saveAll(profiles);
    }

    /**
     * Deletes all existing debater profiles Initializes debater profiles for all debaters in the system with basic
     * biodata
     */
    public void initializeAllDebaterProfiles() {
        deleteAllDebaterProfiles();
        List<Debater> debaters = debaterService.getDebaters();
        for (Debater debater : debaters) {
            DebaterProfile debaterProfile = new DebaterProfile(debater.getId(), debater.getFirstName(),
                    debater.getLastName(), debater.getEmail());
            addDebaterProfile(debaterProfile);
        }
    }

    /**
     * Updates the individual data for all debater profiles in the system
     */
    public void updateAllDebaterProfiles() {

        // Update win/loss stats
        List<WinLossStatDTO> winLosses = statisticsService.calculateWinLoss();
        for (WinLossStatDTO winLossStatDTO : winLosses) {
            updateWinLoss(winLossStatDTO);
            updateScoreStats(winLossStatDTO.getId());
        }

        //Updates the ASS and no. of tournaments
        // And the tournament outround performances
        List<Debater> debaters = debaterService.getDebaters();
        for (Debater debater : debaters) {
            updateScoreStats(debater.getId());
            updateFurthestRounds(debater.getId(),
                    statisticsService.findFurthestRoundsReachedByDebater(debater.getId()));
        }

        //Update percentile fields
        updateAllPercentiles();

        //Update speaker performances
        updateSpeakerPerformances(statisticsService.findSpeakerPerformanceOfDebaters());
    }

    /**
     * Updates the percentiles for all debater profiles in the system
     */
    public void updateAllPercentiles() {
        List<DebaterProfile> profiles = debaterProfileRepository.findAll();
        if (profiles.isEmpty())
            return;

        // 1. Initialize Frequency objects
        Frequency prelimFreq = new Frequency();
        Frequency breakFreq = new Frequency();
        Frequency speakerFreq = new Frequency();
        Frequency activityFreq = new Frequency();

        // 2. Build populations in a single pass (O(N))
        for (DebaterProfile profile : profiles) {
            if (profile.getWinPercentagePrelims() != null) {
                prelimFreq.addValue(profile.getWinPercentagePrelims().doubleValue());
            }
            if (profile.getWinPercentageBreaks() != null) {
                breakFreq.addValue(profile.getWinPercentageBreaks().doubleValue());
            }
            if (profile.getAverageSpeakerScore() != null) {
                speakerFreq.addValue(profile.getAverageSpeakerScore().doubleValue());
            }

            int totalRounds = (profile.getPrelimsDebated() != null ?
                    profile.getPrelimsDebated() :
                    0) + (profile.getBreaksDebated() != null ? profile.getBreaksDebated() : 0);
            if (totalRounds > 0) {
                activityFreq.addValue(totalRounds);
            }
        }

        // 3. Compute and set percentiles (O(N log K) where K is unique values)
        for (DebaterProfile profile : profiles) {
            // Prelim Win % Percentile
            if (profile.getWinPercentagePrelims() != null) {
                double pct = prelimFreq.getCumPct(profile.getWinPercentagePrelims().doubleValue()) * 100;
                profile.setWinPercentagePrelimsPercentile((float) pct);
            }

            // Break Win % Percentile
            if (profile.getWinPercentageBreaks() != null) {
                double pct = breakFreq.getCumPct(profile.getWinPercentageBreaks().doubleValue()) * 100;
                profile.setWinPercentageBreaksPercentile((float) pct);
            }

            // Speaker Score Percentile
            if (profile.getAverageSpeakerScore() != null) {
                double score = profile.getAverageSpeakerScore().doubleValue();
                double pct = speakerFreq.getCumPct(score) * 100;
                profile.setSpeakerScorePercentile((float) pct);

                // Speaker Rank: Total count - number of values less than or equal to (score - epsilon)
                // Using a simple count approach for rank is often clearer
                long totalCount = speakerFreq.getSumFreq();
                long rank = totalCount - speakerFreq.getCumFreq(score) + 1;
                profile.setSpeakerRank((int) rank);
            }

            // Activity Percentile
            int totalRounds = (profile.getPrelimsDebated() != null ?
                    profile.getPrelimsDebated() :
                    0) + (profile.getBreaksDebated() != null ? profile.getBreaksDebated() : 0);
            if (totalRounds > 0) {
                double pct = activityFreq.getCumPct(totalRounds) * 100;
                profile.setActivityPercentile((float) pct);
            }
        }

        // 4. Save in batch
        debaterProfileRepository.saveAll(profiles);
    }
}
