package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.ModeOfProcurement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModeOfProcurementRepo extends JpaRepository<ModeOfProcurement, Integer> {
    List<ModeOfProcurement> findAllByIsForPOTrue();
}