package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.SalvageMaterialRequisition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalvageMaterialRequisitionRepo extends JpaRepository<SalvageMaterialRequisition, Integer> {
    SalvageMaterialRequisition findByDocId(Integer docId);
}
