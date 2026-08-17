package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.StockTransfer;
import com.noreco1.fireflyv2.controller.response.InventoryDocumentDto;
import com.noreco1.fireflyv2.controller.response.ItemTransactionDetailDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface StockTransferService extends DocumentService {
    StockTransfer findById(Integer id);
    List<StockTransfer> findAll();
    Page<StockTransfer> findAll(Pageable pageable);
    Page<StockTransfer> findByQuery(String query, Pageable pageable);
    List<Map> findByDateRangePending(String from, String to, Integer officeId);
    List<Map> findByDateRangeAndStatusId(String from, String to, Integer status, Integer officeId);
    List<DocumentStatus> getDocumentsStatuses();
    List<Map> getDetails(Integer id);
    Page<InventoryDocumentDto> findAllForReleasingByQuery(String query, Pageable pageable);
    Page<InventoryDocumentDto> findAllForReleasing(Pageable pageable);

    Page<InventoryDocumentDto> findAllForReceivingByQuery(String query, Integer invLocId, Pageable pageable);

    List<StockTransfer> getListForSummaryReport(String from, String to, HttpServletRequest request);

    List<ItemTransactionDetailDto> getItems(Integer transId);
}
