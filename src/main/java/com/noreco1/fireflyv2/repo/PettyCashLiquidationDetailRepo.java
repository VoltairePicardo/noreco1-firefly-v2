package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.PettyCashLiquidationDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by tonyc on 6/20/2023.
 */
public interface PettyCashLiquidationDetailRepo extends JpaRepository<PettyCashLiquidationDetail, Integer> {
    List<PettyCashLiquidationDetail> findByPettyCashLiquidationId(Integer id);

    void deleteByPettyCashLiquidationId(Integer id);
}
