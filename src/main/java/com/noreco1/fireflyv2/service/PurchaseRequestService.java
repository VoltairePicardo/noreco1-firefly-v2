package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.ModeOfProcurement;
import com.noreco1.fireflyv2.model.PurchaseRequest;
import com.noreco1.fireflyv2.controller.response.*;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

/**
 * Created by Personal on 3/20/2015.
 */
public interface PurchaseRequestService extends VoucherService {
    public PurchaseRequest findByCode(String code);

    @Transactional(readOnly = true)
    public RvDto findByRvId(Integer rvId);

    @Transactional
    public List<RvListDto> findAllRestricted();

    @Transactional
    public List<RvListDto> findAll();

    @Transactional
    List<RvListDto> findAllWithQuotations();

    @Transactional
    public List<RvListDto> getRequisitionVoucherForCanvass(Integer[] canvassIds);

    @Transactional
    public List<RvListDto> getRequisitionVoucherForPO();

    @Transactional(readOnly = true)
    List<Map> findByDateRangeAndStatusId(String from, String to, Integer docStatusId);

    @Transactional(readOnly = true)
    List<Map> findByDateRangePending(String from, String to);

    List<ModeOfProcurement> modesOfProcurement(int rvId);

    @Transactional
    PostResponse setModeOfProcurement(SetModeOfProcurementDto postData, BindingResult bindingResult, MessageSource messageSource);

    Page<Object[]> getRequisitionVoucherForStockWithdrawal(String query, Integer invLocId, Pageable pageable);

    Page<Object[]> getRequisitionVoucherForRR(String query, Pageable pageable);

    List<PurchaseRequest> getPurchaseRequestForPOBudgetAmountBalance();

    List<RvListDto> getPurchaseRequestForCanvass();

}
