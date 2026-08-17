package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.StockTransfer;
import com.noreco1.fireflyv2.service.StockTransferService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class StockTransferValidator implements Validator {

    private StockTransferService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return StockTransfer.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        StockTransfer mct = (StockTransfer) o;
    }

    public void setService(StockTransferService service) {
        this.service = service;
    }
}
