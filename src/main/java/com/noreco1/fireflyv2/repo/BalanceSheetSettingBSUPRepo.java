package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.BalanceSheetSettingBSUP;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BalanceSheetSettingBSUPRepo extends JpaRepository<BalanceSheetSettingBSUP, Integer> {
    List<BalanceSheetSettingBSUP> findAllByParentIdOrderBySequenceAsc(Integer parentId);
}
