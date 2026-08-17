package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.BudgetItemClassification;
import com.noreco1.fireflyv2.service.implementation.BudgetItemClassificationImpl;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class BudgetItemClassificationValidator implements Validator {

    private BudgetItemClassificationImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return BudgetItemClassification.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        BudgetItemClassification budgetItemClassification = (BudgetItemClassification) o;

        BudgetItemClassification i = this.service.findByDescription(budgetItemClassification.getDescription());
        // insert mode
        if (i != null && !Checker.isValidId(budgetItemClassification.getId())) { // insert mode & same description found
            errors.rejectValue("description", "budgetItemClassification.description.taken");
        } else {
            // update mode
            if (i != null && !i.getId().equals(budgetItemClassification.getId())) { // diff item
                errors.rejectValue("description", "budgetItemClassification.description.taken");
            }
        }
    }

    public void setService(BudgetItemClassificationImpl service) {
        this.service = service;
    }

}
