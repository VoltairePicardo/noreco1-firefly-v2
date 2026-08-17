package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MaintenanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by Tri-Nvent on 1/14/2020.
 */
public interface MaintenanceRecordRepo extends JpaRepository<MaintenanceRecord, Integer> {
    MaintenanceRecord findByAssetId(Integer assetId);
    Long deleteAllByAssetId(Integer assetId);

    Page<MaintenanceRecord> findAllByMaintenanceDateBetweenOrderByCode(Date startDate, Date endDate, Pageable pageable);

    @Query(value = "SELECT m.code FROM MaintenanceRecord m WHERE year(m.maintenanceDate) = :year ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCodeByYear(@Param("year") Integer year);

    @Query(value = "SELECT " +
            "MaintenanceRecord.`maintenanceDate` as voucherDate, " +
            "'' as particulars, " +
            "MaintenanceRecord.code, " +
            "3 as FK_assetVoucherLinkTypeId, " +
            "SUM(SubLedger.debit + SubLedger.credit), " +
            "'' as adjustmentType " +
            "FROM MaintenanceRecord  " +
            "JOIN SubLedger ON SubLedger.FK_transactionId = MaintenanceRecord.FK_voucherTransactionId " +
            "JOIN SegmentAccount ON SegmentAccount.id = SubLedger.FK_segmentAccountId " +
            "WHERE (MaintenanceRecord.`maintenanceDate` BETWEEN :startDate AND :endDate) " +
            "AND (SegmentAccount.FK_accountId = :accountId) " +
            "AND IF(:assetAccountNo = 0 OR :assetAccountNo = SubLedger.FK_accountNo, 1, 0) = 1 " +
            "GROUP BY MaintenanceRecord.FK_voucherTransactionId", nativeQuery = true)
    List<Object[]> findByVoucherDateRangeAndAccountIdAndAssetVoucherLinkTypeId(@Param("startDate") java.sql.Date startDate,
                                                                               @Param("endDate") java.sql.Date endDate,
                                                                               @Param("accountId") Integer accountId,
                                                                               @Param("assetAccountNo") Integer assetAccountNo);

    List<MaintenanceRecord> findAllByMaintenanceDateBetweenOrderByCode(Date startDate, Date endDate);
    List<MaintenanceRecord> findAllByMaintenanceDateBetweenAndAssetDescriptionContainsIgnoreCaseOrderByCode(Date startDate, Date endDate, String query);
    Page<MaintenanceRecord> findAllByMaintenanceDateBetweenAndAssetDescriptionContainsIgnoreCaseOrderByCode(Date startDate, Date endDate, String query, Pageable pageable);

    Page<MaintenanceRecord> findAllByMaintenanceDateBetweenAndAssetAssetTypeId(Date startDate, Date endDate, Integer assetTypeId, Pageable pageable);

}
