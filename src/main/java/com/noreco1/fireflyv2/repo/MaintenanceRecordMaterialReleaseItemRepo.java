package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MaintenanceRecordMaterialReleaseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceRecordMaterialReleaseItemRepo extends JpaRepository<MaintenanceRecordMaterialReleaseItem, Integer> {
    Long deleteAllByMaintenanceRecordId(Integer recordId);
    List<MaintenanceRecordMaterialReleaseItem> findAllByMaintenanceRecordId(Integer recordId);
    List<MaintenanceRecordMaterialReleaseItem> findAllByMaintenanceRecordIdAndStockReleaseId(Integer recordId, Integer stockReleaseId);
}
