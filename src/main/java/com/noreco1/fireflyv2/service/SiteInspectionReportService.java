package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.DocumentNoApproval;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.SiteInspectionReport;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface SiteInspectionReportService {
    Page<SiteInspectionReport> findAll(String from, String to, Pageable pageable);
    Page<SiteInspectionReport> findAll(String from, String to, int statusId, Pageable pageable);
    Page<SiteInspectionReport> findAll(String from, String to, String query, Pageable pageable);
    Page<SiteInspectionReport> findAll(String from, String to, int statusId, String query, Pageable pageable);

    List<DocumentStatus> getDocumentsStatuses();
    SiteInspectionReport findById(Integer id);

    @Transactional
    PostResponse processUpdate(DocumentNoApproval entity, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse processCreate(DocumentNoApproval entity, BindingResult bindingResult, MessageSource messageSource);

    @Transactional(readOnly = true)
    Map defaultSignatories();

    @Transactional
    PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    void logNewValue(Integer logId);

    @Transactional
    PostResponse processCreate(DocumentNoApproval entity, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request);

    @Transactional
    PostResponse processUpdate(DocumentNoApproval entity, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> filesToRemove);

    List<Map> findDetailBySiteInspectionReportTransId(Integer transId);

}
