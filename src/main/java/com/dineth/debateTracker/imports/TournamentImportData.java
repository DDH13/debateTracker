package com.dineth.debateTracker.imports;

import com.dineth.debateTracker.dtos.xmlparsing.BreakCategoryDTO;
import com.dineth.debateTracker.dtos.xmlparsing.InstitutionDTO;
import com.dineth.debateTracker.dtos.xmlparsing.JudgeDTO;
import com.dineth.debateTracker.dtos.xmlparsing.MotionDTO;
import com.dineth.debateTracker.dtos.xmlparsing.RoundDTO;
import com.dineth.debateTracker.dtos.xmlparsing.TeamDTO;
import com.dineth.debateTracker.dtos.xmlparsing.TournamentDTO;

import java.util.List;

/**
 * The complete, source-agnostic input to {@link com.dineth.debateTracker.TournamentImportService}.
 *
 * <p>These are exactly the seven DTO collections the importer needs; the importer knows nothing about
 * where they came from. A {@link TournamentSource} produces this bundle from a Tabbycat XML export, the
 * Tabbycat REST API, or any other origin. The collections use the {@code dtos/xmlparsing} types for
 * historical reasons — they are the importer's contract, not an XML-only model.
 */
public record TournamentImportData(
        TournamentDTO tournament,
        List<InstitutionDTO> institutions,
        List<JudgeDTO> judges,
        List<TeamDTO> teams,
        List<MotionDTO> motions,
        List<RoundDTO> rounds,
        List<BreakCategoryDTO> breakCategories
) {
}
