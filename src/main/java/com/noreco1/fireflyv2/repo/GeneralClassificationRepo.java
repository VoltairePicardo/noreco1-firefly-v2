package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.GeneralClassification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by TSI on 1/27/2024.
 */
public interface GeneralClassificationRepo extends JpaRepository<GeneralClassification, Integer> {

    Page<GeneralClassification> findByDescriptionContainingIgnoreCase(String query, Pageable pageable);
    GeneralClassification findByDescriptionContainingIgnoreCase(String query);
    List<GeneralClassification> findByOrderByDescriptionAsc();

}
