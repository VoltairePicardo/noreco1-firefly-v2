package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.BillOfMaterialAssemblyUnitItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillOfMaterialAssemblyUnitItemRepo extends JpaRepository<BillOfMaterialAssemblyUnitItem, Integer> {
    BillOfMaterialAssemblyUnitItem findTop1ByBillOfMaterialAssemblyUnitBillOfMaterialIdAndItemId(Integer billOfMaterialAssemblyUnitId, Integer itemId);
    void deleteByBillOfMaterialAssemblyUnitBillOfMaterialId(Integer billOfMaterialAssemblyUnitId);
    List<BillOfMaterialAssemblyUnitItem> findByBillOfMaterialAssemblyUnitBillOfMaterialIdAndItemId(Integer billOfMaterialAssemblyUnitId, Integer itemId);
}
