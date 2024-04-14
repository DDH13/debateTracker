package com.dineth.debateTracker.debate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DebateService {
    private final DebateRepository debateRepository;

    @Autowired
    public DebateService(DebateRepository debateRepository) {
        this.debateRepository = debateRepository;
    }

    public List<Debate> getDebate() {
        return debateRepository.findAll();
    }

    public Debate findDebateById(Long id) {
        return debateRepository.findById(id).orElse(null);
    }
    public Debate addDebate(Debate debate) {
        return debateRepository.save(debate);
    }


}
