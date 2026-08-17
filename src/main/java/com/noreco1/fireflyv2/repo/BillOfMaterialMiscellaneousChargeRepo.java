package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.BillOfMaterialMiscellaneousCharge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

/**
 * Created by tonyc on 1/30/2020.
 */
public interface BillOfMaterialMiscellaneousChargeRepo extends JpaRepository<BillOfMaterialMiscellaneousCharge, Integer> {
    ArrayList<BillOfMaterialMiscellaneousCharge> findByBillOfMaterialId(Integer transId);
    ArrayList<BillOfMaterialMiscellaneousCharge> findByBillOfMaterialIdOrderByMiscellaneousChargeDescriptionAsc(Integer transId);

    void deleteByBillOfMaterialId(Integer transId);
}
