package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.BalanceSheetSettingBSUP;
import com.noreco1.fireflyv2.model.IncomeStatementSettingBSUP;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncomeStatementSettingBSUPRepo extends JpaRepository<IncomeStatementSettingBSUP, Integer> {
    List<IncomeStatementSettingBSUP> findAllByTypeOrderBySequenceAsc(String type);
}
