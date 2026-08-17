package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.JournalVoucherBudgetLineItemDetail;
import com.noreco1.fireflyv2.model.JournalVoucherCashFlowBudgetDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface JournalVoucherCashFlowBudgetDetailRepo extends JpaRepository<JournalVoucherCashFlowBudgetDetail, Integer> {

    @Transactional
    public Long deleteByJournalVoucherId(Integer transId);

    List<JournalVoucherCashFlowBudgetDetail> findAllByJournalVoucherId(Integer id);

}
