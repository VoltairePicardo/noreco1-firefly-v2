package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.AssetDepreciation;
import com.noreco1.fireflyv2.model.AssetDepreciationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface AssetDepreciationDetailRepo extends JpaRepository<AssetDepreciationDetail, Integer> {
    List<AssetDepreciationDetail> findByAssetDepreciationId(Integer assetDepreciationId);

    @Query(value = "SELECT " +
            "COUNT(*) " +
            "FROM AssetDepreciationDetail " +
            "JOIN AssetDepreciationSchedule ON AssetDepreciationDetail.FK_assetDepreciationScheduleId = AssetDepreciationSchedule.id " +
            "WHERE FK_assetId = :assetId", nativeQuery = true)
    Integer countAssetDepreciationDetailByAssetId(@Param("assetId") Integer assetId);

    @Query(value = "SELECT " +
            "Asset.refNo as code, " +
            "Asset.description, " +
            "Asset.totalValue, " +
            "AssetDepreciationDetail.depreciationAmount, " +
            "AssetDetail.FK_expenseAccountId, " +
            "AssetDetail.FK_accumDepAccountId " +
            "FROM AssetDepreciationDetail " +
            "JOIN AssetDepreciationScheduleDetail ON AssetDepreciationDetail.FK_assetDepreciationScheduleDetailId = AssetDepreciationScheduleDetail.id  " +
            "JOIN AssetDetail ON AssetDepreciationScheduleDetail.FK_assetDetailId = AssetDetail.id " +
            "JOIN Asset ON AssetDetail.FK_assetId = Asset.id " +
            "JOIN AssetDepreciation ON AssetDepreciationDetail.FK_assetDepreciationId = AssetDepreciation.id " +
            "WHERE AssetDepreciation.`year` = :year and AssetDepreciation.`month` = :month " +
            "ORDER BY Asset.refNo ASC;",
            nativeQuery = true)
    List<Object[]> findAllByYearAndMonth(@Param("year") Integer year, @Param("month") Integer month);

    @Query(value = "SELECT " +
            "SUM(COALESCE(GeneralLedger.debit,0)) as sumDebit, " +
            "SUM(COALESCE(GeneralLedger.credit,0)) as sumCredit " +
            "FROM AssetDepreciation " +
            "JOIN GeneralLedger ON AssetDepreciation.FK_transactionId = GeneralLedger.FK_transactionId " +
            "JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id " +
            "WHERE AssetDepreciation.`year` = :year AND AssetDepreciation.`month` = :month " +
            "GROUP BY SegmentAccount.FK_accountId LIMIT 2",
            nativeQuery = true)
    List<Object[]> sumPerAccountTotalByYearAndMonth(@Param("year") Integer year, @Param("month") Integer month);
}
