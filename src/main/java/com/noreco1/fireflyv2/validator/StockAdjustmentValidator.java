package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.StockAdjustment;
import com.noreco1.fireflyv2.service.StockAdjustmentService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class StockAdjustmentValidator implements Validator {

    private StockAdjustmentService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return StockAdjustment.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        StockAdjustment mct = (StockAdjustment) o;
    }

    public void setService(StockAdjustmentService service) {
        this.service = service;
    }
}
