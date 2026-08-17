package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.SpecialEquipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SpecialEquipmentService {

    Page<SpecialEquipment> findAllSpecialEquipmentByQuery(String query, Pageable pageable);

}
