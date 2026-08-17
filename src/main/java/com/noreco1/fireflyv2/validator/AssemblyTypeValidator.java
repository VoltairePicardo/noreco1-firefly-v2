package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.AssemblyType;
import com.noreco1.fireflyv2.service.implementation.AssemblyUnitServiceImpl;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class AssemblyTypeValidator implements Validator {

    private AssemblyUnitServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return AssemblyType.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        AssemblyType assemblyType = (AssemblyType) o;
    }

    public void setService(AssemblyUnitServiceImpl service) {
        this.service = service;
    }
}
