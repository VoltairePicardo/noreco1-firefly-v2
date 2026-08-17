package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.MemorandumReceipt;
import com.noreco1.fireflyv2.service.implementation.MemorandumReceiptServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class MemorandumReceiptValidator implements Validator {

    @Autowired
    MemorandumReceiptServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return MemorandumReceipt.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        MemorandumReceipt memorandumReceipt = (MemorandumReceipt) o;

        if(Checker.collectionIsEmpty(memorandumReceipt.getMemorandumReceiptDetails())) {
            errors.rejectValue("memorandumReceiptDetails", "memorandumReceipt.details.required");
        }

    }

    public void setService(MemorandumReceiptServiceImpl service) {
        this.service = service;
    }
}
