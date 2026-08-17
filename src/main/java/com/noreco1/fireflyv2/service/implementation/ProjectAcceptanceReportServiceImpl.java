package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.model.enums.WorkflowAction;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.ProjectAcceptanceReportService;
import com.noreco1.fireflyv2.validator.ProjectAcceptanceReportValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by Tri-Nvent on 10/28/2019.
 */
@Service(value = "projectAcceptanceReportServiceImpl")
public class ProjectAcceptanceReportServiceImpl implements ProjectAcceptanceReportService, PrintableVoucher {

    @Autowired
    ProjectAcceptanceReportRepo projectAcceptanceReportRepo;

    private ProjectAcceptanceReport model;

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
    SlEntityRepo slEntityRepo;

    @Autowired
    DocumentDtoer documentDtoer;

    @Autowired
    CostEstimateRepo costEstimateRepo;

    @Autowired
    WorkOrderRepo workOrderRepo;

    @Autowired
    ProjectContractorRepo projectContractorRepo;

    @Override
    public Page<ProjectAcceptanceReport> findAll(String startDate, String endDate, Pageable pageable) {

        Date fromDate = DateHelper.strToDate(startDate, "yyyy-MM-dd");
        Date toDate = DateHelper.strToDate(endDate, "yyyy-MM-dd");

        return projectAcceptanceReportRepo.findAllByDateBetween(fromDate, toDate, pageable);
    }

    @Override
    public Page<ProjectAcceptanceReport> findAllByStatusId(String startDate, String endDate, int statusId, Pageable pageable) {

        Date fromDate = DateHelper.strToDate(startDate, "yyyy-MM-dd");
        Date toDate = DateHelper.strToDate(endDate, "yyyy-MM-dd");

        return projectAcceptanceReportRepo.findAllByDateBetweenAndDocumentStatusId(fromDate, toDate, statusId, pageable);
    }

    @Override
    public ProjectAcceptanceReportDto findById(Integer id) {

        ProjectAcceptanceReport projectAcceptanceReport =  projectAcceptanceReportRepo.findById(id).orElse(null);
        ProjectAcceptanceReportDto projectAcceptanceReportDto = new ProjectAcceptanceReportDto();

        if(projectAcceptanceReport != null){

            SlEntity inspector1 = slEntityRepo.findById(projectAcceptanceReport.getInspector1().getAccountNo()).orElse(null);
            SlEntity inspector2 = slEntityRepo.findById(projectAcceptanceReport.getInspector2().getAccountNo()).orElse(null);
            SlEntity inspector3 = slEntityRepo.findById(projectAcceptanceReport.getInspector3().getAccountNo()).orElse(null);
            SlEntity createdBy = slEntityRepo.findById(projectAcceptanceReport.getCreatedBy().getAccountNo()).orElse(null);
            SlEntity notedBy = projectAcceptanceReport.getNotedBy() != null ? slEntityRepo.findById(projectAcceptanceReport.getNotedBy().getAccountNo()).orElse(null) : null;
            SlEntity recommendedBy = projectAcceptanceReport.getRecommendedBy() != null ? slEntityRepo.findById(projectAcceptanceReport.getRecommendedBy().getAccountNo()).orElse(null) : null;
            SlEntity approvedBy = projectAcceptanceReport.getApprovedBy() != null ? slEntityRepo.findById(projectAcceptanceReport.getApprovedBy().getAccountNo()).orElse(null) : null;

            Project project = new Project();
            project.setId(projectAcceptanceReport.getProject().getId());
            project.setName(projectAcceptanceReport.getProject().getName());
            project.setLocation(projectAcceptanceReport.getProject().getLocation());
            project.setPurpose(projectAcceptanceReport.getProject().getPurpose());
            project.setCode(projectAcceptanceReport.getProject().getCode());
            project.setDate(projectAcceptanceReport.getProject().getDate());

            projectAcceptanceReportDto.setId(projectAcceptanceReport.getId());
            projectAcceptanceReportDto.setCode(projectAcceptanceReport.getCode());
            projectAcceptanceReportDto.setDate(projectAcceptanceReport.getDate());
            projectAcceptanceReportDto.setProject(project);
            projectAcceptanceReportDto.setDocumentStatus(projectAcceptanceReport.getDocumentStatus());
            projectAcceptanceReportDto.setTransId(projectAcceptanceReport.getTransaction().getId());
            projectAcceptanceReportDto.setInspector1(inspector1);
            projectAcceptanceReportDto.setInspector2(inspector2);
            projectAcceptanceReportDto.setInspector3(inspector3);
            projectAcceptanceReportDto.setNotedBy(notedBy);
            projectAcceptanceReportDto.setRecommendedBy(recommendedBy);
            projectAcceptanceReportDto.setApprovedBy(approvedBy);
            projectAcceptanceReportDto.setCreatedBy(createdBy);
            projectAcceptanceReportDto.setCreated(projectAcceptanceReport.getCreatedAt());
            projectAcceptanceReportDto.setLastUpdated(projectAcceptanceReport.getUpdatedAt());

            CostEstimate costEstimate = costEstimateRepo.findTop1ByProjectId(project.getId());
            if(costEstimate != null) {
                projectAcceptanceReportDto.setCostEstimateCode(costEstimate.getCode());
            }

            WorkOrder workOrder = workOrderRepo.findTop1ByProjectId(project.getId());
            if(workOrder != null) {
                projectAcceptanceReportDto.setWorkOrderCode(workOrder.getCode());
            }

        }

        return projectAcceptanceReportDto;
    }

    @Override
    @Transactional
    public PostResponse processUpdate(DocumentNoApproval document, BindingResult bindingResult, MessageSource messageSource) {
        return this.processCreate(document, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(DocumentNoApproval document, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostRoleResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        try {
            ProjectAcceptanceReport projectAcceptanceReport = (ProjectAcceptanceReport) document;

            ProjectAcceptanceReportValidator validator = new ProjectAcceptanceReportValidator();
            validator.setService(this);
            validator.validate(document, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
                response.setSuccess(false);
            } else {

                Map oldProjectMap = null;
                boolean updateMode = Checker.isValidId(projectAcceptanceReport.getId());

                User inspector1 = userRepo.findOneByAccountNo(projectAcceptanceReport.getInspector1().getAccountNo());
                User inspector2 = userRepo.findOneByAccountNo(projectAcceptanceReport.getInspector2().getAccountNo());
                User inspector3 = userRepo.findOneByAccountNo(projectAcceptanceReport.getInspector3().getAccountNo());
                User notedBy = userRepo.findOneByAccountNo(projectAcceptanceReport.getNotedBy().getAccountNo());
                User recommendedBy = userRepo.findOneByAccountNo(projectAcceptanceReport.getRecommendedBy().getAccountNo());
                User approvedBy = userRepo.findOneByAccountNo(projectAcceptanceReport.getApprovedBy().getAccountNo());

                projectAcceptanceReport.setInspector1(inspector1);
                projectAcceptanceReport.setInspector2(inspector2);
                projectAcceptanceReport.setInspector3(inspector3);
                projectAcceptanceReport.setNotedBy(notedBy);
                projectAcceptanceReport.setRecommendedBy(recommendedBy);
                projectAcceptanceReport.setApprovedBy(approvedBy);

                if(updateMode){
                    this.model = projectAcceptanceReportRepo.findById(projectAcceptanceReport.getId()).orElse(null);
                    if(this.model != null) {

                        if(this.model.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId() &&
                                this.model.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.RETURNED_TO_CREATOR.getId()) {
                            response.setFailureMessage("Action is not allowed");
                            return response;
                        }

                        oldProjectMap = documentLoggerFacade.makeLog(this.model);

                        this.model.setDate(projectAcceptanceReport.getDate());
                        this.model.setProject(projectAcceptanceReport.getProject());
                        this.model.setInspector1(projectAcceptanceReport.getInspector1());
                        this.model.setInspector2(projectAcceptanceReport.getInspector2());
                        this.model.setInspector3(projectAcceptanceReport.getInspector3());
                        this.model.setNotedBy(projectAcceptanceReport.getNotedBy());
                        this.model.setRecommendedBy(projectAcceptanceReport.getRecommendedBy());
                        this.model.setApprovedBy(projectAcceptanceReport.getApprovedBy());

                        this.model = projectAcceptanceReportRepo.save(this.model);

                        if(this.model != null) {
                            response.setSuccessMessage("Project Acceptance Report successfully updated.");
                        } else {
                            response.setFailureMessage("Failed to update Project Acceptance Report");
                        }
                    } else {
                        response.setFailureMessage("Project Acceptance Report is not available.");
                    }

                } else {

                    Integer projectAcceptanceYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(projectAcceptanceReport.getDate()));
                    Object latestCode = projectAcceptanceReportRepo.findLatestCodeByYear(projectAcceptanceYear);

                    String code = generatorFacade.voucherCodeNoOffice("PAR", (latestCode == null ? "" : String.valueOf(latestCode)), projectAcceptanceReport.getDate(), GlobalConstant.COUNTER_PAD_4);

                    projectAcceptanceReport.setCode(code);

                    Workflow workflow = new Workflow();
                    workflow.setId(com.noreco1.fireflyv2.model.enums.Workflow.PROJECT_ACCEPTANCE_REPORT.getId());

                    projectAcceptanceReport.setDocumentStatus(ServiceUtil.getDocumentStatusModel(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED));
                    projectAcceptanceReport.setCreatedBy(authenticationFacade.getLoggedIn());
                    projectAcceptanceReport.setTransaction(generatorFacade.transaction());
                    projectAcceptanceReport.setWorkflow(workflow);

                    this.model = projectAcceptanceReportRepo.save(projectAcceptanceReport);
                    response.setSuccessMessage("Project successfully saved.");
                }

                if(this.model != null) {

                    if(!updateMode) {
                        this.model.setCreatedAt(new Date());
                        this.model.setUpdatedAt(new Date());
                    }

                    //update default signatories
                    signatoryFacade.projectAcceptanceReport(this.model);

                    if(!updateMode) {  // document processing logging only when adding document
                        documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), this.model.getCreatedBy());
                    }

                    Map newProjectMap =  documentLoggerFacade.makeLog(this.model);

                    documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldProjectMap, newProjectMap);

                    response.setModelId(this.model.getId());
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return response;
    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        ProjectAcceptanceReport projectAcceptanceReport =  projectAcceptanceReportRepo.findById(postData.getDocumentId()).orElse(null);

        if (projectAcceptanceReport != null && projectAcceptanceReport.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.CERTIFIED.getId()) {
            // for logging
            Map oldMap = this.forLogMapMain(projectAcceptanceReport);

            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
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
            Map newMap = this.forLogMapMain(projectAcceptanceReport);
            newMap.put("remarks", postData.getRemarks());

            if (projectAcceptanceReport != null) {
                documentProcessingFacade.processAction(projectAcceptanceReport.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(projectAcceptanceReport.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, newMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }

        }else {
            response.setFailureMessage("Action is not allowed");
        }
        return response;
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(DocumentType.PROJECT_ACCEPTANCE);
    }

    @Override
    public List<DocumentStatus> getDocumentsStatuses() {
        ProjectAcceptanceReport projectAcceptanceReport = projectAcceptanceReportRepo.findFirstByOrderByIdAsc();
        if (projectAcceptanceReport != null) {
            return documentDtoer.getDocumentStatuses(projectAcceptanceReport.getWorkflow().getId());
        }

        return null;
    }

    private Map forLogMapMain(ProjectAcceptanceReport projectAcceptanceReport) {
        return documentLoggerFacade.makeLog(projectAcceptanceReport);
    }

    @Override
    public HashMap reportParameters(Integer id, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        ProjectAcceptanceReport projectAcceptanceReport = projectAcceptanceReportRepo.findById(id).orElse(null);

        if (projectAcceptanceReport != null) {
            params.put("REPORT_DATE", projectAcceptanceReport.getDate());
            params.put("CODE", projectAcceptanceReport.getCode());
            params.put("PROJECT_NAME", projectAcceptanceReport.getProject().getName());
            params.put("PROJECT_LOCATION", projectAcceptanceReport.getProject().getLocation());

            String contractor = "";
            List<ProjectContractor> contractors = projectContractorRepo.findAllByProjectId(projectAcceptanceReport.getProject().getId());
            if(!contractors.isEmpty()){

                for(ProjectContractor cont : contractors){

                    if(Checker.isStringNullOrEmpty(contractor)){
                        contractor += cont.getSupplier().getAccountNumber() + " - " + cont.getSupplier().getName();
                    } else {
                        contractor += "\n"+cont.getSupplier().getAccountNumber() + " - " + cont.getSupplier().getName();
                    }

                }

            }

            params.put("CONTRACTOR", (Checker.isStringNullOrEmpty(contractor) ? "" : contractor +"\n"  )  + (projectAcceptanceReport.getProject().getProjectManager() == null ? "" : projectAcceptanceReport.getProject().getProjectManager()));

            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.PROJECT_ACCEPTANCE, projectAcceptanceReport);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer vid) {
        List<ProjectAcceptanceReport> data = projectAcceptanceReportRepo.findAll();
        return new JRBeanCollectionDataSource(data);
    }
}
