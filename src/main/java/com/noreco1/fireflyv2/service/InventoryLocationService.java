package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.InventoryLocation;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;

public interface InventoryLocationService {

    PostResponse processCreate(InventoryLocation inventoryLocation, BindingResult bindingResult, MessageSource messageSource);

    PostResponse processUpdate(InventoryLocation inventoryLocation, BindingResult bindingResult, MessageSource messageSource);

    Page<InventoryLocation> findAll(Pageable pageable);
    Page<InventoryLocation> find(String query, Pageable pageable);
    InventoryLocation findById(Integer id);
    InventoryLocation findByDescription(String query);

}
