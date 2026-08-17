package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.JobOrderBudgetDetail;
import com.noreco1.fireflyv2.model.PurchaseOrderBudgetDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface JobOrderBudgetDetailRepo extends JpaRepository<JobOrderBudgetDetail, Integer> {

    @Transactional
    public Long deleteByJobOrderId(Integer joId);

    @Transactional
    List<JobOrderBudgetDetail> findAllByJobOrderId(Integer joId);

    JobOrderBudgetDetail findFirstByJobOrderIdOrderByIdAsc(Integer joId);

}
