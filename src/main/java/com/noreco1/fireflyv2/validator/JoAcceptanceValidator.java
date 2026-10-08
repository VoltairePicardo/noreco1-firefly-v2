package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.JobOrderAcceptance;
import com.noreco1.fireflyv2.service.JoAcceptanceService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class JoAcceptanceValidator implements Validator {

    private JoAcceptanceService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return JobOrderAcceptance.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        JobOrderAcceptance joa = (JobOrderAcceptance) o;

    }

    public void setService(JoAcceptanceService service) {
        this.service = service;
    }
}
