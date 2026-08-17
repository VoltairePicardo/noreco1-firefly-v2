package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.ProjectAttachmentPrefix;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.PostRoleResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.SiteInspectionReportService;
import com.noreco1.fireflyv2.validator.SiteInspectionReportValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class SiteInspectionReportServiceImpl implements SiteInspectionReportService, PrintableVoucher {

    private SiteInspectionReport model;

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    WorkOrderRepo workOrderRepo;

    @Autowired
    SiteInspectionReportRepo siteInspectionReportRepo;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    AuthenticationFacade authenticationFacade;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    DocumentDtoer documentDtoer;

    @Autowired
    UserRepo userRepo;

    @Autowired
    SiteInspectionReportDescriptionRepo siteInspectionReportDescriptionRepo;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    SignatureFacade signatureFacade;

    @Autowired
    FileFacade fileFacade;

    @Autowired
    private DocumentFileRepo fileRepo;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private EmployeeRepo employeeRepo;

    @Override
    @Transactional(readOnly = true)
    public Page<SiteInspectionReport> findAll(String from, String to, Pageable pageable) {

        Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
        Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

        return siteInspectionReportRepo.findByDateBetweenAndDocumentStatusIdNotIn(fromDate, toDate, Arrays.asList(this.getNonPendingStatusIds()), pageable);
    }

    @Override
    public Page<SiteInspectionReport> findAll(String from, String to, int statusId, Pageable pageable) {

        Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
        Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

        return siteInspectionReportRepo.findByDateBetweenAndDocumentStatusId(fromDate, toDate, statusId, pageable);
    }

    @Override
    public Page<SiteInspectionReport> findAll(String from, String to, String query, Pageable pageable) {

        Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
        Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");
        query = "%"+query+"%";

        return siteInspectionReportRepo
                .findByDateBetweenAndQueryAndDocumentStatusIdNotIn(fromDate, toDate, query,
                                                                    Arrays.asList(this.getNonPendingStatusIds()), pageable);
    }

    @Override
    public Page<SiteInspectionReport> findAll(String from, String to, int statusId, String query, Pageable pageable) {

        Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
        Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");
        query = "%"+query+"%";

        return siteInspectionReportRepo.findByDateBetweenAndDocumentStatusIdAndQuery(fromDate, toDate, statusId, query, pageable);
    }

    @Override
    public List<DocumentStatus> getDocumentsStatuses() {
        SiteInspectionReport report = siteInspectionReportRepo.findFirstByOrderByIdAsc();
        if (report != null) {
            return documentDtoer.getDocumentStatuses(report.getWorkflow().getId());
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public SiteInspectionReport findById(Integer id) {
        SiteInspectionReport inspectionReport = siteInspectionReportRepo.findById(id).orElse(null);

        List<SiteInspectionReportDescription> remarks = siteInspectionReportDescriptionRepo.findBySiteInspectionReportId(inspectionReport.getId());
        if(!remarks.isEmpty()) {
            for (SiteInspectionReportDescription r: remarks) {
                r.setSiteInspectionReport(null);
                inspectionReport.getDescriptions().add(r);
            }
        }

        return inspectionReport;
    }

    @Override
    public PostResponse processUpdate(DocumentNoApproval entity, BindingResult bindingResult, MessageSource messageSource) {
        return this.processCreate(entity, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(DocumentNoApproval document, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostRoleResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        try {

            SiteInspectionReport siteInspectionReport = (SiteInspectionReport) document;

            SiteInspectionReportValidator validator = new SiteInspectionReportValidator();
            validator.setService(this);
            validator.validate(siteInspectionReport, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
            } else {
                Map oldProjectMap = null;
                boolean editMode = Checker.isValidId(siteInspectionReport.getId());

                User checker = userRepo.findOneByAccountNo(siteInspectionReport.getChecker().getAccountNo());
                User notedBy = userRepo.findOneByAccountNo(siteInspectionReport.getNotedBy().getAccountNo());
                User approvedBy = userRepo.findOneByAccountNo(siteInspectionReport.getApprovedBy().getAccountNo());

                siteInspectionReport.setNotedBy(notedBy);
                siteInspectionReport.setChecker(checker);
                siteInspectionReport.setApprovedBy(approvedBy);
                siteInspectionReport.setWorkOrder(workOrderRepo.findByProjectId(siteInspectionReport.getProject().getId()));

                if(editMode) {
                    this.model = siteInspectionReportRepo.findOneByCodeAndId(siteInspectionReport.getCode(), siteInspectionReport.getId());
                    if(this.model != null) {

                        if(this.model.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId() &&
                                this.model.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.RETURNED_TO_CREATOR.getId()) {
                            response.setFailureMessage("Action is not allowed");
                            return response;
                        }

                       oldProjectMap = documentLoggerFacade.makeLog(this.model);

                        this.model.setDate(siteInspectionReport.getDate());
                        this.model.setProject(siteInspectionReport.getProject());
                        this.model.setChecker(siteInspectionReport.getChecker());
                        this.model.setNotedBy(siteInspectionReport.getNotedBy());
                        this.model.setApprovedBy(siteInspectionReport.getApprovedBy());

                        this.model = siteInspectionReportRepo.save(this.model);

                        if(this.model != null) {

                            // reset recommendations
                            siteInspectionReportDescriptionRepo.deleteBySiteInspectionReportId(this.model.getId());

                            response.setSuccessMessage("Site Inspection Report successfully updated.");

                        } else {
                            response.setFailureMessage("Failed to update Site Inspection Report");
                        }

                    } else {
                        response.setFailureMessage("Site Inspection Report is not available.");
                    }
                } else {

                    Integer projectYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(siteInspectionReport.getDate()));
                    Object latestCode = siteInspectionReportRepo.findLatestCodeByYear(projectYear);

                    String code = generatorFacade.voucherCodeNoOffice("IR", (latestCode == null ? "" : String.valueOf(latestCode)), siteInspectionReport.getDate(), GlobalConstant.COUNTER_PAD_3);

                    siteInspectionReport.setCode(code);

                    Workflow workflow = new Workflow();
                    workflow.setId(com.noreco1.fireflyv2.model.enums.Workflow.SITE_INSPECTION_REPORT.getId());

                    siteInspectionReport.setDocumentStatus(ServiceUtil.getDocumentStatusModel(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED));
                    siteInspectionReport.setCreatedBy(authenticationFacade.getLoggedIn());
                    siteInspectionReport.setYear(projectYear);
                    siteInspectionReport.setTransaction(generatorFacade.transaction());
                    siteInspectionReport.setWorkflow(workflow);

                    this.model = siteInspectionReportRepo.save(siteInspectionReport);
                    response.setSuccessMessage("Project successfully saved.");
                }

                if(this.model != null) {
                    // for logging only
                    if(!editMode) {
                        this.model.setCreatedAt(new Date());
                        this.model.setUpdatedAt(new Date());
                    } else {
                        this.model.setDescriptions(siteInspectionReport.getDescriptions());
                    }

                    // start: update default signatories
                    signatoryFacade.siteInspectionReport(this.model);
                    // end: update default signatories

                    for (SiteInspectionReportDescription remark: siteInspectionReport.getDescriptions()) {
                        remark.setSiteInspectionReport(this.model);
                        siteInspectionReportDescriptionRepo.save(remark);
                    }

                    if(!editMode) {  // document processing logging only when adding document
                        documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), this.model.getCreatedBy());
                    }

                    this.model.setDescriptions(siteInspectionReport.getDescriptions()); // for logging
                    Map newProjectMap =  documentLoggerFacade.makeLog(this.model);

                    DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldProjectMap, newProjectMap);

                    response.setLogId(log != null ? log.getId() : 0);
                    response.setModelId(this.model.getId());
                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return response;
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.SITE_INSPECTION_REPORT);
    }

    @Override
    @Transactional
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        SiteInspectionReport report =  siteInspectionReportRepo.findById(postData.getDocumentId()).orElse(null);

        if (report != null && report.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.NOTED.getId()) {
            // for logging
            Map oldJvMap = documentLoggerFacade.makeLog(report);

            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(report, report.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            report.setDocumentStatus(afterActionDocumentStatus);
            report.setUpdatedAt(null);
            report = siteInspectionReportRepo.save(report);

            // for logging
            Map newJvMap = documentLoggerFacade.makeLog(report);
            newJvMap.put("remarks", postData.getRemarks());

            if (report != null) {
                documentProcessingFacade.processAction(report.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(report.getTransaction(), authenticationFacade.getLoggedIn(), oldJvMap, newJvMap);

                response.setSuccessMessage("Report successfully processed");
                response.setSuccess(true);
            }
        }
        return response;
    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            SiteInspectionReport report = siteInspectionReportRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (report != null) {

                report.setDescriptions(siteInspectionReportDescriptionRepo.findBySiteInspectionReportId(report.getId()));
                Map map = documentLoggerFacade.makeLog(report);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public PostResponse processCreate(DocumentNoApproval entity, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request) {

        SiteInspectionReport siteInspectionReport = (SiteInspectionReport) entity;
        Project project = projectRepo.findById(siteInspectionReport.getProject().getId()).orElse(null);

        if(this.hasFiles(request, project, new ArrayList<Map>())) {

            PostResponse response = this.processCreate(entity, bindingResult, messageSource);

            if (request instanceof MultipartHttpServletRequest && this.model != null) {

//                ServiceUtil.attachFiles(fileFacade, request, project.getTransaction(), ProjectAttachmentPrefix.BUILT_OF_MATERIALS.getPrefix());
                ServiceUtil.attachFiles(fileFacade, request, project.getTransaction(), ProjectAttachmentPrefix.BUILT_STAKING_SHEET.getPrefix());
            }

            return response;
        }

        return this.filesNotFoundMessage();
    }

    @Override
    public PostResponse processUpdate(DocumentNoApproval entity, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> filesToRemove) {

        SiteInspectionReport siteInspectionReport = (SiteInspectionReport) entity;
        Project project = projectRepo.findById(siteInspectionReport.getProject().getId()).orElse(null);

        if(this.hasFiles(request, project, filesToRemove)) {

            PostResponse response = this.processUpdate(entity, bindingResult, messageSource);

            if (request instanceof MultipartHttpServletRequest && this.model != null) {
                ServiceUtil.removeFiles(fileFacade, request, this.model.getProject().getTransaction(), filesToRemove);

//                ServiceUtil.attachFiles(fileFacade, request, this.model.getProject().getTransaction(), ProjectAttachmentPrefix.BUILT_OF_MATERIALS.getPrefix());
                ServiceUtil.attachFiles(fileFacade, request, this.model.getProject().getTransaction(), ProjectAttachmentPrefix.BUILT_STAKING_SHEET.getPrefix());
            }

            return response;
        }

        return this.filesNotFoundMessage();
    }

    @Override
    public List<Map> findDetailBySiteInspectionReportTransId(Integer transId) {

        List<Map> maps = new ArrayList<>();

        try {

            SiteInspectionReport siteInspectionReport = siteInspectionReportRepo.findOneByTransactionId(transId);

            if (Checker.isValidId(siteInspectionReport.getId())){

                List<SiteInspectionReportDescription> details = siteInspectionReportDescriptionRepo.findBySiteInspectionReportId(siteInspectionReport.getId());

                if (Checker.collectionIsNotEmpty(details)){

                    for (SiteInspectionReportDescription siteInspectionReportDescription : details){

                        Map map = new HashMap();

                        map.put("id", siteInspectionReportDescription.getId());
                        map.put("description", siteInspectionReportDescription.getDescription());
                        map.put("remark", siteInspectionReportDescription.getRemark());

                        maps.add(map);

                    }

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return maps;

    }

    private Integer[] getNonPendingStatusIds() {

        Integer[] nonPendingStatusIds = {
                com.noreco1.fireflyv2.model.enums.DocumentStatus.NOTED.getId(),
                com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId()
        };

        return nonPendingStatusIds;
    }

    @Override
    public HashMap reportParameters(Integer id, HttpServletRequest request) {

        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        SiteInspectionReport report = siteInspectionReportRepo.findById(id).orElse(null);

        if (report != null) {

            Employee createdBy = employeeRepo.findOneByAccountNumber(report.getCreatedBy().getAccountNo());
            Employee checker = employeeRepo.findOneByAccountNumber(report.getChecker().getAccountNo());
            Employee notedBy = employeeRepo.findOneByAccountNumber(report.getNotedBy().getAccountNo());
            Employee approvedBy = employeeRepo.findOneByAccountNumber(report.getApprovedBy().getAccountNo());

            params.put("CODE", report.getCode());
            params.put("WO_NUMBER", report.getWorkOrder().getCode());
            params.put("PROJECT", report.getProject().getName());
            params.put("ADDRESS", report.getProject().getLocation());
            params.put("DATE_OF_INSPECTION", report.getDate());
            params.put("INSPECTED_BY", createdBy.getName());
            params.put("CHECKER", checker.getName());
            params.put("NOTED_BY", notedBy.getName());
            params.put("APPROVED_BY", approvedBy.getName());
            params.put("NOW", new Date());
            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.SITE_INSPECTION_REPORT, report);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer id) {
        List<Map> details = new ArrayList<>();

        SiteInspectionReport report = siteInspectionReportRepo.findById(id).orElse(null);
        if (report != null) {
            List<SiteInspectionReportDescription> descriptions = siteInspectionReportDescriptionRepo.findBySiteInspectionReportId(report.getId());

            int cnt = 1;
            for (SiteInspectionReportDescription d: descriptions) {

                Map row = new HashMap();

                row.put("count", cnt++);
                row.put("description", d.getDescription());
                row.put("remark", d.getRemark());

                details.add(row);
            }
        }
        return new JRBeanCollectionDataSource(details);
    }

    private PostResponse filesNotFoundMessage() {
        PostResponse response = new PostResponse();
        response.setFailureMessage("Please attach needed files");

        return response;
    }

    private boolean hasFiles(HttpServletRequest request, Project project, List<Map> filesToRemove) {

        boolean hasFiles = false;

        List<String> prefixes = new ArrayList<>();
//        prefixes.add(ProjectAttachmentPrefix.BUILT_OF_MATERIALS.getPrefix());
        prefixes.add(ProjectAttachmentPrefix.BUILT_STAKING_SHEET.getPrefix());

        for (String prefix: prefixes) {

            List<String> perTypePrefix = new ArrayList<>();
            perTypePrefix.add(prefix);

            hasFiles = ServiceUtil.hasFiles(request, perTypePrefix);

            if(!hasFiles) {
                List<DocumentFile> files = fileRepo.findByPrefixAndTransactionId(prefix, project.getTransaction().getId());

                out:
                for (Map file: filesToRemove) {
                    Integer fileId = (Integer) file.get("id");

                    in1:
                    for (DocumentFile material: files) {
                        if(material.getFile().getId().equals(fileId)) {
                            files.remove(material);
                            break in1;
                        }
                    }

                }

                hasFiles = Checker.collectionIsNotEmpty(files);
            }

            if(!hasFiles) return hasFiles;

        }

        return hasFiles;
    }
}
