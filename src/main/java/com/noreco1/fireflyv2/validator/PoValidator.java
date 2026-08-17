package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.PurchaseOrder;
import com.noreco1.fireflyv2.service.PurchaseOrderService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PoValidator implements Validator {

    private PurchaseOrderService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return PurchaseOrder.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        PurchaseOrder po = (PurchaseOrder) o;

    }

    public void setService(PurchaseOrderService service) {
        this.service = service;
    }
}
