package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.OtherSpecialEquipmentTesting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtherSpecialEquipmentTestingRepo extends JpaRepository<OtherSpecialEquipmentTesting, Integer> {

    Page<OtherSpecialEquipmentTesting> findAllBySpecialEquipmentSerialNoContainingIgnoreCase(String specialEquipmentSerialNo, Pageable pageable);

}
