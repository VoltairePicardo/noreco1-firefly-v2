package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.PettyCashFund;
import com.noreco1.fireflyv2.service.PettyCashFundService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.HashMap;

@Component
public class PettyCashFundValidator implements Validator {

    private PettyCashFundService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return PettyCashFund.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        PettyCashFund pettyCashFund = (PettyCashFund) o;

        if(Checker.isStringNullOrEmpty(pettyCashFund.getDescription())) {
            errors.rejectValue("description", "pettyCashFund.description.required");
        }
        if(pettyCashFund.getOffice() == null || !Checker.isValidId(pettyCashFund.getOffice().getId())) {
            errors.rejectValue("office", "pettyCashFund.office.required");
        } else {
            // check dupe office
            PettyCashFund exCashFund = this.service.findByOfficeId(pettyCashFund.getOffice().getId());
            if(exCashFund != null) {    // patty cash fund found given office

                // check if found patty cash fund found is NOT the one we are editing
                if(!exCashFund.getId().equals(pettyCashFund.getId())) {
                    errors.rejectValue("office", "pettyCashFund.office.duplicate");
                }
            }
        }
        if(pettyCashFund.getAccount() == null || !Checker.isValidId(pettyCashFund.getAccount().getId())){
            errors.rejectValue("account", "pettyCashFund.account.required");
        }
    }

    public void setService(PettyCashFundService service) {
        this.service = service;
    }
}
