package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MeterForm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeterFormRepo extends JpaRepository<MeterForm, Integer> {
    List<MeterForm> findByOrderByNameAsc();
}
