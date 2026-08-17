package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.facade.LedgerFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.MaterialIssueRegister;
import com.noreco1.fireflyv2.repo.AllocationFactorRepo;
import com.noreco1.fireflyv2.repo.MonthlyCycleRepo;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class MaterialIssueRegisterValidator implements Validator {

    MonthlyCycleRepo monthlyCycleRepo;
    AllocationFactorRepo allocationFactorRepo;
    LedgerFacade legderFacade;

    @Override
    public boolean supports(Class<?> aClass) {
        return MaterialIssueRegister.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        MaterialIssueRegister mir = (MaterialIssueRegister) o;

        CommonValidator.validateBackdating(errors, monthlyCycleRepo, mir.getVoucherDate());

        if(!Checker.collectionIsEmpty(mir.getGeneralLedgerLines())) {
            CommonValidator.validateJournal(errors, mir.getGeneralLedgerLines(), mir.getSubLedgerLines(), this.allocationFactorRepo);
        }
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
