package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.BillOfMaterial;
import com.noreco1.fireflyv2.service.BillOfMaterialService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class BillOfMaterialValidator implements Validator {

    private BillOfMaterialService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return BillOfMaterial.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        BillOfMaterial billOfMaterial = (BillOfMaterial) o;
    }

    public void setService(BillOfMaterialService service) {
        this.service = service;
    }
}
