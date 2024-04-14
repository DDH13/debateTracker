package com.dineth.debateTracker.breakcategory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BreakCategoryService {
    private final BreakCategoryRepository breakCategoryRepository;

    @Autowired
    public BreakCategoryService(BreakCategoryRepository breakCategoryRepository) {
        this.breakCategoryRepository = breakCategoryRepository;
    }

    public BreakCategory addBreakCategory(BreakCategory breakCategory) {
        return breakCategoryRepository.save(breakCategory);
    }

    public BreakCategory findBreakCategoryById(Long id) {
        return breakCategoryRepository.findById(id).orElse(null);
    }
}
