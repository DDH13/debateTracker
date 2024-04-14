package com.dineth.debateTracker.breakcategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BreakCategoryRepository extends JpaRepository<BreakCategory, Long> {
}
