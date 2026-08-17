package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.SpecialEquipmentAssignment;
import com.noreco1.fireflyv2.service.implementation.SpecialEquipmentAssignmentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class SpecialEquipmentAssignmentValidator implements Validator {

    @Autowired
    SpecialEquipmentAssignmentServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return SpecialEquipmentAssignment.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        SpecialEquipmentAssignment specialEquipmentAssignment = (SpecialEquipmentAssignment) o;

        if(specialEquipmentAssignment.isHasConnectOrder()){
            if(specialEquipmentAssignment.getStockRelease() == null) {
                errors.rejectValue("stockRelease", "specialEquipmentAssignment.stockRelease.required");
            }
        } else {

            if(specialEquipmentAssignment.isSoleOwner()){
                if(specialEquipmentAssignment.getConsumer() == null) {
                    errors.rejectValue("consumer", "specialEquipmentAssignment.consumer.required");
                }
            } else{

                if(specialEquipmentAssignment.getTown() == null) {
                    errors.rejectValue("town", "specialEquipmentAssignment.town.required");
                }

                if(specialEquipmentAssignment.getBarangay() == null) {
                    errors.rejectValue("barangay", "specialEquipmentAssignment.barangay.required");
                }
            }

        }

        if(Checker.collectionIsEmpty(specialEquipmentAssignment.getSpecialEquipmentAssignmentDetails())) {
            errors.rejectValue("specialEquipmentAssignmentDetails", "specialEquipmentAssignment.details.required");
        }

    }

    public void setService(SpecialEquipmentAssignmentServiceImpl service) {
        this.service = service;
    }
}
