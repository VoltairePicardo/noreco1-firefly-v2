package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.InventoryCategoryDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.InventoryCategory;
import com.noreco1.fireflyv2.model.InventoryCategoryType;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;

import java.util.List;

public interface InventoryCategoryService {

    PostResponse processCreate(InventoryCategoryDto dto, BindingResult bindingResult, MessageSource messageSource);

    PostResponse processUpdate(InventoryCategoryDto dto, BindingResult bindingResult, MessageSource messageSource);

    Page<InventoryCategory> findAll(Pageable pageable);
    Page<InventoryCategory> find(String query, Pageable pageable);
    InventoryCategoryDto findById(Integer id);
    InventoryCategory findByDescription(String description);
    List<InventoryCategoryType> findAllTypes();

}
