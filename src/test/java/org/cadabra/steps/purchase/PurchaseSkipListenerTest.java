package org.cadabra.steps.purchase;

import org.cadabra.service.ValidationErrorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PurchaseSkipListenerTest {

    @Mock
    private ValidationErrorService validationErrorService;

    @InjectMocks
    private PurchaseSkipListener purchaseSkipListener;

    @BeforeEach
    void setUp() {
        JobExecution jobExecution = new JobExecution(10L);
        StepExecution stepExecution = new StepExecution("loadPurchasesStep", jobExecution);
        ReflectionTestUtils.setField(purchaseSkipListener, "stepExecution", stepExecution);
        ReflectionTestUtils.setField(purchaseSkipListener, "fileName", "january.csv");
    }

    @Test
    void shouldSaveErrorOnSkipInRead() {
        purchaseSkipListener.onSkipInRead(new RuntimeException("Parse failed"));

        verify(validationErrorService).save(
                eq(10L),
                anyString(),
                eq("january.csv"),
                anyString(),
                contains("Parse failed")
        );
    }

    @Test
    void shouldSaveErrorOnSkipInProcess() {
        purchaseSkipListener.onSkipInProcess("bad-item", new RuntimeException("Mapping failed"));

        verify(validationErrorService).save(
                eq(10L),
                anyString(),
                eq("january.csv"),
                eq("bad-item"),
                contains("Mapping failed")
        );
    }

    @Test
    void shouldSaveErrorOnSkipInWrite() {
        purchaseSkipListener.onSkipInWrite("bad-item", new RuntimeException("Write failed"));

        verify(validationErrorService).save(
                eq(10L),
                anyString(),
                eq("january.csv"),
                eq("bad-item"),
                contains("Write failed")
        );
    }

    @Test
    void shouldExtractRawDataFromCauseWhenPresent() {
        RuntimeException cause = new RuntimeException("Root cause");
        RuntimeException wrapper = new RuntimeException("Wrapper", cause);

        purchaseSkipListener.onSkipInRead(wrapper);

        verify(validationErrorService).save(
                eq(10L),
                anyString(),
                eq("january.csv"),
                eq("Root cause"),
                anyString()
        );
    }
}

