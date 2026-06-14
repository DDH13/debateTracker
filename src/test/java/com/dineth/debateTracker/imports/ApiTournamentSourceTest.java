package com.dineth.debateTracker.imports;

import com.dineth.debateTracker.dtos.xmlparsing.DebateDTO;
import com.dineth.debateTracker.dtos.xmlparsing.FinalTeamBallotDTO;
import com.dineth.debateTracker.dtos.xmlparsing.RoundDTO;
import com.dineth.debateTracker.dtos.xmlparsing.SideDTO;
import com.dineth.debateTracker.dtos.xmlparsing.SpeechDTO;
import com.dineth.debateTracker.dtos.xmlparsing.TeamDTO;
import com.dineth.debateTracker.imports.tabbycat.TabbycatApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Unit tests for {@link ApiTournamentSource}: a Tabbycat instance is stubbed with
 * {@link MockRestServiceServer} and the resulting {@link TournamentImportData} is asserted. The focus
 * is the API-to-DTO mapping, especially the per-adjudicator-sheet → per-speaker-position inversion that
 * the XML format gives for free.
 */
class ApiTournamentSourceTest {

    private MockRestServiceServer server;
    private ApiTournamentSource source;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://tab.test");
        server = MockRestServiceServer.bindTo(builder).ignoreExpectOrder(true).build();
        source = new ApiTournamentSource(new TabbycatApiClient(builder.build()), "wudc");

        expect(endsWith("/tournaments/wudc"), """
                {"name":"World Championship 2025","short_name":"WUDC 2025","slug":"wudc"}""");
        expect(containsString("/institutions?"), """
                [{"id":1,"name":"Alpha University","code":"Alpha"}]""");
        expect(containsString("/adjudicators?"), """
                [{"id":100,"name":"Judge One","base_score":4.5,"adj_core":true,"independent":false},
                 {"id":101,"name":"Judge Two","base_score":3.5,"adj_core":false,"independent":true},
                 {"id":102,"name":"Judge Three","base_score":3.0,"adj_core":false,"independent":false}]""");
        expect(containsString("/teams?"), """
                [{"id":10,"institution":"http://tab.test/api/v1/institutions/1","long_name":"Alpha A",
                  "short_name":"Alpha A","code_name":"Apple","break_categories":[],
                  "speakers":[{"id":1000,"name":"Aaa Aaa"},{"id":1001,"name":"Bbb Bbb"}]},
                 {"id":11,"institution":"http://tab.test/api/v1/institutions/1","long_name":"Alpha B",
                  "short_name":"Alpha B","code_name":"Banana","break_categories":[],
                  "speakers":[{"id":1002,"name":"Ccc Ccc"},{"id":1003,"name":"Ddd Ddd"}]}]""");
        expect(containsString("/motions?"), """
                [{"id":5,"text":"THW abolish X","reference":"X","info_slide":""}]""");
        expect(containsString("/break-categories?"), """
                [{"id":7,"name":"Open"}]""");
        expect(containsString("/rounds?"), """
                [{"id":50,"seq":1,"name":"Round 1","abbreviation":"R1","stage":"P","feedback_weight":1.0}]""");
        expect(containsString("/rounds/1/pairings?"), """
                [{"id":20,
                  "teams":[{"team":"http://tab.test/api/v1/tournaments/wudc/teams/10","side":"aff"},
                           {"team":"http://tab.test/api/v1/tournaments/wudc/teams/11","side":"neg"}],
                  "adjudicators":{"chair":"http://tab.test/api/v1/tournaments/wudc/adjudicators/100",
                                  "panellists":["http://tab.test/api/v1/tournaments/wudc/adjudicators/101",
                                                "http://tab.test/api/v1/tournaments/wudc/adjudicators/102"],
                                  "trainees":[]}}]""");
        // One confirmed ballot, three adjudicator sheets (a 3-judge panel). All three award the win to
        // the aff team (10), so it should win 3-0 and every speech should carry three individual ballots.
        expect(containsString("/pairings/20/ballots"), ballotJson());
    }

    @Test
    void mapsScalarCollections() {
        TournamentImportData data = source.load();

        assertEquals("World Championship 2025", data.tournament().getFullName());
        assertEquals("WUDC 2025", data.tournament().getShortName());

        assertEquals(1, data.institutions().size());
        assertEquals("1", data.institutions().get(0).getId());
        assertEquals("Alpha", data.institutions().get(0).getReference());

        assertEquals(3, data.judges().size());
        assertEquals("100", data.judges().get(0).getId());
        assertEquals(4.5f, data.judges().get(0).getScore());
        assertTrue(data.judges().get(0).getCore());
        assertTrue(data.judges().get(1).getIndependent());

        assertEquals(1, data.motions().size());
        assertEquals("5", data.motions().get(0).getId());
        assertEquals("THW abolish X", data.motions().get(0).getMotion());

        assertEquals(1, data.breakCategories().size());
        assertEquals("7", data.breakCategories().get(0).getId());
    }

    @Test
    void mapsTeamsWithDebatersAndInstitution() {
        TournamentImportData data = source.load();

        assertEquals(2, data.teams().size());
        TeamDTO alphaA = data.teams().get(0);
        assertEquals("10", alphaA.getId());
        assertEquals("Alpha A", alphaA.getName());
        assertEquals("Apple", alphaA.getCode());
        assertEquals(2, alphaA.getDebaters().size());
        assertEquals("1000", alphaA.getDebaters().get(0).getId());
        // every debater's institution id must resolve to a declared InstitutionDTO id
        assertEquals("1", alphaA.getDebaters().get(0).getInstitutionId());
    }

    @Test
    void invertsSheetsIntoSidesSpeechesAndBallots() {
        TournamentImportData data = source.load();

        assertEquals(1, data.rounds().size());
        RoundDTO round = data.rounds().get(0);
        assertEquals("Round 1", round.getName());
        assertTrue(!round.isElimination());
        assertEquals(1, round.getDebates().size());

        DebateDTO debate = round.getDebates().get(0);
        assertEquals("20", debate.getId());
        assertEquals("100", debate.getChairId());
        assertEquals("100 101 102", debate.getAdjudicatorIds());
        assertEquals("5", debate.getMotionId());
        assertEquals(2, debate.getSides().size());

        SideDTO prop = debate.getSides().get(0);
        SideDTO opp = debate.getSides().get(1);
        assertEquals("10", prop.getTeamId());
        assertEquals("11", opp.getTeamId());

        // 3 sheets -> 3 final team ballots per side; aff ranked 1 by all, neg ranked 2 by all
        assertEquals(3, prop.getFinalTeamBallots().size());
        assertEquals(3, opp.getFinalTeamBallots().size());
        assertTrue(prop.getFinalTeamBallots().stream().allMatch(b -> b.getRank() == 1));
        assertTrue(opp.getFinalTeamBallots().stream().allMatch(b -> b.getRank() == 2));

        // 2 speakers per side, each with one ballot per adjudicator (3)
        assertEquals(2, prop.getSpeeches().size());
        SpeechDTO firstSpeech = prop.getSpeeches().get(0);
        assertEquals("1000", firstSpeech.getSpeakerId());
        assertEquals(1, firstSpeech.getSpeakerPosition());
        assertEquals(3, firstSpeech.getIndividualSpeechBallots().size());
        List<String> adjOnSpeech = firstSpeech.getIndividualSpeechBallots().stream()
                .map(b -> b.getAdjudicatorId()).toList();
        assertEquals(List.of("100", "101", "102"), adjOnSpeech);
        assertEquals(75.0, firstSpeech.getIndividualSpeechBallots().get(0).getScore());
    }

    @Test
    void skipsByeWithFewerThanTwoSides() {
        // Re-stub the pairings endpoint with a single-team bye; everything else stays the same.
        RestClient.Builder builder = RestClient.builder().baseUrl("http://tab.test");
        MockRestServiceServer byeServer = MockRestServiceServer.bindTo(builder).ignoreExpectOrder(true).build();
        ApiTournamentSource byeSource = new ApiTournamentSource(new TabbycatApiClient(builder.build()), "wudc");
        stubAllButPairings(byeServer);
        byeServer.expect(requestTo(containsString("/rounds/1/pairings?"))).andRespond(withSuccess("""
                [{"id":21,"teams":[{"team":"http://tab.test/api/v1/tournaments/wudc/teams/10","side":"bye"}],
                  "adjudicators":null}]""", MediaType.APPLICATION_JSON));

        RoundDTO round = byeSource.load().rounds().get(0);
        assertEquals(0, round.getDebates().size());
    }

    private void expect(org.hamcrest.Matcher<String> uri, String body) {
        server.expect(requestTo(uri)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
    }

    private void stubAllButPairings(MockRestServiceServer s) {
        s.expect(requestTo(endsWith("/tournaments/wudc"))).andRespond(withSuccess(
                "{\"name\":\"World Championship 2025\",\"short_name\":\"WUDC 2025\",\"slug\":\"wudc\"}",
                MediaType.APPLICATION_JSON));
        s.expect(requestTo(containsString("/institutions?"))).andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        s.expect(requestTo(containsString("/adjudicators?"))).andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        s.expect(requestTo(containsString("/teams?"))).andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        s.expect(requestTo(containsString("/motions?"))).andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        s.expect(requestTo(containsString("/break-categories?"))).andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        s.expect(requestTo(containsString("/rounds?"))).andRespond(withSuccess(
                "[{\"id\":50,\"seq\":1,\"name\":\"Round 1\",\"abbreviation\":\"R1\",\"stage\":\"P\",\"feedback_weight\":1.0}]",
                MediaType.APPLICATION_JSON));
    }

    private static String ballotJson() {
        String sheet = """
                {"adjudicator":"%s",
                 "teams":[
                   {"team":"http://tab.test/api/v1/tournaments/wudc/teams/10","side":"aff","win":true,"score":149.0,
                    "speeches":[{"speaker":"http://tab.test/api/v1/tournaments/wudc/speakers/1000","score":75.0},
                                {"speaker":"http://tab.test/api/v1/tournaments/wudc/speakers/1001","score":74.0}]},
                   {"team":"http://tab.test/api/v1/tournaments/wudc/teams/11","side":"neg","win":false,"score":145.0,
                    "speeches":[{"speaker":"http://tab.test/api/v1/tournaments/wudc/speakers/1002","score":73.0},
                                {"speaker":"http://tab.test/api/v1/tournaments/wudc/speakers/1003","score":72.0}]}]}""";
        String a100 = sheet.formatted("http://tab.test/api/v1/tournaments/wudc/adjudicators/100");
        String a101 = sheet.formatted("http://tab.test/api/v1/tournaments/wudc/adjudicators/101");
        String a102 = sheet.formatted("http://tab.test/api/v1/tournaments/wudc/adjudicators/102");
        return "[{\"id\":900,\"confirmed\":true,\"discarded\":false,"
                + "\"motion\":\"http://tab.test/api/v1/tournaments/wudc/motions/5\","
                + "\"result\":{\"sheets\":[" + a100 + "," + a101 + "," + a102 + "]}}]";
    }
}
