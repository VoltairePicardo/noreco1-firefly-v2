package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.StockReceive;
import com.noreco1.fireflyv2.controller.response.ItemTransactionDetailDto;
import com.noreco1.fireflyv2.controller.response.StockReceiveDocumentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface StockReceiveService extends VoucherService {
    StockReceive findById(Integer id);
    StockReceive findByCode(String code);
    List<StockReceive> findAll();
    Page<StockReceive> findAll(Pageable pageable);
    Page<StockReceive> findByQuery(String query, Pageable pageable);
    List<Map> getDetails(int id);
    @Transactional(readOnly = true)
    List<Map> findByDateRangeAndStatusId(String from, String to, Integer docStatusId, Integer officeId);
    @Transactional(readOnly = true)
    List<Map> findByDateRangePending(String from, String to, Integer officeId);

    List<StockReceive> getListForSummaryReport(String from, String to, HttpServletRequest request);

    List<ItemTransactionDetailDto> getItems(Integer transId);

    Page<StockReceiveDocumentDto> findAllApprovedForAccountSettingPaged(String query, Pageable pageable);

    Page<StockReceiveDocumentDto> findAllApprovedForJVPaged(String query, Pageable pageable);
}
