package com.dineth.debateTracker.utils;

import com.dineth.debateTracker.dtos.xmlparsing.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParseTabbycatXMLTest {
    private ParseTabbycatXML parser;
    private Document document;

    @BeforeEach
    void setUp() {
        String xmlPath = "src/test/resources/testTourney.xml";
        parser = new ParseTabbycatXML(xmlPath);
        parser.parseXML();
        document = parser.document;
    }

    @Test
    void testParseXML() {
        assertNotNull(parser.document);
        assertEquals("tournament", parser.document.getDocumentElement().getNodeName());
    }

    @Test
    void testGetTournamentDTO() {
        TournamentDTO dto = parser.getTournamentDTO(document);
        assertEquals("Shanthi Peiris Memorial Debating Championship 2024", dto.getFullName(),
                "Full name of the tournament should match");
        assertEquals("Metho 24", dto.getShortName(), "Short name of the tournament should match");
    }

    @Test
    void testGetBreakCategoryDTOs() {
        List<BreakCategoryDTO> breakCategories = parser.getBreakCategoryDTOs(document);
        assertNotNull(breakCategories, "Break categories list should not be null");
        assertFalse(breakCategories.isEmpty(), "Break categories list should not be empty");

        BreakCategoryDTO breakCategory = breakCategories.get(0);
        assertEquals("BC1", breakCategory.getId(), "Break category ID should match");
        assertEquals("Open", breakCategory.getName(), "Break category name should match");

    }

    @Test
    void testGetTeamDTOs() {
        List<TeamDTO> teams = parser.getTeamDTOs(document);
        assertNotNull(teams, "Teams list should not be null");
        assertFalse(teams.isEmpty(), "Teams list should not be empty");

        int nTeams = 33; // Expected number of teams in the tournament
        assertEquals(nTeams, teams.size(), "There should be " + nTeams + " teams in the tournament");

        //        Testing for one team
        TeamDTO team = teams.get(0);
        assertEquals("T2", team.getId(), "Team ID should match");
        assertEquals("AC A", team.getName(), "Team name should match");
        assertEquals("Crown", team.getCode(), "Team code should match");
        assertEquals(4, team.getDebaters().size(), "Team should have 4 debaters");
        assertEquals("S5", team.getDebaters().get(0).getId(), "First debater's ID should match");
        assertEquals("Kulith Wickramasinghe", team.getDebaters().get(0).getName(), "First debater's name should match");
        assertEquals("S6", team.getDebaters().get(1).getId(), "Second debater's ID should match");
        assertEquals("Kaveesha Mallawaarachchi", team.getDebaters().get(1).getName(),
                "Second debater's name should match");
        assertEquals("S7", team.getDebaters().get(2).getId(), "Third debater's ID should match");
        assertEquals("Yasindu Edussuriya", team.getDebaters().get(2).getName(), "Third debater's name should match");
        assertEquals("S8", team.getDebaters().get(3).getId(), "Fourth debater's ID should match");
        assertEquals("Kavith Perera", team.getDebaters().get(3).getName(), "Fourth debater's name should match");
    }

    @Test
    void testGetDebaterDTOs() {
        List<TeamDTO> teams = parser.getTeamDTOs(document);
        if (!teams.isEmpty()) {

            int totalDebaters = teams.stream().mapToInt(team -> team.getDebaters().size()).sum();

            Node teamNode = document.getElementsByTagName("team").item(0);
            List<DebaterDTO> debaters = parser.getDebaterDTOs(teamNode);

            int nDebaters = 126; // Expected number of debaters in the tournament
            assertEquals(nDebaters, totalDebaters, "There should be " + nDebaters + " debaters in the tournament");

            assertNotNull(debaters);
            assertFalse(debaters.isEmpty(), "Debaters list should not be empty");
            assertEquals("S5", debaters.get(0).getId());
            assertEquals("Kulith Wickramasinghe", debaters.get(0).getName());
            assertEquals("I1", debaters.get(0).getInstitutionId(), "First debater's institution ID should match");
            assertEquals("S6", debaters.get(1).getId());
            assertEquals("Kaveesha Mallawaarachchi", debaters.get(1).getName());
            assertEquals("I1", debaters.get(1).getInstitutionId(), "Second debater's institution ID should match");
            assertEquals("S7", debaters.get(2).getId());
            assertEquals("Yasindu Edussuriya", debaters.get(2).getName());
            assertEquals("I1", debaters.get(2).getInstitutionId(), "Third debater's institution ID should match");
            assertEquals("S8", debaters.get(3).getId());
            assertEquals("Kavith Perera", debaters.get(3).getName());
            assertEquals("I1", debaters.get(3).getInstitutionId(), "Fourth debater's institution ID should match");

        }
    }

    @Test
    void testGetJudgeDTOs() {
        List<JudgeDTO> judges = parser.getJudgeDTOs(document);
        int nJudges = 47; // Expected number of judges in the tournament
        assertEquals(nJudges, judges.size(), "There should be " + nJudges + " judges in the tournament");

        assertNotNull(judges);
        if (!judges.isEmpty()) {
            JudgeDTO judge = judges.get(0);
            assertEquals("A143", judge.getId(), "Judge ID should match");
            assertEquals("Rachel Cramer", judge.getName(), "Judge name should match");
            assertEquals(Float.valueOf(4.5f), judge.getScore(), "Judge score should match");
            assertFalse(judge.getCore(), "Judge should not be a core judge");
            assertFalse(judge.getIndependent(), "Judge should not be independent");
        } else {
            fail("Judges list should not be empty");
        }

    }

    //    TODO add test for feedback
    //    @Test
    //    void testGetFeedbackDTOs() {
    //        Node judgeNode = document.getElementsByTagName("adjudicator").item(0);
    //        List<FeedbackDTO> feedbacks = parser.getFeedbackDTOs(judgeNode);
    //        assertNotNull(feedbacks);
    //    }

    @Test
    void testGetInstitutionDTOs() {
        List<InstitutionDTO> institutions = parser.getInstitutionDTOs(document);
        int nInstitutions = 24; // Expected number of institutions in the tournament
        assertEquals(nInstitutions, institutions.size(),
                "There should be " + nInstitutions + " institutions in the tournament");

        InstitutionDTO institution = institutions.get(0);
        assertEquals("I1", institution.getId(), "Institution ID should match");
        assertEquals("Ananda College", institution.getName(), "Institution name should match");
        assertEquals("AC", institution.getReference(), "Institution reference should match");
    }

    @Test
    void testGetMotionDTOs() {
        List<MotionDTO> motions = parser.getMotionDTOs(document);

        MotionDTO motion1 = motions.get(8);
        assertEquals("M9", motion1.getId());

        String expectedMotion1 = "In times of a huge economic recession, THW implement a minimum spend policy";
        expectedMotion1 = expectedMotion1.replaceAll("\\s+", " ").strip();
        assertEquals(expectedMotion1, motion1.getMotion().replaceAll("\\s+", " ").strip(), "Motion text should match");

        assertEquals("Econ", motion1.getReference(), "Motion reference should match");

        String expectedInfoSlide = "A minimum spend policy requires households to spend a designated percentage of their income and/or wealth in a given period of time. This policy applies progressively, and would replace tax either partially or entirely.";
        expectedInfoSlide = expectedInfoSlide.replaceAll("\\s+", " ").strip();
        assertEquals(expectedInfoSlide, motion1.getInfoSlide().replaceAll("\\s+", " ").strip(),
                "Motion info slide should match");

        int nMotions = 10; // Expected number of motions in the tournament
        assertEquals(nMotions, motions.size(), "There should be " + nMotions + " motions in the tournament");

    }

    @Test
    void testGetRoundsDTO() {
        List<RoundDTO> rounds = parser.getRoundsDTO(document);
        assertNotNull(rounds);
    }
}
