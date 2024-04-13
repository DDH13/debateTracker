package com.dineth.debateTracker.motion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MotionService {
    private final MotionRepository motionRepository;

    @Autowired
    public MotionService(MotionRepository motionRepository) {
        this.motionRepository = motionRepository;
    }

    public List<Motion> getMotion() {
        return motionRepository.findAll();
    }


    public Motion findMotionById(Long id) {
        return motionRepository.findById(id).orElse(null);
    }
    public Motion addMotion(Motion motion) {
        return motionRepository.save(motion);
    }


}
