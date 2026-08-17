package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.Department;
import com.noreco1.fireflyv2.service.implementation.DepartmentServiceImpl;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class DepartmentValidator implements Validator {

    private DepartmentServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return Department.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Department department = (Department) o;
    }

    public void setService(DepartmentServiceImpl service) {
        this.service = service;
    }
}
