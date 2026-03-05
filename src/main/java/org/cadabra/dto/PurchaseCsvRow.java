package org.cadabra.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO representing a row from the purchases CSV file.
 */
@Data
@NoArgsConstructor
public class PurchaseCsvRow {

    private Long userId;

    private BigDecimal amount;
}

