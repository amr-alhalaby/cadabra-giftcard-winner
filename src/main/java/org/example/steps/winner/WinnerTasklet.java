package org.example.steps.winner;

import lombok.RequiredArgsConstructor;
import org.example.model.User;
import org.example.service.WinnerSelectionService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WinnerTasklet implements Tasklet {

    private final WinnerSelectionService winnerSelectionService;

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {
        Long jobExecutionId = chunkContext.getStepContext().getStepExecution().getJobExecutionId();

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
    }
}
