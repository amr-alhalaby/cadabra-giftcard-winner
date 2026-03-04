package org.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Persists each validation error as a row in the database
 * for tracking and auditing purposes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "validation_errors")
public class ValidationError {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private Long jobExecutionId;

    private String stepName;

    private String fileName;

    @Column(length = 1000)
    private String rawData;

    @Column(length = 1000)
    private String errorMessage;

    private LocalDateTime createdAt;
}
