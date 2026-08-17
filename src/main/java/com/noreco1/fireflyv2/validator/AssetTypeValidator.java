package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.AssetType;
import com.noreco1.fireflyv2.service.implementation.AssetTypeServiceImpl;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class AssetTypeValidator implements Validator {

    private AssetTypeServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return AssetType.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        AssetType assetType = (AssetType) o;

        AssetType i = this.service.findByDescription(assetType.getDescription());
        // insert mode
        if (i != null && !Checker.isValidId(assetType.getId())) { // insert mode & same description found
            errors.rejectValue("description", "assetType.description.taken");
        } else {
            // update mode
            if (i != null && !i.getId().equals(assetType.getId())) { // diff item
                errors.rejectValue("description", "assetType.description.taken");
            }
        }
    }

    public void setService(AssetTypeServiceImpl service) {
        this.service = service;
    }
}
