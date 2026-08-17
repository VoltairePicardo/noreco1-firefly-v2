package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.DocumentNoApproval;
import com.noreco1.fireflyv2.model.JoAcceptance;
import com.noreco1.fireflyv2.controller.response.*;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by Personal on 7/7/2015.
 */
public interface JoAcceptanceService extends VoucherService {
    public JoAcceptance findByCode(String code);

    @Transactional(readOnly = true)
    public JoAcceptanceDto findById(Integer joaId);

    @Transactional
    public List<JoAcceptanceListDto> findAll();

    @Transactional
    public List<JoAcceptanceListDto> findByPayReq();

    @Transactional(readOnly = true)
    public Map findByApvId(Integer apvId);

    @Transactional(readOnly = true)
    List<Map> findByDateRangeAndStatusId(String from, String to, Integer id);

    @Transactional(readOnly = true)
    List<Map> findByDateRangePending(String from, String to);

    @Transactional
    PostResponse processUpdate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse processCreate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse processCreate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request);

    @Transactional
    PostResponse processUpdate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> filesToRemove);

    Page<ApvPurchasingDocumentDto> findAllApprovedForApvPaged(String query, Pageable pageable);

    Page<CvVoucherDto> findAllApprovedForCvPaged(String query, Pageable pageable);

}
