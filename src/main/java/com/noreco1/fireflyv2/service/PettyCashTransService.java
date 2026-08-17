package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.PettyCashBatch;
import com.noreco1.fireflyv2.model.PettyCashTrans;
import com.noreco1.fireflyv2.model.PettyCashTransBudgetDetail;
import com.noreco1.fireflyv2.controller.response.CashFlowItemDto;
import com.noreco1.fireflyv2.controller.response.PettyCashBatchDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ReplenishmentDto;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface PettyCashTransService extends VoucherService {

    HashMap findById(Integer id);

    List<HashMap> findAll();

    PettyCashBatch findByBatchStatus(Boolean status);

    PettyCashBatch findByBatchStatusAndOffice(Boolean status, int officeId);

    List<HashMap> findAllBatches();

    List<HashMap> findAllBatchesByAreaOffice();

    List<HashMap> findAllBatchesByAreaOfficeAndDateRange(String from, String to, Integer officeId);

    @Transactional
    PostResponse processBatch(PettyCashBatchDto pettyCashBatchDto, BindingResult bindingResult, MessageSource messageSource);

    PostResponse createBatch(PettyCashBatchDto pettyCashBatchDto);

    @Transactional
    PostResponse replenish(ReplenishmentDto replenishment);

    Page<Object[]> findAllForSummary(Integer batch, Integer documentStatusId, Integer officeId, Pageable pageable);

    Map defaultSignatoriesForSummary();

    BigDecimal findOtherAmounts();

    Page<PettyCashTrans> findByStatusAndFilter(Integer documentStatusId, String filter, Pageable pageable);

    @Transactional
    boolean isDocumentForCashFlowItemAssignment(Integer transactionId);

    @Transactional
    PostResponse saveCashFlowItem(CashFlowItemDto dto, BindingResult bindingResult, MessageSource messageSource);

    List<PettyCashTransBudgetDetail> getPettyCashTransBudgetDetail(Integer pcvId);

}
