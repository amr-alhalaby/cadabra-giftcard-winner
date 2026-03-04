package org.example.mapper;

import org.example.dto.PurchaseCsvRow;
import org.example.model.Purchase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PurchaseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobExecutionId", ignore = true)
    Purchase toPurchase(PurchaseCsvRow row);
}
