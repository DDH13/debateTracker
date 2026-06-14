package com.dineth.debateTracker.imports.tabbycat;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Thin client over the Tabbycat REST API. Wraps a configured {@link RestClient}; use
 * {@link #create(String, String)} to build one for a given instance base URL + auth token, or pass a
 * pre-built {@link RestClient} directly (used by tests to bind a mock server).
 *
 * <p>Tabbycat list endpoints return bare JSON arrays (no pagination envelope) but accept
 * {@code limit}/{@code offset}; {@link #getPaged} walks those pages defensively in case the server caps
 * a response, stopping once a short page is returned.
 */
public class TabbycatApiClient {

    private static final int PAGE_SIZE = 100;

    private final RestClient restClient;

    public TabbycatApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /** Build a client for a Tabbycat instance. The token is sent as {@code Authorization: Token <token>}. */
    public static TabbycatApiClient create(String baseUrl, String token) {
        RestClient client = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Token " + token)
                .build();
        return new TabbycatApiClient(client);
    }

    public Tabbycat.Tournament getTournament(String slug) {
        return restClient.get()
                .uri("/api/v1/tournaments/{slug}", slug)
                .retrieve()
                .body(Tabbycat.Tournament.class);
    }

    public List<Tabbycat.Institution> listInstitutions(String slug) {
        return getPaged("/api/v1/tournaments/{slug}/institutions",
                new ParameterizedTypeReference<>() {}, slug);
    }

    public List<Tabbycat.Adjudicator> listAdjudicators(String slug) {
        return getPaged("/api/v1/tournaments/{slug}/adjudicators",
                new ParameterizedTypeReference<>() {}, slug);
    }

    public List<Tabbycat.Team> listTeams(String slug) {
        return getPaged("/api/v1/tournaments/{slug}/teams",
                new ParameterizedTypeReference<>() {}, slug);
    }

    public List<Tabbycat.Motion> listMotions(String slug) {
        return getPaged("/api/v1/tournaments/{slug}/motions",
                new ParameterizedTypeReference<>() {}, slug);
    }

    public List<Tabbycat.BreakCategory> listBreakCategories(String slug) {
        return getPaged("/api/v1/tournaments/{slug}/break-categories",
                new ParameterizedTypeReference<>() {}, slug);
    }

    public List<Tabbycat.Round> listRounds(String slug) {
        return getPaged("/api/v1/tournaments/{slug}/rounds",
                new ParameterizedTypeReference<>() {}, slug);
    }

    public List<Tabbycat.Pairing> listPairings(String slug, int roundSeq) {
        return getPaged("/api/v1/tournaments/{slug}/rounds/{seq}/pairings",
                new ParameterizedTypeReference<>() {}, slug, roundSeq);
    }

    /** Confirmed ballots for one debate. Filtered server-side with {@code ?confirmed=true}. */
    public List<Tabbycat.Ballot> listConfirmedBallots(String slug, int roundSeq, int debatePk) {
        List<Tabbycat.Ballot> all = new ArrayList<>();
        int offset = 0;
        while (true) {
            final int off = offset;
            List<Tabbycat.Ballot> page = restClient.get()
                    .uri(b -> b.path("/api/v1/tournaments/{slug}/rounds/{seq}/pairings/{pk}/ballots")
                            .queryParam("confirmed", true)
                            .queryParam("limit", PAGE_SIZE)
                            .queryParam("offset", off)
                            .build(slug, roundSeq, debatePk))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Tabbycat.Ballot>>() {});
            if (page == null || page.isEmpty()) {
                break;
            }
            all.addAll(page);
            if (page.size() < PAGE_SIZE) {
                break;
            }
            offset += PAGE_SIZE;
        }
        return all;
    }

    private <T> List<T> getPaged(String path, ParameterizedTypeReference<List<T>> type, Object... pathVars) {
        List<T> all = new ArrayList<>();
        int offset = 0;
        while (true) {
            final int off = offset;
            List<T> page = restClient.get()
                    .uri(b -> b.path(path)
                            .queryParam("limit", PAGE_SIZE)
                            .queryParam("offset", off)
                            .build(pathVars))
                    .retrieve()
                    .body(type);
            if (page == null || page.isEmpty()) {
                break;
            }
            all.addAll(page);
            if (page.size() < PAGE_SIZE) {
                break;
            }
            offset += PAGE_SIZE;
        }
        return all;
    }
}
