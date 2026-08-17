package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.AccountType;
import com.noreco1.fireflyv2.model.BusinessSegment;

import java.util.List;

public interface AccountTypeService {
    public AccountType create(AccountType o);
    public AccountType delete(Integer id);
    public List<AccountType> findAll();
    public AccountType update(AccountType o);
    public AccountType findById(Integer id);
}
