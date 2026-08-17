package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.AssetDepreciationScheduleDetail;
import com.noreco1.fireflyv2.model.AssetDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface AssetDepreciationScheduleDetailRepo extends JpaRepository<AssetDepreciationScheduleDetail, Integer> {

    Long deleteByDepreciationScheduleAssetIdAndDepreciationScheduleDeducted(Integer assetId, Boolean deducted);
    List<AssetDepreciationScheduleDetail> findByDepreciationScheduleYearAndDepreciationScheduleMonthAndDepreciationScheduleDeducted(Integer year, Integer month, Boolean deducted);
    List<AssetDepreciationScheduleDetail> findByDepreciationScheduleYearAndDepreciationScheduleMonthAndDepreciationScheduleDeductedAndAssetDetailAssetStatus(Integer year, Integer month, Boolean deducted, String status);

    List<AssetDepreciationScheduleDetail> findByDepreciationScheduleId(Integer depreciationScheduleId);
    List<AssetDepreciationScheduleDetail> findByDepreciationScheduleAssetIdAndDepreciationScheduleDeducted(Integer assetId, Boolean deducted);

    @Query(value = "SELECT " +
            "SUM(AssetDepreciationScheduleDetail.depreciationAmount) " +
            "FROM AssetDepreciationScheduleDetail " +
            "JOIN AssetDepreciationSchedule ON AssetDepreciationScheduleDetail.FK_assetDepreciationScheduleId = AssetDepreciationSchedule.id " +
            "WHERE AssetDepreciationScheduleDetail.FK_assetDetailId = :assetDetailId " +
            "and deducted = :deducted", nativeQuery = true)
    BigDecimal sumAssetDepreciationAmount(@Param("assetDetailId") Integer assetDetailId, @Param("deducted") Boolean deducted);

    @Query(value = "SELECT  " +
            "AssetDepreciationSchedule.id " +
            "FROM AssetDepreciationScheduleDetail  " +
            "JOIN AssetDepreciationSchedule ON AssetDepreciationScheduleDetail.FK_assetDepreciationScheduleId = AssetDepreciationSchedule.id  " +
            "JOIN AssetDetail ON AssetDepreciationScheduleDetail.FK_assetDetailId = AssetDetail.id " +
            "WHERE AssetDepreciationScheduleDetail.FK_assetDetailId = :assetDetailId " +
            "and deducted = :deducted and AssetDetail.FK_assetAccountId = :assetAccountId LIMIT 1", nativeQuery = true)
    Integer assetAccountSchedule(@Param("assetDetailId") Integer assetDetailId, @Param("assetAccountId") Integer assetAccountId, @Param("deducted") Boolean deducted);

    @Query(value = "SELECT  " +
            "AssetDepreciationSchedule.id " +
            "FROM AssetDepreciationScheduleDetail  " +
            "JOIN AssetDepreciationSchedule ON AssetDepreciationScheduleDetail.FK_assetDepreciationScheduleId = AssetDepreciationSchedule.id  " +
            "JOIN AssetDetail ON AssetDepreciationScheduleDetail.FK_assetDetailId = AssetDetail.id " +
            "WHERE AssetDepreciationScheduleDetail.FK_assetDetailId = :assetDetailId " +
            "and deducted = :deducted and AssetDetail.FK_expenseAccountId = :expenseAccountId LIMIT 1", nativeQuery = true)
    Integer expenseAccountSchedule(@Param("assetDetailId") Integer assetDetailId, @Param("expenseAccountId") Integer expenseAccountId, @Param("deducted") Boolean deducted);

    @Query(value = "SELECT  " +
            "AssetDepreciationSchedule.id " +
            "FROM AssetDepreciationScheduleDetail  " +
            "JOIN AssetDepreciationSchedule ON AssetDepreciationScheduleDetail.FK_assetDepreciationScheduleId = AssetDepreciationSchedule.id  " +
            "JOIN AssetDetail ON AssetDepreciationScheduleDetail.FK_assetDetailId = AssetDetail.id " +
            "WHERE AssetDepreciationScheduleDetail.FK_assetDetailId = :assetDetailId " +
            "and deducted = :deducted and AssetDetail.FK_accumDepAccountId = :accumDepAccountId LIMIT 1", nativeQuery = true)
    Integer accumDepAccountSchedule(@Param("assetDetailId") Integer assetDetailId, @Param("accumDepAccountId") Integer accumDepAccountId, @Param("deducted") Boolean deducted);

    // just for checking if any of the months is processed
    AssetDepreciationScheduleDetail findFirstByAssetDetailIdAndDepreciationScheduleDeducted(Integer assetDetailId, Boolean deducted);

    @Query(value = "select " +
            "a.refNo, " +
            "a.description, " +
            "ac.code, " +
            "ac.title, " +
            "sum(if(ads.month = 1, adsd.depreciationAmount, 0)) as jan, " +
            "sum(if(ads.month = 2, adsd.depreciationAmount, 0)) as feb, " +
            "sum(if(ads.month = 3, adsd.depreciationAmount, 0)) as mar, " +
            "sum(if(ads.month = 4, adsd.depreciationAmount, 0)) as apr, " +
            "sum(if(ads.month = 5, adsd.depreciationAmount, 0)) as may, " +
            "sum(if(ads.month = 6, adsd.depreciationAmount, 0)) as jun, " +
            "sum(if(ads.month = 7, adsd.depreciationAmount, 0)) as jul, " +
            "sum(if(ads.month = 8, adsd.depreciationAmount, 0)) as aug, " +
            "sum(if(ads.month = 9, adsd.depreciationAmount, 0)) as sep, " +
            "sum(if(ads.month = 10, adsd.depreciationAmount, 0)) as octo, " +
            "sum(if(ads.month = 11, adsd.depreciationAmount, 0)) as nov, " +
            "sum(if(ads.month = 12, adsd.depreciationAmount, 0)) as dece " +
            "from AssetDepreciationSchedule ads " +
            "inner join AssetDepreciationScheduleDetail adsd on ads.id = adsd.FK_assetDepreciationScheduleId " +
            "inner join Asset a on ads.FK_assetId = a.id " +
            "inner join Account ac on adsd.FK_assetAccountId = ac.id " +
            "where ads.year = :year " +
            "group by a.id, ac.id " +
            "order by ads.FK_assetId asc, " +
            "ads.month asc", nativeQuery = true)
    List<Object[]> getDepreciationScheduleSummaryByYear(@Param("year") Integer year);

    @Query(value = "select " +
            "ac.code, " +
            "ac.title, " +
            "sum(if(ads.month = 1, adsd.depreciationAmount, 0)) as jan, " +
            "sum(if(ads.month = 2, adsd.depreciationAmount, 0)) as feb, " +
            "sum(if(ads.month = 3, adsd.depreciationAmount, 0)) as mar, " +
            "sum(if(ads.month = 4, adsd.depreciationAmount, 0)) as apr, " +
            "sum(if(ads.month = 5, adsd.depreciationAmount, 0)) as may, " +
            "sum(if(ads.month = 6, adsd.depreciationAmount, 0)) as jun, " +
            "sum(if(ads.month = 7, adsd.depreciationAmount, 0)) as jul, " +
            "sum(if(ads.month = 8, adsd.depreciationAmount, 0)) as aug, " +
            "sum(if(ads.month = 9, adsd.depreciationAmount, 0)) as sep, " +
            "sum(if(ads.month = 10, adsd.depreciationAmount, 0)) as octo, " +
            "sum(if(ads.month = 11, adsd.depreciationAmount, 0)) as nov, " +
            "sum(if(ads.month = 12, adsd.depreciationAmount, 0)) as dece " +
            "from AssetDepreciationSchedule ads " +
            "inner join AssetDepreciationScheduleDetail adsd on ads.id = adsd.FK_assetDepreciationScheduleId " +
            "inner join Account ac on adsd.FK_assetAccountId = ac.id " +
            "where ads.year = :year " +
            "group by ac.id " +
            "order by ads.FK_assetId asc, ads.month asc", nativeQuery = true)
    List<Object[]> getDepreciationScheduleSummaryRecapByYear(@Param("year") Integer year);
}

