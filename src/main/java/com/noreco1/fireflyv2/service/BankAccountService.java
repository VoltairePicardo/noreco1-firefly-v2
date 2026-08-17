package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.BankAccount;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BankAccountService {
    Page<BankAccount> list(String q, int page, int size);
    BankAccount findById(Integer id);
    PostResponse create(BankAccount bankAccount);
    PostResponse update(BankAccount bankAccount);
    PostResponse deleteById(Integer id);
    List<BankAccount> getAllBankAccountsWithCheckOnly();
    List<BankAccount> getAllBankAccounts();
    List<BankAccount> getAllBankAccountsForCv(Integer bankId);
}
