package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.CheckConfig;
import com.noreco1.fireflyv2.model.Item;
import com.noreco1.fireflyv2.service.implementation.CheckConfigServiceImpl;
import com.noreco1.fireflyv2.service.implementation.ItemServiceImpl;
import com.noreco1.fireflyv2.service.CheckConfigService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class CheckConfigValidator implements Validator {

    private CheckConfigServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return CheckConfig.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        CheckConfig checkConfig = (CheckConfig) o;

        if (checkConfig.getWithSigner() == null) checkConfig.setWithSigner(true);
        if (checkConfig.getShowDesignation() == null) checkConfig.setShowDesignation(true);

        CheckConfig i = this.service.findByCode(checkConfig.getCode());
        // insert mode
        if (checkConfig.getId() == null || checkConfig.getId() == 0) {
            if (i != null) {
                errors.rejectValue("code", "check.code.taken");
            }
        } else {
            // update role
            if (i != null && i.getId() != checkConfig.getId()) { // diff check
                errors.rejectValue("code", "check.code.taken");
            }
        }
    }

    public void setService(CheckConfigServiceImpl service) {
        this.service = service;
    }
}
