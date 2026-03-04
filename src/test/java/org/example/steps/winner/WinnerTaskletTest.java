package org.example.steps.winner;

import org.example.model.User;
import org.example.service.WinnerSelectionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WinnerTaskletTest {

    @Mock
    private WinnerSelectionService winnerSelectionService;

    @InjectMocks
    private WinnerTasklet winnerTasklet;

    @Test
    void shouldCallAnnounceWinnerWithJobExecutionIdAndReturnFinished() {
        ChunkContext chunkContext = buildChunkContext(99L);

        when(winnerSelectionService.announceWinner(99L)).thenReturn(Optional.empty());

        RepeatStatus status = winnerTasklet.execute(
                new StepExecution("selectWinnerStep", new JobExecution(99L)).createStepContribution(),
                chunkContext
        );

        verify(winnerSelectionService).announceWinner(99L);
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    }

    @Test
    void shouldSaveWinnerToJobExecutionContextWhenWinnerFound() {
        ChunkContext chunkContext = buildChunkContext(1L);
        User winner = User.builder()
                .id(5L).name("John Doe").username("johndoe").email("john@example.com")
                .build();

        when(winnerSelectionService.announceWinner(1L)).thenReturn(Optional.of(winner));

        winnerTasklet.execute(
                new StepExecution("selectWinnerStep", new JobExecution(1L)).createStepContribution(),
                chunkContext
        );

        var jobContext = chunkContext.getStepContext().getStepExecution()
                .getJobExecution().getExecutionContext();

        assertThat(jobContext.getLong("winner.id")).isEqualTo(5L);
    }

    @Test
    void shouldNotSaveToContextWhenNoWinnerFound() {
        ChunkContext chunkContext = buildChunkContext(1L);

        when(winnerSelectionService.announceWinner(1L)).thenReturn(Optional.empty());

        winnerTasklet.execute(
                new StepExecution("selectWinnerStep", new JobExecution(1L)).createStepContribution(),
                chunkContext
        );

        var jobContext = chunkContext.getStepContext().getStepExecution()
                .getJobExecution().getExecutionContext();

        assertThat(jobContext.containsKey("winner.id")).isFalse();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private ChunkContext buildChunkContext(Long jobExecutionId) {
        JobExecution jobExecution = new JobExecution(jobExecutionId);
        StepExecution stepExecution = new StepExecution("selectWinnerStep", jobExecution);
        return new ChunkContext(new StepContext(stepExecution));
    }
}
