package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.dtoers.LedgerDtoerImpl;
import com.noreco1.fireflyv2.model.AccountsPayableVoucher;
import com.noreco1.fireflyv2.model.MonthlyCycle;
import com.noreco1.fireflyv2.repo.MonthlyCycleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class MonthlyCycleValidator implements Validator {

    MonthlyCycleRepo monthlyCycleRepo;

    @Override
    public boolean supports(Class<?> aClass) {
        return MonthlyCycle.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        MonthlyCycle mc = (MonthlyCycle) o;
        MonthlyCycle cycle = monthlyCycleRepo.findByYearAndMonth(mc.getYear(), mc.getMonth());

        if (cycle != null) {
            if (mc.getId() == null || !mc.getId().equals(cycle.getId())) { // compare with other cycle
                errors.rejectValue("year", "monthly.cycle.taken");
            }
        }
    }

    public void setMonthlyCycleRepo(MonthlyCycleRepo repo) {
        this.monthlyCycleRepo = repo;
    }
}
