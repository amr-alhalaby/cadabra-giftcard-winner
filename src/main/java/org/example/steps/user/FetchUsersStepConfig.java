package org.example.steps.user;

import org.example.common.Constants;
import org.example.dto.UserApiResponse;
import org.example.model.User;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class FetchUsersStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final UserApiReader userApiReader;
    private final UserApiProcessor userApiProcessor;

    @Value("classpath:sql/upsert-user.sql")
    private Resource upsertUserSql;

    @Bean
    public JdbcBatchItemWriter<User> userItemWriter() throws IOException {
        return new JdbcBatchItemWriterBuilder<User>()
                .dataSource(dataSource)
                .sql(upsertUserSql.getContentAsString(StandardCharsets.UTF_8))
                .beanMapped()
                .build();
    }

    @Bean
    public Step fetchUsersStep() throws IOException {
        return new StepBuilder(Constants.FETCH_USERS_STEP, jobRepository)
                .<UserApiResponse, User>chunk(10, transactionManager)
                .reader(userApiReader)
                .processor(userApiProcessor)
                .writer(userItemWriter())
                .build();
    }
}
