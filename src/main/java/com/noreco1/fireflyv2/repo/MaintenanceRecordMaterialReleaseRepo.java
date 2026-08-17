package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MaintenanceRecordMaterialRelease;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by Tri-Nvent on 3/4/2020.
 */
public interface MaintenanceRecordMaterialReleaseRepo extends JpaRepository<MaintenanceRecordMaterialRelease, Integer> {

    Long deleteAllByMaintenanceRecordId(Integer assetId);
    List<MaintenanceRecordMaterialRelease> findAllByMaintenanceRecordId(Integer maintenanceRecordId);
    MaintenanceRecordMaterialRelease findByMaintenanceRecordId(Integer maintenanceRecordId);

}
