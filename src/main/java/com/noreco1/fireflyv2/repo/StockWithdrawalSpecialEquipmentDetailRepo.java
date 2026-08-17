package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.StockWithdrawalSpecialEquipmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Tri-Nvent on 6/1/2020.
 */
public interface StockWithdrawalSpecialEquipmentDetailRepo extends JpaRepository<StockWithdrawalSpecialEquipmentDetail, Integer> {
    @Transactional
    public Long deleteAllByStockWithdrawalId(Integer id);
}
