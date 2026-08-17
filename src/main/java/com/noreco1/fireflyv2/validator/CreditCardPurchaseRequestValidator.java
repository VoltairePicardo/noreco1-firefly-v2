package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.service.CreditCardPurchaseRequestService;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class CreditCardPurchaseRequestValidator implements Validator {

    private CreditCardPurchaseRequestService service;

    @Override
    public boolean supports(Class<?> clazz) {
        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {

    }

    public void setService(CreditCardPurchaseRequestService service) {
        this.service = service;
    }
}
