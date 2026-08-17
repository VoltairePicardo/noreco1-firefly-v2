package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.BillOfMaterialAssemblyUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface BillOfMaterialAssemblyUnitRepo extends JpaRepository<BillOfMaterialAssemblyUnit, Integer> {
    ArrayList<BillOfMaterialAssemblyUnit> findByBillOfMaterialId(Integer transId);

    void deleteByBillOfMaterialId(Integer transId);
}
