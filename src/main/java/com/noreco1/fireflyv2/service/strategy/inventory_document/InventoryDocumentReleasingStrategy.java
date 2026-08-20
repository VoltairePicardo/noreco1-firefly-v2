package com.noreco1.fireflyv2.service.strategy.inventory_document;

import com.noreco1.fireflyv2.controller.response.InventoryDocumentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Strategy for querying releasable inventory documents (Stock Transfer, Stock
 * Withdrawal, Memorandum Receipt, ...) by their document type code.
 * <p>
 * Implementations are picked up automatically by Spring and registered in
 * {@link InventoryDocumentReleasingStrategyRegistry} - no factory/switch needed.
 */
public interface InventoryDocumentReleasingStrategy {

    String getDocumentType();
    Page<InventoryDocumentDto> findAllForReleasingByQuery(String query, Pageable pageable);
}
