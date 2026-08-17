package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.Asset;

import com.noreco1.fireflyv2.service.implementation.AssetServiceImpl;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class AssetValidator implements Validator {

    private AssetServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return Asset.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Asset asset = (Asset) o;
    }

    public void setService(AssetServiceImpl service) {
        this.service = service;
    }
}
