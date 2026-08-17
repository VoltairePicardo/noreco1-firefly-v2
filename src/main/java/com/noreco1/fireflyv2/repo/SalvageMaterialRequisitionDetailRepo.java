package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.SalvageMaterialRequisitionDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalvageMaterialRequisitionDetailRepo extends JpaRepository<SalvageMaterialRequisitionDetail, Integer> {
    List<SalvageMaterialRequisitionDetail> findAllBySalvageMaterialRequisitionId(Integer id);
}
