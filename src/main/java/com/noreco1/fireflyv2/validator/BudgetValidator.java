package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.Budget;
import com.noreco1.fireflyv2.service.implementation.BudgetServiceImpl;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class BudgetValidator implements Validator {

    private BudgetServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return Budget.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Budget budget = (Budget) o;
    }

    public void setService(BudgetServiceImpl service) {
        this.service = service;
    }
}
