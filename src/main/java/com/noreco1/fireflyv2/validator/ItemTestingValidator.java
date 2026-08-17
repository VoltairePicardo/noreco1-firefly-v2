package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.ItemTesting;
import com.noreco1.fireflyv2.model.ItemTestingDetail;
import com.noreco1.fireflyv2.service.implementation.ItemTestingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;

public class ItemTestingValidator implements Validator {

    @Autowired
    ItemTestingServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return ItemTesting.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        ItemTesting itemTesting = (ItemTesting) o;

        if(!Checker.isValidId(itemTesting.getInventoryLocation().getId())){
            errors.rejectValue("inventoryLocation", "itemTesting.inventoryLocation.required");

        }

        if(Checker.collectionIsEmpty(itemTesting.getItemTestingDetails())){
            errors.rejectValue("itemTestingDetails", "itemTesting.itemTestingDetails.empty");
        } else{

            BigDecimal totalQuantity = BigDecimal.ZERO;

            for (ItemTestingDetail detail : itemTesting.getItemTestingDetails()){

                totalQuantity = totalQuantity.add(detail.getQuantityReceived());
            }

            if(totalQuantity.equals(BigDecimal.ZERO)){
                errors.rejectValue("itemTestingDetails", "itemTesting.itemTestingDetails.quantity.zero");
            }

        }

    }

    public void setService(ItemTestingServiceImpl service) {
        this.service = service;
    }
}
