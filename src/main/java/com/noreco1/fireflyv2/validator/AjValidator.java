package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.facade.LedgerFacade;
import com.noreco1.fireflyv2.model.AdjustmentJournal;
import com.noreco1.fireflyv2.repo.AllocationFactorRepo;
import com.noreco1.fireflyv2.repo.MonthlyCycleRepo;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class AjValidator implements Validator {

    MonthlyCycleRepo monthlyCycleRepo;
    AllocationFactorRepo allocationFactorRepo;
    LedgerFacade legderFacade;

    @Override
    public boolean supports(Class<?> aClass) {
        return AdjustmentJournal.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        AdjustmentJournal aj = (AdjustmentJournal) o;

        CommonValidator.validateBackdating(errors, monthlyCycleRepo, aj.getVoucherDate());
        CommonValidator.validateJournal(errors, aj.getGeneralLedgerLines(), aj.getSubLedgerLines(), this.allocationFactorRepo);
    }

    // this how we add dependency, @Autowired is not working here!
    public void setmonthlyCycleRepo(MonthlyCycleRepo monthlyCycleRepo) {
        this.monthlyCycleRepo = monthlyCycleRepo;
    }
    // this how we add dependency, @Autowired is not working here!
    public void setAllocationFactorRepo(AllocationFactorRepo allocationFactorRepo) {
        this.allocationFactorRepo = allocationFactorRepo;
    }
    public void setLegderFacade(LedgerFacade legderFacade) {
        this.legderFacade = legderFacade;
    }
}
