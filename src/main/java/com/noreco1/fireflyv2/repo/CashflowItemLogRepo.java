package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CashflowItemLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CashflowItemLogRepo extends JpaRepository<CashflowItemLog, Integer> {
    List<CashflowItemLog> findAllByCashflowItemIdOrderByCreatedAtDesc(Integer id);
}