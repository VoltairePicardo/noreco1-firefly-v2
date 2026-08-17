package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.facade.LedgerFacade;
import com.noreco1.fireflyv2.model.BankDeposit;
import com.noreco1.fireflyv2.repo.AllocationFactorRepo;
import com.noreco1.fireflyv2.repo.MonthlyCycleRepo;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class BankDepositValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return BankDeposit.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

    }

}
