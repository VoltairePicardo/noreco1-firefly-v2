package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.Section;
import com.noreco1.fireflyv2.service.implementation.SectionServiceImpl;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class SectionValidator implements Validator {

    private SectionServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return Section.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Section section = (Section) o;
    }

    public void setService(SectionServiceImpl service) {
        this.service = service;
    }
}
