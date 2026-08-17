package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Factor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FactorRepo extends JpaRepository<Factor, Integer> {
    Page<Factor> findAllByOrderByCodeAsc(Pageable pageable);
    List<Factor> findAllByOrderByCodeAsc();
    Factor findOneByCode(String code);
    Page<Factor> findByDescriptionContainingIgnoreCaseOrCodeContainingIgnoreCaseOrderByDescriptionAsc(String query1, String query2, Pageable pageable);
}
