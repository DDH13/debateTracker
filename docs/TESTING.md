# Testing Guide - Debate Tracker

> **Last Updated:** April 29, 2026  
> **Version:** 2.0 (Restructured)

## Table of Contents
- [Overview](#overview)
- [Test Structure](#test-structure)
- [Running Tests](#running-tests)
- [Test Data](#test-data)
- [Writing Tests](#writing-tests)
- [Maintenance](#maintenance)

---

## Overview

The Debate Tracker test suite provides comprehensive coverage across three testing levels:

- **Unit Tests** - Isolated service layer testing with mocked dependencies
- **Integration Tests** - Controller and multi-component integration testing
- **E2E Tests** - Full application workflow testing with real database

### Test Statistics

- **Total Test Files:** 17+
- **E2E Test Files:** 3
- **Unit Test Files:** 11+
- **Test Database:** H2 in-memory
- **Test Data Files:** 3 XML tournament files

---

## Test Structure

### Package Organization

```
src/test/java/com/dineth/debateTracker/
├── e2e/                                    # End-to-End Tests
│   ├── BaseE2ETest.java                   # Base class for E2E tests
│   ├── TournamentDataE2ETest.java         # Tournament data verification
│   ├── ControllerIntegrationTest.java     # Controller endpoint testing
│   └── ProfileAndStatisticsE2ETest.java   # Profile & statistics testing
│
├── integration/                            # Integration Tests (placeholder)
│   └── (future integration tests)
│
├── ballot/                                 # Service Unit Tests
│   └── BallotServiceTest.java
├── debate/
│   └── DebateServiceTest.java
├── debater/
│   └── DebaterServiceTest.java
├── institution/
│   └── InstitutionServiceTest.java
├── statistics/
│   └── StatisticsServiceTest.java
├── team/
│   └── TeamServiceTest.java
├── utils/
│   ├── ParseTabbycatXMLTest.java
│   └── StringUtilTest.java
│
└── builders/                               # Test Infrastructure
    ├── TestDataBuilder.java               # Fluent test data builder
    └── TestFixtures.java                  # Centralized test fixtures
```

### Test Naming Conventions

| Test Type | Naming Pattern | Example |
|-----------|---------------|---------|
| Unit Tests | `*ServiceTest.java` | `DebaterServiceTest.java` |
| Integration Tests | `*IntegrationTest.java` | `ControllerIntegrationTest.java` |
| E2E Tests | `*E2ETest.java` | `TournamentDataE2ETest.java` |
| Utility Tests | `*Test.java` | `StringUtilTest.java` |

---

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Categories

**Unit Tests Only:**
```bash
mvn test -Dtest="*ServiceTest"
```

**E2E Tests Only:**
```bash
mvn test -Dtest="*E2ETest"
```

**Integration Tests Only:**
```bash
mvn test -Dtest="*IntegrationTest"
```

### Run Specific Test Class

```bash
mvn test -Dtest=TournamentDataE2ETest
```

### Run Specific Test Method

```bash
mvn test -Dtest=TournamentDataE2ETest#testTournamentBasicDetails
```

### Skip Tests During Build

```bash
mvn clean install -DskipTests
```

### View Test Results

Test reports are generated in:
- `target/surefire-reports/` (text and XML format)
- Console output with detailed logging

---

## Test Data

### Tournament XML Files

Located in `src/test/resources/`:

1. **testTourney.xml** - Primary tournament data
   - 33 Teams
   - 126 Debaters
   - 47 Judges
   - 24 Institutions
   - 10 Motions
   - 9 Rounds (6 prelim + 3 elimination)

2. **testTourney2.xml** - Secondary tournament for multi-tournament testing

3. **testTourney3.xml** - Tertiary tournament for multi-tournament testing

### Test Database Configuration

File: `src/test/resources/application-test.properties`

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
```

### Expected Data

Test expectations are centralized in `TestFixtures.java`:

```java
TestFixtures.Tournament1.EXPECTED_TEAMS        // 33
TestFixtures.Tournament1.EXPECTED_DEBATERS     // 126
TestFixtures.Tournament1.EXPECTED_JUDGES       // 47
// ... etc
```

---

## Writing Tests

### Unit Tests Pattern

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("Service Tests")
class MyServiceTest {
    
    @Mock
    private MyRepository repository;
    
    @InjectMocks
    private MyService service;
    
    @Nested
    @DisplayName("Method Tests")
    class MethodTests {
        
        @Test
        @DisplayName("Should do something when condition")
        void testMethod() {
            // Arrange
            when(repository.find()).thenReturn(data);
            
            // Act
            Result result = service.method();
            
            // Assert
            assertEquals(expected, result);
            verify(repository, times(1)).find();
        }
    }
}
```

### E2E Tests Pattern

```java
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MyE2ETest extends BaseE2ETest {
    
    private static TournamentInfo tournament;
    
    @BeforeAll
    static void setup(@Autowired MyE2ETest testInstance) {
        tournament = testInstance.buildTournament(
            TestFixtures.TOURNAMENT_1_XML, 
            "Tournament 1"
        );
    }
    
    @Test
    @Order(1)
    @DisplayName("Test description")
    void test() {
        // Test implementation
    }
}
```

### Using TestDataBuilder

```java
Debater debater = TestDataBuilder.debater()
    .withId(1L)
    .withFirstName("John")
    .withLastName("Doe")
    .withEmail("john@example.com")
    .build();
```

### Using TestFixtures

```java
// Build tournament with standard fixtures
TournamentInfo tourney = buildTournament(
    TestFixtures.TOURNAMENT_1_XML,
    TestFixtures.Tournament1.NAME
);

// Use expected values
assertEquals(TestFixtures.Tournament1.EXPECTED_TEAMS, teams.size());
```

---

## Test Categories

### 1. Entity Creation Tests (TournamentDataE2ETest)

Verifies that all entities are correctly created from XML:
- ✅ Tournament details
- ✅ Teams and debater composition
- ✅ Judges with ratings
- ✅ Institutions
- ✅ Motions with info slides
- ✅ Rounds (prelims and eliminations)
- ✅ Break categories

### 2. Controller Integration Tests (ControllerIntegrationTest)

Tests controller endpoints across multiple tournaments:
- ✅ Debater speaks/scores endpoints
- ✅ Judge tournament participation endpoints
- ✅ Judge prelim scores endpoints
- ✅ Cross-tournament data integrity

### 3. Profile & Statistics Tests (ProfileAndStatisticsE2ETest)

Tests profile generation and statistical calculations:
- ✅ Speaker tab calculations
- ✅ Judge profile refresh
- ✅ Debater profile refresh
- ✅ Win-loss statistics
- ✅ Judge sentiment analysis
- ✅ Furthest rounds tracking
- ✅ Speaker performance tracking
- ✅ Percentile calculations

### 4. Service Unit Tests

Each service has comprehensive unit tests:
- **DebaterServiceTest** - CRUD, duplicate detection, existence checking
- **DebateServiceTest** - CRUD, winner determination, participant checking
- **InstitutionServiceTest** - Similarity matching, CRUD operations
- **BallotServiceTest** - Ballot management, score tracking
- **TeamServiceTest** - Team operations
- **StatisticsServiceTest** - Statistical calculations

---

## Maintenance

### Adding New E2E Tests

1. Extend `BaseE2ETest`
2. Use `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`
3. Build tournaments in `@BeforeAll`
4. Use helper methods from base class
5. Follow naming convention: `*E2ETest.java`

### Adding New Unit Tests

1. Use `@ExtendWith(MockitoExtension.class)`
2. Mock dependencies with `@Mock`
3. Inject service with `@InjectMocks`
4. Use nested classes for grouping
5. Follow naming convention: `*ServiceTest.java`

### Updating Test Data

1. Modify XML files in `src/test/resources/`
2. Update expected values in `TestFixtures.java`
3. Re-run tests to verify changes

### Adding New Test Fixtures

1. Add constants to `TestFixtures.java`
2. Add builder methods for complex test data
3. Document expected values with comments

---

## Common Issues & Solutions

### Issue: Tests Pass Individually But Fail Together

**Solution:** Tests may be sharing state. Ensure proper cleanup in `@AfterEach` or use `@DirtiesContext` for Spring tests.

### Issue: H2 Database Errors

**Solution:** Check `application-test.properties` configuration. Ensure `spring.jpa.hibernate.ddl-auto=create-drop` is set.

### Issue: Transactional Test Failures

**Solution:** Add `@Transactional` annotation to tests that need transaction management for lazy loading.

### Issue: Test Data Not Loading

**Solution:** Verify XML file paths are correct and files exist in `src/test/resources/`.

---

## Best Practices

1. **Isolation** - Each test should be independent and not rely on others
2. **Clarity** - Use descriptive test names and DisplayName annotations
3. **Arrange-Act-Assert** - Follow AAA pattern for test structure
4. **Minimal Mocking** - Mock only what's necessary, prefer real objects when possible
5. **Fast Execution** - Keep unit tests fast, use E2E tests sparingly
6. **Logging** - Use informative log messages for debugging failures
7. **Coverage** - Aim for >80% code coverage for critical business logic
8. **Documentation** - Document complex test scenarios and expected behaviors

---

## Test Execution Times (Approximate)

| Test Suite | Duration | Description |
|------------|----------|-------------|
| All Tests | ~45-60s | Complete test suite |
| Unit Tests | ~5-10s | Service layer tests only |
| E2E Tests | ~30-40s | Full integration tests |
| Single E2E Test | ~10-15s | Individual E2E test class |

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Run tests
        run: mvn test
      - name: Generate coverage report
        run: mvn jacoco:report
```

---

## Future Enhancements

- [ ] Add performance/load testing
- [ ] Increase unit test coverage to 90%
- [ ] Add mutation testing
- [ ] Create test data factory for dynamic test cases
- [ ] Add API contract testing
- [ ] Implement test suite tags for selective execution
- [ ] Add visual regression testing for UI components

---

## Support & Resources

- **Test Documentation:** This file
- **Code Examples:** See existing test files
- **Test Data:** `src/test/resources/`
- **Troubleshooting:** See "Common Issues & Solutions" section

For questions or issues with tests, consult the development team or create an issue in the project repository.

