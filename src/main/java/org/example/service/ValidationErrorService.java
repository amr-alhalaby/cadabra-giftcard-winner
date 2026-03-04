package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.model.ValidationError;
import org.example.repository.ValidationErrorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ValidationErrorService {

    private static final int MAX_LENGTH = 2000;

    private final ValidationErrorRepository validationErrorRepository;

    public void save(Long jobExecutionId, String stepName, String fileName, String rawData, String errorMessage) {
        validationErrorRepository.save(ValidationError.builder()
                .jobExecutionId(jobExecutionId)
                .stepName(stepName)
                .fileName(fileName)
                .rawData(truncate(rawData))
                .errorMessage(truncate(errorMessage))
                .createdAt(LocalDateTime.now())
                .build());
    }

    private String truncate(String value) {
        if (value == null) return null;
        return value.length() > MAX_LENGTH ? value.substring(0, MAX_LENGTH) : value;
    }

}
