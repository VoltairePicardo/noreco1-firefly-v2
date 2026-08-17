package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CheckVoucherBudgetDetail;
import com.noreco1.fireflyv2.model.PurchaseOrderBudgetDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CheckVoucherBudgetDetailRepo extends JpaRepository<CheckVoucherBudgetDetail, Integer> {

    @Transactional
    public Long deleteByCheckVoucherId(Integer cvId);

    @Transactional
    List<CheckVoucherBudgetDetail> findAllByCheckVoucherId(Integer cvId);

    CheckVoucherBudgetDetail findFirstByCheckVoucherIdOrderByIdAsc(Integer cvId);

}
