package org.cadabra.service;

import org.cadabra.model.ValidationError;
import org.cadabra.repository.ValidationErrorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidationErrorServiceTest {

    @Mock
    private ValidationErrorRepository validationErrorRepository;

    @InjectMocks
    private ValidationErrorService validationErrorService;

    @Test
    void shouldSaveValidationError() {
        validationErrorService.save(1L, "loadPurchasesStep", "january.csv", "bad,row", "Parse error");

        ArgumentCaptor<ValidationError> captor = ArgumentCaptor.forClass(ValidationError.class);
        verify(validationErrorRepository).save(captor.capture());

        ValidationError saved = captor.getValue();
        assertThat(saved.getJobExecutionId()).isEqualTo(1L);
        assertThat(saved.getStepName()).isEqualTo("loadPurchasesStep");
        assertThat(saved.getFileName()).isEqualTo("january.csv");
        assertThat(saved.getRawData()).isEqualTo("bad,row");
        assertThat(saved.getErrorMessage()).isEqualTo("Parse error");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldTruncateRawDataAndErrorMessageWhenExceedMaxLength() {
        String longString = "x".repeat(3000);

        validationErrorService.save(1L, "loadPurchasesStep", "january.csv", longString, longString);

        ArgumentCaptor<ValidationError> captor = ArgumentCaptor.forClass(ValidationError.class);
        verify(validationErrorRepository).save(captor.capture());

        assertThat(captor.getValue().getRawData()).hasSize(2000);
        assertThat(captor.getValue().getErrorMessage()).hasSize(2000);
    }

    @Test
    void shouldNotTruncateWhenWithinMaxLength() {
        String shortString = "short message";

        validationErrorService.save(1L, "loadPurchasesStep", "january.csv", shortString, shortString);

        ArgumentCaptor<ValidationError> captor = ArgumentCaptor.forClass(ValidationError.class);
        verify(validationErrorRepository).save(captor.capture());

        assertThat(captor.getValue().getRawData()).isEqualTo(shortString);
        assertThat(captor.getValue().getErrorMessage()).isEqualTo(shortString);
    }

    @Test
    void shouldHandleNullRawDataAndErrorMessage() {
        validationErrorService.save(1L, "loadPurchasesStep", "january.csv", null, null);

        ArgumentCaptor<ValidationError> captor = ArgumentCaptor.forClass(ValidationError.class);
        verify(validationErrorRepository).save(captor.capture());

        assertThat(captor.getValue().getRawData()).isNull();
        assertThat(captor.getValue().getErrorMessage()).isNull();
    }
}
