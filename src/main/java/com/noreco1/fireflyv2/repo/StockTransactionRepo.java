package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockTransactionRepo extends JpaRepository<StockTransaction, Integer> {
}
