package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.AssetDepreciationSchedule;
import com.noreco1.fireflyv2.model.AssetDepreciationScheduleDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetDepreciationScheduleRepo extends JpaRepository<AssetDepreciationSchedule, Integer> {
    Long deleteByAssetIdAndDeducted(Integer assetId, Boolean deducted);
    List<AssetDepreciationSchedule> findByAssetIdAndDeductedOrderByYearAscMonthAsc(Integer assetId, Boolean deducted);
    List<AssetDepreciationSchedule> findByAssetId(Integer assetId);
}
