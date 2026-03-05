package org.cadabra.steps.purchase;

import org.cadabra.dto.PurchaseCsvRow;
import org.cadabra.mapper.PurchaseMapper;
import org.cadabra.model.Purchase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseProcessorTest {

    @Mock
    private PurchaseMapper purchaseMapper;

    @InjectMocks
    private PurchaseProcessor purchaseProcessor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(purchaseProcessor, "jobExecutionId", 42L);
    }

    @Test
    void shouldMapRowAndSetJobExecutionId() {
        PurchaseCsvRow row = new PurchaseCsvRow();
        row.setUserId(1L);
        row.setAmount(new BigDecimal("55.00"));

        Purchase mapped = new Purchase();
        mapped.setUserId(1L);
        mapped.setAmount(new BigDecimal("55.00"));

        when(purchaseMapper.toPurchase(row)).thenReturn(mapped);

        Purchase result = purchaseProcessor.process(row);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualByComparingTo("55.00");
        assertThat(result.getJobExecutionId()).isEqualTo(42L);
    }
}

