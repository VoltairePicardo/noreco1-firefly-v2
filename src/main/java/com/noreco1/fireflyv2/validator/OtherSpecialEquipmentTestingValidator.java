package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.OtherSpecialEquipmentTesting;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class OtherSpecialEquipmentTestingValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return OtherSpecialEquipmentTesting.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        OtherSpecialEquipmentTesting otherSpecialEquipmentTesting = (OtherSpecialEquipmentTesting) o;

        if (otherSpecialEquipmentTesting.getSpecialEquipment() == null
                || otherSpecialEquipmentTesting.getSpecialEquipment().getId() == null) {
            errors.rejectValue("specialEquipment", "otherSpecialEquipmentTesting.specialEquipment.empty", "Special equipment is required.");
        }
    }
}
