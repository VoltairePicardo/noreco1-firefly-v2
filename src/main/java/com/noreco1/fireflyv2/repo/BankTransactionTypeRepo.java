package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.BankTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankTransactionTypeRepo extends JpaRepository<BankTransactionType, Integer> {
}
