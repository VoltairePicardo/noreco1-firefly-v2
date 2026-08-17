package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.CashAdvance;
import com.noreco1.fireflyv2.service.implementation.CashAdvanceServiceImpl;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class CashAdvanceValidator implements Validator {

    private CashAdvanceServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return CashAdvance.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        CashAdvance cashAdvance = (CashAdvance) o;
    }

    public void setService(CashAdvanceServiceImpl service) {
        this.service = service;
    }
}
