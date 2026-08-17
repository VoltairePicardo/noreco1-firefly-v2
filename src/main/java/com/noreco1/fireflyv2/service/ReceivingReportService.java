package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.InventoryLocation;
import com.noreco1.fireflyv2.model.ReceivingReport;
import com.noreco1.fireflyv2.model.ReceivingReportDetail;
import com.noreco1.fireflyv2.controller.response.ApvPurchasingDocumentDto;
import com.noreco1.fireflyv2.controller.response.CvVoucherDto;
import com.noreco1.fireflyv2.controller.response.ReceivingReportDocumentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface ReceivingReportService extends VoucherService {

    @Transactional(readOnly = true)
    List<Map> findByDateRangeAndStatusId(String from, String to, Integer id);

    @Transactional(readOnly = true)
    List<Map> findByDateRangePending(String from, String to);

    @Transactional(readOnly = true)
    List<Map> getRRDetails(Integer rrId);

    @Transactional(readOnly = true)
    Map findById(Integer id);

    Map findByApvId(Integer apvId);

    List<Map> findAllApprovedForApv();

    Page<ApvPurchasingDocumentDto> findAllApprovedForApvPaged(String query, Pageable pageable);

    Page<ApvPurchasingDocumentDto> findAllApprovedForApvWithAccountSettingPaged(String query, Integer supplier, Pageable pageable);

    List<InventoryLocation> getAllInventoryLocations();

    List<ReceivingReportDetail> getAllReceivingReportDetail(Integer id);

    Page<CvVoucherDto> findAllApprovedForCvPaged(String query, Pageable pageable);

    Page<ReceivingReportDocumentDto> findAllForJv(String query, Pageable pageable);

}
