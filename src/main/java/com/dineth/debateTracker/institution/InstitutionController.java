package com.dineth.debateTracker.institution;

import com.dineth.debateTracker.utils.ReplacementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController @Slf4j
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/institution")
public class InstitutionController {
    private final InstitutionService institutionService;
    private final ReplacementService replacementService;

    @Autowired
    public InstitutionController(InstitutionService institutionService, ReplacementService replacementService) {
        this.institutionService = institutionService;
        this.replacementService = replacementService;
    }

    @GetMapping
    public List<Institution> getInstitutions() {
        return institutionService.getInstitutions();
    }

    /**
     * Get institutions with similar names
     * @param name - name of the institution
     * @return List of institutions with similar names
     */
    @GetMapping(path = "similar")
    public List<String> getInstitutionsWithSimilarNames(@RequestParam String name) {
        return institutionService.getInstitutionsWithSimilarNames(name);
    }

    /**
     * Takes in an array of institution ids and merges them into one institution (the first id in the list)
     * @param values - Map of institution ids
     */
    @PostMapping(path = "merge")
    public Institution mergeInstitution(@RequestBody Map<String, List<Long>> values) {
        try {
            List<Long> institutionIds = values.get("institutionIds");
            return replacementService.mergeMultipleInstitutions(institutionIds);
        } catch (Exception e) {
            log.error("Error merging institutions: {}", e.getMessage());
        }
        return null;
    }

    @PostMapping
    public Institution addInstitution(@RequestBody Institution debater) {
        return institutionService.addInstitution(debater);
    }



}
