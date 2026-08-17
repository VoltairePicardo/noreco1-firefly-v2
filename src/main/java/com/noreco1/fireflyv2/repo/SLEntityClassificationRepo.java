package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.SLEntityClassification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by Personal on 11/4/2016.
 */
public interface SLEntityClassificationRepo extends JpaRepository<SLEntityClassification, Integer> {
    @Transactional(readOnly = true)
    public List<SLEntityClassification> findByIsSubLedger(Boolean isSubLedger);
}
