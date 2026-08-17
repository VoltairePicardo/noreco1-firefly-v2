package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.SpecialEquipment;
import com.noreco1.fireflyv2.model.SpecialEquipmentAssignment;
import com.noreco1.fireflyv2.model.SpecialEquipmentAssignmentDetail;
import com.noreco1.fireflyv2.model.SpecialEquipmentAssignmentLog;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

/**
 * Created by Tri-Nvent on 7/16/2020.
 */
public interface SpecialEquipmentAssignmentService {

    Page<SpecialEquipmentAssignment> findAll(String startDate, String endDate, Pageable pageable);

    @Transactional
    PostResponse update(SpecialEquipmentAssignment specialEquipmentAssignment, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse create(SpecialEquipmentAssignment specialEquipmentAssignment, BindingResult bindingResult, MessageSource messageSource);

    SpecialEquipmentAssignment findById(Integer id);
    List<Map> findByConsumerId(Integer id);
    List<Map> findBySerial(String serialNo);

    Page<SpecialEquipment> findAllSpecialEquipment(Pageable pageable);
    Page<SpecialEquipment> findAllSpecialEquipmentByQuery(String query, Pageable pageable);

    List<SpecialEquipmentAssignmentDetail> setDetailsWithDefaultSpecialEquipment(Integer stockWithdrawalId);

    List<SpecialEquipment> findAllDefaultForSpecialEquipmentAssignmentNoTurnOn(Integer itemId, Integer noOfItems);

    Page<SpecialEquipment> findAllSpecialEquipmentNoConnectOrder(Integer itemId, Pageable pageable);
    Page<SpecialEquipment> findAllSpecialEquipmentByQueryNoConnectOrder(String query, Integer itemId, Pageable pageable);
    List<SpecialEquipmentAssignmentLog> findAllLogsById(Integer id);

    @Transactional
    PostResponse revoke(SpecialEquipmentAssignment specialEquipmentAssignment);
}
