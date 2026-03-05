package org.cadabra.steps.winner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cadabra.model.User;
import org.cadabra.service.WinnerSelectionService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WinnerTasklet implements Tasklet {

    private final WinnerSelectionService winnerSelectionService;

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {
        Long jobExecutionId = chunkContext.getStepContext().getStepExecution().getJobExecutionId();
        log.debug("Selecting winner for jobExecutionId: {}", jobExecutionId);

        winnerSelectionService.announceWinner(jobExecutionId)
                .ifPresent(winner -> saveWinnerToContext(winner, chunkContext));

        return RepeatStatus.FINISHED;
    }

    private void saveWinnerToContext(User winner, ChunkContext chunkContext) {
        ExecutionContext jobContext = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();
        jobContext.putLong("winner.id", winner.getId());
        log.debug("Winner saved to execution context — userId: {}, name: {}", winner.getId(), winner.getName());
    }
}
