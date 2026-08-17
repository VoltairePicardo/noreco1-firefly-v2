package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MaintenanceRecordWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Tri-Nvent on 3/2/2020.
 */
public interface MaintenanceRecordWorkRepo extends JpaRepository<MaintenanceRecordWork, Integer> {

    Long deleteAllByMaintenanceRecordId(Integer assetId);
    List<MaintenanceRecordWork> findAllByMaintenanceRecordId(Integer assetId);

    @Query(value = "SELECT SUM(amount) from MaintenanceRecordWork " +
            "WHERE FK_maintenanceRecordId = :maintenanceRecordId ", nativeQuery = true)
    BigDecimal totalLaborAmount(@Param("maintenanceRecordId") Integer maintenanceRecordId);

}
