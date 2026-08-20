package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.FactorFacade;
import com.noreco1.fireflyv2.common.facade.SettingFacade;
import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.model.enums.AccountClassification;
import com.noreco1.fireflyv2.model.enums.SettingCode;
import com.noreco1.fireflyv2.controller.response.PostAccountResponse;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.common.helpers.StringFormatter;
import com.noreco1.fireflyv2.controller.response.AccountDto;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.SegmentAccountDto;
import com.noreco1.fireflyv2.service.AccountService;
import com.noreco1.fireflyv2.validator.AccountValidator;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class AccountServiceImpl implements AccountService {
    private List<AccountDto> accountsDtoList;
    private List<Map> accountsMapList;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private SettingFacade settingFacade;

    @Autowired
    private AccountTypeRepo accountTypeRepo;

    @Autowired
    private AccountGroupRepo accountGroupRepo;

    @Autowired
    private SegmentAccountRepo segmentAccountRepo;

    @Autowired
    private BusinessSegmentRepo businessSegmentRepo;

    @Autowired
    private DateRangeRepo dateRangeRepo;

    @Autowired
    private AllocationFactorRepo allocationFactorRepo;

    @Autowired
    private FactorPercentageDistroRepo percentageDistroRepo;

    @Autowired
    private GeneralLedgerRepo generalLedgerRepo;

    @Autowired
    private FactorFacade factorFacade;

    @Override
    public Account create(Account account) {

        account.setId(null);
        Account savedAccount = accountRepo.save(account);
        if(savedAccount != null)  this.saveAllocationFactor(account);

        return savedAccount;
    }

    @Override
    public Account delete(int id) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "accountsCache")
    public List<AccountDto> findAll() {
        this.accountsDtoList = new ArrayList<>();

        List<Account> accountList = accountRepo.findAllByOrderByCodeAsc();

        for(Account account : accountList) {
            AccountDto accountDto = new AccountDto();
            accountDto.setCode(account.getCode());
            accountDto.setParentAccountId(account.getParentAccountId());
            accountDto.setId(account.getId());
            accountDto.setTitle(account.getTitle());
            accountDto.setLevel(account.getLevel());
            accountDto.setClassification(account.getClassification());
            accountDto.setNormalBalance(account.getNormalBalance());
            accountDto.isActive(account.getActive());
            if (account.getAccountType() != null) {
                accountDto.setAccountType(account.getAccountType());
            }
            accountsDtoList.add(accountDto);
        }
        return accountsDtoList;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<AccountDto> findAll(String classification) {
        List<AccountDto> list = new ArrayList<>();

        List<Account> accountList = accountRepo.findAllByClassificationOrderByCodeAsc(classification);

        for(Account account : accountList) {
            AccountDto accountDto = new AccountDto();
            accountDto.setCode(account.getCode());
            accountDto.setParentAccountId(account.getParentAccountId());
            accountDto.setId(account.getId());
            accountDto.setTitle(account.getTitle());
            accountDto.setLevel(account.getLevel());
            accountDto.setClassification(account.getClassification());
            if (account.getAccountType() != null) {
                accountDto.setAccountType(account.getAccountType());
            }

            list.add(accountDto);
        }
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "accountsTreeCache")
    public List<AccountDto> findAllTree() {

        this.accountsDtoList = new ArrayList<>();

        List<Account> topLevelAccounts = accountRepo.findByParentAccountIdOrderByCodeAsc(0);// top level accounts
        for(Account account : topLevelAccounts) {
            AccountDto accountDto = new AccountDto();
            accountDto.setCode(account.getCode());
            accountDto.setId(account.getId());
            accountDto.setTitle(account.getTitle());
            if (account.getAccountType() != null) {
                accountDto.setAccountType(account.getAccountType());
            }
            this.accountsDtoList.add(accountDto);
            findDescendants(account);
        }
        return this.accountsDtoList;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<SegmentAccountDto> findAllBySegment(String[] segmentIds) {
        List<SegmentAccountDto> list = new ArrayList<>();
        if (segmentIds != null && segmentIds.length > 0) {
            List<Object[]> result = accountRepo.findBySegmentIds(Arrays.asList(segmentIds));;
            if (result != null) {
                for(Object[] objects : result) {
                    SegmentAccountDto a = new SegmentAccountDto();
                    a.setAccountId((Integer) objects[0]);
                    a.setTitle((String) objects[1]);
                    a.setSegmentAccountId((Integer) objects[2]);
                    a.setSegmentAccountCode((String) objects[3]);

                    AccountType at = new AccountType();
                    at.setId((Integer) objects[4]);
                    at.setDescription((String) objects[5]);
                    a.setAccountType(at);

                    list.add(a);
                }
            }
        }
        return list;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<AccountDto> findAllWithSegment() {
        List<AccountDto> list = new ArrayList<>();

        List<Object[]> result = accountRepo.findAllWithSegment();
        if (result != null) {
            for(Object[] objects : result) {
                list.add(this.composeAccountDto(objects));
            }
        }
        return list;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<AccountDto> findAllWithSegmentAndAllocationFactor() {
        List<AccountDto> list = new ArrayList<>();

        List<Object[]> result = accountRepo.findAllWithSegmentAndAllocationFactor();
        if (result != null) {
            for(Object[] objects : result) {
                list.add(this.composeAccountDto(objects));
            }
        }
        return list;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<AccountDto> findAllWithSegmentAndAllocationFactor(String classification) {
        List<AccountDto> list = new ArrayList<>();

        List<Object[]> result = accountRepo.findAllWithSegmentAndAllocationFactor(classification);
        if (result != null) {
            for(Object[] objects : result) {
                list.add(this.composeAccountDto(objects));
            }
        }
        return list;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<AccountDto> findAllWithSegmentAndAllocationFactorAndDate(String classification, String date) {

        if(date == null || date.equals("undefined")) {
            date = DateHelper.dateToSQL(new Date());
            // return this.findAllWithSegmentAndAllocationFactor(classification);
        }

        List<AccountDto> list = new ArrayList<>();

        List<Object[]> result = accountRepo.findAllWithSegmentAndAllocationFactor(classification, date);
        if (result != null) {
            for(Object[] objects : result) {
                list.add(this.composeAccountDto(objects));
            }
        }
        return list;
    }

    @Override
    public Account update(Account account) {

        Account savedAccount = accountRepo.save(account);
        if(savedAccount != null) {
            allocationFactorRepo.deleteByAccountId(account.getId());
            this.saveAllocationFactor(account);
        }

        return savedAccount;
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto findById(int id) {
        AccountDto accountDto = new AccountDto();

        Account account = accountRepo.findById(id).orElse(null);
        if (account != null) {
            accountDto.setId(account.getId());
            accountDto.setTitle(account.getTitle());
            accountDto.setCode(account.getCode());
            if (account.getAccountType() != null) {
                accountDto.setAccountType(account.getAccountType());
            }
            accountDto.setLevel(account.getLevel());
            accountDto.setAuxAccount(account.getAuxiliaryAccount());
            accountDto.setGLAccount(account.getGLAccount());
            if (account.getAccountGroup() != null) {
                accountDto.setAccountGroup(account.getAccountGroup());
            }
            accountDto.hasSL(account.getHasSL() == 1);
            accountDto.isActive(account.getActive());

            if (account.getParentAccountId() != null) {
                Account parentAccount = accountRepo.findById(account.getParentAccountId()).orElse(null);
                if (parentAccount != null) {
                    Account pa = new Account();
                    pa.setId(parentAccount.getId());
                    pa.setTitle(parentAccount.getTitle());
                    pa.setCode(parentAccount.getCode());

                    accountDto.setParentAccount(pa);
                }
            }

            /*List<SegmentAccount> segmentAccounts = segmentAccountRepo.findByAccountId(account.getId());
            accountDto.setSegmentAccounts(segmentAccounts);*/

            accountDto.setSLAccount(account.getSLAccount());
            accountDto.setNormalBalance(account.getNormalBalance());
            accountDto.setIsHeader(account.getIsHeader());
            accountDto.setClassification(account.getClassification());

            Account bsupAccount = account.getBsupAccount();
            if (bsupAccount != null) {
                Account bsupA = new Account();
                bsupA.setId(bsupAccount.getId());
                bsupA.setTitle(bsupAccount.getTitle());
                bsupA.setCode(bsupAccount.getCode());

                accountDto.setBsupAccount(bsupA);
            }

            AllocationFactor allocationFactor = allocationFactorRepo.findOneByAccountId(account.getId());
            if(allocationFactor != null) {

                Factor factor = allocationFactor.getFactor();
                if(factor != null) {

                    factor.setFactorPercentageDistroSetByValidity(factorFacade.getPercentageDistrosByValidity(factor.getId()));
                    accountDto.setFactor(factor);
                }
            }
        }
        return accountDto;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Map findWTax() {
        return settingFacade.getByCode(SettingCode.WTAX_ACCOUNT.toString());
    }

    @CacheEvict(value =  {"accountsTreeCache", "accountsCache"}, allEntries = true)
    public PostResponse processCreate(Account account, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostAccountResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        try {

            AccountValidator accountValidator = new AccountValidator();
            accountValidator.setService(this);
            accountValidator.validate(account, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
                response.setSuccess(false);
            } else {

                AccountType accountType = accountTypeRepo.findById(account.getAccountType().getId()).orElse(null);
                AccountGroup accountGroup = accountGroupRepo.findById(account.getAccountGroup().getId()).orElse(null);

                account.setTitle(StringFormatter.ucFirst(account.getTitle()));
                if (account.getClassification().equals(AccountClassification.BSUP.toString()))
                    account.setCode(StringFormatter
                            .buildAccountCode(
                                    accountType.getCode(),
                                    accountGroup.getCode(),
                                    account.getGLAccount(),
                                    account.getSLAccount(),
                                    account.getAuxiliaryAccount()
                            )
                    );
                else
                    account.setCode(StringFormatter
                            .buildAccountCode(
                                    account.getGLAccount(),
                                    account.getSLAccount(),
                                    account.getAuxiliaryAccount()
                            )
                    );

                if (account.getParentAccountId() != null) {
                    Account parentAccount = accountRepo.findById(account.getParentAccountId()).orElse(null);
                    if (parentAccount != null) {
                        account.setLevel(parentAccount.getLevel() + 1);
                    }
                } else {
                    account.setParentAccountId(0);
                }

                if (account.getId() != null && account.getId() > 0) {  // update mode

                    boolean factorChanged = true;

                    Integer newFactorId = account.getAllocationFactor().getId();
                    AllocationFactor allocationFactor = allocationFactorRepo.findOneByAccountId(account.getId());

                    if(allocationFactor != null && allocationFactor.getFactor() != null) {

                        Integer exFactorId = allocationFactor.getFactor().getId();
                        factorChanged = !exFactorId.equals(newFactorId);
                    }

                    update(account);

                    persistSegmentAccounts(account, account.getAllocationFactor().getId(), factorChanged);

                    /*if(!Checker.collectionIsEmpty(account.getSegmentAccounts())) {
                        List<SegmentAccount> segmentAccountList = segmentAccountRepo.findByAccountId(account.getId());
                        if (Checker.collectionIsEmpty(segmentAccountList)) {
                            // fresh insert
                            persistSegmentAccounts(account);
                        } else { // add up
                            Set<SegmentAccount> segmentAccounts = account.getSegmentAccounts();
                            for(SegmentAccount segmentAccount : segmentAccounts) {
                                BusinessSegment businessSegment = businessSegmentRepo.findById(segmentAccount.getBusinessSegment().getId()).orElse(null);

                                String code = this.generateSegmentAccountCode(businessSegment, account);
                                segmentAccountRepo.saveWithExistenceCheck(businessSegment.getId(), account.getId(), code);

                            }
                        }
                    }*/
                } else {
                    Factor allocationFactor = account.getAllocationFactor();
                    account = create(account);
                    persistSegmentAccounts(account, allocationFactor.getId(), true);
                }

                response.setModelId(account.getId());
                response.setSuccessMessage("Account successfully saved!");
                response.setSuccess(true);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> findByTitle(String title) {
        return accountRepo.findByTitle(title);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Account> findByIdNotIn(Integer... accountId) {
        return accountRepo.findByIdNotInOrderByTitleAsc(accountId);
    }

    @CacheEvict(value =  {"accountsTreeCache", "accountsCache"}, allEntries = true)
    public PostResponse processUpdate(Account account, BindingResult bindingResult, MessageSource messageSource) {
        return processCreate(account, bindingResult, messageSource);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<AccountDto> findAllWithSegmentAndHasSL() {
        List<AccountDto> list = new ArrayList<>();

        List<Object[]> result = accountRepo.findAllWithSegmentAndHasSL();
        if (result != null) {
            for(Object[] objects : result) {
                list.add(this.composeAccountDto(objects));
            }
        }
        return list;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public SegmentAccountDto findSegmentAccountById(Integer segmentId) {
        SegmentAccountDto segmentAccountDto = new SegmentAccountDto();
        List<Object[]> result = accountRepo.findBySegmentAccountId(segmentId);
        if (result != null) {
            for(Object[] objects : result) {
                segmentAccountDto.setAccountId((Integer) objects[0]);
                segmentAccountDto.setTitle((String) objects[1]);
                segmentAccountDto.setSegmentAccountId((Integer) objects[2]);
                segmentAccountDto.setSegmentAccountCode((String) objects[3]);

                AccountType at = new AccountType();
                at.setId((Integer) objects[4]);
                at.setDescription((String) objects[5]);
                segmentAccountDto.setAccountType(at);
            }
        }
        return segmentAccountDto;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Map> findAllTreeMap() {

        this.accountsMapList = new ArrayList<>();
        try {

            List<Account> topLevelAccounts = accountRepo.findByParentAccountIdOrderByClassificationAscCodeAsc(0);// top level accounts

            if (!topLevelAccounts.isEmpty()) {

                for(Account account : topLevelAccounts) {

                    this.accountsMapList.add(this.composeAccountMap(account));
                    findDescendantsMap(account);
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        return this.accountsMapList;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Map> findAllTreeMapByCode(String query) {

        this.accountsMapList = new ArrayList<>();
        try {

            List<Account> accounts = accountRepo.findAllByCodeContainsOrTitleContainsOrderByClassificationAscCodeAsc(query, query);

            if (!accounts.isEmpty()) {

                for(Account account : accounts) {

                    this.accountsMapList.add(this.composeAccountMap(account));

                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        return this.accountsMapList;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Map findVat() {
        return settingFacade.getByCode(SettingCode.VAT_ACCOUNT.toString());
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Map findSupplierAccount() {
        return settingFacade.getByCode(SettingCode.SUPPLIER_ACCOUNT.toString());
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public SegmentAccount findByBusinessSegmentIdAndAccountId(int bsegmentId, int accountId) {
        return segmentAccountRepo.findOneByAccountIdAndBusinessSegmentId(accountId, bsegmentId);
    }

    @Override
    public boolean segmentAccountIdHasGLUsage(int segmentAccountId) {
        return !this.generalLedgerRepo.findBySegmentAccountId(segmentAccountId).isEmpty();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Account find(int accountId) {
        return accountRepo.findById(accountId).orElse(null);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public AllocationFactor findAllocationFactor(int accountId) {
        return this.allocationFactorRepo.findOneByAccountId(accountId);
    }

    private AccountDto composeAccountDto(Object[] objects) {
        AccountDto a = new AccountDto();
        a.setId((Integer) objects[0]);
        a.setTitle((String) objects[1]);
        a.setCode((String) objects[2]);

        AccountType at = new AccountType();
        at.setId((Integer) objects[3]);
        at.setDescription((String) objects[4]);
        a.setAccountType(at);

        a.hasSL((Boolean) objects[5]);

        if(objects.length > 6){
            a.setManualAllocation(objects[6] == null || ((Integer) objects[6]) == 33); // Manual Allocation Factor
        }

        a.setSearchText(StringFormatter.accountSearchText(a));
        a.setSearchTextDisplay(StringFormatter.accountSearchText(a) + " " + a.getCode().replace("-", ""));

        return a;
    }

    private String generateSegmentAccountCode(BusinessSegment businessSegment, Account account) {
        if (account.getClassification().equals(AccountClassification.NEA.toString())) {
            return account.getCode();
        }
        return businessSegment.getBusinessActivity().getCode() + businessSegment.getCode() + "-" + account.getCode();
    }

    private void persistSegmentAccounts(Account account) {
        if (!Checker.collectionIsEmpty(account.getSegmentAccounts())) {
            Set<SegmentAccount> segmentAccounts = account.getSegmentAccounts();

            for(SegmentAccount segmentAccount : segmentAccounts) {
                BusinessSegment businessSegment = businessSegmentRepo.findById(segmentAccount.getBusinessSegment().getId()).orElse(null);

                String code = this.generateSegmentAccountCode(businessSegment, account);
                segmentAccount.setAccountCode(code);

                segmentAccount.setAccount(account);
                segmentAccountRepo.save(segmentAccount);
            }
        }
    }

    private void persistSegmentAccounts(Account account, Integer factorId, boolean changed) {

        try {

            if(changed) {

//                List<SegmentAccount> segmentAccounts = segmentAccountRepo.findByAccountId(account.getId());
//                segmentAccountRepo.deleteInBatch(segmentAccounts);

                Set<FactorPercentageDistro> distros = percentageDistroRepo.findByFactorId(factorId);

                if(!distros.isEmpty()) {

                    for(FactorPercentageDistro d : distros) {

                        // check dupes, dont insert if has dupe
                        SegmentAccount segmentAccount = segmentAccountRepo.findOneByAccountIdAndBusinessSegmentId(account.getId(), d.getBusinessSegment().getId());

                        if(segmentAccount == null) {

                            segmentAccount = new SegmentAccount();

                            segmentAccount.setAccountCode(account.getCode());
                            segmentAccount.setAccount(account);
                            segmentAccount.setBusinessSegment(d.getBusinessSegment());

                            segmentAccountRepo.save(segmentAccount);

                        }

                    }

                }

            }

        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void findDescendants(Account currentAccount) {
        List<Account> children = accountRepo.findByParentAccountIdOrderByCodeAsc(currentAccount.getId());

        for (Account childAccount : children) {
            AccountDto accountDto = new AccountDto();

            accountDto.setCode(childAccount.getCode());
            accountDto.setId(childAccount.getId());
            accountDto.setTitle(childAccount.getTitle());
            accountDto.setParentAccountId(childAccount.getParentAccountId());

            if (childAccount.getAccountType() != null) {
                accountDto.setAccountType(childAccount.getAccountType());
            }

            this.accountsDtoList.add(accountDto);
            findDescendants(childAccount);
        }
    }

    private void findDescendantsMap(Account currentAccount) {

        List<Account> children = accountRepo.findByParentAccountIdOrderByClassificationAscCodeAsc(currentAccount.getId());

        for (Account account : children) {

            this.accountsMapList.add(this.composeAccountMap(account));
            this.findDescendantsMap(account);
        }
    }

    private  Map composeAccountMap(Account account) {
        Map accountMap = new HashMap();

        accountMap.put("id", account.getId());
        accountMap.put("code", account.getCode());
        accountMap.put("title", account.getTitle());
        accountMap.put("class", account.getClassification());
        accountMap.put("isHeader", account.getIsHeader());

        accountMap.put("$$treeLevel", account.getLevel());
        if (account.getAccountType() != null)  accountMap.put("type", account.getAccountType().getDescription());

        return accountMap;
    }

    private void saveAllocationFactor(Account account) {

        if(account.getAllocationFactor() != null) {

            List<DateRange> ranges = dateRangeRepo.findByOrderByEndDesc();
            DateRange effectivity = ranges.get(0);

            AllocationFactor allocationFactor = new AllocationFactor();
            allocationFactor.setFactor(account.getAllocationFactor());
            allocationFactor.setEffectivityDate(effectivity);
            allocationFactor.setAccount(account);

            allocationFactorRepo.save(allocationFactor);
        }
    }

    public FactorPercentageDistroRepo getPercentageDistroRepo() {
        return this.percentageDistroRepo;
    }
}
