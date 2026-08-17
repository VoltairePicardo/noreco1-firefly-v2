package com.noreco1.fireflyv2.common.facade;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.AssetDepreciationScheduleDetailRepo;
import com.noreco1.fireflyv2.repo.AssetDepreciationScheduleRepo;
import com.noreco1.fireflyv2.repo.AssetDetailRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by TSI Admin on 5/20/2016.
 */

@Component
public class AssetDepreciationScheduleFacadeImpl implements AssetDepreciationScheduleFacade {

    @Autowired
    AssetDepreciationScheduleRepo assetDepreciationScheduleRepo;

    @Autowired
    AssetDetailRepo assetDetailRepo;

    @Autowired
    AssetDepreciationScheduleDetailRepo assetDepreciationScheduleDetailRepo;

    @Override
    public void generateSchedule(Asset asset) {
        try {
            List<AssetDetail> detailList = assetDetailRepo.findByAssetIdAndAssetAccountIsNotNull(asset.getId());

            if (!detailList.isEmpty()) {

                Integer deductedMonths = 0;

                Integer year = asset.getStartYear();
                Integer month = asset.getStartMonth();

                // in case editing
                List<AssetDepreciationSchedule> exScheds = assetDepreciationScheduleRepo.findByAssetIdAndDeductedOrderByYearAscMonthAsc(asset.getId(), true);
                if (Checker.collectionIsNotEmpty(exScheds)) {
                    deductedMonths = exScheds.size();

                    AssetDepreciationSchedule lastDeductedSched = exScheds.get(deductedMonths-1);

                    year = lastDeductedSched.getYear();
                    month = lastDeductedSched.getMonth();

                    // schedule
                    if (month == 12) {
                        month = 1;
                        year++;
                    } else {
                        month++;
                    }
                }

                Integer depreciationMonths = asset.getDepreciationMonths() - deductedMonths;

                Map totalPerAssetDetail = new HashMap<>();

                for(int x = 0; x < depreciationMonths; x++) {

                    AssetDepreciationSchedule schedule = new AssetDepreciationSchedule();
                    schedule.setAsset(asset);
                    schedule.setDeducted(false);
                    schedule.setTotalDepreciationAmount(BigDecimal.ZERO);
                    schedule.setMonth(month++);
                    schedule.setYear(year);

                    schedule = assetDepreciationScheduleRepo.save(schedule);

                    BigDecimal totalDepreciationAmount = BigDecimal.ZERO;

                    AssetDepreciationScheduleDetail lastScheduleDetail = null;
                    for(AssetDetail assetDetail: detailList) {

                        AssetDepreciationScheduleDetail scheduleDetail = new AssetDepreciationScheduleDetail();
                        scheduleDetail.setAssetAccount(assetDetail.getAssetAccount());
                        scheduleDetail.setDepreciationSchedule(schedule);
                        scheduleDetail.setAssetDetail(assetDetail);

                        BigDecimal depreciationAmount = assetDetail.getRemainingValue().divide(new BigDecimal(depreciationMonths), 2, RoundingMode.HALF_UP);

                        Object totalObj =  totalPerAssetDetail.get(assetDetail.getId());
                        if (totalObj == null) {
                            totalPerAssetDetail.put(assetDetail.getId(), depreciationAmount);
                        } else {
                            BigDecimal totalBd = (BigDecimal) totalPerAssetDetail.get(assetDetail.getId());
                            totalPerAssetDetail.put(assetDetail.getId(), totalBd.add(depreciationAmount));
                        }

                        BigDecimal totalBd = (BigDecimal) totalPerAssetDetail.get(assetDetail.getId());

                        if (totalBd.compareTo(assetDetail.getRemainingValue()) > 0) {

                            BigDecimal excess = totalBd.subtract(assetDetail.getRemainingValue());
                            depreciationAmount = depreciationAmount.subtract(excess);

                        } else if (x == (depreciationMonths-1)) { // last row

                            if (totalBd.compareTo(assetDetail.getRemainingValue()) < 0) {
                                
                                BigDecimal kulang = assetDetail.getRemainingValue().subtract(totalBd);
                                depreciationAmount = depreciationAmount.add(kulang);
                            }
                        }

                        totalDepreciationAmount = totalDepreciationAmount.add(depreciationAmount);

                        scheduleDetail.setDepreciationAmount(depreciationAmount);
                        lastScheduleDetail = assetDepreciationScheduleDetailRepo.save(scheduleDetail);

                    }

                    if(x == depreciationMonths-1) {
                        // ensure that the last month in the depreciation schedule will leave P1 residual value.
                        totalDepreciationAmount = totalDepreciationAmount.subtract(BigDecimal.ONE);

                        lastScheduleDetail.setDepreciationAmount(lastScheduleDetail.getDepreciationAmount().subtract(BigDecimal.ONE));
                        assetDepreciationScheduleDetailRepo.save(lastScheduleDetail);
                    }

                    schedule.setTotalDepreciationAmount(totalDepreciationAmount);
                    assetDepreciationScheduleRepo.save(schedule);

                    if (month == 13) {
                        month = 1;
                        year++;
                    }
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

    }

}
