package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.PaymentRequest;
import com.noreco1.fireflyv2.service.PaymentRequestService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PayReqValidator implements Validator {

    private PaymentRequestService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return PaymentRequest.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        PaymentRequest rp = (PaymentRequest) o;

    }

    public void setService(PaymentRequestService service) {
        this.service = service;
    }
}
