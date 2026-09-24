package com.noreco1.fireflyv2.mssql_repo;

import com.noreco1.fireflyv2.mssql_model.MeterModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MssqlMeterModelRepo extends JpaRepository<MeterModel, Integer> {
    Page<MeterModel> findByOrderByModelNameAsc(Pageable pageable);
    Page<MeterModel> findByModelNameContainingIgnoreCaseOrderByModelNameAsc(String q, Pageable pageable);
    MeterModel findTop1ByModelNameIgnoreCase(String modelName);
}
