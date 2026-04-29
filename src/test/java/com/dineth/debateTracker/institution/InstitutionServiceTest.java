package com.dineth.debateTracker.institution;

import com.dineth.debateTracker.dtos.InstitutionMergeInfoDTO;
import com.dineth.debateTracker.team.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InstitutionService Tests")
class InstitutionServiceTest {

    @Mock
    private InstitutionRepository institutionRepository;

    @InjectMocks
    private InstitutionService institutionService;

    private Institution testInstitution1;
    private Institution testInstitution2;
    private Institution testInstitution3;

    @BeforeEach
    void setUp() {
        testInstitution1 = new Institution("Harvard University", "HU");
        testInstitution1.setId(1L);

        testInstitution2 = new Institution("Oxford University", "OU");
        testInstitution2.setId(2L);

        testInstitution3 = new Institution("Harvard College", "HC");
        testInstitution3.setId(3L);
    }

    @Nested
    @DisplayName("getInstitutions Tests")
    class GetInstitutionsTests {
        @Test
        @DisplayName("Should return all institutions")
        void shouldReturnAllInstitutions() {
            // Arrange
            List<Institution> expectedInstitutions = Arrays.asList(testInstitution1, testInstitution2);
            when(institutionRepository.findAll()).thenReturn(expectedInstitutions);

            // Act
            List<Institution> result = institutionService.getInstitutions();

            // Assert
            assertEquals(2, result.size());
            verify(institutionRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no institutions")
        void shouldReturnEmptyListWhenNoInstitutions() {
            // Arrange
            when(institutionRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<Institution> result = institutionService.getInstitutions();

            // Assert
            assertTrue(result.isEmpty());
            verify(institutionRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("getInstitutionsWithSimilarNames Tests")
    class GetInstitutionsWithSimilarNamesTests {
        @Test
        @DisplayName("Should find exact match institutions")
        void shouldFindExactMatchInstitutions() {
            // Arrange
            List<Institution> allInstitutions = Arrays.asList(testInstitution1, testInstitution2, testInstitution3);
            when(institutionRepository.findAll()).thenReturn(allInstitutions);

            // Act
            List<String> result = institutionService.getInstitutionsWithSimilarNames("Harvard University");

            // Assert
            assertFalse(result.isEmpty());
            assertTrue(result.get(0).contains("Harvard University"));
            assertTrue(result.contains("1,Harvard University"));
        }

        @Test
        @DisplayName("Should find similar institutions with high similarity")
        void shouldFindSimilarInstitutionsWithHighSimilarity() {
            // Arrange
            List<Institution> allInstitutions = Arrays.asList(testInstitution1, testInstitution2, testInstitution3);
            when(institutionRepository.findAll()).thenReturn(allInstitutions);

            // Act
            List<String> result = institutionService.getInstitutionsWithSimilarNames("Harvard");

            // Assert
            assertFalse(result.isEmpty());
            // Both "Harvard University" and "Harvard College" should be in results
            boolean containsHarvardUniversity = result.stream().anyMatch(s -> s.contains("Harvard University"));
            boolean containsHarvardCollege = result.stream().anyMatch(s -> s.contains("Harvard College"));
            assertTrue(containsHarvardUniversity || containsHarvardCollege);
        }

        @Test
        @DisplayName("Should find institutions containing the search term")
        void shouldFindInstitutionsContainingSearchTerm() {
            // Arrange
            List<Institution> allInstitutions = Arrays.asList(testInstitution1, testInstitution2, testInstitution3);
            when(institutionRepository.findAll()).thenReturn(allInstitutions);

            // Act
            List<String> result = institutionService.getInstitutionsWithSimilarNames("University");

            // Assert
            assertFalse(result.isEmpty());
            // Should find institutions containing "University"
            assertTrue(result.size() >= 1);
        }

        @Test
        @DisplayName("Should return results sorted by similarity score")
        void shouldReturnResultsSortedBySimilarityScore() {
            // Arrange
            List<Institution> allInstitutions = Arrays.asList(testInstitution1, testInstitution2, testInstitution3);
            when(institutionRepository.findAll()).thenReturn(allInstitutions);

            // Act
            List<String> result = institutionService.getInstitutionsWithSimilarNames("Harvard University");

            // Assert
            assertFalse(result.isEmpty());
            // First result should be the exact or closest match
            assertTrue(result.get(0).contains("Harvard"));
        }

        @Test
        @DisplayName("Should return empty list when no similar institutions found")
        void shouldReturnEmptyListWhenNoSimilarInstitutionsFound() {
            // Arrange
            List<Institution> allInstitutions = Arrays.asList(testInstitution1, testInstitution2);
            when(institutionRepository.findAll()).thenReturn(allInstitutions);

            // Act
            List<String> result = institutionService.getInstitutionsWithSimilarNames("CompletelyDifferentName123456");

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle case-insensitive matching")
        void shouldHandleCaseInsensitiveMatching() {
            // Arrange
            List<Institution> allInstitutions = Arrays.asList(testInstitution1, testInstitution2, testInstitution3);
            when(institutionRepository.findAll()).thenReturn(allInstitutions);

            // Act
            List<String> result1 = institutionService.getInstitutionsWithSimilarNames("harvard");
            List<String> result2 = institutionService.getInstitutionsWithSimilarNames("HARVARD");
            List<String> result3 = institutionService.getInstitutionsWithSimilarNames("Harvard");

            // Assert
            assertFalse(result1.isEmpty());
            assertFalse(result2.isEmpty());
            assertFalse(result3.isEmpty());
            // All three should return similar results
            assertEquals(result1.size(), result2.size());
            assertEquals(result2.size(), result3.size());
        }

        @Test
        @DisplayName("Should return distinct results")
        void shouldReturnDistinctResults() {
            // Arrange
            List<Institution> allInstitutions = Arrays.asList(testInstitution1, testInstitution2, testInstitution3);
            when(institutionRepository.findAll()).thenReturn(allInstitutions);

            // Act
            List<String> result = institutionService.getInstitutionsWithSimilarNames("Harvard");

            // Assert
            // Check for distinct values
            Set<String> uniqueResults = new HashSet<>(result);
            assertEquals(uniqueResults.size(), result.size());
        }

        @Test
        @DisplayName("Should handle partial name matching")
        void shouldHandlePartialNameMatching() {
            // Arrange
            List<Institution> allInstitutions = Arrays.asList(testInstitution1, testInstitution2, testInstitution3);
            when(institutionRepository.findAll()).thenReturn(allInstitutions);

            // Act
            List<String> result = institutionService.getInstitutionsWithSimilarNames("Oxf");

            // Assert
            assertFalse(result.isEmpty());
            boolean containsOxford = result.stream().anyMatch(s -> s.contains("Oxford"));
            assertTrue(containsOxford);
        }
    }

    @Nested
    @DisplayName("findInstitutionByName Tests")
    class FindInstitutionByNameTests {
        @Test
        @DisplayName("Should find institution by exact name match")
        void shouldFindInstitutionByExactNameMatch() {
            // Arrange
            List<Institution> allInstitutions = Arrays.asList(testInstitution1, testInstitution2);
            when(institutionRepository.findAll()).thenReturn(allInstitutions);
            when(institutionRepository.findById(1L)).thenReturn(Optional.of(testInstitution1));

            // Act
            Institution result = institutionService.findInstitutionByName("Harvard University");

            // Assert
            assertNotNull(result);
            assertEquals("Harvard University", result.getName());
            assertEquals(1L, result.getId());
        }

        @Test
        @DisplayName("Should find institution ignoring case and special characters")
        void shouldFindInstitutionIgnoringCaseAndSpecialCharacters() {
            // Arrange
            List<Institution> allInstitutions = Arrays.asList(testInstitution1, testInstitution2);
            when(institutionRepository.findAll()).thenReturn(allInstitutions);
            when(institutionRepository.findById(1L)).thenReturn(Optional.of(testInstitution1));

            // Act
            Institution result = institutionService.findInstitutionByName("harvard-university");

            // Assert
            assertNotNull(result);
            assertEquals("Harvard University", result.getName());
        }

        @Test
        @DisplayName("Should return null when institution not found")
        void shouldReturnNullWhenInstitutionNotFound() {
            // Arrange
            List<Institution> allInstitutions = Arrays.asList(testInstitution1, testInstitution2);
            when(institutionRepository.findAll()).thenReturn(allInstitutions);

            // Act
            Institution result = institutionService.findInstitutionByName("NonexistentInstitution");

            // Assert
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("findInstitutionById Tests")
    class FindInstitutionByIdTests {
        @Test
        @DisplayName("Should find institution by id")
        void shouldFindInstitutionById() {
            // Arrange
            when(institutionRepository.findById(1L)).thenReturn(Optional.of(testInstitution1));

            // Act
            Institution result = institutionService.findInstitutionById(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(institutionRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should return null when institution not found by id")
        void shouldReturnNullWhenInstitutionNotFoundById() {
            // Arrange
            when(institutionRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Institution result = institutionService.findInstitutionById(999L);

            // Assert
            assertNull(result);
            verify(institutionRepository, times(1)).findById(999L);
        }
    }

    @Nested
    @DisplayName("addInstitution Tests")
    class AddInstitutionTests {
        @Test
        @DisplayName("Should add new institution")
        void shouldAddNewInstitution() {
            // Arrange
            Institution newInstitution = new Institution("Cambridge University", "CU");
            when(institutionRepository.save(newInstitution)).thenReturn(newInstitution);

            // Act
            Institution result = institutionService.addInstitution(newInstitution);

            // Assert
            assertNotNull(result);
            assertEquals("Cambridge University", result.getName());
            verify(institutionRepository, times(1)).save(newInstitution);
        }
    }

    @Nested
    @DisplayName("updateInstitution Tests")
    class UpdateInstitutionTests {
        @Test
        @DisplayName("Should update institution")
        void shouldUpdateInstitution() {
            // Arrange
            testInstitution1.setAbbreviation("HRV");
            when(institutionRepository.save(testInstitution1)).thenReturn(testInstitution1);

            // Act
            institutionService.updateInstitution(testInstitution1);

            // Assert
            verify(institutionRepository, times(1)).save(testInstitution1);
        }
    }

    @Nested
    @DisplayName("deleteInstitution Tests")
    class DeleteInstitutionTests {
        @Test
        @DisplayName("Should delete institution by id")
        void shouldDeleteInstitutionById() {
            // Arrange
            Long institutionId = 1L;
            doNothing().when(institutionRepository).deleteById(institutionId);

            // Act
            institutionService.deleteInstitution(institutionId);

            // Assert
            verify(institutionRepository, times(1)).deleteById(institutionId);
        }
    }

    @Nested
    @DisplayName("addTeamToInstitution Tests")
    class AddTeamToInstitutionTests {
        @Test
        @DisplayName("Should add team to institution")
        void shouldAddTeamToInstitution() {
            // Arrange
            Team team = new Team();
            team.setTeamName("Team A");
            testInstitution1.setTeams(new ArrayList<>());
            when(institutionRepository.findById(1L)).thenReturn(Optional.of(testInstitution1));
            when(institutionRepository.save(any(Institution.class))).thenReturn(testInstitution1);

            // Act
            institutionService.addTeamToInstitution(1L, team);

            // Assert
            verify(institutionRepository, times(1)).findById(1L);
            verify(institutionRepository, times(1)).save(testInstitution1);
        }

        @Test
        @DisplayName("Should initialize teams list if null")
        void shouldInitializeTeamsListIfNull() {
            // Arrange
            Team team = new Team();
            team.setTeamName("Team A");
            testInstitution1.setTeams(null);
            when(institutionRepository.findById(1L)).thenReturn(Optional.of(testInstitution1));
            when(institutionRepository.save(any(Institution.class))).thenReturn(testInstitution1);

            // Act
            institutionService.addTeamToInstitution(1L, team);

            // Assert
            verify(institutionRepository, times(1)).save(testInstitution1);
        }

        @Test
        @DisplayName("Should not add team when institution not found")
        void shouldNotAddTeamWhenInstitutionNotFound() {
            // Arrange
            Team team = new Team();
            when(institutionRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            institutionService.addTeamToInstitution(999L, team);

            // Assert
            verify(institutionRepository, times(1)).findById(999L);
            verify(institutionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getInstitutionsWithTeamsCounts Tests")
    class GetInstitutionsWithTeamsCountsTests {
        @Test
        @DisplayName("Should return institutions with team counts")
        void shouldReturnInstitutionsWithTeamCounts() {
            // Arrange
            Object[] row1 = new Object[]{1L, "Harvard University", "HU", 5L, new String[]{"Team A", "Team B"}};
            Object[] row2 = new Object[]{2L, "Oxford University", "OU", 3L, new String[]{"Team C"}};
            List<Object> mockData = Arrays.asList(row1, row2);
            when(institutionRepository.findInstitutionsWithTeamsCounts()).thenReturn(mockData);

            // Act
            List<InstitutionMergeInfoDTO> result = institutionService.getInstitutionsWithTeamsCounts();

            // Assert
            assertEquals(2, result.size());
            assertEquals(1L, result.get(0).getId());
            assertEquals("Harvard University", result.get(0).getName());
            assertEquals("5", result.get(0).getTeamCount());
            verify(institutionRepository, times(1)).findInstitutionsWithTeamsCounts();
        }

        @Test
        @DisplayName("Should return empty list when no institutions")
        void shouldReturnEmptyListWhenNoInstitutions() {
            // Arrange
            when(institutionRepository.findInstitutionsWithTeamsCounts()).thenReturn(Collections.emptyList());

            // Act
            List<InstitutionMergeInfoDTO> result = institutionService.getInstitutionsWithTeamsCounts();

            // Assert
            assertTrue(result.isEmpty());
        }
    }
}

