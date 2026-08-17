package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.BillOfMaterialDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface BillOfMaterialDetailRepo extends JpaRepository<BillOfMaterialDetail, Integer> {
    ArrayList<BillOfMaterialDetail> findByBillOfMaterialId(Integer transId);
    ArrayList<BillOfMaterialDetail> findByBillOfMaterialIdOrderByCategoryAscItemDescriptionAsc(Integer transId);

    void deleteByBillOfMaterialId(Integer transId);
}
