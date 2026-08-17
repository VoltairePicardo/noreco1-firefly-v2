package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.StrategicInitiative;
import com.noreco1.fireflyv2.service.StrategicInitiativeService;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class StrategicInitiativeValidator implements Validator {

    private StrategicInitiativeService service;

    @Override
    public boolean supports(Class<?> clazz) {
        return StrategicInitiative.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        StrategicInitiative strategicInitiative = (StrategicInitiative) target;

        StrategicInitiative i = this.service.findByDepartmentIdAndDescription(strategicInitiative.getDepartment().getId(), strategicInitiative.getDescription());
        // insert mode
        if (i != null && !Checker.isValidId(strategicInitiative.getId())) { // insert mode & same description found
            errors.rejectValue("description", "strategic.initiative.description.taken");
        } else {
            // update mode
            if (i != null && !i.getId().equals(strategicInitiative.getId())) { // diff item
                errors.rejectValue("description", "strategic.initiative.description.taken");
            }
        }

    }

    public void setService(StrategicInitiativeService service) {
        this.service = service;
    }
}
