package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Meter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeterRepo extends JpaRepository<Meter, Integer> {
    Optional<Meter> findFirstBySerialNoOrderByIdDesc(String serialNo);
    Page<Meter> findAllByOrderByUpdatedAtDesc(Pageable pageable);
    Page<Meter> findBySerialNoContainingIgnoreCaseOrderByUpdatedAtDesc(String serialNo, Pageable pageable);
}
