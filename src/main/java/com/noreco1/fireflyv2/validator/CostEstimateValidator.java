package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.CostEstimate;
import com.noreco1.fireflyv2.service.CostEstimateService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class CostEstimateValidator implements Validator {

    private CostEstimateService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return CostEstimate.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        CostEstimate costEstimate = (CostEstimate) o;
    }

    public void setService(CostEstimateService service) {
        this.service = service;
    }
}
