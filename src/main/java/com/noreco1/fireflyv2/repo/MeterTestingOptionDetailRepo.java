package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MeterTestingOptionDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeterTestingOptionDetailRepo extends JpaRepository<MeterTestingOptionDetail, Integer> {
    List<MeterTestingOptionDetail> findByMeterTestingId(Integer meterTestingId);
}
