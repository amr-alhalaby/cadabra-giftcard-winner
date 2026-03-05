package org.cadabra.steps.winner;

import org.cadabra.common.Constants;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SelectWinnerStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final WinnerTasklet winnerTasklet;

    @Bean
    public Step selectWinnerStep() {
        return new StepBuilder(Constants.SELECT_WINNER_STEP, jobRepository)
                .tasklet(winnerTasklet, transactionManager)
                .build();
    }
}

