package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.MeterTesting;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class MeterTestingValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return MeterTesting.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        MeterTesting meterTesting = (MeterTesting) o;

        if (meterTesting.getMeterModel() == null || !Checker.isValidId(meterTesting.getMeterModel().getId())) {
            errors.rejectValue("meterModel", "meterTesting.meterModel.required", "Please select a meter model.");
        }

        if (Checker.collectionIsEmpty(meterTesting.getDetails())) {
            errors.rejectValue("details", "meterTesting.details.empty", "No meter testing records to save.");
        }
    }
}