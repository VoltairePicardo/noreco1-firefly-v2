package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.InventoryLocation;
import com.noreco1.fireflyv2.model.MaterialCreditTicket;
import com.noreco1.fireflyv2.controller.response.ItemTransactionDetailDto;
import com.noreco1.fireflyv2.controller.response.MaterialCreditTicketDocumentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface MaterialCreditTicketService extends DocumentService {
    MaterialCreditTicket findById(Integer id);
    List<MaterialCreditTicket> findAll();
    Page<MaterialCreditTicket> findAll(Pageable pageable);
    Page<MaterialCreditTicket> findByQuery(String query, Pageable pageable);
    List<Map> findByDateRangePending(String from, String to, Integer officeId);
    List<Map> findByDateRangeAndStatusId(String from, String to, Integer status, Integer officeId);
    List<DocumentStatus> getDocumentsStatuses();
    List<MaterialCreditTicket> getListForSummaryReport(String from, String to, HttpServletRequest request);
    List<ItemTransactionDetailDto> getItems(Integer transId);
    Map getReportMeta();
    List<InventoryLocation> getAllInventoryLocations();

    Page<MaterialCreditTicketDocumentDto> findAllApprovedForAccountSettingPaged(String query, Pageable pageable);

}
