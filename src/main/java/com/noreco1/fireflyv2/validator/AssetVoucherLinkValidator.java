package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.form.AssetVoucherLinkForm;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class AssetVoucherLinkValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return AssetVoucherLinkForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        AssetVoucherLinkForm form = (AssetVoucherLinkForm) o;

        if(form.getLinkType() == null || !Checker.isValidId(form.getLinkType().getId())) {
            errors.rejectValue("linkType", "asset.voucherLink.transType.required");
        }
        if(form.getVoucherTransaction() == null || !Checker.isValidId(form.getVoucherTransaction().getId())) {
            errors.rejectValue("voucherTransaction", "asset.voucherLink.transType.required");
        }
        if(form.getAsset() == null || !Checker.isValidId(form.getAsset().getId())) {
            errors.rejectValue("asset", "asset.voucherLink.asset.required");
        } else {

            if(Checker.isMajorRepair(form.getLinkType().getId()) ||
                    Checker.isAssetAcquisition(form.getLinkType().getId()) ||
                    Checker.isAssetAdjustment(form.getLinkType().getId())) {

                if(form.getAssetDetails().size() > 0) {

                    for (Map row: form.getAssetDetails()) {

                        Object assetAccount = row.get("assetAccount");
                        if(assetAccount == null) {
                            errors.rejectValue("assetDetails", "asset.voucherLink.account.asset.missing");
                            break;
                        }

                        Object expenseAccount = row.get("expenseAccount");
                        if(expenseAccount == null) {
                            errors.rejectValue("assetDetails", "asset.voucherLink.account.expense.missing");
                            break;
                        }

                        Object accumDepAccount = row.get("accumDepAccount");
                        if(accumDepAccount == null) {
                            errors.rejectValue("assetDetails", "asset.voucherLink.account.accump.missing");
                            break;
                        }

                        Object valueObj = row.get("value");
                        if(valueObj == null) {
                            BigDecimal value = new BigDecimal(valueObj.toString());
                            if(value.equals(BigDecimal.ZERO)) {
                                errors.rejectValue("assetDetails", "asset.voucherLink.account.value.missing");
                            }
                            break;
                        }
                    }

                } else {
                    errors.rejectValue("assetDetails", "asset.voucherLink.account.missing");
                }
            }

            if(Checker.isAssetAdjustment(form.getLinkType().getId())) {
                if(Checker.isStringNullOrEmpty(form.getAdjustmentType())) {
                    errors.rejectValue("adjustmentType", "asset.voucherLink.adjustmentType.required");
                }
            }
        }
    }
}
