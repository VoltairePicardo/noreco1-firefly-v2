package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.SecondaryVoltage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SecondaryVoltageRepo extends JpaRepository<SecondaryVoltage, Integer> {
    List<SecondaryVoltage> findByOrderByDescriptionAsc();
}
