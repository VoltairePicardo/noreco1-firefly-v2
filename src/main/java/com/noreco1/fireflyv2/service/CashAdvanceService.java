package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.CashAdvance;
import com.noreco1.fireflyv2.model.CashAdvanceBudgetDetail;
import com.noreco1.fireflyv2.controller.response.CashFlowItemDto;
import com.noreco1.fireflyv2.controller.response.CvVoucherDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.HashMap;
import java.util.List;

public interface CashAdvanceService extends VoucherService {

    HashMap findById(Integer id);

    List<HashMap> findAll();

    @Transactional(readOnly = true)
    List<HashMap> findByStatusId(Integer id);

    @Transactional(readOnly = true)
    List<HashMap> findByDateRangeAndStatusId(String from, String to, Integer id, Integer officeId);

    @Transactional(readOnly = true)
    List<HashMap> findByDateRange(String from, String to, Integer officeId);

    Page<CashAdvance> findAllForCV(Pageable pageable);
    Page<CashAdvance> findAllForCVByQuery(String query, Pageable pageable);
    PostResponse setAsLiquidated(Integer id);

    PostResponse confirmBudgetLineItem(CashAdvance cashAdvance);

    Page<CvVoucherDto> findAllApprovedForCvPaged(String query, Pageable pageable);

    Page<CashAdvance> findAllForLiquidation(Pageable pageable);
    Page<CashAdvance> findAllForLiquidationByQuery(String query, Pageable pageable);

    Page<CashAdvance> findAllForPO(Pageable pageable);
    Page<CashAdvance> findAllForPOByQuery(String query, Pageable pageable);

    List<CashAdvance> unliquidatedList();

    @Transactional
    boolean isDocumentForCashFlowItemAssignment(Integer transactionId);

    @Transactional
    PostResponse saveCashFlowItem(CashFlowItemDto dto, BindingResult bindingResult, MessageSource messageSource);

    List<CashAdvanceBudgetDetail> getCashAdvanceBudgetDetail(Integer caId);
}
