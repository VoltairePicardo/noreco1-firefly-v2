package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.CashflowItem;
import com.noreco1.fireflyv2.service.implementation.CashflowItemServiceImpl;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class CashflowAccountValidator implements Validator {

    private CashflowItemServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return CashflowItem.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        CashflowItem asset = (CashflowItem) o;
    }

    public void setService(CashflowItemServiceImpl service) {
        this.service = service;
    }
}
