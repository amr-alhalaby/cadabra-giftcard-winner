package org.example.steps.purchase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.service.ValidationErrorService;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Listens for skipped items during fault-tolerant step execution.
 * Persists each skipped row to the validation_errors table.
 */
@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class PurchaseSkipListener implements SkipListener<Object, Object> {

    private final ValidationErrorService validationErrorService;

    @Value("#{stepExecution}")
    private StepExecution stepExecution;

    @Value("#{jobParameters['purchases.csv.path'] ?: '${csv.purchases.default-path}'}")
    private String fileName;

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("Skipped row during read: {}", t.getMessage());
        saveError("Read error: " + t.getMessage(), extractRawData(t));
    }

    @Override
    public void onSkipInProcess(Object item, Throwable t) {
        log.warn("Skipped item during process: {}. Error: {}", item, t.getMessage());
        saveError("Process error: " + t.getMessage(), item.toString());
    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        log.warn("Skipped item during write: {}. Error: {}", item, t.getMessage());
        saveError("Write error: " + t.getMessage(), item.toString());
    }

    private void saveError(String errorMessage, String rawData) {
        validationErrorService.save(
                stepExecution.getJobExecutionId(),
                Constants.LOAD_PURCHASES_STEP,
                fileName,
                rawData,
                errorMessage);
    }

    private String extractRawData(Throwable t) {
        if (t.getCause() != null) {
            return t.getCause().getMessage();
        }
        return t.getMessage();
    }
}
