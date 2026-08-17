package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.SalvageInventoryAdjustmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalvageInventoryAdjustmentDetailRepo extends JpaRepository<SalvageInventoryAdjustmentDetail, Integer> {
    List<SalvageInventoryAdjustmentDetail> findAllBySalvageInventoryAdjustmentId(Integer id);
}
