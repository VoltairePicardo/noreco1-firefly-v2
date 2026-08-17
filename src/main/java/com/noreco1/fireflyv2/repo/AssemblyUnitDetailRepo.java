package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.AssemblyUnitDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssemblyUnitDetailRepo extends JpaRepository<AssemblyUnitDetail, Integer> {
    List<AssemblyUnitDetail> findByAssemblyUnitId(Integer assemblyUnitId);
    AssemblyUnitDetail findByAssemblyUnitIdAndItemId(Integer assemblyUnitId, Integer itemId);

    void deleteByAssemblyUnitId(Integer id);
}
