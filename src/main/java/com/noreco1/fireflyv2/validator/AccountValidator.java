package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.Account;
import com.noreco1.fireflyv2.model.AllocationFactor;
import com.noreco1.fireflyv2.model.FactorPercentageDistro;
import com.noreco1.fireflyv2.model.SegmentAccount;
import com.noreco1.fireflyv2.repo.AccountRepo;
import com.noreco1.fireflyv2.controller.response.AccountDto;
import com.noreco1.fireflyv2.service.implementation.AccountServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import javax.swing.*;
import java.util.List;
import java.util.Set;

@Component
public class AccountValidator implements Validator {

    private AccountServiceImpl accountService;

    @Override
    public boolean supports(Class<?> aClass) {
        return Account.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Account account = (Account) o;

        if (account.getAccountGroup() == null || account.getAccountGroup().getId() <= 0) {
            errors.rejectValue("accountGroup", null, "Must select account group");
        }

        if (account.getAccountType() == null || account.getAccountType().getId() <= 0) {
            errors.rejectValue("accountType", null, "Must select account type");
        }

        if (account.getParentAccountId() == null || account.getParentAccountId() < 0) {
            errors.rejectValue("parentAccountId", null, "This is empty");
        }

        List<Account> accountList = accountService.findByTitle(account.getTitle());

        if (accountList != null && accountList.size() > 0) {
            Account a = accountList.get(0);
            if (account.getId() > 0) { // is edit mode
                if (a.getId() != account.getId()) { // compare itself
                    // other accounts hold similar value and edit mode
                    FieldError titleError = errors.getFieldError("title");
                    if (titleError != null) {
                        errors.rejectValue("title", "account.name.duplicate");
                    } else {
                        errors.rejectValue("title", "account.name.duplicate");
                    }
                }
            } else {
                FieldError titleError = errors.getFieldError("title");
                if (titleError != null) {
                    errors.rejectValue("title", "account.name.duplicate");
                }
            }
        }

        // check if allocation is change
        if(account.getAllocationFactor() != null) {

            Account accountEx = this.accountService.find(account.getId());
            if(accountEx != null) {

                AllocationFactor allocationFactor = this.accountService.findAllocationFactor(accountEx.getId());

                // factor changed, check if segment accounts used in General Ledger
                if(allocationFactor != null && allocationFactor.getFactor() != null) {

                    Integer id1 = allocationFactor.getFactor().getId();
                    Integer id2 = account.getAllocationFactor().getId();

                    if(id1.equals(id2)) return;

                    Set<FactorPercentageDistro> distros = this.accountService.getPercentageDistroRepo().findByFactorId(allocationFactor.getFactor().getId());
                    if(!distros.isEmpty()) {
                        for (FactorPercentageDistro d: distros) {

                            SegmentAccount segmentAccount = this.accountService.findByBusinessSegmentIdAndAccountId(d.getBusinessSegment().getId(), accountEx.getId());

                            if(segmentAccount != null && this.accountService.segmentAccountIdHasGLUsage(segmentAccount.getId())) {
                                errors.rejectValue("allocationFactor", null, "Changing of factor allocation is not allowed. Business segment already used in ledger entries");
                                break;
                            }
                        }
                    }

                }
            }
        }
    }

    public void setService(AccountServiceImpl accountService) {
        this.accountService = accountService;
    }
}
