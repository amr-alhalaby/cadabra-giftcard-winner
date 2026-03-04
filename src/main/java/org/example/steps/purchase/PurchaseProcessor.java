package org.example.steps.purchase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.PurchaseCsvRow;
import org.example.mapper.PurchaseMapper;
import org.example.model.Purchase;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class PurchaseProcessor implements ItemProcessor<PurchaseCsvRow, Purchase> {

    private final PurchaseMapper purchaseMapper;

    @Value("#{stepExecution.jobExecutionId}")
    private Long jobExecutionId;

    @Override
    public Purchase process(PurchaseCsvRow row) {
        log.debug("Processing CSV row — userId: {}, amount: {}", row.getUserId(), row.getAmount());
        Purchase purchase = purchaseMapper.toPurchase(row);
        purchase.setJobExecutionId(jobExecutionId);
        log.debug("Mapped purchase — userId: {}, amount: {}, jobExecutionId: {}", purchase.getUserId(), purchase.getAmount(), jobExecutionId);
        return purchase;
    }
}
