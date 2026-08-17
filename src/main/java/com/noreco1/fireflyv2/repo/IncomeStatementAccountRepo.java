package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.IncomeStatementAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncomeStatementAccountRepo extends JpaRepository<IncomeStatementAccount, Integer> {
    List<IncomeStatementAccount> findAllByIncomeStatementSettingId(Integer issId);
}
