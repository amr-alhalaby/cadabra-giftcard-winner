package org.example.steps.user;

import org.example.common.Constants;
import org.example.dto.UserApiResponse;
import org.example.model.User;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class FetchUsersStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserApiReader userApiReader;
    private final UserApiProcessor userApiProcessor;
    private final UserItemWriter userItemWriter;

    @Bean
    public Step fetchUsersStep() {
        return new StepBuilder(Constants.FETCH_USERS_STEP, jobRepository)
                .<UserApiResponse, User>chunk(10, transactionManager)
                .reader(userApiReader)
                .processor(userApiProcessor)
                .writer(userItemWriter)
                .build();
    }
}

