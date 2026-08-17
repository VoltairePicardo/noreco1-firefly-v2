package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CashflowItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CashflowItemRepo extends JpaRepository<CashflowItem, Integer> {
    List<CashflowItem> findByParentCashflowItemNotNull();

    List<CashflowItem> findByIdNotIn(Collection<Integer> id);

    List<CashflowItem> findAllByOrderByIdAscNameAsc();

}
