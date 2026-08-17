package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.DocumentNoApproval;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.ProjectAcceptanceReport;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.controller.response.ProjectAcceptanceReportDto;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

/**
 * Created by Tri-Nvent on 10/28/2019.
 */
public interface ProjectAcceptanceReportService{

    Page<ProjectAcceptanceReport> findAll(String startDate, String endDate, Pageable pageable);
    Page<ProjectAcceptanceReport> findAllByStatusId(String startDate, String endDate, int statusId, Pageable pageable);

    ProjectAcceptanceReportDto findById(Integer id);

    @Transactional
    PostResponse processUpdate(DocumentNoApproval document, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse processCreate(DocumentNoApproval document, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource);

    List<DocumentStatus> getDocumentsStatuses();

    @Transactional(readOnly = true)
    Map defaultSignatories();

}
