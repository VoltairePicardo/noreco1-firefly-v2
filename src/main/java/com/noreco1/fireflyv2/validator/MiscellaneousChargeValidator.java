package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.MiscellaneousCharge;
import com.noreco1.fireflyv2.service.MiscellaneousChargeService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class MiscellaneousChargeValidator implements Validator {

    private MiscellaneousChargeService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return MiscellaneousCharge.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        MiscellaneousCharge miscellaneousCharge = (MiscellaneousCharge) o;

        if(Checker.isStringNullOrEmpty(miscellaneousCharge.getDescription())) {
            errors.rejectValue("description", "miscellaneousCharge.description.required");
        }
        if(miscellaneousCharge.getAccount() == null || !Checker.isValidId(miscellaneousCharge.getAccount().getId())) {
            errors.rejectValue("account", "miscellaneousCharge.account.required");
        }

    }

    public void setService(MiscellaneousChargeService service) {
        this.service = service;
    }
}
