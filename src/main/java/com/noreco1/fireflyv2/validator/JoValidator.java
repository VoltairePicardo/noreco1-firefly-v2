package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.JobOrder;
import com.noreco1.fireflyv2.service.JobOrderService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class JoValidator implements Validator {

    private JobOrderService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return JobOrder.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        JobOrder jo = (JobOrder) o;

    }

    public void setService(JobOrderService service) {
        this.service = service;
    }
}