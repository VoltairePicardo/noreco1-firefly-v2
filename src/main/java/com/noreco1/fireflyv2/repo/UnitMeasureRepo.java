package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.UnitMeasure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UnitMeasureRepo extends JpaRepository<UnitMeasure, Integer> {
    UnitMeasure findOneByCode(String str);

    UnitMeasure findOneByDescription(String str);

    UnitMeasure findOneByDescriptionIgnoreCase(String str);

    UnitMeasure findOneByCodeIgnoreCase(String str);

    UnitMeasure findOneByCodeIgnoreCaseOrDescriptionIgnoreCase(String code, String desc);

    @Transactional
    Page<UnitMeasure> findAllByOrderByCodeAsc(Pageable pageable);

    Page<UnitMeasure> findByDescriptionContainingIgnoreCaseOrCodeContainingIgnoreCaseOrderByCodeAsc(String q1, String q2, Pageable pageable);

    List<UnitMeasure> findAllByOrderByCodeAsc();
}
