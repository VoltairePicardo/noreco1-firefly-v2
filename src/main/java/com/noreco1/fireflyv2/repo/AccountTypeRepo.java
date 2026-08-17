package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Account;
import com.noreco1.fireflyv2.model.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountTypeRepo extends JpaRepository<AccountType, Integer> {

}
