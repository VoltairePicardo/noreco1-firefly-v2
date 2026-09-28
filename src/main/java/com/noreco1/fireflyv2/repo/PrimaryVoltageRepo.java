package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.PrimaryVoltage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrimaryVoltageRepo extends JpaRepository<PrimaryVoltage, Integer> {
    List<PrimaryVoltage> findByOrderByDescriptionAsc();
}
