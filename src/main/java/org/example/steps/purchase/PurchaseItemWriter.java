package org.example.steps.purchase;

import org.example.model.Purchase;
import org.example.repository.PurchaseRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PurchaseItemWriter implements ItemWriter<Purchase> {

    private final PurchaseRepository purchaseRepository;

    @Override
    public void write(Chunk<? extends Purchase> chunk) {
        purchaseRepository.saveAll(chunk.getItems());
    }
}

