package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.InventoryCategoryDto;
import com.noreco1.fireflyv2.model.InventoryCategory;
import com.noreco1.fireflyv2.service.implementation.InventoryCategoryServiceImpl;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class InventoryCategoryValidator implements Validator {

    private InventoryCategoryServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return InventoryCategoryDto.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        InventoryCategoryDto dto = (InventoryCategoryDto) o;

        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            errors.rejectValue("description", "inventoryCategory.description.required");
        } else {
            InventoryCategory found = this.service.findByDescription(dto.getDescription());
            if (found != null && !found.getId().equals(dto.getId())) {
                errors.rejectValue("description", "inventoryCategory.description.taken");
            }
        }

        if (dto.getType() == null || !Checker.isValidId(dto.getType().getId())) {
            errors.rejectValue("type", "inventoryCategory.type.required");
        }

    }

    public void setService(InventoryCategoryServiceImpl service) {
        this.service = service;
    }

}
