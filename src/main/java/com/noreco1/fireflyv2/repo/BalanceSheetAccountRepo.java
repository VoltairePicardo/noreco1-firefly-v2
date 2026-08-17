package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.BalanceSheetAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BalanceSheetAccountRepo extends JpaRepository<BalanceSheetAccount, Integer> {
    List<BalanceSheetAccount> findAllByBalanceSheetSettingId(Integer issId);
}
