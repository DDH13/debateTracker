package com.dineth.debateTracker.ballot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BallotService {
    private final BallotRepository ballotRepository;

    @Autowired
    public BallotService(BallotRepository ballotRepository) {
        this.ballotRepository = ballotRepository;
    }

    public List<Ballot> getBallots() {
        return ballotRepository.findAll();
    }

    public Ballot addBallot(Ballot ballot) {
        return ballotRepository.save(ballot);
    }

    public Ballot findBallotById(Long id) {
        return ballotRepository.findById(id).orElse(null);
    }

}
