package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.UnitMeasure;
import com.noreco1.fireflyv2.service.UnitMeasureService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class UnitMeasureValidator implements Validator {

    private UnitMeasureService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return UnitMeasure.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        UnitMeasure unitMeasure = (UnitMeasure) o;

        // ============= check code =================
        UnitMeasure r = this.service.findByCode(unitMeasure.getCode());
        // insert mode
        if (unitMeasure.getId() == null || unitMeasure.getId() == 0) {
            if (r != null) {
                errors.rejectValue("code", "unit.code.taken");
            }
        } else {
            // update
            if (r != null && r.getId() != unitMeasure.getId()) { // diff object
                errors.rejectValue("code", "unit.code.taken");
            }
        }

        // ============= check description =================
        r = this.service.findByDescription(unitMeasure.getDescription());
        // insert mode
        if (unitMeasure.getId() == null || unitMeasure.getId() == 0) {
            if (r != null) {
                errors.rejectValue("description", "unit.description.taken");
            }
        } else {
            // update
            if (r != null && r.getId() != unitMeasure.getId()) { // diff object
                errors.rejectValue("description", "unit.description.taken");
            }
        }
    }

    public void setService(UnitMeasureService service) {
        this.service = service;
    }
}
