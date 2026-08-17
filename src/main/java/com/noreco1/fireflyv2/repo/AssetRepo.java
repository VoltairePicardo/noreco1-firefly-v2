package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public interface AssetRepo extends JpaRepository<Asset, Integer> {
    Asset findOneByRefNoOrCode(String refNo, String code);
    @Query(value = "SELECT e.code FROM Asset e WHERE year = :year AND code LIKE '%A%' ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCodeByYear(@Param("year") Integer year);

    Asset findByWorkOrderProjectId(Integer projectId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT * FROM (SELECT " +
            "AllVouchers.voucherDate, " +
            "AllVouchers.particulars, " +
            "AllVouchers.code, " +
            "AssetVoucher.FK_assetVoucherLinkTypeId, " +
            "SUM(SubLedger.debit + SubLedger.credit), " +
            "AssetVoucher.adjustmentType " +
            "FROM AllVouchers " +
            "JOIN AssetVoucher ON AssetVoucher.FK_voucherTransactionId = AllVouchers.fk_transactionid " +
            "JOIN SubLedger ON SubLedger.FK_transactionId = AssetVoucher.FK_voucherTransactionId " +
            "JOIN SegmentAccount ON SegmentAccount.id = SubLedger.FK_segmentAccountId " +
            "WHERE (AllVouchers.voucherDate BETWEEN :startDate AND :endDate) " +
            "AND (SegmentAccount.FK_accountId = :accountId) " +
            "AND IF(:assetAccountNo = 0 OR :assetAccountNo = SubLedger.FK_accountNo, 1, 0) = 1 " +
            "GROUP BY AllVouchers.fk_transactionid " +
            "UNION " +
            "SELECT " +
            "MaintenanceRecord.`date` as voucherDate, " +
            "particulars, " +
            "MaintenanceRecord.code, " +
            "3 as FK_assetVoucherLinkTypeId, " +
            "SUM(SubLedger.debit + SubLedger.credit), " +
            "'' as adjustmentType " +
            "FROM MaintenanceRecord  " +
            "JOIN AllVouchers ON MaintenanceRecord.FK_voucherTransactionId = AllVouchers.FK_transactionId  " +
            "JOIN SubLedger ON SubLedger.FK_transactionId = MaintenanceRecord.FK_voucherTransactionId " +
            "JOIN SegmentAccount ON SegmentAccount.id = SubLedger.FK_segmentAccountId " +
            "WHERE (MaintenanceRecord.`date` BETWEEN :startDate AND :endDate) " +
            "AND (SegmentAccount.FK_accountId = :accountId) " +
            "AND IF(:assetAccountNo = 0 OR :assetAccountNo = SubLedger.FK_accountNo, 1, 0) = 1 " +
            "GROUP BY MaintenanceRecord.FK_voucherTransactionId) as vouchers ORDER BY voucherDate", nativeQuery = true)
    List<Object[]> findByVoucherDateRangeAndAccountIdAndAssetVoucherLinkTypeId(@Param("startDate") Date startDate,
                                                                               @Param("endDate") Date endDate,
                                                                               @Param("accountId") Integer accountId,
                                                                               @Param("assetAccountNo") Integer assetAccountNo);

    Page<Asset> findAll(Pageable pageable);
    @Query("SELECT e from Asset e where e.code LIKE :query1 OR e.description LIKE :query2")
    Page<Asset> findByCodeOrDescription(@Param("query1") String query1, @Param("query2") String query2, Pageable pageable);

    Page<Asset> findByAssetTypeId(Integer assetTypeId, Pageable pageable);
    @Query("SELECT e from Asset e where e.assetType.id = :assetTypeId AND (e.code LIKE :query1 OR e.description LIKE :query2)")
    Page<Asset> findByAssetTypeAndCodeOrDescription(@Param("assetTypeId") Integer assetTypeId, @Param("query1") String query1, @Param("query2") String query2, Pageable pageable);

    Page<Asset> findByAssetTypeIdAndTotalRemainingValueGreaterThan(Integer assetTypeId, BigDecimal totalRemainingValue, Pageable pageable);
    @Query("SELECT e from Asset e where e.assetType.id = :assetTypeId AND e.totalRemainingValue > 0 AND (e.code LIKE :query1 OR e.description LIKE :query2)")
    Page<Asset> findByAssetTypeAndIsNotFullyDepreciatedAndCodeOrDescription(@Param("assetTypeId") Integer assetTypeId, @Param("query1") String query1, @Param("query2") String query2, Pageable pageable);

    Page<Asset> findByAssetTypeIdAndTotalRemainingValueLessThanEqual(Integer assetTypeId, BigDecimal totalRemainingValue, Pageable pageable);
    @Query("SELECT e from Asset e where e.assetType.id = :assetTypeId AND e.totalRemainingValue <= 0 AND (e.code LIKE :query1 OR e.description LIKE :query2)")
    Page<Asset> findByAssetTypeAndIsFullyDepreciatedAndCodeOrDescription(@Param("assetTypeId") Integer assetTypeId, @Param("query1") String query1, @Param("query2") String query2, Pageable pageable);

    Page<Asset> findByAssetTypeIdAndStatus(Integer assetTypeId, String status, Pageable pageable);
    @Query("SELECT e from Asset e where e.assetType.id = :assetTypeId AND e.status = :status AND (e.code LIKE :query1 OR e.description LIKE :query2)")
    Page<Asset> findByAssetTypeAndStatusAndCodeOrDescription(@Param("assetTypeId") Integer assetTypeId, @Param("status") String status, @Param("query1") String query1, @Param("query2") String query2, Pageable pageable);

    Page<Asset> findByAssetTypeIdAndStatusAndTotalRemainingValueGreaterThan(Integer assetTypeId, String status, BigDecimal totalRemainingValue, Pageable pageable);
    @Query("SELECT e from Asset e where e.assetType.id = :assetTypeId AND e.status = :status AND e.totalRemainingValue > 0 AND (e.code LIKE :query1 OR e.description LIKE :query2)")
    Page<Asset> findByAssetTypeAndStatusAndIsNotFullyDepreciatedAndCodeOrDescription(@Param("assetTypeId") Integer assetTypeId, @Param("status") String status, @Param("query1") String query1, @Param("query2") String query2, Pageable pageable);

    Page<Asset> findByAssetTypeIdAndStatusAndTotalRemainingValueLessThanEqual(Integer assetTypeId, String status, BigDecimal totalRemainingValue, Pageable pageable);
    @Query("SELECT e from Asset e where e.assetType.id = :assetTypeId AND e.status = :status AND e.totalRemainingValue <= 0 AND (e.code LIKE :query1 OR e.description LIKE :query2)")
    Page<Asset> findByAssetTypeAndStatusAndIsFullyDepreciatedAndCodeOrDescription(@Param("assetTypeId") Integer assetTypeId, @Param("status") String status, @Param("query1") String query1, @Param("query2") String query2, Pageable pageable);

    Page<Asset> findByTotalRemainingValueGreaterThan(BigDecimal totalRemainingValue, Pageable pageable);
    @Query("SELECT e from Asset e where e.totalRemainingValue > 0 AND (e.code LIKE :query1 OR e.description LIKE :query2)")
    Page<Asset> findByIsNotFullyDepreciatedAndCodeOrDescription(@Param("query1") String query1, @Param("query2") String query2, Pageable pageable);

    Page<Asset> findByTotalRemainingValueLessThanEqual(BigDecimal totalRemainingValue, Pageable pageable);
    @Query("SELECT e from Asset e where e.totalRemainingValue <= 0 AND (e.code LIKE :query1 OR e.description LIKE :query2)")
    Page<Asset> findByIsFullyDepreciatedAndCodeOrDescription(@Param("query1") String query1, @Param("query2") String query2, Pageable pageable);

    Page<Asset> findByStatusAndTotalRemainingValueGreaterThan(String status, BigDecimal totalRemainingValue, Pageable pageable);
    @Query("SELECT e from Asset e where e.status = :status AND e.totalRemainingValue > 0 AND (e.code LIKE :query1 OR e.description LIKE :query2)")
    Page<Asset> findByStatusAndIsNotFullyDepreciatedAndCodeOrDescription(@Param("status") String status, @Param("query1") String query1, @Param("query2") String query2, Pageable pageable);

    Page<Asset> findByStatusAndTotalRemainingValueLessThanEqual(String status, BigDecimal totalRemainingValue, Pageable pageable);
    @Query("SELECT e from Asset e where e.status = :status AND e.totalRemainingValue <= 0 AND (e.code LIKE :query1 OR e.description LIKE :query2)")
    Page<Asset> findByStatusAndIsFullyDepreciatedAndCodeOrDescription(@Param("status") String status, @Param("query1") String query1, @Param("query2") String query2, Pageable pageable);

    Page<Asset> findByStatus(String status, Pageable pageable);
    @Query("SELECT e from Asset e where e.status = :status AND (e.code LIKE :query1 OR e.description LIKE :query2)")
    Page<Asset> findByStatusAndCodeOrDescription(@Param("status") String status, @Param("query1") String query1, @Param("query2") String query2, Pageable pageable);

    Page<Asset> findAllByCodeContainsOrDescriptionContainsAndStatus(String code, String Description, String status, Pageable pageable);
    Page<Asset> findAllByStatus(String status, Pageable pageable);

}
