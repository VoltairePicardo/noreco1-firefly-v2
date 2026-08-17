package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.BudgetItemClassification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetItemClassificationRepo extends JpaRepository<BudgetItemClassification, Integer>{

    Page<BudgetItemClassification> findByDescriptionContainingIgnoreCase(String query, Pageable pageable);
    BudgetItemClassification findByDescriptionContainingIgnoreCase(String query);
    List<BudgetItemClassification> findByOrderByDescriptionAsc();

}
