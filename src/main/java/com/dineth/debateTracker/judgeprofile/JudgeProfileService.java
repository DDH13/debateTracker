package com.dineth.debateTracker.judgeprofile;

import com.dineth.debateTracker.dtos.JudgeSentimentDTO;
import com.dineth.debateTracker.dtos.statistics.JudgeStatsDTO;
import com.dineth.debateTracker.judge.Judge;
import com.dineth.debateTracker.judge.JudgeService;
import com.dineth.debateTracker.statistics.StatisticsService;
import org.apache.commons.math3.stat.Frequency;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JudgeProfileService {
    private final JudgeProfileRepository judgeProfileRepository;
    private final JudgeService judgeService;
    private final StatisticsService statisticsService;
    JudgeProfileService(JudgeProfileRepository judgeProfileRepository, JudgeService judgeService,
            StatisticsService statisticsService) {
        this.judgeProfileRepository = judgeProfileRepository;
        this.judgeService = judgeService;
        this.statisticsService = statisticsService;
    }
    public void addJudgeProfile(JudgeProfile judgeProfile) {
        judgeProfileRepository.save(judgeProfile);
    }
    public void updateJudgeProfile(JudgeProfile judgeProfile) {
        judgeProfileRepository.save(judgeProfile);
    }
    
    public JudgeProfile getJudgeProfileById(Long id) {
        return judgeProfileRepository.findById(id).orElse(null);
    }
    
    public JudgeProfile getJudgeProfileByJudgeId(Long judgeId) {
        return judgeProfileRepository.findByJudgeId(judgeId);
    }
    
    public void deleteJudgeProfile(Long id) {
        judgeProfileRepository.deleteById(id);
    }

    public void initializeAllJudgeProfiles() {
        judgeProfileRepository.deleteAll();
        List<Judge> judges = judgeService.getJudges();
        List<JudgeProfile> judgeProfiles = new ArrayList<>();
        for (Judge judge : judges) {
            JudgeProfile judgeProfile = new JudgeProfile(judge.getId(), judge.getFname(), judge.getLname(),
                    judge.getEmail());
            judgeProfiles.add(judgeProfile);
        }
        judgeProfileRepository.saveAll(judgeProfiles);
    }

    @Transactional
    public void updateAllJudgeProfiles() {
        // 1. Fetch all sentiments
        List<JudgeSentimentDTO> sentiments = statisticsService.getSentiment(0.5);
        if (sentiments.isEmpty())
            return;

        // 2. Bulk fetch all relevant JudgeProfiles to avoid N+1 queries
        List<Long> judgeIds = sentiments.stream().map(JudgeSentimentDTO::getJudgeId).collect(Collectors.toList());

        Map<Long, JudgeProfile> profileMap = judgeProfileRepository.findAllByJudgeIdIn(judgeIds).stream()
                .collect(Collectors.toMap(JudgeProfile::getJudgeId, p -> p));

        Frequency freq = new Frequency();

        // 3. Update profiles and populate Frequency object in one pass
        for (JudgeSentimentDTO sentiment : sentiments) {
            JudgeProfile profile = profileMap.get(sentiment.getJudgeId());
            if (profile == null)
                continue;

            // Apply Sentiment Data
            profile.setLeniencyCount(sentiment.getLeniencyCount());
            profile.setHarshnessCount(sentiment.getHarshnessCount());
            profile.setNeutralCount(sentiment.getNeutralCount());
            profile.setLeniency(sentiment.getLeniency());
            profile.setHarshness(sentiment.getHarshness());
            profile.setOverallSentiment(sentiment.getOverallSentiment());

            // Fetch Stats
            JudgeStatsDTO stats = judgeService.getJudgeStats(sentiment.getJudgeId());
            if (stats != null) {
                profile.setPrelimsJudged(stats.getPrelimsJudged());
                profile.setBreaksJudged(stats.getBreaksJudged());
                profile.setTournamentsJudged(stats.getTournamentsJudged().size());
                profile.setAverageFirst(stats.getAverageFirst());
                profile.setAverageSecond(stats.getAverageSecond());
                profile.setAverageThird(stats.getAverageThird());
                profile.setAverageSubstantive(stats.getAverageSubstantive());
                profile.setRoundPreferences(stats.getRoundPreferences());
            }

            // Add to frequency distribution for percentile calculation
            freq.addValue(profile.getPrelimsJudged() + profile.getBreaksJudged());
        }

        // 4. Calculate percentiles in-memory (no need to re-query the DB)
        for (JudgeProfile profile : profileMap.values()) {
            int totalJudged = profile.getPrelimsJudged() + profile.getBreaksJudged();
            double percentile = freq.getCumPct(totalJudged) * 100;
            profile.setActivityPercentile((float) percentile);
        }

        // 5. Single bulk save
        judgeProfileRepository.saveAll(profileMap.values());
    }
}
