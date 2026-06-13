package com.dineth.debateTracker;

import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.debater.DebaterService;
import com.dineth.debateTracker.dtos.TournamentDataDTO;
import com.dineth.debateTracker.dtos.xmlparsing.RoundDTO;
import com.dineth.debateTracker.utils.ParseCSV;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP entry points for importing Tabbycat tournament XML. All persistence logic lives in
 * {@link TournamentImportService}; this controller only resolves file paths and delegates.
 */
@RestController
@Slf4j
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/tournament")
public class TournamentBuilder {

    private static final String SPEAKS_XML_BASE_PATH = "src/main/resources/static/speaksXML";
    private static final int DEFAULT_BUILD_YEAR = 2025;
    private static final int DEFAULT_SUMMARY_YEAR = 2024;

    private final TournamentImportService importService;
    private final DebaterService debaterService;

    @Autowired
    public TournamentBuilder(TournamentImportService importService, DebaterService debaterService) {
        this.importService = importService;
        this.debaterService = debaterService;
    }

    @GetMapping("/build")
    public TournamentDataDTO buildTournament(@RequestParam String fileName,
            @RequestParam(required = false) Integer year) {
        int selectedYear = year != null ? year : DEFAULT_BUILD_YEAR;
        return importService.importTournament(buildTournamentFilePath(fileName, selectedYear));
    }

    @GetMapping("/buildall")
    public List<TournamentDataDTO> buildAllTournaments(@RequestParam(required = false) Integer year) {
        List<String> fileNames = new ArrayList<>();
        List<TournamentDataDTO> tournamentDataList = new ArrayList<>();
        int selectedYear = year != null ? year : DEFAULT_BUILD_YEAR;

        try {
            Path folderPath = buildTournamentDirectoryPath(selectedYear);
            if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
                log.warn("Tournament XML directory not found for year {} at {}", selectedYear, folderPath);
                return tournamentDataList;
            }
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath, "*.xml")) {
                for (Path entry : stream) {
                    fileNames.add(entry.getFileName().toString());
                }
            }
        } catch (IOException e) {
            log.error("Error in reading files : " + e.getMessage());
            return tournamentDataList; // Return empty list on error
        }

        for (String fileName : fileNames) {
            try {
                tournamentDataList.add(importService.importTournament(buildTournamentFilePath(fileName, selectedYear)));
                log.info("Built tournament : " + fileName);
            } catch (Exception e) {
                log.error("Error building tournament " + fileName + ": " + e.getMessage(), e);
            }
        }
        return tournamentDataList;
    }

    @GetMapping("/parsecsv")
    public Object parseCSV() {
        List<Debater> debaters = new ParseCSV("src/main/resources/static/Debater_Information.csv").parseDebaterInfo();
        for (Debater debater : debaters) {
            try {
                Debater existingDebater = debaterService.checkIfDebaterExists(debater);
                if (existingDebater != null) {
                    log.debug("Debater already exists");
                } else {
                    log.debug("Adding debater");
                    debaterService.addDebater(debater);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return debaters;
    }

    /**
     * Get tournament data summary - useful for checking what data was parsed
     */
    @GetMapping("/summary")
    public String getTournamentSummary(@RequestParam String fileName,
            @RequestParam(required = false) Integer year) {
        int selectedYear = year != null ? year : DEFAULT_SUMMARY_YEAR;
        TournamentDataDTO tournamentData;
        try {
            tournamentData = importService.importTournament(buildTournamentFilePath(fileName, selectedYear));
        } catch (Exception e) {
            return "Failed to build tournament data: " + e.getMessage();
        }

        StringBuilder summary = new StringBuilder();
        summary.append("Tournament: ").append(tournamentData.getTournament().getFullName()).append("\n");
        summary.append("Short Name: ").append(tournamentData.getTournament().getShortName()).append("\n");
        summary.append("Metadata: ").append(tournamentData.getMetadata().toString()).append("\n");

        summary.append("\nRound Summary:\n");
        for (RoundDTO round : tournamentData.getRounds()) {
            summary.append("- ").append(round.getName()).append(" (")
                    .append(round.isElimination() ? "Elimination" : "Preliminary").append(")").append(" - ")
                    .append(round.getDebates().size()).append(" debates\n");
        }

        return summary.toString();
    }

    private String buildTournamentFilePath(String fileName, int year) {
        return SPEAKS_XML_BASE_PATH + "/" + year + "/" + fileName;
    }

    private Path buildTournamentDirectoryPath(int year) {
        return Paths.get(SPEAKS_XML_BASE_PATH, String.valueOf(year));
    }
}
