package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.model.BankAccount;
import com.noreco1.fireflyv2.model.enums.BankTransactionType;
import com.noreco1.fireflyv2.repo.BankAccountRepo;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class BankAccountServiceImpl implements BankAccountService {

    @Autowired
    private BankAccountRepo bankAccountRepo;

    @Autowired
    private AuthenticationFacade authFacade;

    @Override
    public Page<BankAccount> list(String q, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return q.isBlank()
                ? bankAccountRepo.findAll(pageable)
                : bankAccountRepo.findAllByBankNameContainingIgnoreCaseOrAccountNumberContainingIgnoreCase(q, q, pageable);
    }

    @Override
    public BankAccount findById(Integer id) {
        return bankAccountRepo.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public PostResponse create(BankAccount bankAccount) {
        bankAccount.setId(null);
        PostResponse res = new PostResponse();
        try {
            bankAccount.setCreatedBy(authFacade.getLoggedIn());
            bankAccount.setCreatedAt(new Date());
            bankAccount.setUpdatedAt(new Date());
            BankAccount saved = bankAccountRepo.save(bankAccount);
            res.setModelId(saved.getId());
            res.setSuccessMessage("Bank account successfully saved!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    @Transactional
    public PostResponse update(BankAccount bankAccount) {
        PostResponse res = new PostResponse();
        try {
            BankAccount existing = bankAccountRepo.findById(bankAccount.getId()).orElse(null);
            if (existing == null) { res.setFailureMessage("Bank account not found."); return res; }
            existing.setBank(bankAccount.getBank());
            existing.setAccountNumber(bankAccount.getAccountNumber());
            existing.setDescription(bankAccount.getDescription());
            existing.setBankTransactionType(bankAccount.getBankTransactionType());
            existing.setAccount(bankAccount.getAccount());
            existing.setCreatedBy(authFacade.getLoggedIn());
            existing.setUpdatedAt(new Date());
            bankAccountRepo.save(existing);
            res.setModelId(existing.getId());
            res.setSuccessMessage("Bank account successfully updated!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    public PostResponse deleteById(Integer id) {
        PostResponse res = new PostResponse();
        try {
            bankAccountRepo.deleteById(id);
            res.setSuccessMessage("Bank account successfully deleted!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    public List<BankAccount> getAllBankAccountsWithCheckOnly() {
        return bankAccountRepo.getAllBankAccountsWithCheckOnly(BankTransactionType.CHECK.getId());
    }

    @Override
    public List<BankAccount> getAllBankAccounts() {
        return bankAccountRepo.findAll();
    }

    @Override
    public List<BankAccount> getAllBankAccountsForCv(Integer bankId) {
        return bankAccountRepo.findAllByBankId(bankId);
    }
}
