package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.IncomeStatementAccountsBSUP;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncomeStatementAccountBSUPRepo extends JpaRepository<IncomeStatementAccountsBSUP, Integer> {
    List<IncomeStatementAccountsBSUP> findAllByIncomeStatementSettingBsupId(Integer settingId);
}
