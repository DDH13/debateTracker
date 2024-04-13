package com.dineth.debateTracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DebateTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DebateTrackerApplication.class, args);
        System.out.println("Debate Tracker Application Started\n");

//		TournamentBuilder tournamentBuilder = new TournamentBuilder();
//		tournamentBuilder.buildTournament();
    }


}
