package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.ItemsForRepair;
import com.noreco1.fireflyv2.service.ItemsForRepairService;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ItemsForRepairValidator implements Validator {

    private ItemsForRepairService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return ItemsForRepair.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        ItemsForRepair ifr = (ItemsForRepair) o;
    }

    public void setService(ItemsForRepairService service) {
        this.service = service;
    }
}
