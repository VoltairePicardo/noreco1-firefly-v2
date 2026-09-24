package com.noreco1.fireflyv2.service.strategy.inventory_document;

import com.noreco1.fireflyv2.controller.response.InventoryDocumentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryDocumentReleasingStrategy {

    String getDocumentType();
    Page<InventoryDocumentDto> findAllForReleasingByQuery(String query, Pageable pageable);
}
