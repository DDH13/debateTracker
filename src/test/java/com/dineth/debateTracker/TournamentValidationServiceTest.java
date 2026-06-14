package com.dineth.debateTracker;

import com.dineth.debateTracker.builders.TestFixtures;
import com.dineth.debateTracker.dtos.validation.ValidationReportDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class TournamentValidationServiceTest {

    @Autowired
    private TournamentValidationService validationService;

    @Autowired
    private TournamentImportService importService;

    private MockMultipartFile fileFrom(String path) throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of(path));
        return new MockMultipartFile("file", "tournament.xml", "application/xml", bytes);
    }

    @Test
    void realFixtureProducesValidReportWithCorrectCounts() throws Exception {
        ValidationReportDTO report = validationService.validate(fileFrom(TestFixtures.TOURNAMENT_1_XML));

        assertTrue(report.isValid(), "A real Tabbycat export should have no ERROR findings");
        assertEquals(0, report.getErrorCount());
        assertEquals(TestFixtures.Tournament1.EXPECTED_TEAMS, report.getSummary().get("teams").intValue());
        assertEquals(TestFixtures.Tournament1.EXPECTED_ROUNDS, report.getSummary().get("rounds").intValue());
        assertEquals(TestFixtures.Tournament1.EXPECTED_MOTIONS, report.getSummary().get("motions").intValue());
    }

    @Test
    void malformedFileReportsParseErrorInsteadOfThrowing() {
        MockMultipartFile bad = new MockMultipartFile("file", "bad.xml", "application/xml",
                "<tournament><not-closed>".getBytes());

        ValidationReportDTO report = validationService.validate(bad);

        assertFalse(report.isValid());
        assertTrue(report.getFindings().stream().anyMatch(f -> "PARSE_ERROR".equals(f.code())),
                "Malformed XML should yield a PARSE_ERROR finding, not a thrown exception");
    }

    @Test
    void emptyFileReportsEmptyFile() {
        MockMultipartFile empty = new MockMultipartFile("file", "empty.xml", "application/xml", new byte[0]);

        ValidationReportDTO report = validationService.validate(empty);

        assertFalse(report.isValid());
        assertTrue(report.getFindings().stream().anyMatch(f -> "EMPTY_FILE".equals(f.code())));
    }

    @Test
    void crossChecksFlagAlreadyImportedTournamentAndDebaters() throws Exception {
        // Import the tournament, then validate the same file: the read-only DB cross-checks should fire.
        importService.importTournament(TestFixtures.TOURNAMENT_1_XML);

        ValidationReportDTO report = validationService.validate(fileFrom(TestFixtures.TOURNAMENT_1_XML));

        assertTrue(report.getFindings().stream().anyMatch(f -> "TOURNAMENT_EXISTS".equals(f.code())),
                "Re-validating an imported tournament should warn that it already exists");
        assertTrue(report.getFindings().stream().anyMatch(f -> "DEBATER_EXISTS".equals(f.code())),
                "Speakers already in the DB should be flagged as existing");

        // The disambiguating context (existing debater's teams) must be attached so a human can
        // tell apart speakers with the same/misspelled/last-name-less names.
        assertTrue(report.getFindings().stream()
                        .filter(f -> "DEBATER_EXISTS".equals(f.code()))
                        .anyMatch(f -> !f.matches().isEmpty() && f.matches().stream().anyMatch(m -> !m.teams().isEmpty())),
                "A DEBATER_EXISTS finding should carry the existing debater's team context");
    }
}
