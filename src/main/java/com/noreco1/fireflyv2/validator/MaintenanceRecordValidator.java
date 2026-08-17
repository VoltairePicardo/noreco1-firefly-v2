package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.MaintenanceRecord;
import com.noreco1.fireflyv2.service.implementation.MaintenanceRecordServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class MaintenanceRecordValidator implements Validator {

    @Autowired
    MaintenanceRecordServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return MaintenanceRecord.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object object, Errors errors) {
        MaintenanceRecord maintenanceRecord = (MaintenanceRecord) object;

        if(maintenanceRecord.getAsset() == null) {
            errors.rejectValue("asset", "maintenanceRecord.asset.required");
        }

        if(Checker.collectionIsEmpty(maintenanceRecord.getMaintenanceRecordWorks()) && Checker.collectionIsEmpty(maintenanceRecord.getMaintenanceRecordMaterialReleases())) {
            errors.rejectValue("maintenanceRecordWorks", "maintenanceRecord.items.required");
        }

    }

    public void setService(MaintenanceRecordServiceImpl service) {
        this.service = service;
    }
}
