package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Meter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeterRepo extends JpaRepository<Meter, Integer> {

    List<Meter> findBySerialNoIn(List<String> serialNos);
}
