package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CashAdvanceBudgetDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CashAdvanceBudgetDetailRepo extends JpaRepository<CashAdvanceBudgetDetail, Integer> {

    @Transactional
    Long deleteByCashAdvanceId(Integer id);

    @Transactional
    List<CashAdvanceBudgetDetail> findAllByCashAdvanceId(Integer caId);

    CashAdvanceBudgetDetail findFirstByCashAdvanceIdOrderByIdAsc(Integer caId);

}
