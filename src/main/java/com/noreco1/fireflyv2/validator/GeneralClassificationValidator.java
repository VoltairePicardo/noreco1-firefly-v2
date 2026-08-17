package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.GeneralClassification;
import com.noreco1.fireflyv2.service.GeneralClassificationService;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class GeneralClassificationValidator implements Validator {

    private GeneralClassificationService service;

    @Override
    public boolean supports(Class<?> clazz) {
        return GeneralClassification.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        GeneralClassification fundingSource = (GeneralClassification) target;

        GeneralClassification i = this.service.findByDescription(fundingSource.getDescription());
        // insert mode
        if (i != null && !Checker.isValidId(fundingSource.getId())) { // insert mode & same description found
            errors.rejectValue("description", "general.classification.description.taken");
        } else {
            // update mode
            if (i != null && !i.getId().equals(fundingSource.getId())) { // diff item
                errors.rejectValue("description", "general.classification.description.taken");
            }
        }

    }

    public GeneralClassificationService getService() {
        return service;
    }

    public void setService(GeneralClassificationService service) {
        this.service = service;
    }
}
