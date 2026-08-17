package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.DocumentNoApproval;
import com.noreco1.fireflyv2.model.MaintenanceRecord;
import com.noreco1.fireflyv2.controller.response.MaintenanceRecordDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

/**
 * Created by Tri-Nvent on 3/2/2020.
 */
public interface MaintenanceRecordService {

    Page<MaintenanceRecord> findAll(String startDate, String endDate, Pageable pageable);
    Page<MaintenanceRecord> findAllByQuery(String query,String startDate, String endDate, Pageable pageable);
    Page<MaintenanceRecord> findAllByDateRangeAndAssetTypeId(String startDate, String endDate, Integer assetTypeId, Pageable pageable);

    MaintenanceRecord findById(Integer id);

    @Transactional
    PostResponse update(MaintenanceRecord maintenanceRecord, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse create(MaintenanceRecord maintenanceRecord, BindingResult bindingResult, MessageSource messageSource);

    MaintenanceRecordDto findOne(Integer id);

}
