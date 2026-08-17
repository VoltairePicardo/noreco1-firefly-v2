package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.IncomeStatementSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncomeStatementSettingRepo extends JpaRepository<IncomeStatementSetting, Integer> {
    List<IncomeStatementSetting> findAllByParentIdOrderBySequenceAsc(Integer parentId);
}
