package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CheckConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckConfigRepo extends JpaRepository<CheckConfig, Integer> {
    CheckConfig findOneByCode(String codes);
    CheckConfig findOneByAccountId(Integer accountId);
    CheckConfig findOneByBankAccountId(Integer bankAccountId);
}
