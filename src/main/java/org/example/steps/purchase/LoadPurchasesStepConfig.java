package org.example.steps.purchase;

import org.example.common.Constants;
import org.example.dto.PurchaseCsvRow;
import org.example.model.Purchase;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;


import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class LoadPurchasesStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ItemStreamReader<PurchaseCsvRow> purchaseCsvReader;
    private final PurchaseProcessor purchaseProcessor;
    private final PurchaseItemWriter purchaseItemWriter;
    private final PurchaseSkipListener purchaseSkipListener;

    @Value("${csv.purchases.max-skip-lines}")
    private int maxSkipLines;

    @Value("${csv.purchases.chunk-size}")
    private int chunkSize;

    @Bean
    public Step loadPurchasesStep() {
        return new StepBuilder(Constants.LOAD_PURCHASES_STEP, jobRepository)
                .<PurchaseCsvRow, Purchase>chunk(chunkSize, transactionManager)
                .reader(purchaseCsvReader)
                .processor(purchaseProcessor)
                .writer(purchaseItemWriter)
                .faultTolerant()
                .skipLimit(maxSkipLines)
                .skip(FlatFileParseException.class)
                .skip(Exception.class)
                .listener(purchaseSkipListener)
                .allowStartIfComplete(true)
                .build();
    }
}
