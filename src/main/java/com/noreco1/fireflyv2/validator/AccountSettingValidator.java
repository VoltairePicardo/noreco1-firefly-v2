package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.AccountSetting;
import com.noreco1.fireflyv2.controller.response.AccountSettingDetailDto;
import com.noreco1.fireflyv2.service.implementation.AccountSettingServiceImpl;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class AccountSettingValidator implements Validator {

    private AccountSettingServiceImpl settingService;

    @Override
    public boolean supports(Class<?> clazz) {
       return AccountSetting.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        AccountSetting accountSetting = (AccountSetting) target;

        if(accountSetting.getAccountSettingDetails().isEmpty()){
            errors.rejectValue("accountSettingDetails", null, "Selected document has no items!");
        } else {

            for (AccountSettingDetailDto accountSettingDetailDto : accountSetting.getAccountSettingDetails()){

                if (accountSettingDetailDto.getDebitAccount() == null && accountSettingDetailDto.getCreditAccount() == null) {
                    errors.rejectValue("accountSettingDetails", null, "Please assign DR or CR account for " + accountSettingDetailDto.getItemDescription());
                }

            }

        }

    }

    public void setSettingService(AccountSettingServiceImpl settingService) {
        this.settingService = settingService;
    }

}
