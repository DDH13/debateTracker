package com.dineth.debateTracker.debate;

import com.dineth.debateTracker.builders.TestDataBuilder;
import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.team.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DebateService Tests")
class DebateServiceTest {

    @Mock
    private DebateRepository debateRepository;

    @InjectMocks
    private DebateService debateService;

    private Debate testDebate;
    private Team proposition;
    private Team opposition;
    private Debater debater1;
    private Debater debater2;

    @BeforeEach
    void setUp() {
        debater1 = TestDataBuilder.debater().withId(1L).withFirstName("John").build();
        debater2 = TestDataBuilder.debater().withId(2L).withFirstName("Jane").build();
        
        proposition = TestDataBuilder.team()
                .withId(1L)
                .withName("Prop Team")
                .addDebater(debater1)
                .build();
        
        opposition = TestDataBuilder.team()
                .withId(2L)
                .withName("Opp Team")
                .addDebater(debater2)
                .build();
        
        // Create ballots for the debaters so they're considered as having participated
        var ballot1 = TestDataBuilder.ballot()
                .withDebater(debater1)
                .withScore(75.0f)
                .withPosition(1)
                .build();
        
        var ballot2 = TestDataBuilder.ballot()
                .withDebater(debater2)
                .withScore(74.0f)
                .withPosition(1)
                .build();
        
        testDebate = TestDataBuilder.debate()
                .withId(1L)
                .withProposition(proposition)
                .withOpposition(opposition)
                .withWinner(proposition)
                .withBallots(Arrays.asList(ballot1, ballot2))
                .build();
    }

    @Test
    @DisplayName("Should return all debates")
    void shouldReturnAllDebates() {
        when(debateRepository.findAll()).thenReturn(Arrays.asList(testDebate));
        List<Debate> debates = debateService.getDebate();
        assertEquals(1, debates.size());
        verify(debateRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should add new debate")
    void shouldAddNewDebate() {
        when(debateRepository.save(testDebate)).thenReturn(testDebate);
        Debate result = debateService.addDebate(testDebate);
        assertNotNull(result);
        verify(debateRepository, times(1)).save(testDebate);
    }

    @Test
    @DisplayName("Should find prelims by debater id")
    void shouldFindPrelimsByDebaterId() {
        when(debateRepository.findPrelimsByDebaterId(1L)).thenReturn(Arrays.asList(testDebate));
        List<Debate> debates = debateService.findPrelimsByDebaterId(1L);
        assertEquals(1, debates.size());
        verify(debateRepository, times(1)).findPrelimsByDebaterId(1L);
    }

    @Test
    @DisplayName("Should find breaks by debater id")
    void shouldFindBreaksByDebaterId() {
        when(debateRepository.findBreaksByDebaterId(1L)).thenReturn(Arrays.asList(testDebate));
        List<Debate> debates = debateService.findBreaksByDebaterId(1L);
        assertEquals(1, debates.size());
        verify(debateRepository, times(1)).findBreaksByDebaterId(1L);
    }

    @Test
    @DisplayName("Should check if debater won debate - winner case")
    void shouldCheckIfDebaterWonDebate_WinnerCase() {
        Boolean result = debateService.didDebaterWinDebate(testDebate, debater1);
        assertTrue(result);
    }

    @Test
    @DisplayName("Should check if debater won debate - loser case")
    void shouldCheckIfDebaterWonDebate_LoserCase() {
        Boolean result = debateService.didDebaterWinDebate(testDebate, debater2);
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return null when debate has no winner")
    void shouldReturnNullWhenDebateHasNoWinner() {
        testDebate.setWinner(null);
        Boolean result = debateService.didDebaterWinDebate(testDebate, debater1);
        assertNull(result);
    }

    @Test
    @DisplayName("Should check if debater participated in debate")
    void shouldCheckIfDebaterParticipatedInDebate() {
        boolean result = debateService.didDebaterParticipateInDebate(testDebate, debater1);
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when debater did not participate")
    void shouldReturnFalseWhenDebaterDidNotParticipate() {
        Debater nonParticipant = TestDataBuilder.debater().withId(999L).build();
        boolean result = debateService.didDebaterParticipateInDebate(testDebate, nonParticipant);
        assertFalse(result);
    }
}

