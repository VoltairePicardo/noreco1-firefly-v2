package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.OtherDeposit;
import com.noreco1.fireflyv2.service.BankReconService;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class OdValidator implements Validator {

    private BankReconService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return OtherDeposit.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        OtherDeposit od = (OtherDeposit) o;

    }

    public void setService(BankReconService service) {
        this.service = service;
    }
}
