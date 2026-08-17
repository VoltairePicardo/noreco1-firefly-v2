package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.facade.LedgerFacade;
import com.noreco1.fireflyv2.model.OtherAccountReceivable;
import com.noreco1.fireflyv2.repo.AllocationFactorRepo;
import com.noreco1.fireflyv2.repo.MonthlyCycleRepo;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class OtherAccountReceivableValidator implements Validator {

    MonthlyCycleRepo monthlyCycleRepo;
    AllocationFactorRepo allocationFactorRepo;
    LedgerFacade legderFacade;

    @Override
    public boolean supports(Class<?> aClass) {
        return OtherAccountReceivable.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        OtherAccountReceivable oar = (OtherAccountReceivable) o;

 /*       CommonValidator.validateBackdating(errors, monthlyCycleRepo, oar.getVoucherDate());
        CommonValidator.validateJournal(errors, oar.getGeneralLedgerLines(), oar.getSubLedgerLines(),
                this.allocationFactorRepo, this.legderFacade, oar.getVoucherDate());
*/
        CommonValidator.validateBackdating(errors, monthlyCycleRepo, oar.getVoucherDate());
        CommonValidator.validateJournal(errors, oar.getGeneralLedgerLines(), oar.getSubLedgerLines(), this.allocationFactorRepo);

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
