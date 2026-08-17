package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CostEstimateDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface CostEstimateDetailRepo extends JpaRepository<CostEstimateDetail, Integer> {
    ArrayList<CostEstimateDetail> findByCostEstimateId(Integer transId);
    ArrayList<CostEstimateDetail> findByCostEstimateIdOrderByCategoryAscItemDescriptionAsc(Integer transId);

    void deleteByCostEstimateId(Integer transId);
}
