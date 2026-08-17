package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.JunkMaterialsAdjustmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JunkMaterialsAdjustmentDetailRepo extends JpaRepository<JunkMaterialsAdjustmentDetail, Integer> {
    List<JunkMaterialsAdjustmentDetail> findAllByJunkMaterialsAdjustmentId(Integer id);
}
