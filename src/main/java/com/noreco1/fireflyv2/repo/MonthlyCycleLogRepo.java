package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MonthlyCycleLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonthlyCycleLogRepo extends JpaRepository<MonthlyCycleLog, Integer> {
    List<MonthlyCycleLog> findAllByMonthlyCycleIdOrderByCreatedAtDesc(Integer cycleId);
}
