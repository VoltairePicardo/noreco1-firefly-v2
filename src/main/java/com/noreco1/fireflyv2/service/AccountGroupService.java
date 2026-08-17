package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.AccountGroup;
import com.noreco1.fireflyv2.model.AccountType;

import java.util.List;

public interface AccountGroupService {
    public AccountGroup create(AccountGroup o);
    public AccountGroup delete(Integer id);
    public List<AccountGroup> findAll();
    public AccountGroup update(AccountType o);
    public AccountGroup findById(Integer id);
}
