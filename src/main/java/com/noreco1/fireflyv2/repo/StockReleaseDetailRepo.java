package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.StockReleaseDetail;
import com.noreco1.fireflyv2.model.StockWithdrawalDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * Created by lenovo on 5/20/2017.
 */
public interface StockReleaseDetailRepo extends JpaRepository<StockReleaseDetail, Integer> {

    @Transactional
    public Long deleteByStockReleaseId(Integer id);
    @Transactional
    public ArrayList<StockReleaseDetail> findByStockReleaseId(Integer id);
}
