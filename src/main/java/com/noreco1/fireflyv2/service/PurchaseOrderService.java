package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.Document;
import com.noreco1.fireflyv2.model.PurchaseOrder;
import com.noreco1.fireflyv2.controller.response.CashFlowItemDto;
import com.noreco1.fireflyv2.controller.response.PoDto;
import com.noreco1.fireflyv2.controller.response.PoListDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.reports.PODetail;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

/**
 * Created by Personal on 5/15/2015.
 */
public interface PurchaseOrderService extends VoucherService {
    public PurchaseOrder findByCode(String code);

    @Transactional(readOnly = true)
    public PoDto findById(Integer canvassId);

    @Transactional
    public List<PoListDto> findAll();

    @Transactional(readOnly = true)
    List<Map> findByDateRangeAndStatusId(String from, String to, Integer id);

    @Transactional(readOnly = true)
    List<Map> findByDateRangePending(String from, String to);

    Page<PurchaseOrder> findByStatusAndFilter(Integer statusId, String filter, Pageable pageable);

    @Transactional(readOnly = true)
    List<PurchaseOrder> findByStatusId(Integer statusId);

    @Transactional(readOnly = true)
    List<Map> findByDateRangeAndStatusIdAndForEditing(String from, String to, Integer id, Integer officeId);

    List<PODetail> poDetails(Integer id);
    Map reportMeta();

    Page<PurchaseOrder> findPurchaseOrderForItemTestingByStatusAndFilter(Integer statusId, String filter, Pageable pageable);

    Page<PurchaseOrder> findPurchaseOrderWithItemTestingForRRByFilter(String filter, Pageable pageable);

    @Transactional
    public List<PoListDto> findForCV();

    @Transactional
    boolean isDocumentForCashFlowItemAssignment(Integer transactionId);

    @Transactional
    PostResponse saveCashFlowItem(CashFlowItemDto dto, BindingResult bindingResult, MessageSource messageSource);

    Page<PurchaseOrder> findAllForCreditCardPurchaseRequestByStatusAndFilter(Integer statusId, String filter, Pageable pageable);

    @Transactional
    PostResponse processSupplierReceived(Document v, BindingResult bindingResult, MessageSource messageSource);

    @Transactional(readOnly = true)
    List<Map> findApprovedVendors();

    @Transactional(readOnly = true)
    List<PoListDto> findBySupplierAccountNo(Integer accountNo);
}
