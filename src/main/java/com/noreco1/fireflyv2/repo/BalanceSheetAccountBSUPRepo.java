package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.BalanceSheetAccountBSUP;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BalanceSheetAccountBSUPRepo extends JpaRepository<BalanceSheetAccountBSUP, Integer> {
    List<BalanceSheetAccountBSUP> findAllByBalanceSheetSettingbsupId(Integer issId);

    BalanceSheetAccountBSUP findByBalanceSheetSettingbsupId(Integer id);
}
