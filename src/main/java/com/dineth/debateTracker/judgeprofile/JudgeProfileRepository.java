package com.dineth.debateTracker.judgeprofile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface JudgeProfileRepository extends JpaRepository <JudgeProfile, Long> {
    
    JudgeProfile findByJudgeId(Long judgeId);

    Collection<JudgeProfile> findAllByJudgeIdIn(List<Long> judgeIds);
}
