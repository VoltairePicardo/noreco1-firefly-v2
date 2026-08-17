package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.JobOrder;
import com.noreco1.fireflyv2.model.JobOrderBudgetDetail;
import com.noreco1.fireflyv2.model.PurchaseOrderBudgetDetail;
import com.noreco1.fireflyv2.controller.response.CashFlowItemDto;
import com.noreco1.fireflyv2.controller.response.JoDto;
import com.noreco1.fireflyv2.controller.response.JoListDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

/**
 * Created by Personal on 6/18/2015.
 */
public interface JobOrderService extends VoucherService {
    public JobOrder findOneByCode(String code);

    @Transactional(readOnly = true)
    public JoDto findById(Integer canvassId);

    @Transactional
    public List<JoListDto> findAll();

    @Transactional
    public List<Map> findJobOrderSuppliersByStatus(Integer statusId);

    @Transactional
    public List<JoListDto> findBySupplierAccountNo(Integer accountNo);

    @Transactional
    public List<JoListDto> findForCV();

    @Transactional(readOnly = true)
    List<Map> findByDateRangeAndStatusId(String from, String to, Integer id);

    @Transactional(readOnly = true)
    List<Map> findByDateRangePending(String from, String to);

    Page<JobOrder> findByStatusAndFilter(Integer statusId, String filter, Pageable pageable);

    public List<JobOrderBudgetDetail> getJobOrderBudgetDetail(Integer joId);

    @Transactional
    boolean isDocumentForCashFlowItemAssignment(Integer transactionId);

    @Transactional
    PostResponse saveCashFlowItem(CashFlowItemDto dto, BindingResult bindingResult, MessageSource messageSource);

    Page<JobOrder> findAllForCreditCardPurchaseRequestByStatusAndFilter(Integer statusId, String filter, Pageable pageable);
}
