package com.dineth.debateTracker.statistics;

import com.dineth.debateTracker.ballot.Ballot;
import com.dineth.debateTracker.ballot.BallotRepository;
import com.dineth.debateTracker.ballot.BallotService;
import com.dineth.debateTracker.debate.Debate;
import com.dineth.debateTracker.debate.DebateRepository;
import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.debater.DebaterRepository;
import com.dineth.debateTracker.debater.DebaterService;
import com.dineth.debateTracker.dtos.DebaterTournamentScoreDTO;
import com.dineth.debateTracker.dtos.JudgeSentimentDTO;
import com.dineth.debateTracker.dtos.RoundScoreDTO;
import com.dineth.debateTracker.dtos.SpeakerTab.SpeakerTabBallot;
import com.dineth.debateTracker.dtos.SpeakerTab.SpeakerTabDTO;
import com.dineth.debateTracker.dtos.SpeakerTab.SpeakerTabRowDTO;
import com.dineth.debateTracker.dtos.TournamentRoundDTO;
import com.dineth.debateTracker.dtos.statistics.WinLossStatDTO;
import com.dineth.debateTracker.judge.Judge;
import com.dineth.debateTracker.judge.JudgeRepository;
import com.dineth.debateTracker.round.Round;
import com.dineth.debateTracker.team.Team;
import com.dineth.debateTracker.tournament.Tournament;
import com.dineth.debateTracker.tournament.TournamentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StatisticsService {
    private final BallotRepository ballotRepository;
    private final JudgeRepository judgeRepository;
    private final DebateRepository debateRepository;
    private final DebaterRepository debaterRepository;
    private final BallotService ballotService;
    private final DebaterService debaterService;
    private final TournamentService tournamentService;

    StatisticsService(BallotRepository ballotRepository, JudgeRepository judgeRepository,
            DebateRepository debateRepository, DebaterRepository debaterRepository, BallotService ballotService,
            DebaterService debaterService, TournamentService tournamentService) {
        this.ballotRepository = ballotRepository;
        this.judgeRepository = judgeRepository;
        this.debateRepository = debateRepository;
        this.debaterRepository = debaterRepository;
        this.ballotService = ballotService;
        this.debaterService = debaterService;
        this.tournamentService = tournamentService;
    }

    public HashMap<String, Double> getGlobalDistribution() {
        List<Ballot> ballots = ballotRepository.findBallotBySpeakerScoreGreaterThan(40.5f);
        int totalBallots = ballots.size();
        double scores = ballots.stream().mapToDouble(Ballot::getSpeakerScore).sum();
        Double globalMean = scores / totalBallots;
        Double stdDeviation = Math.sqrt(
                ballots.stream().mapToDouble(ballot -> Math.pow(ballot.getSpeakerScore() - globalMean, 2)).average()
                        .orElse(0.0));
        return new HashMap<>() {{
            put("mean", globalMean);
            put("stdDeviation", stdDeviation);
        }};
    }

    /**
     * Get sentiment of judges
     *
     * @param allowedDeviation - the extent to which a judge is allowed to deviate from the average score of a debater
     *                         before being considered lenient or harsh
     * @return List of JudgeSentimentDTO
     */

    public List<JudgeSentimentDTO> getSentiment(double allowedDeviation) {
        //        Querying db for initial data
        List<Ballot> ballots = ballotRepository.findBallotBySpeakerScoreGreaterThan(40.5f);
        List<Judge> judges = judgeRepository.findAll();

        //      Storing the average scores of debaters excluding the scores given by a specific judge to inefficient repeated calculations
        HashMap<Long, HashMap<Long, Double>> debaterAverages = new HashMap<>();
        for (Ballot ballot : ballots) {
            Long debaterId = ballot.getDebater().getId();
            Long judgeId = ballot.getJudge().getId();
            if (!debaterAverages.containsKey(debaterId)) {
                debaterAverages.put(debaterId, new HashMap<>());
            }
            if (!debaterAverages.get(debaterId).containsKey(judgeId)) {
                Double avg = calculateSpeakerAverageExcludingJudge(judgeId, debaterId, ballots);
                debaterAverages.get(debaterId).put(judgeId, avg);
            }
        }

        List<JudgeSentimentDTO> judgeSentiments = new ArrayList<>();

        for (Judge judge : judges) {
            //            Get all ballots for the given judge
            List<Ballot> judgeBallots = ballots.stream()
                    .filter(ballot -> ballot.getJudge().getId().equals(judge.getId())).toList();

            //          If a judge has not judged any debates, they are not included in the analysis
            if (judgeBallots.isEmpty()) {
                continue;
            }

            List<Double> leniency = new ArrayList<>();
            List<Double> harshness = new ArrayList<>();
            int neutrality = 0;
            int speechesConsidered = 0;

            //            Calculating leniency and harshness
            for (Ballot ballot : judgeBallots) {
                Double debaterAvg = debaterAverages.get(ballot.getDebater().getId()).get(judge.getId());
                if (debaterAvg != null) {
                    if ((ballot.getSpeakerScore() - debaterAvg) >= allowedDeviation) {
                        leniency.add(ballot.getSpeakerScore() - debaterAvg);
                    } else if ((ballot.getSpeakerScore() - debaterAvg) <= -allowedDeviation) {
                        harshness.add(ballot.getSpeakerScore() - debaterAvg);
                    } else {
                        neutrality++;
                    }
                    speechesConsidered++;
                }
            }

            double leniencySum = leniency.stream().mapToDouble(Double::doubleValue).sum();
            double harshnessSum = harshness.stream().mapToDouble(Double::doubleValue).sum();
            double overallSentiment = (leniencySum + harshnessSum) / speechesConsidered;

            JudgeSentimentDTO judgeSentiment = new JudgeSentimentDTO(judge, speechesConsidered, leniency.size(),
                    harshness.size(), neutrality, leniencySum, harshnessSum, overallSentiment);

            judgeSentiments.add(judgeSentiment);
        }
        return judgeSentiments;
    }

    /**
     * Calculate the average score of a debater excluding the scores given by a specific judge
     *
     * @param judgeId   - the id of the judge whose scores are to be excluded
     * @param debaterId - the id of the debater whose average score is to be calculated
     * @param ballots   - list of all ballots to be considered
     */
    public Double calculateSpeakerAverageExcludingJudge(Long judgeId, Long debaterId, List<Ballot> ballots) {
        //        get all ballots for a debater
        List<Ballot> debaterBallots = new ArrayList<>(
                ballots.stream().filter(ballot -> ballot.getDebater().getId().equals(debaterId)).toList());
        //        remove ballots from the judge
        debaterBallots.removeIf(ballot -> ballot.getJudge().getId().equals(judgeId));
        //        check if the debater has a minimum number of ballots to calculate average
        int minBallots = 5;
        if (debaterBallots.size() < minBallots) {
            return null;
        }
        return debaterBallots.stream().mapToDouble(Ballot::getSpeakerScore).average().orElse(0.0);
    }

    /**
     * Calculate the win percentage of all debaters in prelims
     */
    public List<WinLossStatDTO> calculateWinLossPrelims() {
        HashMap<Long, WinLossStatDTO> debaterStats = new HashMap<>();
        List<Debater> debaters = debaterRepository.findAll();

        for (Debater debater : debaters) {
            List<Debate> relevantDebates = debateRepository.findPrelimsByDebaterId(debater.getId());
            if (relevantDebates.isEmpty()) {
                continue;
            }
            WinLossStatDTO debaterStat = new WinLossStatDTO(debater.getFirstName(), debater.getLastName(),
                    debater.getId());

            for (Debate debate : relevantDebates) {
                Boolean didWin = didDebaterWinDebate(debate, debater);
                if (didWin == null) {
                    continue;
                }
                if (didWin) {
                    debaterStat.setPrelimWins(debaterStat.getPrelimWins() + 1);
                } else {
                    debaterStat.setPrelimLosses(debaterStat.getPrelimLosses() + 1);
                }
            }
            debaterStats.put(debater.getId(), debaterStat);
        }
        return new ArrayList<>(debaterStats.values());
    }

    /**
     * Calculate the win percentage of all debaters in elimination rounds
     */
    public List<WinLossStatDTO> calculateWinLossBreaks() {
        HashMap<Long, WinLossStatDTO> debaterStats = new HashMap<>();
        List<Debater> debaters = debaterRepository.findAll();

        for (Debater debater : debaters) {
            List<Debate> relevantDebates = debateRepository.findBreaksByDebaterId(debater.getId());
            if (relevantDebates.isEmpty()) {
                continue;
            }
            WinLossStatDTO debaterStat = new WinLossStatDTO(debater.getFirstName(), debater.getLastName(),
                    debater.getId());

            for (Debate debate : relevantDebates) {
                Boolean didWin = didDebaterWinDebate(debate, debater);
                if (didWin == null) {
                    continue;
                }
                if (didWin) {
                    debaterStat.setBreakWins(debaterStat.getBreakWins() + 1);
                } else {
                    debaterStat.setBreakLosses(debaterStat.getBreakLosses() + 1);
                }
            }
            debaterStats.put(debater.getId(), debaterStat);
        }
        return new ArrayList<>(debaterStats.values());
    }

    /**
     * Calculate the win percentage of all debaters in both prelims and elimination rounds
     */
    public List<WinLossStatDTO> calculateWinLoss() {
        List<WinLossStatDTO> prelimStats = calculateWinLossPrelims();
        List<WinLossStatDTO> breakStats = calculateWinLossBreaks();
        HashMap<Long, WinLossStatDTO> debaterStats = new HashMap<>();

        for (WinLossStatDTO stat : prelimStats) {
            debaterStats.put(stat.getId(), stat);
        }
        for (WinLossStatDTO stat : breakStats) {
            if (debaterStats.containsKey(stat.getId())) {
                WinLossStatDTO debaterStat = debaterStats.get(stat.getId());
                debaterStat.setBreakWins(stat.getBreakWins());
                debaterStat.setBreakLosses(stat.getBreakLosses());
            } else {
                debaterStats.put(stat.getId(), stat);
            }
        }
        return new ArrayList<>(debaterStats.values());
    }

    /**
     * Check if a debater won a debate
     *
     * @param debate  - the debate to be checked
     * @param debater - the debater to be checked
     * @return true if the debater won, false if the debater lost, null if the debate is undecided
     */
    public Boolean didDebaterWinDebate(Debate debate, Debater debater) {
        Team winner = debate.getWinner();
        if (winner == null) {
            return null;
        }
        if (debate.getProposition().getDebaters().contains(debater)) {
            return winner.equals(debate.getProposition());
        }
        if (debate.getOpposition().getDebaters().contains(debater)) {
            return winner.equals(debate.getOpposition());
        }
        return null;
    }

    /**
     * Calculates the speaker tab for a tournament
     *
     * @param tournamentId - the id of the tournament
     */

    public SpeakerTabDTO calculateSpeakerTabForTournament(Long tournamentId) {
        List<Debater> debaters = debaterService.getDebaters();
        Tournament tournament = tournamentService.findTournamentById(tournamentId);
        long prelims = tournament.getRounds().stream().filter(round -> !round.getIsBreakRound()).count();
        int minimumSpeeches = (int) Math.ceil(prelims / 2.0);
        SpeakerTabDTO speakerTab = new SpeakerTabDTO(tournamentId, tournament.getShortName(), minimumSpeeches);
        for (Debater debater : debaters) {
            List<SpeakerTabBallot> ballots = ballotService.findBallotsByTournamentAndDebater(tournamentId,
                    debater.getId());
            SpeakerTabRowDTO speakerTabRow = new SpeakerTabRowDTO(debater.getId(), ballots);
            speakerTab.addSpeakerTabRow(speakerTabRow);
        }
        speakerTab.setRanks();
        printSpeakerTab(speakerTab);
        return speakerTab;
    }

    public void printSpeakerTab(SpeakerTabDTO speakerTab) {
        System.out.println(
                "Speaker Tab for Tournament: " + speakerTab.getTournamentShortName() + " (ID: " + speakerTab.getTournamentId() + ")");
        System.out.println("Minimum Speeches Required: " + speakerTab.getMinimumSpeeches());
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-10s %-20s %-20s %-20s %-20s%n", "Rank", "Debater", "Avg Speaker Score", "Speeches Count",
                "Std Deviation");
        System.out.println("--------------------------------------------------------------------------");
        for (SpeakerTabRowDTO row : speakerTab.getSpeakerTabRows()) {
            Debater debater = debaterService.getDebaterById(row.getDebaterId());
            System.out.printf("%-10d %-20s %-20.2f %-20d %-20.2f%n", row.getRank(), debater.getFirstName(),
                    row.getAverageSpeakerScore(), row.getSpeechesCount(),
                    row.getStandardDeviation() != null ? row.getStandardDeviation() : 0.0);
        }
        System.out.println("--------------------------------------------------");

    }

    /**
     * This method gets the scores of all debaters in all tournaments as per reflected in the speaker tab
     */
    public Map<Long, DebaterTournamentScoreDTO> getAllSpeakerTabScores() {
        // Implementation goes here
        List<Debater> debaters = debaterService.getDebaters();
        List<Tournament> tournaments = tournamentService.getTournaments();
        HashMap<Long, DebaterTournamentScoreDTO> debaterTournamentScoreMap = new HashMap<>();

        for (Debater debater : debaters) {
            DebaterTournamentScoreDTO debaterScoresOfTournaments = new DebaterTournamentScoreDTO(debater.getFirstName(),
                    debater.getLastName(), debater.getId(), null);
            for (Tournament tournament : tournaments) {
                List<SpeakerTabBallot> ballots = ballotService.findBallotsByTournamentAndDebater(tournament.getId(),
                        debater.getId());
                if (ballots.isEmpty())
                    continue;

                TournamentRoundDTO tournamentDTO = new TournamentRoundDTO(tournament.getShortName(), tournament.getId(),
                        null);

                //Collect all scores of the debater in the tournament by iterating over the ballot received for each round
                for (SpeakerTabBallot ballot : ballots) {
                    tournamentDTO.addRoundScore(
                            new RoundScoreDTO(null, ballot.getRoundId(), ballot.getSpeakerScore().doubleValue(),
                                    ballot.getSpeakerPosition()));
                }
                debaterScoresOfTournaments.addTournamentRoundScore(tournamentDTO);

            }
            debaterTournamentScoreMap.put(debater.getId(), debaterScoresOfTournaments);
        }
        return debaterTournamentScoreMap;
    }

}
