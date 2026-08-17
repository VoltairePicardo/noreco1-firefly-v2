package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.StockWithdrawalDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * Created by lenovo on 5/20/2017.
 */
public interface StockWithdrawalDetailRepo extends JpaRepository<StockWithdrawalDetail, Integer> {

    @Transactional
    public Long deleteByStockWithdrawalId(Integer id);
    @Transactional
    public ArrayList<StockWithdrawalDetail> findByStockWithdrawalId(Integer id);

    ArrayList<StockWithdrawalDetail> findByStockWithdrawalTransactionId(Integer transId);
}
