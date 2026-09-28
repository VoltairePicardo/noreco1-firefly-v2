package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.MeterTesting;
import com.noreco1.fireflyv2.model.TransformerTesting;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class TransformerTestingValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return MeterTesting.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        TransformerTesting transformerTesting = (TransformerTesting) o;

        if (Checker.collectionIsEmpty(transformerTesting.getVoltageRatioTests())) {
            errors.rejectValue("details", "transformer.voltageRatioTests.empty", "No voltage ration test records to save.");
        }

        if (Checker.collectionIsEmpty(transformerTesting.getLossTests())) {
            errors.rejectValue("details", "transformer.lossTests.empty", "No transformer loss test records to save.");
        }
    }
}
