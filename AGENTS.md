# Debate Tracker — Agent Reference

A Spring Boot REST API that imports competitive debate tournament data from [Tabbycat](https://tabbycat.readthedocs.io/) — either an XML export or the live Tabbycat REST API — persists it to PostgreSQL, and exposes statistics and profile endpoints for debaters and judges.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.0, Spring Data JPA |
| Database (prod) | PostgreSQL 5432, `debatetracker` |
| Database (test) | H2 in-memory |
| Build | Maven 3.6+ |
| Code gen | Lombok 1.18 |
| XML parsing | Jackson Dataformat XML |
| Tabbycat API client | Spring `RestClient` (+ Jackson) |
| Statistics | Apache Commons Math 3 |
| String matching | Apache Commons Text (fuzzy matching for dedup) |
| Graph algorithms | JGraphT |
| API docs | SpringDoc OpenAPI → `/swagger-ui.html` |

---

## Package Layout

```
com.dineth.debateTracker/
├── <entity>/               # One package per domain entity
│   ├── <Entity>.java       # JPA entity
│   ├── <Entity>Repository.java
│   ├── <Entity>Service.java
│   └── <Entity>Controller.java
├── dtos/
│   ├── xmlparsing/         # The importer's input DTOs (historically named; produced by every TournamentSource, not just XML)
│   ├── statistics/         # Output DTOs for stat endpoints
│   ├── debaterprofiles/    # Debater profile sub-DTOs
│   └── SpeakerTab/         # Speaker leaderboard DTOs
├── imports/                # Source-agnostic import layer (see "Import Pipeline")
│   ├── TournamentSource.java        # Interface: load() → TournamentImportData (all data gathered up front)
│   ├── TournamentImportData.java    # Record bundling the 7 DTO collections the importer consumes
│   ├── XmlTournamentSource.java     # TournamentSource backed by a Tabbycat XML export (wraps ParseTabbycatXML)
│   ├── ApiTournamentSource.java     # TournamentSource backed by the Tabbycat REST API (maps JSON → the same DTOs)
│   └── tabbycat/                    # Tabbycat REST client + Jackson response models
│       ├── TabbycatApiClient.java   # RestClient wrapper; create(baseUrl, token); limit/offset paging
│       └── Tabbycat.java            # Jackson records for the read subset of the API
├── utils/                  # StringUtil, RoundUtil, ProfileUtil, Constants, CustomExceptions, ParseTabbycatXML
├── TournamentImportService.java  # The DTO → entity persistence pipeline (one @Transactional import)
├── TournamentBuilder.java        # Thin @RestController; resolves the source (file path or API params) and delegates
└── DebateTrackerApplication.java
```

All controllers use `@CrossOrigin(origins = "*")` and are versioned under `/api/v1/<resource>`.

---

## Domain Model

**14 entities.** Core relationships:

- **Tournament** owns Rounds, Motions, and BreakCategories.
- **Round** (prelim or elimination, tracked by `roundNo` and `isBreakRound`) owns Debates.
- **Debate** links a proposition Team and opposition Team, records a winner, and owns Ballots/EliminationBallots.
- **Ballot** records one judge's speaker score for one debater in one debate.
- **Team** has a many-to-many with **Debater**. Debaters belong to an **Institution**.
- **Judge/Adjudicator** is referenced by Ballots and Feedback.
- **DebaterProfile** and **JudgeProfile** are computed aggregates (not raw relational data).

All entities carry `createdAt`/`updatedAt` managed by `@PrePersist`/`@PreUpdate`. IDs use PostgreSQL sequences.

---

## Key Files

| File | Role |
|---|---|
| `TournamentImportService.java` | Reads a `TournamentSource`'s DTOs and writes all entities to the DB; the main import pipeline (runs as one transaction) |
| `TournamentValidationService.java` | Read-only dry-run validator for uploaded XML; returns a `ValidationReportDTO` without persisting |
| `TournamentBuilder.java` | `@RestController` for `/api/v1/tournament/*`; resolves the source and delegates to the import/validation services |
| `imports/TournamentSource.java` | The import contract: any source (XML, API, ...) produces a `TournamentImportData` bundle the importer consumes |
| `imports/ApiTournamentSource.java` | Fetches a tournament from the Tabbycat REST API and maps it into the same DTOs as the XML path |
| `imports/tabbycat/TabbycatApiClient.java` | `RestClient` wrapper over the Tabbycat API; per-instance base URL + `Authorization: Token …`; paginates list endpoints |
| `utils/ParseTabbycatXML.java` | Deserializes Tabbycat XML into `dtos/xmlparsing/` DTOs (from a file path or an `InputStream`); used by `XmlTournamentSource` |
| `statistics/StatisticsService.java` | Percentile ranks, win/loss ratios, judge sentiment |
| `debaterprofile/DebaterProfileService.java` | Aggregates multi-tournament debater stats into a profile |
| `judgeprofile/JudgeProfileService.java` | Aggregates judge activity and sentiment into a profile |
| `institution/InstitutionService.java` | Fuzzy-matches institution names to avoid duplicates |
| `replacement/ReplacementService.java` | Merges duplicate debaters / judges across tournaments |
| `utils/StringUtil.java` | Name normalization used throughout |
| `utils/RoundUtil.java` | Round ordering and comparison |
| `src/main/resources/application.properties` | DB URL, JPA DDL mode (`update`), API key |
| `src/test/resources/application-test.properties` | H2, DDL `create-drop` |
| `docs/TESTING.md` | Full testing guide — read this before writing tests |

---

## Import Pipeline

**The import is decoupled from where the data comes from.** A `TournamentSource` (in `imports/`) gathers all data up front and returns it as a `TournamentImportData` record — the seven DTO collections (`dtos/xmlparsing/`) the importer needs. The importer never does source-specific I/O, so the same persistence runs for any source:

- `XmlTournamentSource` — wraps `ParseTabbycatXML` (a Tabbycat XML export, from a file path or `InputStream`).
- `ApiTournamentSource` — fetches from the Tabbycat REST API via `TabbycatApiClient` and maps the JSON into the **same** DTOs. No XML is synthesized.

`importTournament(TournamentSource)` is the real entry point; `importTournament(String filePath)` is a thin overload that wraps a path in an `XmlTournamentSource`. It is one `@Transactional` method:

1. `source.load()` produces the `TournamentImportData` bundle (all parsing / network I/O happens here, before the transaction does any writes).
2. Persist entities in dependency order: `saveInstitutions` → `saveJudges` → `saveTeams`/`saveDebaters` → tournament → `saveBreakCategories` → `saveMotions`.
3. `buildLookupCaches` batch-loads judges/teams/motions/debaters into `Map<Long, Entity>` caches (keyed by db id) so the rounds loop never queries per debate.
4. `processRound` → `buildDebate` builds debates/ballots from the caches; `buildDebate` returns `null` to skip a debate (unresolved teams or ballot-count mismatch).

The whole method rolls back on any exception. Source-id→DTO maps (`debaterDTOMap`, etc.) are import-internal working state — they are **not** returned in `TournamentDataDTO`.

**Entry points:** `GET /api/v1/tournament/build?fileName=&year=` (XML on disk) and `POST /api/v1/tournament/build-from-api?baseUrl=&token=&slug=` (live Tabbycat API; credentials are per-request, so one deployment can pull from any instance).

**API → DTO mapping (`ApiTournamentSource`):** the API differs from the XML export in ways the source normalizes — integer PKs / hyperlink URIs are reduced to the string ids the importer's lookup maps expect; ballots are fetched `?confirmed=true` and `discarded` ones dropped (the XML export only contains the confirmed result); per-adjudicator scoresheets are **inverted** into the side-level speeches + one `IndividualSpeechBallotDTO` per adjudicator that the importer expects; consensus (no-adjudicator) sheets are attributed to the chair; sides are ordered aff/prop-first; byes (fewer than two sides) are skipped. Verified equivalent to the XML import on a real tournament (identical counts, name sets, and per-score/rank histograms).

**Dry-run validation:** `POST /api/v1/tournament/validate` (multipart `file`) → `TournamentValidationService` parses an uploaded XML and returns a `ValidationReportDTO` (`dtos/validation/`) of findings — missing/single-word names, ballot-count mismatches, empty teams, plus read-only DB cross-checks (speaker/tournament already exists). When a speaker name matches existing debaters, the finding carries each candidate's teams and institution (`DebaterMatch`) so a human can tell apart speakers who share a name (or whose names are misspelled / missing a last name) — single matches are `DEBATER_EXISTS` (INFO), multiple are `DEBATER_AMBIGUOUS` (WARNING). Institutions are cross-checked the same way: an uploaded institution that matches an existing one (mirroring the importer's `findInstitutionByName` reuse) yields an `INSTITUTION_MATCH` (INFO) carrying the matched institution's identity (`InstitutionMatch`), so a reuse — or an unintended merge of a differently-spelled name — is visible before import. It is strictly read-only (never persists) and never throws for a bad file (a malformed upload is reported as a `PARSE_ERROR` finding). Use this to preview an import before committing one.

---

## Build & Run

```bash
# Build
mvn clean install

# Run (requires local PostgreSQL with database 'debatetracker')
mvn spring-boot:run

# API docs
open http://localhost:8080/swagger-ui.html
```

---

## Tests

Tests use H2 and the `test` Spring profile. No external services are needed.

```bash
# All tests
mvn test

# E2E tests only (full XML import → DB → assertions)
mvn test -Dtest="*E2ETest"

# Service unit tests only
mvn test -Dtest="*ServiceTest"

# Single test class
mvn test -Dtest=TournamentDataE2ETest

# Single test method
mvn test -Dtest=TournamentDataE2ETest#testTournamentBasicDetails
```

### Test Infrastructure

| Class | Purpose |
|---|---|
| `e2e/BaseE2ETest.java` | Loads XML fixtures and builds tournament; extend for E2E tests |
| `builders/TestDataBuilder.java` | Fluent builder for constructing test entities in isolation |
| `builders/TestFixtures.java` | Centralized expected values (counts, scores, names) for all three XML fixtures |

Three XML tournament fixtures live in `src/test/resources/`: `testTourney.xml` (33 teams, 126 debaters, 47 judges, 9 rounds), `testTourney2.xml`, `testTourney3.xml`.

---

## Conventions

- **Lombok everywhere:** Entities and DTOs use `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`. Avoid writing manual getters/setters.
- **DTO separation:** Controllers return DTOs, never raw entities. Create a DTO in the appropriate `dtos/` sub-package.
- **Service transactions:** Complex multi-entity operations belong in a `@Transactional` service method.
- **No business logic in controllers:** Controllers call one service method and return the result.
- **Fuzzy matching for dedup:** `InstitutionService` and `ReplacementService` use Commons Text similarity — follow the same pattern for any new deduplication logic.
- **Cascade:** Set cascade types deliberately; `TournamentImportService` is the authoritative example of how related entities are linked during import.
- **ID generation:** Use `@SequenceGenerator` + `@GeneratedValue` matching the existing pattern in any new entity.

---

## Common Tasks

### Adding a new entity

1. Create `<Entity>.java` in a new package with `@Entity`, `@SequenceGenerator`, Lombok annotations, and `createdAt`/`updatedAt`.
2. Create `<Entity>Repository.java` extending `JpaRepository`.
3. Create `<Entity>Service.java` with `@Service`; inject the repository.
4. Create `<Entity>Controller.java` with `@RestController`, `@RequestMapping("/api/v1/<resource>")`, and `@CrossOrigin(origins = "*")`.
5. If the entity is imported from Tabbycat, add a corresponding DTO to `dtos/xmlparsing/` and populate it in **every** `TournamentSource` (`XmlTournamentSource`/`ParseTabbycatXML` and `ApiTournamentSource`), then consume it in `TournamentImportService`.

### Adding a new statistic

1. Add the calculation to `StatisticsService` (or `DebaterProfileService` / `JudgeProfileService` if profile-scoped).
2. Create a DTO in `dtos/statistics/` or the relevant sub-package.
3. Expose it via the appropriate existing controller or add a new endpoint.
4. Add an E2E assertion in `ProfileAndStatisticsE2ETest` using values from `TestFixtures`.
