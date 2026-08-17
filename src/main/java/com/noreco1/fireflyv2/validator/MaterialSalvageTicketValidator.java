package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.MaterialSalvageTicket;
import com.noreco1.fireflyv2.service.MaterialSalvageTicketService;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class MaterialSalvageTicketValidator implements Validator {

    private MaterialSalvageTicketService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return MaterialSalvageTicket.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        MaterialSalvageTicket mct = (MaterialSalvageTicket) o;
    }

    public void setService(MaterialSalvageTicketService service) {
        this.service = service;
    }
}

