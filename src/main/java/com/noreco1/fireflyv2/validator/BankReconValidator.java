package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.BankReconciliation;
import com.noreco1.fireflyv2.service.BankReconService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class BankReconValidator implements Validator {

    private BankReconService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return BankReconciliation.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        BankReconciliation rv = (BankReconciliation) o;

    }

    public void setService(BankReconService service) {
        this.service = service;
    }
}
