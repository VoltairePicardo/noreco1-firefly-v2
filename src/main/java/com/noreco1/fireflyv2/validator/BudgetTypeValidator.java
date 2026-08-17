package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.BudgetType;
import com.noreco1.fireflyv2.service.BudgetTypeService;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class BudgetTypeValidator implements Validator {

    private BudgetTypeService service;

    @Override
    public boolean supports(Class<?> clazz) {
        return BudgetType.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        BudgetType budgetType = (BudgetType) target;

        BudgetType i = this.service.findByDescription(budgetType.getDescription());
        // insert mode
        if (i != null && !Checker.isValidId(budgetType.getId())) { // insert mode & same description found
            errors.rejectValue("description", "budgetType.description.taken");
        } else {
            // update mode
            if (i != null && !i.getId().equals(budgetType.getId())) { // diff item
                errors.rejectValue("description", "budgetType.description.taken");
            }
        }

    }

    public void setService(BudgetTypeService service) {
        this.service = service;
    }
}
