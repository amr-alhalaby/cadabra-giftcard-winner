package org.example.job;

import org.example.IntegrationTest;
import org.example.common.Constants;
import org.example.dto.UserApiResponse;
import org.example.model.Purchase;
import org.example.model.User;
import org.example.repository.PurchaseRepository;
import org.example.repository.UserRepository;
import org.example.service.UserApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@IntegrationTest
@SpringBatchTest
class GiftCardWinnerJobIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private Job giftCardWinnerJob;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private UserApiService userApiService;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(giftCardWinnerJob);
        jobRepositoryTestUtils.removeJobExecutions();
        purchaseRepository.deleteAll();
        userRepository.deleteAll();


        // Mock API response with 3 users
        when(userApiService.fetchAllUsers()).thenReturn(List.of(
                buildUser(1L, "Alice", "alice", "alice@example.com"),
                buildUser(2L, "Bob", "bob", "bob@example.com"),
                buildUser(3L, "Charlie", "charlie", "charlie@example.com")
        ));
    }

    @Test
    void shouldCompleteJobSuccessfully() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters());

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getStepExecutions()).hasSize(3);
    }

    @Test
    void shouldCompleteAllThreeSteps() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters());

        var stepNames = execution.getStepExecutions().stream()
                .map(StepExecution::getStepName)
                .toList();

        assertThat(stepNames).containsExactlyInAnyOrder(
                Constants.FETCH_USERS_STEP,
                Constants.LOAD_PURCHASES_STEP,
                Constants.SELECT_WINNER_STEP
        );
    }

    @Test
    void shouldSaveUsersFromApi() throws Exception {
        jobLauncherTestUtils.launchJob(jobParameters());

        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(3);
        assertThat(users).extracting(User::getName)
                .containsExactlyInAnyOrder("Alice", "Bob", "Charlie");
    }

    @Test
    void shouldLoadPurchasesFromCsv() throws Exception {
        jobLauncherTestUtils.launchJob(jobParameters());

        List<Purchase> purchases = purchaseRepository.findAll();
        // test-purchases.csv has 4 rows: user1=25, user2=35.5, user3=10, user1=15
        assertThat(purchases).hasSize(4);
    }

    @Test
    void shouldTagPurchasesWithJobExecutionId() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters());

        Long jobExecutionId = execution.getId();
        List<Purchase> purchases = purchaseRepository.findAll();

        assertThat(purchases).allMatch(p -> p.getJobExecutionId().equals(jobExecutionId));
    }

    @Test
    void shouldSaveWinnerIdToExecutionContext() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters());

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getExecutionContext().containsKey("winner.id")).isTrue();

        Long winnerId = execution.getExecutionContext().getLong("winner.id");
        assertThat(userRepository.findById(winnerId)).isPresent();
    }

    @Test
    void shouldNotSaveWinnerIdWhenNoEligibleUser() throws Exception {
        // Use a CSV where all amounts are below threshold
        JobExecution execution = jobLauncherTestUtils.launchStep(
                Constants.SELECT_WINNER_STEP, jobParameters());

        assertThat(execution.getExecutionContext().containsKey("winner.id")).isFalse();
    }

    @Test
    void shouldRunFetchUsersStepSuccessfully() {
        JobExecution execution = jobLauncherTestUtils.launchStep(
                Constants.FETCH_USERS_STEP, jobParameters());

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(userRepository.findAll()).hasSize(3);
    }

    @Test
    void shouldRunLoadPurchasesStepSuccessfully() {
        // Pre-save users first since purchases step expects them
        userRepository.saveAll(List.of(
                buildUserEntity(1L, "Alice"),
                buildUserEntity(2L, "Bob"),
                buildUserEntity(3L, "Charlie")
        ));

        JobExecution execution = jobLauncherTestUtils.launchStep(
                Constants.LOAD_PURCHASES_STEP,
                new JobParametersBuilder()
                        .addString("purchases.csv.path", "data/test-purchases.csv")
                        .toJobParameters()
        );

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(purchaseRepository.findAll()).hasSize(4);
    }


    private JobParameters jobParameters() {
        return new JobParametersBuilder()
                .addString("purchases.csv.path", "data/test-purchases.csv")
                .addLong("run.at", System.currentTimeMillis())
                .toJobParameters();
    }

    private UserApiResponse buildUser(Long id, String name, String username, String email) {
        return UserApiResponse.builder()
                .id(id)
                .name(name)
                .username(username)
                .email(email)
                .build();
    }

    private User buildUserEntity(Long id, String name) {
        return User.builder().id(id).name(name).build();
    }
}
