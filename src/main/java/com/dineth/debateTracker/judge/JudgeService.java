package com.dineth.debateTracker.judge;

import com.dineth.debateTracker.dtos.JudgeTournamentScoreDTO;
import com.dineth.debateTracker.dtos.RoundScoreDTO;
import com.dineth.debateTracker.dtos.TournamentRoundDTO;
import com.dineth.debateTracker.dtos.statistics.JudgeStatsDTO;
import com.dineth.debateTracker.tournament.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Service
public class JudgeService {
    private final JudgeRepository judgeRepository;
    private final TournamentRepository tournamentRepository;

    @Autowired
    public JudgeService(JudgeRepository judgeRepository, TournamentRepository tournamentRepository) {
        this.judgeRepository = judgeRepository;
        this.tournamentRepository = tournamentRepository;
    }

    public List<Judge> getJudges() {
        return judgeRepository.findAll();
    }

    public Judge findJudgeById(Long id) {
        return judgeRepository.findById(id).orElse(null);
    }

    public Judge addJudge(Judge judge) {
        return judgeRepository.save(judge);
    }

    public Judge checkJudgeExists(Judge judge) {
        return judgeRepository.findByFnameAndLname(judge.getFname(), judge.getLname());
    }

    /**
     * get all the tournaments judged by a given judge
     */
    public List<String> getTournamentsJudged(Long judgeID) {
        JudgeTournamentScoreDTO breaks = this.getTournamentsAndBreaksJudged(judgeID);
        JudgeTournamentScoreDTO prelims = this.getTournamentsAndScoresForJudge(judgeID, false);

        HashSet<String> set = new HashSet<>();
        for (TournamentRoundDTO tr : breaks.getTournamentRoundScores()) {
            set.add(tr.getTournamentShortName());
        }
        for (TournamentRoundDTO tr : prelims.getTournamentRoundScores()) {
            set.add(tr.getTournamentShortName());
        }
        return new ArrayList<>(set);


    }

    /**
     * get the number of rounds judged by a judge
     */
    public HashMap<String, Integer> getRoundsJudged(Long judgeID) {
        JudgeTournamentScoreDTO breaks = this.getTournamentsAndBreaksJudged(judgeID);
        JudgeTournamentScoreDTO prelims = this.getTournamentsAndScoresForJudge(judgeID, false);
        return new HashMap<>() {{
            put("prelims", prelims.getTotalDebatesJudged());
            put("breaks", breaks.getTotalDebatesJudged());
        }};
    }

    /**
     * get all speaks given by a judge at each tournament
     */
    public JudgeTournamentScoreDTO getTournamentsAndScoresForJudge(Long judgeID, Boolean reply) {
        List<Object> temp = judgeRepository.findTournamentsAndScoresForJudge(judgeID);
        Judge judge = findJudgeById(judgeID);
        JudgeTournamentScoreDTO x = new JudgeTournamentScoreDTO(judge.getFname(), judge.getLname(), judge.getId(), null);

        HashMap<Long, TournamentRoundDTO> tournamentMap = new HashMap<>();

        for (Object o : temp) {
            Object[] obj = (Object[]) o;
            Long tid = (Long) obj[0];
            if (!tournamentMap.containsKey(tid)) {
                TournamentRoundDTO tr = new TournamentRoundDTO((String) obj[1], tid, null);
                tr.setDate(tournamentRepository.findById(tid).get().getDate());
                tournamentMap.put(tid, tr);
            }
        }

        for (Object o : temp) {
            Object[] obj = (Object[]) o;
            Long tid = (Long) obj[0];
            TournamentRoundDTO tr = tournamentMap.get(tid);
            RoundScoreDTO rs = new RoundScoreDTO((String) obj[3], (Long) obj[2], ((Float) obj[4]).doubleValue(), (Integer) obj[5]);
//            skip reply rounds if required
            if (!reply && rs.getSpeakerPosition() == 4) continue;
            tr.addRoundScore(rs);
            tournamentMap.put(tid, tr);
        }
        x.setTournamentRoundScores(new ArrayList<>(tournamentMap.values()));
        return x;
    }

    /**
     * get all break rounds judged by a judge at each tournament
     */

    public JudgeTournamentScoreDTO getTournamentsAndBreaksJudged(Long judgeID) {
        List<Object> temp = judgeRepository.findTournamentsAndBreaksJudged(judgeID);
        Judge judge = findJudgeById(judgeID);
        JudgeTournamentScoreDTO x = new JudgeTournamentScoreDTO(judge.getFname(), judge.getLname(), judge.getId(), null);

        HashMap<Long, TournamentRoundDTO> tournamentMap = new HashMap<>();

        for (Object o : temp) {
            Object[] obj = (Object[]) o;
            Long tid = (Long) obj[0];
            if (!tournamentMap.containsKey(tid)) {
                TournamentRoundDTO tr = new TournamentRoundDTO((String) obj[1], tid, null);
                tr.setDate(tournamentRepository.findById(tid).get().getDate());
                tournamentMap.put(tid, tr);
            }
        }

        for (Object o : temp) {
            Object[] obj = (Object[]) o;
            Long tid = (Long) obj[0];
            TournamentRoundDTO tr = tournamentMap.get(tid);
            RoundScoreDTO rs = new RoundScoreDTO((String) obj[3], (Long) obj[2], 0.0, 5);
            tr.addRoundScore(rs);
            tournamentMap.put(tid, tr);
        }
        x.setTournamentRoundScores(new ArrayList<>(tournamentMap.values()));
        return x;
    }

    /**
     * Get stats for a given judge for the master tab
     *
     * @param judgeID the id of the judge
     */
    public JudgeStatsDTO getJudgeStats(Long judgeID) {
        JudgeTournamentScoreDTO breaks = this.getTournamentsAndBreaksJudged(judgeID);
        JudgeTournamentScoreDTO prelims = this.getTournamentsAndScoresForJudge(judgeID, false);
        Judge judge = findJudgeById(judgeID);
        List<Double> replyScoresGiven = new ArrayList<>();
        List<Double> substantiveScoresGiven = prelims.getTournamentRoundScores().stream().map(TournamentRoundDTO::getRoundScores).flatMap(List::stream).filter(a -> a.getSpeakerPosition() != 4).map(RoundScoreDTO::getScore).toList();
        List<RoundScoreDTO> allScores = prelims.getTournamentRoundScores().stream().map(TournamentRoundDTO::getRoundScores).flatMap(List::stream).toList();


        HashSet<String> tournamentsJudged = new HashSet<>();
        for (TournamentRoundDTO tr : breaks.getTournamentRoundScores()) {
            tournamentsJudged.add(tr.getTournamentShortName());
        }
        for (TournamentRoundDTO tr : prelims.getTournamentRoundScores()) {
            tournamentsJudged.add(tr.getTournamentShortName());
        }
        Double averageFirst = allScores.stream().filter(a -> a.getSpeakerPosition() == 1).mapToDouble(RoundScoreDTO::getScore).average().orElse(0.0);
        Double averageSecond = allScores.stream().filter(a -> a.getSpeakerPosition() == 2).mapToDouble(RoundScoreDTO::getScore).average().orElse(0.0);
        Double averageThird = allScores.stream().filter(a -> a.getSpeakerPosition() == 3).mapToDouble(RoundScoreDTO::getScore).average().orElse(0.0);
        Double averageReply = allScores.stream().filter(a -> a.getSpeakerPosition() == 4).mapToDouble(RoundScoreDTO::getScore).average().orElse(0.0);

        Double averageSubstantive = substantiveScoresGiven.stream().mapToDouble(a -> a).average().orElse(0.0);

        int breaksJudged = breaks.getTotalDebatesJudged();
        int prelimsJudged = prelims.getTotalDebatesJudged();
        Double stDeviation = Math.sqrt(substantiveScoresGiven.stream().mapToDouble(a -> Math.pow(a - averageSubstantive, 2)).sum() / substantiveScoresGiven.size());

        return new JudgeStatsDTO(judge, breaksJudged + prelimsJudged, breaksJudged,
                prelimsJudged, averageFirst, averageSecond, averageThird, averageReply, averageSubstantive,
                stDeviation, new ArrayList<>(tournamentsJudged), substantiveScoresGiven, replyScoresGiven);


    }
}
