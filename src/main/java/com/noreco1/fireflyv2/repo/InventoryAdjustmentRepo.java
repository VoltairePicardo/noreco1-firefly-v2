package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.InventoryAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryAdjustmentRepo extends JpaRepository<InventoryAdjustment, Integer> {
    InventoryAdjustment findByDocId(Integer docId);
}
