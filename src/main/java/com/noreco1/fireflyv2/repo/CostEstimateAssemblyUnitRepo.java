package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CostEstimateAssemblyUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface CostEstimateAssemblyUnitRepo extends JpaRepository<CostEstimateAssemblyUnit, Integer> {
    ArrayList<CostEstimateAssemblyUnit> findByCostEstimateId(Integer transId);

    void deleteByCostEstimateId(Integer transId);
}
