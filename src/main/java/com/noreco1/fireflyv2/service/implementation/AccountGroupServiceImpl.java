package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.model.AccountGroup;
import com.noreco1.fireflyv2.model.AccountType;
import com.noreco1.fireflyv2.repo.AccountGroupRepo;
import com.noreco1.fireflyv2.repo.AccountTypeRepo;
import com.noreco1.fireflyv2.service.AccountGroupService;
import com.noreco1.fireflyv2.service.AccountTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class AccountGroupServiceImpl implements AccountGroupService {
    @Autowired
    AccountGroupRepo accountGroupRepo;

    @Override
    public AccountGroup create(AccountGroup o) {
        return null;
    }

    @Override
    public AccountGroup delete(Integer id) {
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<AccountGroup> findAll() {
        return accountGroupRepo.findAll();
    }

    @Override
    public AccountGroup update(AccountType o) {
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public AccountGroup findById(Integer id) {
        return null;
    }
}
