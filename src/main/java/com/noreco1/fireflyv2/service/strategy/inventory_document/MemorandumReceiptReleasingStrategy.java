package com.noreco1.fireflyv2.service.strategy.inventory_document;

import com.noreco1.fireflyv2.controller.response.InventoryDocumentDto;
import com.noreco1.fireflyv2.service.MemorandumReceiptService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class MemorandumReceiptReleasingStrategy implements InventoryDocumentReleasingStrategy {

    private final MemorandumReceiptService memorandumReceiptService;

    public MemorandumReceiptReleasingStrategy(MemorandumReceiptService memorandumReceiptService) {
        this.memorandumReceiptService = memorandumReceiptService;
    }

    @Override
    public String getDocumentType() {
        return "MR";
    }

    @Override
    public Page<InventoryDocumentDto> findAllForReleasingByQuery(String query, Pageable pageable) {
        return memorandumReceiptService.findAllForReleasingByQuery(query, pageable);
    }
}
