package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.JournalVoucherBudgetLineItemDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface JournalVoucherBudgetLineItemDetailRepo extends JpaRepository<JournalVoucherBudgetLineItemDetail, Integer> {

    @Transactional
    public Long deleteByJournalVoucherId(Integer transId);

    List<JournalVoucherBudgetLineItemDetail> findAllByJournalVoucherId(Integer id);

}
