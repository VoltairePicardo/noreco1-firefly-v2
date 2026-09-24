package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MeterType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeterTypeRepo extends JpaRepository<MeterType, Integer> {
    List<MeterType> findByOrderByDescriptionAsc();
}
