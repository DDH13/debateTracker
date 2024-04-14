package com.dineth.debateTracker.debate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DebateRepository extends JpaRepository<Debate, Long> {
}
