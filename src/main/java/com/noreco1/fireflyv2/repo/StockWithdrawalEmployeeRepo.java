package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.StockWithdrawalEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface StockWithdrawalEmployeeRepo extends JpaRepository<StockWithdrawalEmployee, Integer> {

    void deleteAllByStockWithdrawalId(Integer id);

    ArrayList<StockWithdrawalEmployee> findAllByStockWithdrawalId(Integer id);

}
