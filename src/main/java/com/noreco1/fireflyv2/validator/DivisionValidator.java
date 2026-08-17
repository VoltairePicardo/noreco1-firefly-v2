package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.Division;
import com.noreco1.fireflyv2.service.implementation.DivisionServiceImpl;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class DivisionValidator implements Validator {

    private DivisionServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return Division.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Division division = (Division) o;
    }

    public void setService(DivisionServiceImpl service) {
        this.service = service;
    }
}
