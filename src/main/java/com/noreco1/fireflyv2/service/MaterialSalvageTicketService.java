package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.DocumentNoApproval;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.MaterialSalvageTicket;
import com.noreco1.fireflyv2.controller.response.ItemTransactionDetailDto;
import com.noreco1.fireflyv2.controller.response.MaterialSalvageTicketDocumentDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface MaterialSalvageTicketService extends DocumentService {
    MaterialSalvageTicket findById(Integer id);
    List<MaterialSalvageTicket> findAll();
    Page<MaterialSalvageTicket> findAll(Pageable pageable);
    Page<MaterialSalvageTicket> findByQuery(String query, Pageable pageable);
    Page<Map<String, Object>> getMaterialSalvageTicketPaged(String from, String to, Integer statusId, String query, Pageable pageable);
    List<Map> findByDateRangePending(String from, String to, Integer officeId);
    List<Map> findByDateRangeAndStatusId(String from, String to, Integer status, Integer officeId);
    List<DocumentStatus> getDocumentsStatuses();

    @Transactional
    PostResponse processUpdate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse processCreate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource);

    List<MaterialSalvageTicket> getListForSummaryReport(String from, String to, HttpServletRequest request);
    List<ItemTransactionDetailDto> getItems(Integer transId);
    Map getReportMeta();

    Page<MaterialSalvageTicketDocumentDto> findAllApprovedForAccountSettingPaged(String query, Pageable pageable);

}
