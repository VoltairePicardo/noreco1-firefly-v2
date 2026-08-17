package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.WorkflowAction;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.ProjectAcceptanceCertificationService;
import com.noreco1.fireflyv2.validator.ProjectAcceptanceCertificationValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.hibernate.annotations.Check;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by Tri-Nvent on 11/4/2019.
 */
@Service(value = "projectAcceptanceCertificationServiceImpl")
public class ProjectAcceptanceCertificationServiceImpl implements ProjectAcceptanceCertificationService, PrintableVoucher {

    private ProjectAcceptanceCertification model;

    @Autowired
    ProjectAcceptanceCertificationRepo certificationRepo;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    SignatureFacade signatureFacade;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    Environment env;

    @Autowired
    SlEntityRepo slEntityRepo;

    @Autowired
    DocumentDtoer documentDtoer;

    @Autowired
    ProjectAcceptanceReportRepo projectAcceptanceReportRepo;

    @Autowired
    CostEstimateRepo costEstimateRepo;

    @Autowired
    WorkOrderRepo workOrderRepo;

    @Autowired
    ProjectContractorRepo projectContractorRepo;

    @Override
    public Page<ProjectAcceptanceCertification> findByDateRangeAndStatusId(String from, String to, Integer id, Pageable pageable) {

        Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
        Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

        return certificationRepo.findByDocumentStatusIdAndDateBetween(id, fromDate, toDate, pageable);
    }

    @Override
    public Page<ProjectAcceptanceCertification> findByDateRangePending(String from, String to, Pageable pageable) {

        Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
        Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

        return certificationRepo.findByDateBetweenAndDocumentStatusIdNotIn(fromDate, toDate, Arrays.asList(this.getNonPendingStatusIds()), pageable);
    }

    @Override
    public ProjectAcceptanceCertificationDto findById(Integer id) {
        ProjectAcceptanceCertification projectAcceptanceCertification =  certificationRepo.findById(id).orElse(null);
        ProjectAcceptanceCertificationDto projectAcceptanceCertificationDto = new ProjectAcceptanceCertificationDto();

        if(projectAcceptanceCertification != null){

            SlEntity approvingOfficer = slEntityRepo.findById(projectAcceptanceCertification.getApprovingOfficer().getAccountNo()).orElse(null);
            SlEntity recommendedBy = slEntityRepo.findById(projectAcceptanceCertification.getRecommendedBy().getAccountNo()).orElse(null);
            SlEntity createdBy = slEntityRepo.findById(projectAcceptanceCertification.getCreatedBy().getAccountNo()).orElse(null);

            Project project = new Project();
            project.setId(projectAcceptanceCertification.getProject().getId());
            project.setName(projectAcceptanceCertification.getProject().getName());
            project.setLocation(projectAcceptanceCertification.getProject().getLocation());
            project.setPurpose(projectAcceptanceCertification.getProject().getPurpose());
            project.setCode(projectAcceptanceCertification.getProject().getCode());
            project.setDate(projectAcceptanceCertification.getProject().getDate());
            CostEstimate costEstimate = costEstimateRepo.findTop1ByProjectId(project.getId());
            if(costEstimate != null) {
                project.setCostEstimateCode(costEstimate.getCode());
            }

            WorkOrder workOrder = workOrderRepo.findTop1ByProjectId(project.getId());
            if(workOrder != null) {
                project.setWorkOrderCode(workOrder.getCode());
            }

            projectAcceptanceCertificationDto.setId(projectAcceptanceCertification.getId());
            projectAcceptanceCertificationDto.setCode(projectAcceptanceCertification.getCode());
            projectAcceptanceCertificationDto.setDate(projectAcceptanceCertification.getDate());
            projectAcceptanceCertificationDto.setProject(project);
            projectAcceptanceCertificationDto.setDocumentStatus(projectAcceptanceCertification.getDocumentStatus());
            projectAcceptanceCertificationDto.setTransId(projectAcceptanceCertification.getTransaction().getId());
            projectAcceptanceCertificationDto.setApprovedBy(approvingOfficer);
            projectAcceptanceCertificationDto.setRecommendedBy(recommendedBy);
            projectAcceptanceCertificationDto.setCreatedBy(createdBy);
            projectAcceptanceCertificationDto.setCreated(projectAcceptanceCertification.getCreatedAt());
            projectAcceptanceCertificationDto.setLastUpdated(projectAcceptanceCertification.getUpdatedAt());

        }

        return projectAcceptanceCertificationDto;
    }

    @Override
    public PostResponse processCreate(Document document, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostRoleResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        try {

            ProjectAcceptanceCertification projectAcceptanceCertification = (ProjectAcceptanceCertification) document;

            ProjectAcceptanceCertificationValidator validator = new ProjectAcceptanceCertificationValidator();
            validator.setService(this);
            validator.validate(projectAcceptanceCertification, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
                response.setSuccess(false);
            } else {

                Map oldProjectCertificationMap = null;
                boolean updateMode = Checker.isValidId(projectAcceptanceCertification.getId());

                User approvingOfficer = userRepo.findOneByAccountNo(projectAcceptanceCertification.getApprovingOfficer().getAccountNo());
                User recommendedBy = userRepo.findOneByAccountNo(projectAcceptanceCertification.getRecommendedBy().getAccountNo());

                projectAcceptanceCertification.setApprovingOfficer(approvingOfficer);
                projectAcceptanceCertification.setRecommendedBy(recommendedBy);

                if(updateMode){
                    this.model = certificationRepo.findById(projectAcceptanceCertification.getId()).orElse(null);
                    if(this.model != null) {

                        if(this.model.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId() &&
                                this.model.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.RETURNED_TO_CREATOR.getId()) {
                            response.setFailureMessage("Action is not allowed");
                            return response;
                        }

                        oldProjectCertificationMap = documentLoggerFacade.makeLog(this.model);

                        this.model.setDate(projectAcceptanceCertification.getDate());
                        this.model.setProject(projectAcceptanceCertification.getProject());
                        this.model.setApprovingOfficer(projectAcceptanceCertification.getApprovingOfficer());
                        this.model.setRecommendedBy(projectAcceptanceCertification.getRecommendedBy());

                        this.model = certificationRepo.save(this.model);

                        if(this.model != null) {
                            response.setSuccessMessage("Project Acceptance Certification successfully updated.");
                        } else {
                            response.setFailureMessage("Failed to update Project Acceptance Certification");
                        }
                    } else {
                        response.setFailureMessage("Project Acceptance Certification is not available.");
                    }

                } else {

                    Integer projectYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(projectAcceptanceCertification.getDate()));
                    Object latestCode = certificationRepo.findLatestCodeByYear(projectYear);

                    String code = generatorFacade.voucherCodeNoOffice("PCA", (latestCode == null ? "" : String.valueOf(latestCode)), projectAcceptanceCertification.getDate(), GlobalConstant.COUNTER_PAD_4);

                    projectAcceptanceCertification.setCode(code);

                    Workflow workflow = new Workflow();
                    workflow.setId(com.noreco1.fireflyv2.model.enums.Workflow.PROJECT_ACCEPTANCE_CERTIFICATION.getId());

                    projectAcceptanceCertification.setDocumentStatus(ServiceUtil.getDocumentStatusModel(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED));
                    projectAcceptanceCertification.setCreatedBy(authenticationFacade.getLoggedIn());
                    projectAcceptanceCertification.setTransaction(generatorFacade.transaction());
                    projectAcceptanceCertification.setWorkflow(workflow);

                    this.model = certificationRepo.save(projectAcceptanceCertification);

                    if(this.model != null){
                        //For Updating Project Acceptance Report Status to Certified
                        ProjectAcceptanceReport projectAcceptanceReport = projectAcceptanceReportRepo.findByProjectId(this.model.getProject().getId());
                        if(projectAcceptanceReport != null){
                            this.processProjectAcceptanceReport(projectAcceptanceReport);
                        }
                    }

                    response.setSuccessMessage("Project Acceptance Certification successfully saved.");
                }

                if(this.model != null) {

                    if(!updateMode) {
                        this.model.setCreatedAt(new Date());
                        this.model.setUpdatedAt(new Date());
                    }

                    //update default signatories
                    signatoryFacade.projectAcceptanceCertification(this.model);

                    if(!updateMode) {  // document processing logging only when adding document
                        documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), this.model.getCreatedBy());
                    }

                    Map newProjectMap =  documentLoggerFacade.makeLog(this.model);

                    documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldProjectCertificationMap, newProjectMap);

                    response.setModelId(this.model.getId());
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return response;
    }

    @Override
    public PostResponse processUpdate(Document document, BindingResult bindingResult, MessageSource messageSource) {
        return this.processCreate(document, bindingResult, messageSource);
    }

    @Override
    public HashMap reportParameters(Integer id, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        ProjectAcceptanceCertification projectAcceptanceCertification = certificationRepo.findById(id).orElse(null);

        if (projectAcceptanceCertification != null) {

            ProjectAcceptanceReport projectAcceptanceReport = projectAcceptanceReportRepo.findByProjectId(projectAcceptanceCertification.getProject().getId());

            String contractor = "";
            List<ProjectContractor> contractors = projectContractorRepo.findAllByProjectId(projectAcceptanceCertification.getProject().getId());
            if(!contractors.isEmpty()){

                for(ProjectContractor cont : contractors){

                    if(Checker.isStringNullOrEmpty(contractor)){
                        contractor += cont.getSupplier().getAccountNumber() + " - " + cont.getSupplier().getName();
                    } else {
                        contractor += "\n"+cont.getSupplier().getAccountNumber() + " - " + cont.getSupplier().getName();
                    }

                }

            }

            params.put("CERTIFICATION_DATE", projectAcceptanceCertification.getDate());
            params.put("PROJECT_NAME", projectAcceptanceCertification.getProject().getName());
            params.put("PROJECT_LOCATION", projectAcceptanceCertification.getProject().getLocation());
            params.put("ACCEPTANCE_REPORT_DATE", projectAcceptanceReport.getDate());
            params.put("CONTRACTOR", (Checker.isStringNullOrEmpty(contractor) ? "" : contractor +"\n"  ) + (projectAcceptanceCertification.getProject().getProjectManager() == null ? "" : projectAcceptanceCertification.getProject().getProjectManager()));
            params.put("CODE", projectAcceptanceCertification.getCode());

            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.PROJECT_ACCEPTANCE_CERTIFICATION, projectAcceptanceCertification);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer vid) {
        List<ProjectAcceptanceCertification> data = certificationRepo.findAll();
        return new JRBeanCollectionDataSource(data);
    }

    @Override
    public List<DocumentStatus> getDocumentsStatuses() {
        ProjectAcceptanceCertification projectAcceptanceCertification = certificationRepo.findFirstByOrderByIdAsc();
        if (projectAcceptanceCertification != null) {
            return documentDtoer.getDocumentStatuses(projectAcceptanceCertification.getWorkflow().getId());
        }

        return null;
    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        ProjectAcceptanceCertification projectAcceptanceCertification =  certificationRepo.findById(postData.getDocumentId()).orElse(null);

        if (projectAcceptanceCertification != null && projectAcceptanceCertification.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId()) {
            // for logging
            Map oldMap = this.forLogMapMain(projectAcceptanceCertification);
            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(projectAcceptanceCertification, projectAcceptanceCertification.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            projectAcceptanceCertification.setDocumentStatus(afterActionDocumentStatus);
            projectAcceptanceCertification.setUpdatedAt(null);
            projectAcceptanceCertification = certificationRepo.save(projectAcceptanceCertification);

            // for logging
            Map newMap = this.forLogMapMain(projectAcceptanceCertification);
            newMap.put("remarks", postData.getRemarks());

            if (projectAcceptanceCertification != null) {
                documentProcessingFacade.processAction(projectAcceptanceCertification.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(projectAcceptanceCertification.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, newMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }

        } else {
            response.setFailureMessage("Action is not allowed");
        }
        return response;
    }

    @Override
    public Map defaultSignatories() {
        return  signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.PROJECT_ACCEPTANCE_CERTIFICATION);
    }

    private Map forLogMapMain(ProjectAcceptanceCertification projectAcceptanceCertification) {
        return documentLoggerFacade.makeLog(projectAcceptanceCertification);
    }

    private void processProjectAcceptanceReport(ProjectAcceptanceReport projectAcceptanceReport){
        User processedBy = authenticationFacade.getLoggedIn();

        if (projectAcceptanceReport != null && projectAcceptanceReport.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.CERTIFIED.getId()) {

            // for logging
            Map oldMap = documentLoggerFacade.makeLog(projectAcceptanceReport);
            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findByWorkflowIdAndWorkflowActionId(projectAcceptanceReport.getWorkflow().getId(), WorkflowAction.CERTIFY.getId()); //Certify Document
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(projectAcceptanceReport, projectAcceptanceReport.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            projectAcceptanceReport.setDocumentStatus(afterActionDocumentStatus);
            projectAcceptanceReport.setUpdatedAt(null);
            projectAcceptanceReport = projectAcceptanceReportRepo.save(projectAcceptanceReport);

            // for logging
            Map newMap = documentLoggerFacade.makeLog(projectAcceptanceReport);

            if (projectAcceptanceReport != null) {
                documentProcessingFacade.processAction(projectAcceptanceReport.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(projectAcceptanceReport.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, newMap);
            }

        }
    }

    private Integer[] getNonPendingStatusIds() {

        Integer[] ids = {
                com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(),
                com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId(),
                com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId()
        };

        return ids;
    }

}
