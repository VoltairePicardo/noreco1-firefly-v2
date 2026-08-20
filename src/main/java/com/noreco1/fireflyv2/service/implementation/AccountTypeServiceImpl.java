package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.model.AccountType;
import com.noreco1.fireflyv2.model.BusinessSegment;
import com.noreco1.fireflyv2.repo.AccountTypeRepo;
import com.noreco1.fireflyv2.repo.BusinessSegmentRepo;
import com.noreco1.fireflyv2.service.AccountTypeService;
import com.noreco1.fireflyv2.service.BusinessSegmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class AccountTypeServiceImpl implements AccountTypeService {
    @Autowired
    AccountTypeRepo accountTypeRepo;

    @Override
    public AccountType create(AccountType o) {
        return null;
    }

    @Override
    public AccountType delete(Integer id) {
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<AccountType> findAll() {
        return accountTypeRepo.findAll();
    }

    @Override
    public AccountType update(AccountType o) {
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public AccountType findById(Integer id) {
        return null;
    }
}
