package com.noreco1.fireflyv2.service.strategy.inventory_document;

import com.noreco1.fireflyv2.controller.response.InventoryDocumentDto;
import com.noreco1.fireflyv2.service.StockTransferService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class StockTransferReleasingStrategy implements InventoryDocumentReleasingStrategy {

    private final StockTransferService stockTransferService;

    public StockTransferReleasingStrategy(StockTransferService stockTransferService) {
        this.stockTransferService = stockTransferService;
    }

    @Override
    public String getDocumentType() {
        return "ST";
    }

    @Override
    public Page<InventoryDocumentDto> findAllForReleasingByQuery(String query, Pageable pageable) {
        return stockTransferService.findAllForReleasingByQuery(query, pageable);
    }
}
