package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.IncomeStatementCashFlowAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncomeStatementCashFlowAccountRepo extends JpaRepository<IncomeStatementCashFlowAccount, Integer> {
    List<IncomeStatementCashFlowAccount> findAllByIncomeStatementSettingId(Integer issId);
}
