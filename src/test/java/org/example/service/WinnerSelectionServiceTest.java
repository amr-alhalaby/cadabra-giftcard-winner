package org.example.service;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WinnerSelectionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    @InjectMocks
    private WinnerSelectionService winnerSelectionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(winnerSelectionService, "minPurchaseAmount", 20.0);
    }

    @Test
    void shouldReturnWinnerWhenEligibleUserFound() {
        Long jobExecutionId = 1L;
        User winner = User.builder()
                .id(5L).name("John Doe").username("johndoe")
                .email("john@example.com").phone("123-456")
                .build();

        when(userRepository.findRandomEligibleUser(jobExecutionId, 20.0)).thenReturn(Optional.of(winner));
        doNothing().when(winnerSelectionService).logWinner(winner);

        Optional<User> result = winnerSelectionService.announceWinner(jobExecutionId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(5L);
        assertThat(result.get().getName()).isEqualTo("John Doe");
        assertThat(result.get().getEmail()).isEqualTo("john@example.com");
        verify(winnerSelectionService).logWinner(winner);
        verify(userRepository).findRandomEligibleUser(jobExecutionId, 20.0);
    }

    @Test
    void shouldReturnEmptyAndNotLogWhenNoEligibleUserFound() {
        Long jobExecutionId = 1L;

        when(userRepository.findRandomEligibleUser(jobExecutionId, 20.0)).thenReturn(Optional.empty());

        Optional<User> result = winnerSelectionService.announceWinner(jobExecutionId);

        assertThat(result).isEmpty();
        verify(winnerSelectionService, never()).logWinner(any());
        verify(userRepository).findRandomEligibleUser(jobExecutionId, 20.0);
    }

    @Test
    void shouldUseConfiguredMinPurchaseAmount() {
        Long jobExecutionId = 1L;
        ReflectionTestUtils.setField(winnerSelectionService, "minPurchaseAmount", 50.0);

        when(userRepository.findRandomEligibleUser(jobExecutionId, 50.0)).thenReturn(Optional.empty());

        winnerSelectionService.announceWinner(jobExecutionId);

        verify(userRepository).findRandomEligibleUser(jobExecutionId, 50.0);
        verify(userRepository, never()).findRandomEligibleUser(jobExecutionId, 20.0);
    }
}
