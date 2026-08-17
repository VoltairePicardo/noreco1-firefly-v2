package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CostEstimateAssemblyUnitItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CostEstimateAssemblyUnitItemRepo extends JpaRepository<CostEstimateAssemblyUnitItem, Integer> {
    CostEstimateAssemblyUnitItem findTop1ByCostEstimateAssemblyUnitCostEstimateIdAndItemId(Integer costEstimateAssemblyUnitId, Integer itemId);
    void deleteByCostEstimateAssemblyUnitCostEstimateId(Integer costEstimateAssemblyUnitId);
    List<CostEstimateAssemblyUnitItem> findByCostEstimateAssemblyUnitCostEstimateIdAndItemId(Integer costEstimateAssemblyUnitId, Integer itemId);

    List<CostEstimateAssemblyUnitItem> findAllByCostEstimateAssemblyUnitCostEstimateId(Integer costEstimateId);

}
