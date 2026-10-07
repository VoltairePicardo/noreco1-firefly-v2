package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.InventorySubCategoryDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.InventorySubCategory;
import org.springframework.context.MessageSource;
import org.springframework.validation.BindingResult;

public interface InventorySubCategoryService {

    PostResponse processCreate(InventorySubCategoryDto dto, BindingResult bindingResult, MessageSource messageSource);

    PostResponse processUpdate(InventorySubCategoryDto dto, BindingResult bindingResult, MessageSource messageSource);

    InventorySubCategoryDto findById(Integer id);
    InventorySubCategory findByCategoryAndDescription(Integer categoryId, String description);

}
