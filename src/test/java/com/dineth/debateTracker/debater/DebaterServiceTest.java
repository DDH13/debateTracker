package com.dineth.debateTracker.debater;

import com.dineth.debateTracker.builders.TestDataBuilder;
import com.dineth.debateTracker.tournament.TournamentRepository;
import com.dineth.debateTracker.utils.CustomExceptions;
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
@DisplayName("DebaterService Tests")
class DebaterServiceTest {

    @Mock
    private DebaterRepository debaterRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @InjectMocks
    private DebaterService debaterService;

    private Debater testDebater;

    @BeforeEach
    void setUp() {
        testDebater = TestDataBuilder.debater()
                .withId(1L)
                .withFirstName("John")
                .withLastName("Doe")
                .build();
    }

    @Nested
    @DisplayName("getDebaters Tests")
    class GetDebatersTests {
        @Test
        @DisplayName("Should return all debaters")
        void shouldReturnAllDebaters() {
            // Arrange
            List<Debater> expectedDebaters = Arrays.asList(testDebater,
                    TestDataBuilder.debater().withId(2L).withFirstName("Jane").build());
            when(debaterRepository.findAll()).thenReturn(expectedDebaters);

            // Act
            List<Debater> actualDebaters = debaterService.getDebaters();

            // Assert
            assertEquals(expectedDebaters.size(), actualDebaters.size());
            verify(debaterRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("getDebaterById Tests")
    class GetDebaterByIdTests {
        @Test
        @DisplayName("Should return debater when found")
        void shouldReturnDebaterWhenFound() {
            // Arrange
            when(debaterRepository.findById(1L)).thenReturn(Optional.of(testDebater));

            // Act
            Debater result = debaterService.getDebaterById(1L);

            // Assert
            assertNotNull(result);
            assertEquals(testDebater.getId(), result.getId());
            verify(debaterRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should return null when debater not found")
        void shouldReturnNullWhenDebaterNotFound() {
            // Arrange
            when(debaterRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Debater result = debaterService.getDebaterById(999L);

            // Assert
            assertNull(result);
            verify(debaterRepository, times(1)).findById(999L);
        }
    }

    @Nested
    @DisplayName("addDebater Tests")
    class AddDebaterTests {
        @Test
        @DisplayName("Should add new debater when not exists")
        void shouldAddNewDebaterWhenNotExists() {
            // Arrange
            Debater newDebater = TestDataBuilder.debater()
                    .withFirstName("Alice")
                    .withLastName("Smith")
                    .build();
            when(debaterRepository.findDebatersByFirstNameEqualsIgnoreCaseAndLastNameEqualsIgnoreCase(
                    "Alice", "Smith")).thenReturn(Collections.emptyList());
            when(debaterRepository.save(newDebater)).thenReturn(newDebater);

            // Act
            Debater result = debaterService.addDebater(newDebater);

            // Assert
            assertNotNull(result);
            verify(debaterRepository, times(1)).save(newDebater);
        }

        @Test
        @DisplayName("Should return existing debater when already exists")
        void shouldReturnExistingDebaterWhenAlreadyExists() {
            // Arrange
            when(debaterRepository.findDebatersByFirstNameEqualsIgnoreCaseAndLastNameEqualsIgnoreCase(
                    "John", "Doe")).thenReturn(Collections.singletonList(testDebater));

            // Act
            Debater result = debaterService.addDebater(testDebater);

            // Assert
            assertEquals(testDebater.getId(), result.getId());
            verify(debaterRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("checkIfDebaterExists Tests")
    class CheckIfDebaterExistsTests {
        @Test
        @DisplayName("Should return debater when single match found without birthdate")
        void shouldReturnDebaterWhenSingleMatchFoundWithoutBirthdate() {
            // Arrange
            Debater searchDebater = TestDataBuilder.debater()
                    .withFirstName("John")
                    .withLastName("Doe")
                    .build();
            when(debaterRepository.findDebatersByFirstNameEqualsIgnoreCaseAndLastNameEqualsIgnoreCase(
                    "John", "Doe")).thenReturn(Collections.singletonList(testDebater));

            // Act
            Debater result = debaterService.checkIfDebaterExists(searchDebater);

            // Assert
            assertNotNull(result);
            assertEquals(testDebater.getId(), result.getId());
        }

        @Test
        @DisplayName("Should return debater when match found with birthdate")
        void shouldReturnDebaterWhenMatchFoundWithBirthdate() {
            // Arrange
            Date birthdate = new Date();
            Debater searchDebater = TestDataBuilder.debater()
                    .withFirstName("John")
                    .withLastName("Doe")
                    .withBirthdate(birthdate)
                    .build();
            testDebater.setBirthdate(birthdate);
            when(debaterRepository.findDebatersByFirstNameEqualsIgnoreCaseAndLastNameEqualsIgnoreCaseAndBirthdate(
                    "John", "Doe", birthdate)).thenReturn(Collections.singletonList(testDebater));

            // Act
            Debater result = debaterService.checkIfDebaterExists(searchDebater);

            // Assert
            assertNotNull(result);
            assertEquals(testDebater.getId(), result.getId());
        }

        @Test
        @DisplayName("Should return null when no match found")
        void shouldReturnNullWhenNoMatchFound() {
            // Arrange
            Debater searchDebater = TestDataBuilder.debater()
                    .withFirstName("Nonexistent")
                    .withLastName("Person")
                    .build();
            when(debaterRepository.findDebatersByFirstNameEqualsIgnoreCaseAndLastNameEqualsIgnoreCase(
                    "Nonexistent", "Person")).thenReturn(Collections.emptyList());

            // Act
            Debater result = debaterService.checkIfDebaterExists(searchDebater);

            // Assert
            assertNull(result);
        }

        @Test
        @DisplayName("Should throw exception when multiple matches found")
        void shouldThrowExceptionWhenMultipleMatchesFound() {
            // Arrange
            Debater searchDebater = TestDataBuilder.debater()
                    .withFirstName("John")
                    .withLastName("Doe")
                    .build();
            Debater duplicate1 = TestDataBuilder.debater().withId(1L).build();
            Debater duplicate2 = TestDataBuilder.debater().withId(2L).build();
            when(debaterRepository.findDebatersByFirstNameEqualsIgnoreCaseAndLastNameEqualsIgnoreCase(
                    "John", "Doe")).thenReturn(Arrays.asList(duplicate1, duplicate2));

            // Act & Assert
            assertThrows(CustomExceptions.MultipleDebatersFoundException.class,
                    () -> debaterService.checkIfDebaterExists(searchDebater));
        }
    }

    @Nested
    @DisplayName("findDebatersWithDuplicateNames Tests")
    class FindDebatersWithDuplicateNamesTests {
        @Test
        @DisplayName("Should find debaters with duplicate names")
        void shouldFindDebatersWithDuplicateNames() {
            // Arrange
            Object[] namePair = new Object[]{"John", "Doe"};
            List<Object[]> duplicateNames = Collections.singletonList(namePair);
            Debater debater1 = TestDataBuilder.debater().withId(1L).build();
            Debater debater2 = TestDataBuilder.debater().withId(2L).build();
            when(debaterRepository.findDebaterNameDuplicates()).thenReturn(duplicateNames);
            when(debaterRepository.findByFirstNameAndLastNameAllIgnoreCase("John", "Doe"))
                    .thenReturn(Arrays.asList(debater1, debater2));

            // Act
            List<Debater> result = debaterService.findDebatersWithDuplicateNames();

            // Assert
            assertEquals(2, result.size());
            verify(debaterRepository, times(1)).findDebaterNameDuplicates();
        }

        @Test
        @DisplayName("Should return empty list when no duplicates")
        void shouldReturnEmptyListWhenNoDuplicates() {
            // Arrange
            when(debaterRepository.findDebaterNameDuplicates()).thenReturn(Collections.emptyList());

            // Act
            List<Debater> result = debaterService.findDebatersWithDuplicateNames();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("deleteDebater Tests")
    class DeleteDebaterTests {
        @Test
        @DisplayName("Should delete debater by id")
        void shouldDeleteDebaterById() {
            // Arrange
            Long debaterId = 1L;
            doNothing().when(debaterRepository).deleteById(debaterId);

            // Act
            debaterService.deleteDebater(debaterId);

            // Assert
            verify(debaterRepository, times(1)).deleteById(debaterId);
        }
    }

    @Nested
    @DisplayName("updateDebater Tests")
    class UpdateDebaterTests {
        @Test
        @DisplayName("Should update debater")
        void shouldUpdateDebater() {
            // Arrange
            testDebater.setEmail("newemail@example.com");
            when(debaterRepository.save(testDebater)).thenReturn(testDebater);

            // Act
            debaterService.updateDebater(testDebater);

            // Assert
            verify(debaterRepository, times(1)).save(testDebater);
        }
    }

    @Nested
    @DisplayName("findDebatersByInstitutionId Tests")
    class FindDebatersByInstitutionIdTests {
        @Test
        @DisplayName("Should find debaters by institution id")
        void shouldFindDebatersByInstitutionId() {
            // Arrange
            Long institutionId = 1L;
            List<Debater> expectedDebaters = Arrays.asList(testDebater,
                    TestDataBuilder.debater().withId(2L).build());
            when(debaterRepository.findDebatersByInstitutionId(institutionId)).thenReturn(expectedDebaters);

            // Act
            List<Debater> result = debaterService.findDebatersByInstitutionId(institutionId);

            // Assert
            assertEquals(2, result.size());
            verify(debaterRepository, times(1)).findDebatersByInstitutionId(institutionId);
        }
    }
}

