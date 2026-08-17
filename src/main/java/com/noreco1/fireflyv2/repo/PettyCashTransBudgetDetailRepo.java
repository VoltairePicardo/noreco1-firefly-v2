package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.PettyCashTransBudgetDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PettyCashTransBudgetDetailRepo extends JpaRepository<PettyCashTransBudgetDetail, Integer> {

    @Transactional
    Long deleteByPettyCashTransId(Integer id);

    @Transactional
    List<PettyCashTransBudgetDetail> findAllByPettyCashTransId(Integer pcvId);

    PettyCashTransBudgetDetail findFirstByPettyCashTransIdOrderByIdAsc(Integer pcvId);

}
