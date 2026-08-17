package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.PurchaseRequest;
import com.noreco1.fireflyv2.service.PurchaseRequestService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class RvValidator implements Validator {

    private PurchaseRequestService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return PurchaseRequest.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
    }

    public void setService(PurchaseRequestService service) {
        this.service = service;
    }
}
