package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Bank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankRepo extends JpaRepository<Bank, Integer> {

    Page<Bank> findByNameContainingIgnoreCase(String query, Pageable pageable);
    Bank findByNameContainingIgnoreCase(String query);
    List<Bank> findByOrderByNameAsc();

}
