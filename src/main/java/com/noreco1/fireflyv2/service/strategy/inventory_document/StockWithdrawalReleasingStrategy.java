package com.noreco1.fireflyv2.service.strategy.inventory_document;

import com.noreco1.fireflyv2.controller.response.InventoryDocumentDto;
import com.noreco1.fireflyv2.service.StockWithdrawalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class StockWithdrawalReleasingStrategy implements InventoryDocumentReleasingStrategy {

    private final StockWithdrawalService stockWithdrawalService;

    public StockWithdrawalReleasingStrategy(StockWithdrawalService stockWithdrawalService) {
        this.stockWithdrawalService = stockWithdrawalService;
    }

    @Override
    public String getDocumentType() {
        return "SW";
    }

    @Override
    public Page<InventoryDocumentDto> findAllForReleasingByQuery(String query, Pageable pageable) {
        return stockWithdrawalService.findAllForReleasingByQuery(query, pageable);
    }
}
