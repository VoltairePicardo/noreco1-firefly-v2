package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.InventoryAdjustmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryAdjustmentDetailRepo extends JpaRepository<InventoryAdjustmentDetail, Integer> {
    List<InventoryAdjustmentDetail> findAllByInventoryAdjustmentId(Integer id);
}
