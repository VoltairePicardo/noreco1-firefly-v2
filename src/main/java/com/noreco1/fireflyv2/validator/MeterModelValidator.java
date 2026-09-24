package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.MeterModel;
import com.noreco1.fireflyv2.service.implementation.MeterModelServiceImpl;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class MeterModelValidator implements Validator {

    private MeterModelServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return MeterModel.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        MeterModel meterModel = (MeterModel) o;

        if (meterModel.getModelName() == null || meterModel.getModelName().isBlank()) {
            errors.rejectValue("modelName", "meterModel.modelName.required");
        } else {
            MeterModel i = this.service.findByModelName(meterModel.getModelName());
            // insert mode
            if (i != null && !Checker.isValidId(meterModel.getId())) { // insert mode & same name found
                errors.rejectValue("modelName", "meterModel.modelName.taken");
            } else {
                // update mode
                if (i != null && !i.getId().equals(meterModel.getId())) { // diff item
                    errors.rejectValue("modelName", "meterModel.modelName.taken");
                }
            }
        }

        if (meterModel.getBrand() == null || !Checker.isValidId(meterModel.getBrand().getId())) {
            errors.rejectValue("brand", "meterModel.brand.required");
        }

        if (meterModel.getMeterType() == null || !Checker.isValidId(meterModel.getMeterType().getId())) {
            errors.rejectValue("meterType", "meterModel.meterType.required");
        }
    }

    public void setService(MeterModelServiceImpl service) {
        this.service = service;
    }
}
