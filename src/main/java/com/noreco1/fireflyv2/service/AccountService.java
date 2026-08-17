package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.AllocationFactor;
import com.noreco1.fireflyv2.model.SegmentAccount;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.AccountDto;
import com.noreco1.fireflyv2.model.Account;
import com.noreco1.fireflyv2.controller.response.SegmentAccountDto;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

public interface AccountService {
    Account create(Account account);
    Account delete(int id);
    List<AccountDto> findAll();
    List<AccountDto> findAll(String classification);
    Account update(Account account);
    AccountDto findById(int id);
    Map findWTax();
    @Transactional
    PostResponse processCreate(Account account, BindingResult bindingResult, MessageSource messageSource);
    @Transactional
    PostResponse processUpdate(Account account, BindingResult bindingResult, MessageSource messageSource);
    List<Account> findByTitle(String title);
    List<Account> findByIdNotIn(Integer... accountId);
    List<AccountDto> findAllTree();
    List<SegmentAccountDto> findAllBySegment(String[] segmentIds);
    List<AccountDto> findAllWithSegment();
    List<AccountDto> findAllWithSegmentAndHasSL();
    List<AccountDto> findAllWithSegmentAndAllocationFactor();
    List<AccountDto> findAllWithSegmentAndAllocationFactor(String classification);
    List<AccountDto> findAllWithSegmentAndAllocationFactorAndDate(String classification, String date);
    SegmentAccountDto findSegmentAccountById(Integer segmentId);

    List<Map> findAllTreeMap();

    List<Map> findAllTreeMapByCode(String query);

    Map findVat();

    Account find(int accountId);
    AllocationFactor findAllocationFactor(int accountId);
    SegmentAccount findByBusinessSegmentIdAndAccountId(int bsegmentId, int accountId);
    boolean segmentAccountIdHasGLUsage(int segmentAccountId);

    Map findSupplierAccount();
}
