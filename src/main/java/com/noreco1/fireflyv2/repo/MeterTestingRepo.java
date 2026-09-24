package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MeterTesting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeterTestingRepo extends JpaRepository<MeterTesting, Integer> {
    Page<MeterTesting> findAllByOrderByDateDesc(Pageable pageable);
    Page<MeterTesting> findAllByTestedByContainingIgnoreCaseOrderByDateDesc(String testedBy, Pageable pageable);
}
