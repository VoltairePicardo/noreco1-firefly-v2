package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.DocumentNoApproval;
import com.noreco1.fireflyv2.model.PaymentRequest;
import com.noreco1.fireflyv2.model.PaymentRequestBudgetDetail;
import com.noreco1.fireflyv2.controller.response.ApvPurchasingDocumentDto;
import com.noreco1.fireflyv2.controller.response.PayReqDto;
import com.noreco1.fireflyv2.controller.response.PayReqListDto;
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
 * Created by Personal on 7/21/2015.
 */
public interface PaymentRequestService extends VoucherService {
    public PaymentRequest findOneByCode(String code);

    @Transactional(readOnly = true)
    public PayReqDto findById(Integer joaId);

    @Transactional(readOnly = true)
    public List<PayReqListDto> findAll();

    @Transactional(readOnly = true)
    public List<Map> findAllApprovedForApv();

    @Transactional(readOnly = true)
    public Map findByApvId(Integer apvId);

    @Transactional(readOnly = true)
    List<Map> findByDateRangeAndStatusId(String from, String to, Integer id);

    @Transactional(readOnly = true)
    List<Map> findByDateRangePending(String from, String to);

    Page<ApvPurchasingDocumentDto> findAllApprovedForApvPaged(String query, Pageable pageable);

    @Transactional
    PostResponse processUpdate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse processCreate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse processCreate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request);

    @Transactional
    PostResponse processUpdate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> filesToRemove);

    List<PaymentRequestBudgetDetail> getPaymentRequestBudgetDetail(Integer prId);
}