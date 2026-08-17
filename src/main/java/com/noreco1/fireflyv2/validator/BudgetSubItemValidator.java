package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.controller.response.BudgetSubItemDto;
import com.noreco1.fireflyv2.service.BudgetSubItemService;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class BudgetSubItemValidator implements Validator {

    private BudgetSubItemService service;

    @Override
    public boolean supports(Class<?> clazz) {
        return BudgetSubItemDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        BudgetSubItemDto budgetSubItemDto = (BudgetSubItemDto) target;

    }

    public void setService(BudgetSubItemService service) {
        this.service = service;
    }
}
