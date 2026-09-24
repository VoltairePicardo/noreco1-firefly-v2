package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MeterTestingDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeterTestingDetailRepo extends JpaRepository<MeterTestingDetail, Integer> {
    List<MeterTestingDetail> findByMeterTestingId(Integer meterTestingId);
}
