package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.FundingSource;
import com.noreco1.fireflyv2.service.FundingSourceService;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class FundingSourceValidator implements Validator {

    private FundingSourceService service;

    @Override
    public boolean supports(Class<?> clazz) {
        return FundingSource.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        FundingSource fundingSource = (FundingSource) target;

        FundingSource i = this.service.findByDescription(fundingSource.getDescription());
        // insert mode
        if (i != null && !Checker.isValidId(fundingSource.getId())) { // insert mode & same description found
            errors.rejectValue("description", "fundingSource.description.taken");
        } else {
            // update mode
            if (i != null && !i.getId().equals(fundingSource.getId())) { // diff item
                errors.rejectValue("description", "fundingSource.description.taken");
            }
        }

    }

    public void setService(FundingSourceService service) {
        this.service = service;
    }

}
