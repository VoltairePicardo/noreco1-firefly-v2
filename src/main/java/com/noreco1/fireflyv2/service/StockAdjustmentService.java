package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.StockAdjustment;
import com.noreco1.fireflyv2.controller.response.ItemTransactionDetailDto;
import com.noreco1.fireflyv2.controller.response.StockAdjustmentDocumentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface StockAdjustmentService extends DocumentService {
    StockAdjustment findById(Integer id);
    List<StockAdjustment> findAll();
    Page<StockAdjustment> findAll(Pageable pageable);
    Page<StockAdjustment> findByQuery(String query, Pageable pageable);
    List<Map> findByDateRangePending(String from, String to, Integer officeId);
    List<Map> findByDateRangeAndStatusId(String from, String to, Integer status, Integer officeId);
    List<DocumentStatus> getDocumentsStatuses();

    List<StockAdjustment> getListForSummaryReport(String from, String to, HttpServletRequest request);

    List<ItemTransactionDetailDto> getItems(Integer transId);

    Page<StockAdjustmentDocumentDto> findAllApprovedForAccountSettingPaged(String query, Pageable pageable);

    Page<StockAdjustmentDocumentDto> findAllApprovedForJVPaged(String query, Pageable pageable);
}
