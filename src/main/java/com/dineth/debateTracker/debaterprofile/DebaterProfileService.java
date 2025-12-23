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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

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
    
    public void updateSpeakerPerformances(Map<Long,List<SpeakerPerformanceDTO>> debaterPerformances) {
       for (Long debaterId : debaterPerformances.keySet()) {
           DebaterProfile debaterProfile = getDebaterProfileByDebaterId(debaterId);
           if (debaterProfile != null) {
               List<SpeakerPerformanceDTO> performances = debaterPerformances.get(debaterId);
               debaterProfile.setSpeakerPerformances(performances);
               updateDebaterProfile(debaterProfile);
           }
       }
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
     * Calculates a percentile
     */
    public static double percentileRank(double val, List<Double> values, double delta) {
        if (values == null || values.isEmpty()) {
            return Double.NaN;
        }

        int less = 0;
        int equal = 0;

        for (double v : values) {
            double diff = v - val;

            if (diff < -delta) {
                less++;
            } else if (Math.abs(diff) <= delta) {
                equal++;
            }
            // else: greater, ignore
        }

        int n = values.size();

        if (equal == 0) {
            return (less * 100.0) / n;
        }

        // midrank for ties
        return ((less + equal / 2.0) * 100.0) / n;
    }

    /**
     * Updates the percentiles for all debater profiles in the system
     */
    public void updateAllPercentiles() {
        List<DebaterProfile> profiles = debaterProfileRepository.findAll();

        List<Double> prelimWinRates = new ArrayList<>();
        List<Double> breakWinRates = new ArrayList<>();
        List<Double> speakerScores = new ArrayList<>();
        List<Long> roundsDebated = new ArrayList<>();

        // Build populations (ignore nulls)
        for (DebaterProfile profile : profiles) {
            if (profile.getWinPercentagePrelims() != null) {
                prelimWinRates.add(profile.getWinPercentagePrelims().doubleValue());
            }
            if (profile.getWinPercentageBreaks() != null) {
                breakWinRates.add(profile.getWinPercentageBreaks().doubleValue());
            }
            if (profile.getAverageSpeakerScore() != null) {
                speakerScores.add(profile.getAverageSpeakerScore().doubleValue());
            }
            long rounds = Stream.of(profile.getPrelimsDebated(), profile.getBreaksDebated()).filter(Objects::nonNull)
                    .mapToLong(Integer::longValue).sum();
            roundsDebated.add(rounds);
        }
        //remove 0 values from rounds debated to avoid skewing activity percentiles
        roundsDebated.removeIf(r -> r == 0);

        // Compute percentiles
        for (DebaterProfile profile : profiles) {

            if (profile.getWinPercentagePrelims() != null && !prelimWinRates.isEmpty()) {
                double p = percentileRank(profile.getWinPercentagePrelims().doubleValue(), prelimWinRates,
                        WIN_RATE_DELTA);
                profile.setWinPercentagePrelimsPercentile((float) p);
            } else {
                profile.setWinPercentagePrelimsPercentile(null);
            }

            if (profile.getWinPercentageBreaks() != null && !breakWinRates.isEmpty()) {
                double p = percentileRank(profile.getWinPercentageBreaks().doubleValue(), breakWinRates,
                        WIN_RATE_DELTA);
                profile.setWinPercentageBreaksPercentile((float) p);
            } else {
                profile.setWinPercentageBreaksPercentile(null);
            }

            if (profile.getAverageSpeakerScore() != null && !speakerScores.isEmpty()) {
                double p = percentileRank(profile.getAverageSpeakerScore().doubleValue(), speakerScores,
                        SPEAKER_SCORE_DELTA);
                profile.setSpeakerScorePercentile((float) p);
            } else {
                profile.setSpeakerScorePercentile(null);
            }

            long rounds = Stream.of(profile.getPrelimsDebated(), profile.getBreaksDebated()).filter(Objects::nonNull)
                    .mapToLong(Integer::longValue).sum();
            if (!roundsDebated.isEmpty() && rounds > 0) {
                double p = percentileRank((double) rounds,
                        roundsDebated.stream().mapToDouble(Long::doubleValue).boxed().toList(), 0);
                profile.setActivityPercentile((float) p);
            }
        }

        // Save in batch (important)
        debaterProfileRepository.saveAll(profiles);
    }

}
