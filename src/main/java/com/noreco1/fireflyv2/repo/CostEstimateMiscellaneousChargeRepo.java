package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CostEstimateMiscellaneousCharge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

/**
 * Created by tonyc on 1/30/2020.
 */
public interface CostEstimateMiscellaneousChargeRepo extends JpaRepository<CostEstimateMiscellaneousCharge, Integer> {
    ArrayList<CostEstimateMiscellaneousCharge> findByCostEstimateId(Integer transId);
    ArrayList<CostEstimateMiscellaneousCharge> findByCostEstimateIdOrderByMiscellaneousChargeDescriptionAsc(Integer transId);

    void deleteByCostEstimateId(Integer transId);
}
