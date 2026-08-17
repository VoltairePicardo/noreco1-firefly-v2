package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.InventoryLocation;
import com.noreco1.fireflyv2.service.implementation.InventoryLocationServiceImpl;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class InventoryLocationValidator implements Validator {

    private InventoryLocationServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return InventoryLocation.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        InventoryLocation inventoryLocation = (InventoryLocation) o;

        InventoryLocation i = this.service.findByDescription(inventoryLocation.getDescription());

        // insert mode
        if (i != null && !Checker.isValidId(inventoryLocation.getId())) { // insert mode & same description found
            errors.rejectValue("description", "inventoryLocation.description.taken");
        } else {
            // update mode
            if (i != null && !i.getId().equals(inventoryLocation.getId())) { // diff item
                errors.rejectValue("description", "inventoryLocation.description.taken");
            }
        }

    }

    public void setService(InventoryLocationServiceImpl service) {
        this.service = service;
    }

}
