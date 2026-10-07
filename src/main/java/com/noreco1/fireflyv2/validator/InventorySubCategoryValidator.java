package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.InventorySubCategoryDto;
import com.noreco1.fireflyv2.model.InventorySubCategory;
import com.noreco1.fireflyv2.service.implementation.InventorySubCategoryServiceImpl;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class InventorySubCategoryValidator implements Validator {

    private InventorySubCategoryServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return InventorySubCategoryDto.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        InventorySubCategoryDto dto = (InventorySubCategoryDto) o;

        if (!Checker.isValidId(dto.getCategoryId())) {
            errors.rejectValue("categoryId", "inventorySubCategory.category.required");
        }

        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            errors.rejectValue("description", "inventorySubCategory.description.required");
        } else if (Checker.isValidId(dto.getCategoryId())) {
            InventorySubCategory found = this.service.findByCategoryAndDescription(dto.getCategoryId(), dto.getDescription());
            if (found != null && !found.getId().equals(dto.getId())) {
                errors.rejectValue("description", "inventorySubCategory.description.taken");
            }
        }

    }

    public void setService(InventorySubCategoryServiceImpl service) {
        this.service = service;
    }

}
