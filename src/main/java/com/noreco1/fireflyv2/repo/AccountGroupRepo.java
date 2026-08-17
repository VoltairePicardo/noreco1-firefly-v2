package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.AccountGroup;
import com.noreco1.fireflyv2.model.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountGroupRepo extends JpaRepository<AccountGroup, Integer> {

}
