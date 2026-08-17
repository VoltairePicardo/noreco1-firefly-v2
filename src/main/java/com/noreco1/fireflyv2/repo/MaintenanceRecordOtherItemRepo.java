package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MaintenanceRecordOtherItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaintenanceRecordOtherItemRepo extends JpaRepository<MaintenanceRecordOtherItem, Integer> {
    Long deleteAllByMaintenanceRecordId(Integer assetId);
    List<MaintenanceRecordOtherItem> findAllByMaintenanceRecordId(Integer assetId);
}
