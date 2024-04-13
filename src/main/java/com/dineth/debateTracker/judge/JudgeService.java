package com.dineth.debateTracker.judge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class JudgeService {
    private final JudgeRepository judgeRepository;

    @Autowired
    public JudgeService(JudgeRepository judgeRepository) {
        this.judgeRepository = judgeRepository;
    }

    public List<Judge> getJudge() {
        return judgeRepository.findAll();
    }

    public Judge findJudgeById(Long id) {
        return judgeRepository.findById(id).orElse(null);
    }
    public Judge addJudge(Judge judge) {
        return judgeRepository.save(judge);
    }


}
