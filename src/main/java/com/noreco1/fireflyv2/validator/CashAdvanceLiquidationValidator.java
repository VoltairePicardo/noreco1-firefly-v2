package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.CashAdvanceLiquidation;
import com.noreco1.fireflyv2.repo.CashAdvanceLiquidationRepo;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class CashAdvanceLiquidationValidator implements Validator {

    private CashAdvanceLiquidationRepo repo;

    @Override
    public boolean supports(Class<?> clazz) {
        return CashAdvanceLiquidation.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CashAdvanceLiquidation cashAdvanceLiquidation = (CashAdvanceLiquidation) target;
    }

    public void setRepo(CashAdvanceLiquidationRepo repo) {
        this.repo = repo;
    }
}
