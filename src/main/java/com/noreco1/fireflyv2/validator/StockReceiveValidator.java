package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.StockReceive;
import com.noreco1.fireflyv2.service.StockReceiveService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class StockReceiveValidator implements Validator {

    private StockReceiveService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return StockReceive.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        StockReceive voucher = (StockReceive) o;

    }

    public void setService(StockReceiveService service) {
        this.service = service;
    }
}
