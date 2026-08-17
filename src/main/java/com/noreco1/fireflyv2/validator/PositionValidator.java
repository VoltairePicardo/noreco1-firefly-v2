package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.Position;
import com.noreco1.fireflyv2.service.implementation.PositionServiceImpl;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PositionValidator implements Validator {

    private PositionServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return Position.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Position pos = (Position) o;
    }

    public void setService(PositionServiceImpl service) {
        this.service = service;
    }
}
