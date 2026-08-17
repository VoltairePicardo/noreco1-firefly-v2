package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.SalvageInventoryAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalvageInventoryAdjustmentRepo extends JpaRepository<SalvageInventoryAdjustment, Integer> {
    SalvageInventoryAdjustment findByDocId(Integer docId);
}
