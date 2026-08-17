package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.AllocationFactorDto;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;
import java.util.Map;

public class AllocationFactorValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return AllocationFactorDto.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        AllocationFactorDto afd = (AllocationFactorDto) o;

        if (afd.getEffectivity() == null || afd.getEffectivity().getId() == null || afd.getEffectivity().getId()  <= 0) {
            errors.rejectValue("effectivity", "allocation.factor.effect.date");
        }
        if (afd.getAccount() == null) {
            errors.rejectValue("account", "allocation.factor.account");
        }
        if (!Checker.collectionIsEmpty(afd.getSegmentPercentage())) {

            BigDecimal total = BigDecimal.ZERO;
            for (Map f:afd.getSegmentPercentage()){
                BigDecimal percentage = new BigDecimal(f.get("value") == null ? "0":f.get("value").toString());
                total = total.add(percentage);
            }
            if (total.compareTo(new BigDecimal("100")) != 0) {
                errors.rejectValue("segmentPercentage", "allocation.factor.total");
            }
        } else {
            errors.rejectValue("segmentPercentage", "allocation.factor.segment");
        }
    }
}
