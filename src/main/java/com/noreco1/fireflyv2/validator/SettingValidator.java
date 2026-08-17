package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.Setting;
import com.noreco1.fireflyv2.service.SettingService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Map;

@Component
public class SettingValidator implements Validator {

    private SettingService settingService;

    @Override
    public boolean supports(Class<?> aClass) {
        return Setting.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Setting setting = (Setting) o;

        Setting exSetting = settingService.findById(setting.getId());

        Integer settingId = null;
        if (exSetting != null)  settingId = exSetting.getId();

        if ((setting.getId() == null || setting.getId().equals(0)) && settingId != null) { // insert mode
            errors.rejectValue("code", "setting.code.taken");
        } else if (settingId != null && !settingId.equals(setting.getId())) {
            errors.rejectValue("code", "setting.code.taken");
        }
    }

    public void setService(SettingService service) { this.settingService = service; }
}
