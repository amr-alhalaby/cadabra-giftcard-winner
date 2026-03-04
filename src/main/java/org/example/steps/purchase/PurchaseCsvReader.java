package org.example.steps.purchase;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.common.ResourceResolver;
import org.example.dto.PurchaseCsvRow;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Reads purchases from CSV file, returning them one by one.
 * The CSV path is provided as a job parameter, falling back to the configured default classpath resource.
 */
@Slf4j
@Component
@StepScope
public class PurchaseCsvReader implements ItemStreamReader<PurchaseCsvRow> {

    @Value("#{jobParameters['purchases.csv.path'] ?: '${csv.purchases.default-path}'}")
    private String csvPath;

    @Value("${csv.purchases.columns}")
    private String[] columns;

    @Value("${csv.purchases.delimiter}")
    private String delimiter;

    @Value("${csv.purchases.lines-to-skip}")
    private int linesToSkip;

    private FlatFileItemReader<PurchaseCsvRow> delegate;

    @PostConstruct
    public void init() throws Exception {
        log.info("Initializing PurchaseCsvReader — path: {}, delimiter: '{}', linesToSkip: {}", csvPath, delimiter, linesToSkip);
        delegate = new FlatFileItemReaderBuilder<PurchaseCsvRow>()
                .name("purchaseCsvReader")
                .resource(ResourceResolver.resolve(csvPath))
                .delimited()
                .delimiter(delimiter)
                .names(columns)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(PurchaseCsvRow.class);
                }})
                .linesToSkip(linesToSkip)
                .build();
        delegate.afterPropertiesSet();
    }

    @Override
    public PurchaseCsvRow read() throws Exception {
        return delegate.read();
    }

    @Override
    public void open(@NonNull ExecutionContext executionContext) {
        log.info("Opening CSV reader for file: {}", csvPath);
        delegate.open(executionContext);
    }

    @Override
    public void update(@NonNull ExecutionContext executionContext) {
        delegate.update(executionContext);
    }

    @Override
    public void close() {
        log.info("Closing CSV reader for file: {}", csvPath);
        delegate.close();
    }
}
