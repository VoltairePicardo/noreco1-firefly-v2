package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.MemorandumReceipt;
import com.noreco1.fireflyv2.service.implementation.MemorandumReceiptServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

public class MultipleMemorandumReceiptValidator implements Validator {

    @Autowired
    MemorandumReceiptServiceImpl service;

    @Override
    public boolean supports(Class<?> clazz) {
        return MemorandumReceipt.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        List<MemorandumReceipt> memorandumReceipts = (List<MemorandumReceipt>) target;
    }

    public void setService(MemorandumReceiptServiceImpl service) {
        this.service = service;
    }
}
