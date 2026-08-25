package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.model.SpecialEquipment;
import com.noreco1.fireflyv2.repo.SpecialEquipmentRepo;
import com.noreco1.fireflyv2.service.SpecialEquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpecialEquipmentServiceImpl implements SpecialEquipmentService {

    @Autowired
    private SpecialEquipmentRepo specialEquipmentRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<SpecialEquipment> findAllSpecialEquipmentByQuery(String query, Pageable pageable) {
        return specialEquipmentRepo.findAllBySerialNoContainingIgnoreCase(query, pageable);
    }

}
