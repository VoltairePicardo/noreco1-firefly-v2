package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.PettyCashTrans;
import com.noreco1.fireflyv2.service.implementation.PettyCashFundServiceImpl;
import com.noreco1.fireflyv2.service.implementation.PettyCashTransServiceImpl;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;

public class PettyCashTransValidator implements Validator {

    private PettyCashTransServiceImpl service;
    private PettyCashFundServiceImpl pcfService;

    @Override
    public boolean supports(Class<?> aClass) {
        return PettyCashTrans.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        PettyCashTrans pettyCashTrans = (PettyCashTrans) o;
        BigDecimal balance = this.service.findPcf().getBalance();

        if(pettyCashTrans.getId() != null) {
            BigDecimal oldPcvAmount = (BigDecimal)this.service.findById(pettyCashTrans.getId()).get("amount");
            if (pettyCashTrans.getAmount().compareTo(balance.add(oldPcvAmount)) > 0) {
                errors.rejectValue("amount", null, "Total amount is greater than available balance.");
            }
        } else {
            if (pettyCashTrans.getAmount().compareTo(balance) > 0) {
                errors.rejectValue("amount", null, "Total amount is greater than available balance.");
            }
        }

        BigDecimal limit = new BigDecimal("1000");
        if (pettyCashTrans.getAmount().compareTo(limit) > 0) {
            errors.rejectValue("amount", null, "Total amount should not be higher than 1,000.00");
        }
    }

    public void setService(PettyCashTransServiceImpl service) {
        this.service = service;
    }
}
