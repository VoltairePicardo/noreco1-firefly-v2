package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.BalanceSheetSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BalanceSheetSettingRepo extends JpaRepository<BalanceSheetSetting, Integer> {
    List<BalanceSheetSetting> findAllByParentIdOrderBySequenceAsc(Integer parentId);
}
