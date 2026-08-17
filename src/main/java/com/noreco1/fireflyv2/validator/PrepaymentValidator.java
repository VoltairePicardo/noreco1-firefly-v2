package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.Prepayment;
import com.noreco1.fireflyv2.service.PrepaymentService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PrepaymentValidator implements Validator {

    private PrepaymentService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return Prepayment.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Prepayment pp = (Prepayment) o;

    }

    public void setService(PrepaymentService service) {
        this.service = service;
    }
}
