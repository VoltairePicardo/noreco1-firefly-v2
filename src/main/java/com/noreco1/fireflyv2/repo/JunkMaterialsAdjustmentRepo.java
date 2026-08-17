package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.JunkMaterialsAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JunkMaterialsAdjustmentRepo extends JpaRepository<JunkMaterialsAdjustment, Integer> {
    JunkMaterialsAdjustment findByDocId(Integer docId);
}
