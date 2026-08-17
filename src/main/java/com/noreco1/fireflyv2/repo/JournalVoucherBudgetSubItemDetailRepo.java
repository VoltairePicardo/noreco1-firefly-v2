package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.JournalVoucherBudgetSubItemDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface JournalVoucherBudgetSubItemDetailRepo extends JpaRepository<JournalVoucherBudgetSubItemDetail, Integer> {

    List<JournalVoucherBudgetSubItemDetail> findAllByJournalVoucherId(Integer jvId);

    @Transactional
    public Long deleteByJournalVoucherId(Integer transId);

}
