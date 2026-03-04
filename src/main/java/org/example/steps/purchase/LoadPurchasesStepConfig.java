package org.example.steps.purchase;

import org.example.common.Constants;
import org.example.dto.PurchaseCsvRow;
import org.example.model.Purchase;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileParseException;
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
public class LoadPurchasesStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final ItemStreamReader<PurchaseCsvRow> purchaseCsvReader;
    private final PurchaseProcessor purchaseProcessor;
    private final PurchaseSkipListener purchaseSkipListener;

    @Value("${csv.purchases.max-skip-lines}")
    private int maxSkipLines;

    @Value("${csv.purchases.chunk-size}")
    private int chunkSize;

    @Value("classpath:sql/insert-purchase.sql")
    private Resource insertPurchaseSql;

    @Bean
    public JdbcBatchItemWriter<Purchase> purchaseItemWriter() throws IOException {
        return new JdbcBatchItemWriterBuilder<Purchase>()
                .dataSource(dataSource)
                .sql(insertPurchaseSql.getContentAsString(StandardCharsets.UTF_8))
                .beanMapped()
                .build();
    }

    @Bean
    public Step loadPurchasesStep() throws IOException {
        return new StepBuilder(Constants.LOAD_PURCHASES_STEP, jobRepository)
                .<PurchaseCsvRow, Purchase>chunk(chunkSize, transactionManager)
                .reader(purchaseCsvReader)
                .processor(purchaseProcessor)
                .writer(purchaseItemWriter())
                .faultTolerant()
                .skipLimit(maxSkipLines)
                .skip(FlatFileParseException.class)
                .skip(Exception.class)
                .listener(purchaseSkipListener)
                .allowStartIfComplete(true)
                .build();
    }
}
