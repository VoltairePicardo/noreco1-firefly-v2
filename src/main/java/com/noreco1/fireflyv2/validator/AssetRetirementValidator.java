package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.form.AssetVoucherLinkForm;
import com.noreco1.fireflyv2.controller.form.RetireAssetForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Map;

@Component
public class AssetRetirementValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return RetireAssetForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        RetireAssetForm form = (RetireAssetForm) o;

        if(form.getAssetDetails().isEmpty()) {
            errors.rejectValue("assetDetails", "asset.retirement.accounts.missing");
        }

        if(Checker.isStringNullOrEmpty(form.getRetirementRemarks())) {
            errors.rejectValue("retirementRemarks", "asset.retirement.remarks.required");
        }
    }
}
