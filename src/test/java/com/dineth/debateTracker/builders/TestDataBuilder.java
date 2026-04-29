package com.dineth.debateTracker.builders;

import com.dineth.debateTracker.ballot.Ballot;
import com.dineth.debateTracker.debate.Debate;
import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.institution.Institution;
import com.dineth.debateTracker.judge.Judge;
import com.dineth.debateTracker.motion.Motion;
import com.dineth.debateTracker.round.Round;
import com.dineth.debateTracker.team.Team;
import com.dineth.debateTracker.tournament.Tournament;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Builder class for creating test data entities
 */
public class TestDataBuilder {

    public static DebaterBuilder debater() {
        return new DebaterBuilder();
    }

    public static TeamBuilder team() {
        return new TeamBuilder();
    }

    public static JudgeBuilder judge() {
        return new JudgeBuilder();
    }

    public static InstitutionBuilder institution() {
        return new InstitutionBuilder();
    }

    public static TournamentBuilder tournament() {
        return new TournamentBuilder();
    }

    public static RoundBuilder round() {
        return new RoundBuilder();
    }

    public static DebateBuilder debate() {
        return new DebateBuilder();
    }

    public static BallotBuilder ballot() {
        return new BallotBuilder();
    }

    public static MotionBuilder motion() {
        return new MotionBuilder();
    }

    public static class DebaterBuilder {
        private Long id;
        private String firstName = "John";
        private String lastName = "Doe";
        private String email;
        private String phone;
        private Date birthdate;
        private Institution institution;

        public DebaterBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public DebaterBuilder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public DebaterBuilder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public DebaterBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public DebaterBuilder withPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public DebaterBuilder withBirthdate(Date birthdate) {
            this.birthdate = birthdate;
            return this;
        }

        public DebaterBuilder withInstitution(Institution institution) {
            this.institution = institution;
            return this;
        }

        public Debater build() {
            Debater debater = new Debater(firstName, lastName);
            if (id != null) {
                debater.setId(id);
            }
            debater.setEmail(email);
            debater.setPhone(phone);
            debater.setBirthdate(birthdate);
            debater.setInstitution(institution);
            return debater;
        }
    }

    public static class TeamBuilder {
        private Long id;
        private String teamName = "Test Team";
        private String teamCode = "TT";
        private List<Debater> debaters = new ArrayList<>();

        public TeamBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public TeamBuilder withName(String teamName) {
            this.teamName = teamName;
            return this;
        }

        public TeamBuilder withCode(String teamCode) {
            this.teamCode = teamCode;
            return this;
        }

        public TeamBuilder withDebaters(List<Debater> debaters) {
            this.debaters = debaters;
            return this;
        }

        public TeamBuilder addDebater(Debater debater) {
            this.debaters.add(debater);
            return this;
        }

        public Team build() {
            Team team = new Team(teamName, teamCode, debaters);
            if (id != null) {
                team.setId(id);
            }
            return team;
        }
    }

    public static class JudgeBuilder {
        private Long id;
        private Float score = 4.5f;
        private String firstName = "Jane";
        private String lastName = "Judge";

        public JudgeBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public JudgeBuilder withScore(Float score) {
            this.score = score;
            return this;
        }

        public JudgeBuilder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public JudgeBuilder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Judge build() {
            Judge judge = new Judge(score, firstName, lastName);
            if (id != null) {
                judge.setId(id);
            }
            return judge;
        }
    }

    public static class InstitutionBuilder {
        private Long id;
        private String name = "Test Institution";
        private String reference = "TI";

        public InstitutionBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public InstitutionBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public InstitutionBuilder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public Institution build() {
            Institution institution = new Institution(name, reference);
            if (id != null) {
                institution.setId(id);
            }
            return institution;
        }
    }

    public static class TournamentBuilder {
        private Long id;
        private String fullName = "Test Tournament";
        private String shortName = "TT2025";
        private Date date;

        public TournamentBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public TournamentBuilder withFullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public TournamentBuilder withShortName(String shortName) {
            this.shortName = shortName;
            return this;
        }

        public TournamentBuilder withDate(Date date) {
            this.date = date;
            return this;
        }

        public Tournament build() {
            Tournament tournament = new Tournament(fullName, shortName);
            if (id != null) {
                tournament.setId(id);
            }
            if (date != null) {
                tournament.setDate(date);
            }
            return tournament;
        }
    }

    public static class RoundBuilder {
        private Long id;
        private String roundName = "Round 1";
        private Tournament tournament;
        private boolean isBreakRound = false;

        public RoundBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public RoundBuilder withName(String roundName) {
            this.roundName = roundName;
            return this;
        }

        public RoundBuilder withTournament(Tournament tournament) {
            this.tournament = tournament;
            return this;
        }

        public RoundBuilder asBreakRound() {
            this.isBreakRound = true;
            return this;
        }

        public Round build() {
            Round round = new Round(roundName, null, isBreakRound);
            if (id != null) {
                round.setId(id);
            }
            if (tournament != null) {
                round.setTournament(tournament);
            }
            return round;
        }
    }

    public static class DebateBuilder {
        private Long id;
        private Team proposition;
        private Team opposition;
        private Team winner;
        private List<Ballot> ballots = new ArrayList<>();
        private Motion motion;

        public DebateBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public DebateBuilder withProposition(Team proposition) {
            this.proposition = proposition;
            return this;
        }

        public DebateBuilder withOpposition(Team opposition) {
            this.opposition = opposition;
            return this;
        }

        public DebateBuilder withWinner(Team winner) {
            this.winner = winner;
            return this;
        }

        public DebateBuilder withBallots(List<Ballot> ballots) {
            this.ballots = ballots;
            return this;
        }

        public DebateBuilder withMotion(Motion motion) {
            this.motion = motion;
            return this;
        }

        public Debate build() {
            Debate debate = new Debate(proposition, opposition, winner, ballots, motion);
            if (id != null) {
                debate.setId(id);
            }
            return debate;
        }
    }

    public static class BallotBuilder {
        private Long id;
        private Judge judge;
        private Debater debater;
        private Float speakerScore = 75.0f;
        private Integer speakerPosition = 1;

        public BallotBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public BallotBuilder withJudge(Judge judge) {
            this.judge = judge;
            return this;
        }

        public BallotBuilder withDebater(Debater debater) {
            this.debater = debater;
            return this;
        }

        public BallotBuilder withScore(Float speakerScore) {
            this.speakerScore = speakerScore;
            return this;
        }

        public BallotBuilder withPosition(Integer speakerPosition) {
            this.speakerPosition = speakerPosition;
            return this;
        }

        public Ballot build() {
            Ballot ballot = new Ballot(judge, debater, speakerScore, speakerPosition);
            if (id != null) {
                ballot.setId(id);
            }
            return ballot;
        }
    }

    public static class MotionBuilder {
        private Long id;
        private String motion = "This House Would...";
        private String infoSlide = "";
        private String reference = "M1";

        public MotionBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public MotionBuilder withMotion(String motion) {
            this.motion = motion;
            return this;
        }

        public MotionBuilder withInfoSlide(String infoSlide) {
            this.infoSlide = infoSlide;
            return this;
        }

        public MotionBuilder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public Motion build() {
            Motion motionEntity = new Motion(motion, infoSlide, reference);
            if (id != null) {
                motionEntity.setId(id);
            }
            return motionEntity;
        }
    }
}


