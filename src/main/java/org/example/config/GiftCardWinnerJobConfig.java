package org.example.config;

import org.example.common.Constants;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class GiftCardWinnerJobConfig {

    private final JobRepository jobRepository;
    private final JobMdcListener jobMdcListener;

    @Bean
    public Job giftCardWinnerJob(
            @Qualifier(Constants.FETCH_USERS_STEP) Step fetchUsersStep,
            @Qualifier(Constants.LOAD_PURCHASES_STEP) Step loadPurchasesStep,
            @Qualifier(Constants.SELECT_WINNER_STEP) Step selectWinnerStep) {
        return new JobBuilder(Constants.GIFT_CARD_WINNER_JOB, jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobMdcListener)
                .start(fetchUsersStep)
                .next(loadPurchasesStep)
                .next(selectWinnerStep)
                .build();
    }
}

