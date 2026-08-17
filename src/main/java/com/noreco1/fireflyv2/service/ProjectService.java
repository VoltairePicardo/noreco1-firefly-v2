package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.DocumentNoApproval;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Project;
import com.noreco1.fireflyv2.model.ProjectFunding;
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

public interface ProjectService {
    Page<Project> findAll(Pageable pageable);
    Page<Project> findAll(int statusId, Pageable pageable);
    Page<Project> findAll(String query, Pageable pageable);
    Page<Project> findAll(int statusId, String query, Pageable pageable);
    Page<Project> findAll(String from, String to, Pageable pageable);
    Page<Project> findAll(String from, String to, int statusId, Pageable pageable);
    Page<Project> findAll(String from, String to, String query, Pageable pageable);
    Page<Project> findAll(String from, String to, int statusId, String query, Pageable pageable);

    Project findById(Integer id);

    @Transactional
    PostResponse processUpdate(Project entity, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse processCreate(Project entity, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse processCreate(DocumentNoApproval entity, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request);

    @Transactional
    PostResponse processUpdate(DocumentNoApproval entity, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> filesToRemove);

    @Transactional
    void logNewValue(Integer logId);

    List<DocumentStatus> getDocumentsStatuses();
    List<ProjectFunding> getProjectFunding();

    Page<Object[]> findForWorkOrderBrowser(String query, Pageable pageable);

    Page<Object[]> findWithApprovedCostEstimate(String query, Pageable pageable);

    Page<Object[]> findForCostEstimate(String query, Pageable pageable);
    Page<Object[]> findForBillOfMaterial(String query, Pageable pageable);

    @Transactional
    PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request);

    @Transactional
    PostResponse process2(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request);

    Map checkHasApprovedCostEstimate(Integer projectId);
    Map checkHasWorkOrderCreated(Integer projectId);
    Map checkHasUploadedAsBuiltStakingSheetAndAsBuiltMaterials(Integer projectId);
    Map checkHasAcceptanceReportAndCertificateOfAcceptance(Integer projectId);

    Page<Project> findAllForAcceptance(Pageable pageable);
    Page<Project> findAllForAcceptanceByQuery(String query, Pageable pageable);

    Page<Project> findAllForCertification(Pageable pageable);
    Page<Project> findAllForCertificationByQuery(String query, Pageable pageable);
    Page<Object[]> findForSiteInspectionProjectBrowser(String query, Pageable pageable);

    PostResponse confirmProjectType(Project project);

}
