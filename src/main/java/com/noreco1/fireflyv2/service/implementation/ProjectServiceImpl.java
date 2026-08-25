package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.model.enums.WorkflowAction;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.PostRoleResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.service.ProjectService;
import com.noreco1.fireflyv2.validator.ProjectValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    FileFacade fileFacade;

    private Project model;

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    ProjectRepo projectRepo;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    ProjectFundingRepo projectFundingRepo;

    @Autowired
    SupplierRepo supplierRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    DocumentDtoer documentDtoer;

    @Autowired
    CostEstimateRepo costEstimateRepo;

    @Autowired
    BillOfMaterialRepo billOfMaterialRepo;

    @Autowired
    WorkOrderRepo workOrderRepo;

    @Autowired
    DocumentFileRepo documentFileRepo;

    @Autowired
    ProjectAcceptanceReportRepo projectAcceptanceReportRepo;

    @Autowired
    ProjectAcceptanceCertificationRepo projectAcceptanceCertificationRepo;

    @Autowired
    SiteInspectionReportRepo siteInspectionReportRepo;

    @Autowired
    AssetRepo assetRepo;

    @Autowired
    ProjectContractorRepo projectContractorRepo;

    @Override
    @Transactional(readOnly = true)
    public Page<Project> findAll(Pageable pageable) {
        Integer[] ids = {
                com.noreco1.fireflyv2.model.enums.DocumentStatus.CLOSED_OUT.getId(),
        };
        return projectRepo.findByDocumentStatusIdNotIn(Arrays.asList(ids), pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Project> findAll(int statusId, Pageable pageable) {
        return projectRepo.findByDocumentStatusId(statusId, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Project> findAll(String filter, Pageable pageable) {
        Integer[] ids = {
                com.noreco1.fireflyv2.model.enums.DocumentStatus.CLOSED_OUT.getId(),
        };
        filter = "%"+filter+"%";
        return projectRepo.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrLocationContainingIgnoreCaseAndDocumentStatusIdNotIn(filter, Arrays.asList(ids), pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Project> findAll(int statusId, String query, Pageable pageable) {
        query = "%"+query+"%";
        return projectRepo.findByStatusIdAndQuery(statusId, query, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Project> findAll(String from, String to, Pageable pageable) {
        Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
        Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");
        Integer[] ids = {
                com.noreco1.fireflyv2.model.enums.DocumentStatus.CLOSED_OUT.getId(),
        };
        return projectRepo.findByDateBetweenAndDocumentStatusIdNotIn(fromDate, toDate, Arrays.asList(ids), pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Project> findAll(String from, String to, int statusId, Pageable pageable) {
        Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
        Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");
        return projectRepo.findByDateBetweenAndDocumentStatusId(fromDate, toDate, statusId, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Project> findAll(String from, String to, String filter, Pageable pageable) {
        Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
        Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");
        Integer[] ids = {
                com.noreco1.fireflyv2.model.enums.DocumentStatus.CLOSED_OUT.getId(),
        };
        filter = "%"+filter+"%";
        return projectRepo.findByDateBetweenAndCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrLocationContainingIgnoreCaseAndDocumentStatusIdNotIn(fromDate, toDate, filter, Arrays.asList(ids), pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Project> findAll(String from, String to, int statusId, String query, Pageable pageable) {
        Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
        Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");
        query = "%"+query+"%";
        return projectRepo.findByDateBetweenAndStatusIdAndQuery(fromDate, toDate, statusId, query, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Project findById(Integer id) {

        Project project = projectRepo.findById(id).orElse(null);

        try {

            if (project != null){

                List<ProjectContractor> contractors = projectContractorRepo.findAllByProjectId(project.getId());
                if(!contractors.isEmpty()){
                    List<ProjectContractor> newContractors = new ArrayList<>();

                    String contractorsStr = "";

                    for(ProjectContractor contractor : contractors){
                        ProjectContractor newContractor = new ProjectContractor();

                        if(Checker.isStringNullOrEmpty(contractorsStr)){
                            contractorsStr += contractor.getSupplier().getAccountNumber() + " - " + contractor.getSupplier().getName();
                        } else {
                            contractorsStr += "\n"+contractor.getSupplier().getAccountNumber() + " - " + contractor.getSupplier().getName();
                        }

                        newContractor.setId(contractor.getId());
                        newContractor.setSupplier(contractor.getSupplier());
                        newContractors.add(newContractor);
                    }
                    project.setContractors(newContractors);
                    project.setContractorsStr(contractorsStr);
                }

                CostEstimate costEstimate = costEstimateRepo.findTop1ByProjectId(project.getId());

                if (costEstimate != null){
                    project.setCostEstimateCode(costEstimate.getCode());
                    project.setType(costEstimate.getType());
                }

                BillOfMaterial billOfMaterial = billOfMaterialRepo.findTop1ByProjectId(project.getId());

                if (billOfMaterial != null){
                    project.setBillOfMaterialCode(billOfMaterial.getCode());
                }

                WorkOrder workOrder = workOrderRepo.findTop1ByProjectId(project.getId());

                if (workOrder != null){
                    project.setWorkOrderCode(workOrder.getCode());
                }

                SiteInspectionReport siteInspectionReport = siteInspectionReportRepo.findByProjectId(project.getId());
                ProjectAcceptanceReport projectAcceptanceReport = projectAcceptanceReportRepo.findByProjectId(project.getId());
                ProjectAcceptanceCertification projectAcceptanceCertification = projectAcceptanceCertificationRepo.findByProjectId(project.getId());
                Asset asset = assetRepo.findByWorkOrderProjectId(project.getId());

                if (siteInspectionReport != null) {
                    project.setSiteInspectionReportCode(siteInspectionReport.getCode());
                }
                if (projectAcceptanceReport != null) {
                    project.setProjectAcceptanceReportCode(projectAcceptanceReport.getCode());
                }
                if (projectAcceptanceCertification != null) {
                    project.setProjectAcceptanceCertificationCode(projectAcceptanceCertification.getCode());
                }
                if (asset != null) {
                    project.setAssetCode(asset.getRefNo());
                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return project;

    }

    @Override
    @Transactional
    public PostResponse processUpdate(Project project, BindingResult bindingResult, MessageSource messageSource) {
        return this.processCreate(project, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(Project project, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostRoleResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        try {

            ProjectValidator validator = new ProjectValidator();
            validator.setService(this);
            validator.validate(project, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
                response.setSuccess(false);
            } else {

                boolean editMode = Checker.isValidId(project.getId());

                if(editMode) {
                    this.model = projectRepo.findOneByCodeAndId(project.getCode(), project.getId());
                    if(this.model != null) {

                        if(this.model.getDocumentStatus().getId() == com.noreco1.fireflyv2.model.enums.DocumentStatus.CLOSED_OUT.getId()) {
                            response.setFailureMessage("Action is not allowed");
                            return response;
                        }

                        Map oldProjectMap =  documentLoggerFacade.makeLog(this.model);

                        // NOTE: date should not be updatable, it will affect Project code sequence

                        this.model.setLocation(project.getLocation());
                        this.model.setName(project.getName());
                        this.model.setPurpose(project.getPurpose());
                        this.model.setProjectManager(project.getProjectManager());
                        this.model.setPaymentDetails(project.getPaymentDetails());
                        this.model.setOffice(project.getOffice());
                        this.model.setDepartment(project.getDepartment());
                        this.model.setConsumerAccountNumber(project.getConsumerAccountNumber());
                        this.model.setConsumerName(project.getConsumerName());
                        this.model.setGovernmentOfficeName(project.getGovernmentOfficeName());

                        this.model = projectRepo.save(this.model);

                        if(this.model != null) {

                            projectContractorRepo.deleteByProjectId(project.getId());
                            saveContractors(this.model, project.getContractors());

                            DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldProjectMap, null);

                            response.setLogId(log != null ? log.getId() : 0);
                            response.setModelId(this.model.getId());

                            response.setSuccessMessage("Project successfully updated.");

                        } else {
                            response.setFailureMessage("Failed to update Project");
                        }

                    } else {
                        response.setFailureMessage("Project is not available.");
                    }
                } else {

                    project.setDate(new Date());

                    Integer projectYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(project.getDate()));
                    Object latestCode = projectRepo.findLatestCodeByYear(projectYear);

                    String code = generatorFacade.voucherCodeNoOffice("PROJ", (latestCode == null ? "" : String.valueOf(latestCode)), project.getDate(), GlobalConstant.COUNTER_PAD_4);
                    project.setCode(code);

                    project.setDocumentStatus(ServiceUtil.getDocumentStatusModel(com.noreco1.fireflyv2.model.enums.DocumentStatus.PROJECT_CREATED));
                    project.setWorkflow(ServiceUtil.getWorkflowModel(com.noreco1.fireflyv2.model.enums.Workflow.PROJECT));
                    project.setCreatedBy(authenticationFacade.getLoggedIn());
                    project.setYear(projectYear);
                    project.setTransaction(generatorFacade.transaction());

                    this.model = projectRepo.save(project);

                    if(this.model != null) {

                        // for logging only
                        this.model.setCreatedAt(new Date());
                        this.model.setUpdatedAt(new Date());

                        saveContractors(this.model, project.getContractors());

                        // document processing logging only when adding document
                        documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), this.model.getCreatedBy());

                        Map newLogMap =  documentLoggerFacade.makeLog(this.model);
                        DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), null, newLogMap);

                        response.setLogId(log != null ? log.getId() : 0);
                        response.setModelId(this.model.getId());

                        response.setSuccessMessage("Project successfully saved.");
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return response;
    }

    @Override
    public PostResponse processCreate(DocumentNoApproval document, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request) {
        PostResponse response = this.processCreate((Project) document, bindingResult, messageSource);

        if (request instanceof MultipartHttpServletRequest && this.model != null) {
            ServiceUtil.attachFiles(fileFacade, request, this.model.getTransaction(), ProjectAttachmentPrefix.BUILT_OF_MATERIALS.getPrefix());
            ServiceUtil.attachFiles(fileFacade, request, this.model.getTransaction(), ProjectAttachmentPrefix.BUILT_STAKING_SHEET.getPrefix());
            ServiceUtil.attachFiles(fileFacade, request, this.model.getTransaction(), ProjectAttachmentPrefix.PLANNED_STAKING_SHEET.getPrefix());
        }

        return response;
    }

    @Override
    public PostResponse processUpdate(DocumentNoApproval document, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> filesToRemove) {
        PostResponse response = this.processUpdate((Project) document, bindingResult, messageSource);

        if (request instanceof MultipartHttpServletRequest && this.model != null) {
            ServiceUtil.removeFiles(fileFacade, request, this.model.getTransaction(), filesToRemove);

            ServiceUtil.attachFiles(fileFacade, request, this.model.getTransaction(), ProjectAttachmentPrefix.BUILT_OF_MATERIALS.getPrefix());
            ServiceUtil.attachFiles(fileFacade, request, this.model.getTransaction(), ProjectAttachmentPrefix.BUILT_STAKING_SHEET.getPrefix());
            ServiceUtil.attachFiles(fileFacade, request, this.model.getTransaction(), ProjectAttachmentPrefix.PLANNED_STAKING_SHEET.getPrefix());
        }

        return  response;
    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            Project project = projectRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (project != null) {
                Map map = documentLoggerFacade.makeLog(project);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentStatus> getDocumentsStatuses() {
        Project project = projectRepo.findFirstByOrderByIdAsc();
        if (project != null && project.getWorkflow() != null) {
            return documentDtoer.getDocumentStatuses(project.getWorkflow().getId());
        }

        return null;
    }

    @Override
    public List<ProjectFunding> getProjectFunding() {
        return projectFundingRepo.findAll();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Object[]> findForWorkOrderBrowser(String query, Pageable pageable) {
        if (Checker.isStringNullAndEmpty(query)) {
            return projectRepo.findProjectWithOpenWorkOrder(pageable);
        } else {
            return projectRepo.findProjectWithOpenWorkOrder("%"+query+"%", pageable);
        }
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Object[]> findForSiteInspectionProjectBrowser(String query, Pageable pageable) {
        if (Checker.isStringNullAndEmpty(query)) {
            return projectRepo.findProjectForSiteInspection(pageable);
        } else {
            return projectRepo.findProjectForSiteInspection("%"+query+"%", pageable);
        }
    }

    @Override
    public PostResponse confirmProjectType(Project p) {
        PostResponse response = new PostRoleResponse();
        Project project = projectRepo.findById(p.getId()).orElse(null);

        if(project != null) {

            // use for document logging
            Map oldProjectMap =  documentLoggerFacade.makeLog(project);

            project.setProjectType(p.getProjectType());
            project.setPeriodCoveredFrom(p.getPeriodCoveredFrom());
            project.setPeriodCoveredTo(p.getPeriodCoveredTo());

            projectRepo.save(project);

            Map newProjectMap =documentLoggerFacade.makeLog(project);

            documentLoggerFacade.log(project.getTransaction(), authenticationFacade.getLoggedIn(), oldProjectMap, newProjectMap);

            response.setSuccessMessage("Project successfully reviewed.");

        } else  {
            response.setFailureMessage("Project is not available.");
        }

        return response;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Object[]> findWithApprovedCostEstimate(String query, Pageable pageable) {

        int docStatus = com.noreco1.fireflyv2.model.enums.DocumentStatus.PROJECT_STARTED.getId();

        if (Checker.isStringNullAndEmpty(query)) {
            return projectRepo.findProjectWithApprovedCostEstimate(docStatus, pageable);
        } else {
            return projectRepo.findProjectWithApprovedCostEstimate("%"+query+"%", docStatus, pageable);
        }
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Object[]> findForCostEstimate(String query, Pageable pageable) {
        if (Checker.isStringNullAndEmpty(query)) {
            return projectRepo.findProjectForCostEstimate(pageable);
        } else {
            return projectRepo.findProjectForCostEstimate("%"+query+"%", pageable);
        }
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Object[]> findForBillOfMaterial(String query, Pageable pageable) {
        if (Checker.isStringNullAndEmpty(query)) {
            return projectRepo.findProjectForBillOfMaterial(pageable);
        } else {
            return projectRepo.findProjectForBillOfMaterial("%"+query+"%", pageable);
        }
    }

    @Override
    @Transactional
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request) {
        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        Project project = projectRepo.findById(postData.getDocumentId()).orElse(null);

        if (project != null && project.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.CLOSED_OUT.getId()) {

            Map checkMap = new HashMap();
            // for logging
            Map oldProjectMap = documentLoggerFacade.makeLog(project);

            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // The project should have an approved cost estimate before:
            // Send for Work Order Creation
            if(actionMap.getWorkflowAction().getId().equals(WorkflowAction.SEND_FOR_WORK_ORDER_CREATION.getId())) {
                checkMap = this.checkHasApprovedCostEstimate(project.getId());
            } else

            // The project should have a work order created before:
            // Send for Start of Construction
            if(actionMap.getWorkflowAction().getId().equals(WorkflowAction.SEND_FOR_START_OF_CONSTRUCTION.getId())) {
                checkMap = this.checkHasWorkOrderCreated(project.getId());
            } else

            // The project should have an uploaded As Built Staking sheet and As Built Materials before:
            // Send for Site Inspection
            if(actionMap.getWorkflowAction().getId().equals(WorkflowAction.SEND_FOR_SITE_INSPECTION.getId())) {
                // checkMap = this.checkHasUploadedAsBuiltStakingSheetAndAsBuiltMaterials(project.getId());
            } else

            // The project should have an Acceptance Report and Certificate of Acceptance before:
            // Send for Final Inspection
            if(actionMap.getWorkflowAction().getId().equals(WorkflowAction.SEND_FOR_FINAL_INSPECTION.getId())) {
                checkMap = this.checkHasAcceptanceReportAndCertificateOfAcceptance(project.getId());
            }

            Object confirmedObj = checkMap.get("confirmed");
            if(confirmedObj != null) {

                boolean confirmed = (Boolean) confirmedObj;
                if(!confirmed) {
                    response.setFailureMessage("Action is not allowed.");
                    return response;
                }
            }

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(project, project.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            project.setDocumentStatus(afterActionDocumentStatus);
            project.setUpdatedAt(null);
            project = projectRepo.save(project);

            // save files
            if (request instanceof MultipartHttpServletRequest && this.model != null) {
                ServiceUtil.attachFiles(fileFacade, request, project.getTransaction(), ProjectAttachmentPrefix.OTHER_DOCUMENTS.getPrefix());
            }

            // for logging
            Map newProjectMap = documentLoggerFacade.makeLog(project);
            newProjectMap.put("remarks", postData.getRemarks());

            documentProcessingFacade.processAction(project.getTransaction(), actionMap, null, processedBy);
            documentLoggerFacade.log(project.getTransaction(), authenticationFacade.getLoggedIn(), oldProjectMap, newProjectMap);

            response.setSuccessMessage("Document successfully processed");
            response.setSuccess(true);

            this.model = project;
        }
        return response;
    }

    @Override
    public PostResponse process2(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request) {
        return this.process(postData, bindingResult, messageSource, request);
    }

    @Override
    public Map checkHasApprovedCostEstimate(Integer projectId) {
        Map data = new HashMap();

        CostEstimate costEstimate = costEstimateRepo.findTop1ByProjectIdAndDocumentStatusId(projectId, com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());
        data.put("confirmed", costEstimate != null);

        return data;
    }

    @Override
    public Map checkHasWorkOrderCreated(Integer projectId) {
        Map data = new HashMap();

        WorkOrder workOrder = workOrderRepo.findByProjectId(projectId);
        data.put("confirmed", workOrder != null);

        return data;
    }

    @Override
    public Map checkHasUploadedAsBuiltStakingSheetAndAsBuiltMaterials(Integer projectId) {
        Map data = new HashMap();
        data.put("confirmed", false);

        Project project = projectRepo.findById(projectId).orElse(null);
        if(project != null) {

            List<DocumentFile> materialsFiles = documentFileRepo.findByPrefixAndTransactionId(ProjectAttachmentPrefix.BUILT_OF_MATERIALS.getPrefix(), project.getTransaction().getId());
            List<DocumentFile> stakingFiles = documentFileRepo.findByPrefixAndTransactionId(ProjectAttachmentPrefix.BUILT_STAKING_SHEET.getPrefix(), project.getTransaction().getId());

            data.put("confirmed", Checker.collectionIsNotEmpty(materialsFiles) && Checker.collectionIsNotEmpty(stakingFiles));
        }

        return data;
    }

    @Override
    public Map checkHasAcceptanceReportAndCertificateOfAcceptance(Integer projectId) {
        Map data = new HashMap();
        data.put("confirmed", false);

        ProjectAcceptanceReport acceptanceReport = projectAcceptanceReportRepo.findByProjectId(projectId);
        ProjectAcceptanceCertification projectAcceptanceCertification = projectAcceptanceCertificationRepo.findByProjectId(projectId);

        data.put("confirmed", acceptanceReport != null && projectAcceptanceCertification != null);

        return data;
    }
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Project> findAllForAcceptance(Pageable pageable) {

        int statusId1 = com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_SITE_INSPECTION.getId();
        int statusId2 = com.noreco1.fireflyv2.model.enums.DocumentStatus.NOTED.getId();

        return projectRepo.findAllForAcceptance(statusId1, statusId2, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Project> findAllForAcceptanceByQuery(String query, Pageable pageable) {

        int statusId1 = com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_SITE_INSPECTION.getId();
        int statusId2 = com.noreco1.fireflyv2.model.enums.DocumentStatus.NOTED.getId();
        query = "%"+query+"%";

        return projectRepo.findAllForAcceptanceByQuery(statusId1, statusId2, query, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Project> findAllForCertification(Pageable pageable) {

        int statusId = com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_CERTIFICATION.getId();

        return projectRepo.findAllForCertificationNew(statusId, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Project> findAllForCertificationByQuery(String query, Pageable pageable) {

        int statusId = com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_CERTIFICATION.getId();
        query = "%"+query+"%";

        return projectRepo.findAllForCertificationByQueryNew(statusId, query, pageable);
    }

    private void saveContractors(Project savedProject, List<ProjectContractor> contractors){
        if(!contractors.isEmpty()){
            for(ProjectContractor contractor : contractors){

                Supplier supplier = supplierRepo.findOneByAccountNumber(contractor.getSupplier().getAccountNumber());

                contractor.setSupplier(supplier);
                contractor.setProject(savedProject);
                projectContractorRepo.save(contractor);

            }
        }
    }

}
