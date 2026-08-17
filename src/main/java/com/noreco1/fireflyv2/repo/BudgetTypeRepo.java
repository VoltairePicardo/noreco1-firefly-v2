package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.BudgetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetTypeRepo extends JpaRepository<BudgetType, Integer>{

    Page<BudgetType> findByDescriptionContainingIgnoreCase(String query, Pageable pageable);
    BudgetType findByDescriptionContainingIgnoreCase(String query);
    List<BudgetType> findByOrderByDescriptionAsc();

}
