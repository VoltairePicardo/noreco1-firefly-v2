package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.ReturnMemorandumReceipt;
import com.noreco1.fireflyv2.service.implementation.ReturnMemorandumReceiptServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ReturnMemorandumReceiptValidator implements Validator {

    @Autowired
    ReturnMemorandumReceiptServiceImpl service;

    @Override
    public boolean supports(Class<?> clazz) {
        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {

        ReturnMemorandumReceipt returnMemorandumReceipt = (ReturnMemorandumReceipt) target;

    }

    public void setService(ReturnMemorandumReceiptServiceImpl service) {
        this.service = service;
    }
}
