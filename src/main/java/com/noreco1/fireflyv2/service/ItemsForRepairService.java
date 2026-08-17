package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.DocumentNoApproval;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.ItemsForRepair;
import com.noreco1.fireflyv2.controller.response.ItemTransactionDetailDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by tonyc on 9/22/2020.
 */
public interface ItemsForRepairService extends DocumentService {
    ItemsForRepair findById(Integer id);
    List<ItemsForRepair> findAll();
    Page<ItemsForRepair> findAll(Pageable pageable);
    Page<ItemsForRepair> findByQuery(String query, Pageable pageable);
    List<Map> findByDateRangePending(String from, String to, Integer officeId);
    List<Map> findByDateRangeAndStatusId(String from, String to, Integer status, Integer officeId);
    List<DocumentStatus> getDocumentsStatuses();

    @Transactional
    PostResponse processUpdate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse processCreate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource);

    List<ItemsForRepair> getListForSummaryReport(String from, String to, HttpServletRequest request);
    List<ItemTransactionDetailDto> getItemDetails(Integer transId);

    Page<Object[]> getForRR(String query, Integer invLocId, Pageable pageable);
    Map getReportMeta();
}
