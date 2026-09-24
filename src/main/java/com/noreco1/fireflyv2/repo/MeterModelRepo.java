package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MeterModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeterModelRepo extends JpaRepository<MeterModel, Integer> {
    Page<MeterModel> findByOrderByModelNameAsc(Pageable pageable);
    Page<MeterModel> findByModelNameContainingIgnoreCaseOrderByModelNameAsc(String q, Pageable pageable);
    MeterModel findTop1ByModelNameIgnoreCase(String modelName);
}
