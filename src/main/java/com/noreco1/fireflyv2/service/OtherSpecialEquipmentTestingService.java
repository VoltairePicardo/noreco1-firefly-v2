package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.OtherSpecialEquipmentTesting;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;

public interface OtherSpecialEquipmentTestingService {

    OtherSpecialEquipmentTesting getById(Integer id);
    Page<OtherSpecialEquipmentTesting> findAll(Pageable pageable);
    Page<OtherSpecialEquipmentTesting> findAllByQuery(String query, Pageable pageable);

    PostResponse create(OtherSpecialEquipmentTesting otherSpecialEquipmentTesting, BindingResult bindingResult, MessageSource messageSource);

}
