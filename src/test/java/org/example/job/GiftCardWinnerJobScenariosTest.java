package org.example.job;

import org.example.IntegrationTest;
import org.example.dto.UserApiResponse;
import org.example.model.Purchase;
import org.example.model.ValidationError;
import org.example.repository.PurchaseRepository;
import org.example.repository.ValidationErrorRepository;
import org.example.repository.UserRepository;
import org.example.service.UserApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
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
@DisplayName("Gift Card Winner Job - Full Scenarios")
class GiftCardWinnerJobScenariosTest {

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

    @Autowired
    private ValidationErrorRepository validationErrorRepository;

    @MockBean
    private UserApiService userApiService;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(giftCardWinnerJob);
        jobRepositoryTestUtils.removeJobExecutions();
        purchaseRepository.deleteAll();
        userRepository.deleteAll();
        validationErrorRepository.deleteAll();

        when(userApiService.fetchAllUsers()).thenReturn(List.of(
                buildUser(1L, "Alice"),
                buildUser(2L, "Bob"),
                buildUser(3L, "Charlie"),
                buildUser(4L, "Dave")
        ));
    }

    @Test
    @DisplayName("Standard CSV — eligible users found, winner saved to context")
    void standardCsv_eligibleUsersExist_winnerSavedToContext() throws Exception {
        // test-purchases.csv: user1=25+15=40, user2=35.5, user3=10 → user1 and user2 eligible
        JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters("data/test-purchases.csv"));

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getExecutionContext().containsKey("winner.id")).isTrue();

        Long winnerId = execution.getExecutionContext().getLong("winner.id");
        assertThat(List.of(1L, 2L)).contains(winnerId);
    }

    @Test
    @DisplayName("No eligible users — all amounts below threshold, no winner saved")
    void noEligibleCsv_allAmountsBelowThreshold_noWinnerSaved() throws Exception {
        // no-eligible-purchases.csv: user1=5, user2=8, user3=3 → none exceed 20
        JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters("data/no-eligible-purchases.csv"));

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getExecutionContext().containsKey("winner.id")).isFalse();
    }

    @Test
    @DisplayName("Multi-row same user — amounts summed per user, eligible if sum > 20")
    void multiRowSameUser_amountsSummed_eligibleUserWins() throws Exception {
        // multi-row-same-user.csv: user1=10+15=25 (eligible), user2=8+14=22 (eligible)
        JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters("data/multi-row-same-user.csv"));

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<Purchase> purchases = purchaseRepository.findAll();
        assertThat(purchases).hasSize(4);

        assertThat(execution.getExecutionContext().containsKey("winner.id")).isTrue();
        Long winnerId = execution.getExecutionContext().getLong("winner.id");
        assertThat(List.of(1L, 2L)).contains(winnerId);
    }

    @Test
    @DisplayName("Invalid rows — bad rows skipped, valid rows processed, errors persisted")
    void invalidRowsCsv_badRowsSkipped_validRowsProcessed() throws Exception {
        // invalid-rows.csv: rows 1 and 3 have invalid amounts, rows 2 and 4 are valid
        JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters("data/invalid-rows.csv"));

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // only user2=25 and user4=30 saved
        List<Purchase> purchases = purchaseRepository.findAll();
        assertThat(purchases).hasSize(2);

        // 2 invalid rows persisted to validation_errors
        List<ValidationError> errors = validationErrorRepository.findByJobExecutionId(execution.getId());
        assertThat(errors).hasSize(2);
        assertThat(errors).allMatch(e -> e.getFileName().contains("invalid-rows.csv"));
        assertThat(errors).allMatch(e -> e.getJobExecutionId().equals(execution.getId()));

        // winner is from valid rows only
        assertThat(execution.getExecutionContext().containsKey("winner.id")).isTrue();
        Long winnerId = execution.getExecutionContext().getLong("winner.id");
        assertThat(List.of(2L, 4L)).contains(winnerId);
    }

    @Test
    @DisplayName("Same CSV run twice — second run is isolated from first run's purchases")
    void sameCsvRunTwice_secondRunIsolatedFromFirst() throws Exception {
        // first run
        JobExecution first = jobLauncherTestUtils.launchJob(jobParameters("data/test-purchases.csv"));
        assertThat(first.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(first.getExecutionContext().containsKey("winner.id")).isTrue();

        // second run — different run.at makes it a new job instance
        jobRepositoryTestUtils.removeJobExecutions();
        JobExecution second = jobLauncherTestUtils.launchJob(jobParameters("data/test-purchases.csv"));
        assertThat(second.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // second run only counts its own purchases
        List<Purchase> secondRunPurchases = purchaseRepository.findAll()
                .stream()
                .filter(p -> p.getJobExecutionId().equals(second.getId()))
                .toList();
        assertThat(secondRunPurchases).hasSize(4);

        // winner is from the current run's purchases only
        Long secondWinnerId = second.getExecutionContext().getLong("winner.id");
        assertThat(List.of(1L, 2L)).contains(secondWinnerId);
    }

    @Test
    @DisplayName("Winner isolation — purchases from previous run do not affect current run's winner")
    void winnerIsolation_previousRunPurchasesNotConsidered() throws Exception {
        // first run with eligible CSV
        JobExecution first = jobLauncherTestUtils.launchJob(jobParameters("data/test-purchases.csv"));
        assertThat(first.getExecutionContext().containsKey("winner.id")).isTrue();

        // second run with no-eligible CSV — should have no winner despite first run's data in DB
        jobRepositoryTestUtils.removeJobExecutions();
        JobExecution second = jobLauncherTestUtils.launchJob(jobParameters("data/no-eligible-purchases.csv"));

        assertThat(second.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(second.getExecutionContext().containsKey("winner.id")).isFalse();
    }

    @Test
    @DisplayName("Unknown users — purchases with user IDs not in users table, no winner found")
    void unknownUsersCsv_purchasesLoadedButNoWinner() throws Exception {
        // unknown-users.csv: userId 99 and 98 don't exist in users table (INNER JOIN filters them out)
        JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters("data/unknown-users.csv"));

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // purchases are saved — no FK constraint
        List<Purchase> purchases = purchaseRepository.findAll();
        assertThat(purchases).hasSize(2);

        // but no winner because INNER JOIN with users returns nothing
        assertThat(execution.getExecutionContext().containsKey("winner.id")).isFalse();
    }

    private JobParameters jobParameters(String csvPath) {
        return new JobParametersBuilder()
                .addString("purchases.csv.path", csvPath)
                .addLong("run.at", System.currentTimeMillis())
                .toJobParameters();
    }

    private UserApiResponse buildUser(Long id, String name) {
        return UserApiResponse.builder()
                .id(id)
                .name(name)
                .username(name.toLowerCase())
                .email(name.toLowerCase() + "@example.com")
                .build();
    }
}
