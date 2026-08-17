package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.Factor;
import com.noreco1.fireflyv2.model.FactorPercentageDistro;
import com.noreco1.fireflyv2.service.FactorService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class FactorValidator implements Validator {

    private FactorService factorService;

    @Override
    public boolean supports(Class<?> aClass) {
        return Factor.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Factor factor = (Factor) o;

        if(factor.getCode() == null || factor.getCode().equals("") || factor.getCode().trim().length() == 0){
            errors.rejectValue("code", "factor.code.required");
        }else{
            Factor factorFound = this.factorService.findByCode(factor.getCode());
            if(factorFound != null) {
                if(!factorFound.getId().equals(factor.getId())) {
                    errors.rejectValue("code", "factor.code.unique");
                }
            }
        }

        if(factor.getDescription() == null || factor.getDescription().equals("") || factor.getDescription().trim().length() == 0){
            errors.rejectValue("description", "factor.description.required");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (FactorPercentageDistro distro : factor.getFactorPercentageDistroSet()) {

            if (distro.getPercentage() != null) {
                total = total.add(distro.getPercentage());
            }
        }

        boolean noError = true;

        if(factor.getId() == null){
            if(factor.getValidityDate() == null || factor.getValidityDate().getId() == null) {
                errors.rejectValue("validityDate", "factor.validity.required");
            }
        }

        if(factor.getValidityDate() != null) {

            if (total.compareTo(new BigDecimal("100")) != 0) {
                noError = false;
                errors.rejectValue("factorPercentageDistroSet", "factor.code.100");
            }

        } else if(total.compareTo(BigDecimal.ZERO) > 0) {
            noError = false;
            errors.rejectValue("validityDate", "allocation.factor.effect.date");
        }

        // check duplicate validity date
        if(noError && factor.getValidityDate() != null) {
            boolean dateInUsed = this.factorService.validityDateInUsed(factor.getId(), factor.getValidityDate());
            if(dateInUsed) {
                errors.rejectValue("validityDate", "factor.validity.unique");
            }
        }
    }

    // this how we add dependency, @Autowired is not working here!
    public void setService(FactorService service) {
        this.factorService = service;
    }

}
