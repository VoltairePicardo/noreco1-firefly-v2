package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.service.implementation.NeaPriceIndexServiceImpl;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class NeaPriceIndexValidator implements Validator {

    private NeaPriceIndexServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return false;
    }

    @Override
    public void validate(Object o, Errors errors) {

    }

    public void setService(NeaPriceIndexServiceImpl service) {
        this.service = service;
    }

}
