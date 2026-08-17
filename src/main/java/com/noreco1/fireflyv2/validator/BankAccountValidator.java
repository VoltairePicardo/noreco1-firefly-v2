package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.BankAccount;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class BankAccountValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return BankAccount.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        BankAccount bankAccount = (BankAccount) o;

        if(Checker.isStringNullOrEmpty(bankAccount.getAccountNumber())){
            errors.rejectValue("accountNumber", "bankAccount.accountNumber.required");
        }

        if(Checker.isStringNullOrEmpty(bankAccount.getDescription())){
            errors.rejectValue("description", "bankAccount.description.required");
        }

    }
}
