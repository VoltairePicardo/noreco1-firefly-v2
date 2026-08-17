package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.Document;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.ProjectAcceptanceCertification;
import com.noreco1.fireflyv2.model.ProjectAcceptanceReport;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.controller.response.ProjectAcceptanceCertificationDto;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

/**
 * Created by Tri-Nvent on 11/4/2019.
 */
public interface ProjectAcceptanceCertificationService {

    Page<ProjectAcceptanceCertification> findByDateRangeAndStatusId(String from, String to, Integer id, Pageable pageable);

    Page<ProjectAcceptanceCertification> findByDateRangePending(String from, String to, Pageable pageable);

    ProjectAcceptanceCertificationDto findById(Integer id);

    @Transactional
    PostResponse processUpdate(Document document, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse processCreate(Document document, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource);

    List<DocumentStatus> getDocumentsStatuses();

    @Transactional(readOnly = true)
    Map defaultSignatories();

}
