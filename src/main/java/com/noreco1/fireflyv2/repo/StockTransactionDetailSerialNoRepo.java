package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.StockTransactionDetailSerialNo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by Tri-Nvent on 4/6/2021.
 */
public interface StockTransactionDetailSerialNoRepo extends JpaRepository<StockTransactionDetailSerialNo, Integer> {

    List<StockTransactionDetailSerialNo> findAllByStockTransactionDetailId(Integer id);

}
