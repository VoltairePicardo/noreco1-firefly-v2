package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.MaterialCreditTicket;
import com.noreco1.fireflyv2.service.MaterialCreditTicketService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class MaterialCreditTicketValidator implements Validator {

    private MaterialCreditTicketService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return MaterialCreditTicket.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        MaterialCreditTicket mct = (MaterialCreditTicket) o;
    }

    public void setService(MaterialCreditTicketService service) {
        this.service = service;
    }
}
