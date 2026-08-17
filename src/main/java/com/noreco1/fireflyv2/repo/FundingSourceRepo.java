package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.FundingSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FundingSourceRepo extends JpaRepository<FundingSource, Integer> {

    Page<FundingSource> findByDescriptionContainingIgnoreCase(String query, Pageable pageable);
    FundingSource findByDescriptionContainingIgnoreCase(String query);
    List<FundingSource> findByOrderByDescriptionAsc();

}
