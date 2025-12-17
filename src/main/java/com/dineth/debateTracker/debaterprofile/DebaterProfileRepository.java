package com.dineth.debateTracker.debaterprofile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DebaterProfileRepository extends JpaRepository<DebaterProfile, Long> {
    DebaterProfile getDebaterProfileByDebaterId(Long debaterId);
}
