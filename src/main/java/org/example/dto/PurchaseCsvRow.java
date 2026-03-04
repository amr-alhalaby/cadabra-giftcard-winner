package org.example.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO representing a row from the purchases CSV file.
 */
@Data
@NoArgsConstructor
public class PurchaseCsvRow {

    @NotNull(message = "User ID must not be null")
    private Long userId;

    @NotNull(message = "Amount must not be null")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
}

