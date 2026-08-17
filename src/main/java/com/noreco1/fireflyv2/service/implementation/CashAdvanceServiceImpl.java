package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.service.CashAdvanceService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.CashAdvanceValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service(value = "cashAdvanceServiceImpl")
public class CashAdvanceServiceImpl implements CashAdvanceService, PrintableVoucher {

    private CashAdvance model;

    @Autowired
    CashAdvanceRepo cashAdvanceRepo;

    @Autowired
    CashAdvanceParticularRepo cashAdvanceParticularRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    UserRepo userRepo;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    FileFacade fileFacade;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    DocumentDtoer documentDtoer;

    @Autowired
    SignatureFacade signatureFacade;

    @Autowired
    private SettingFacade settingFacade;

    @Autowired
    private CashAdvanceBudgetDetailRepo cashAdvanceBudgetDetailRepo;

    @Override
    public HashMap findById(Integer id) {
        HashMap map = new HashMap();
        CashAdvance pettyCashTrans = cashAdvanceRepo.findById(id).orElse(null);

        if (pettyCashTrans != null) {
            map = composeHashMap(pettyCashTrans);
        }

        return map;
    }

    @Override
    public List<HashMap> findAll() {
        List<CashAdvance> cashAdvances = cashAdvanceRepo.findAll();

        return this.makeCAList(cashAdvances);
    }

    @Override
    public List<HashMap> findByStatusId(Integer id) {
        try {
            List<CashAdvance> list = cashAdvanceRepo.findByDocumentStatusId(id);

            return this.makeCAList(list);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public List<HashMap> findByDateRangeAndStatusId(String from, String to, Integer id, Integer officeId) {
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            List<CashAdvance> list = cashAdvanceRepo.findByDateRangeAndStatusIdAndOfficeId(
                    authenticationFacade.getLoggedIn().getId(),
                    id,
                    fromDate,
                    toDate, officeId);

            return this.makeCAList(list);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public List<HashMap> findByDateRange(String from, String to, Integer officeId) {
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            Integer[] ids = {
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId()
            };

            List<CashAdvance> vouchers = cashAdvanceRepo.findByDateRangeAndNotApprovedOrDeniedAndOfficeId(
                    authenticationFacade.getLoggedIn().getId(),
                    Arrays.asList(ids),
                    fromDate,
                    toDate, officeId);

            return this.makeCAList(vouchers);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public Page<CashAdvance> findAllForCV(Pageable pageable) {
        return cashAdvanceRepo.findAllForCV(pageable);
    }

    @Override
    public Page<CashAdvance> findAllForCVByQuery(String query, Pageable pageable) {
        query = "%"+query+"%";
        return cashAdvanceRepo.findAllForCVByQuery(query, pageable);
    }

    @Override
    public PostResponse setAsLiquidated(Integer id) {
        PostResponse response = new PostRoleResponse();

        CashAdvance cashAdvance = cashAdvanceRepo.findById(id).orElse(null);

        if(cashAdvance != null) {

            // use for document logging
            Map oldCAMap = this.forLogMapMain(cashAdvance);

            cashAdvance.setLiquidated(true);

            cashAdvanceRepo.save(cashAdvance);

            Map newCAMap = this.forLogMapMain(cashAdvance);

            documentLoggerFacade.log(cashAdvance.getTransaction(), authenticationFacade.getLoggedIn(), oldCAMap, newCAMap);

            response.setSuccessMessage("Cash Advance successfully tagged as Liquidated.");

        } else  {
            response.setFailureMessage("Cash Advance is not available.");
        }

        return response;
    }

    @Override
    public PostResponse confirmBudgetLineItem(CashAdvance ca) {
        PostResponse response = new PostRoleResponse();
        User budgetOfficer = authenticationFacade.getLoggedIn();
        CashAdvance cashAdvance = cashAdvanceRepo.findById(ca.getId()).orElse(null);

        if(cashAdvance != null) {

            // use for document logging
            Map oldCAMap = this.forLogMapMain(cashAdvance);

            cashAdvance.setBudgetOfficer(budgetOfficer);
            cashAdvance.setBudgetLineItemDetail(ca.getBudgetLineItemDetail());

            cashAdvanceRepo.save(cashAdvance);

            Map newCAMap = this.forLogMapMain(cashAdvance);

            documentLoggerFacade.log(cashAdvance.getTransaction(), authenticationFacade.getLoggedIn(), oldCAMap, newCAMap);

            response.setSuccessMessage("Cash Advance Budget Line Item successfully checked.");

        } else  {
            response.setFailureMessage("Cash Advance is not available.");
        }

        return response;
    }

    @Override
    public Page<CvVoucherDto> findAllApprovedForCvPaged(String query, Pageable pageable) {

        org.springframework.data.domain.Page<CashAdvance> cashAdvances;

        if(query != null){
            cashAdvances = cashAdvanceRepo.findAllByQueryAndDocumentStatusForCv("%"+query+"%", com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        } else {
            cashAdvances = cashAdvanceRepo.findAllByDocumentStatusForCv(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        }

        return cashAdvances.map(entity -> {
            CvVoucherDto dto = new CvVoucherDto();

            dto.setVoucherDate(entity.getVoucherDate());
            dto.setLocalCode(entity.getCode());
            dto.setId(entity.getId());
            dto.setPreparedBy(entity.getCreatedBy().getFullName());
            dto.setAmount(entity.getAmount());
            dto.setParticulars(entity.getCode());
            dto.setSlentityAccountNo(entity.getEmployee().getAccountNumber());
            dto.setSlentityName(entity.getEmployee().getName());
            dto.setTransId(entity.getTransaction().getId());
            dto.setExtensionUrl("cash-advance");
            dto.setBudgetLineItemDetail(entity.getBudgetLineItemDetail());

            return dto;
        });

    }

    @Override
    public Page<CashAdvance> findAllForLiquidation(Pageable pageable) {
        return cashAdvanceRepo.findAllForLiquidation(pageable);
    }

    @Override
    public Page<CashAdvance> findAllForLiquidationByQuery(String query, Pageable pageable) {
        query = "%"+query+"%";
        return cashAdvanceRepo.findAllForLiquidationByQuery(query, pageable);
    }

    @Override
    public Page<CashAdvance> findAllForPO(Pageable pageable) {
        return cashAdvanceRepo.findAllForPO(pageable);
    }

    @Override
    public Page<CashAdvance> findAllForPOByQuery(String query, Pageable pageable) {
        query = "%"+query+"%";
        return cashAdvanceRepo.findAllForPOByQuery(query, pageable);
    }

    @Override
    public List<CashAdvance> unliquidatedList() {
        User createdBy = authenticationFacade.getLoggedIn();
        Employee employee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());

        if(employee != null){
            return cashAdvanceRepo.findAllUnliquidatedByEmployee(employee.getId());
        } else {
            return null;
        }

    }

    @Override
    public boolean isDocumentForCashFlowItemAssignment(Integer transactionId) {

        boolean isDocumentForCashFlowItemAssignment = false;

        try {
            CashAdvance existingCA = this.cashAdvanceRepo.findOneByTransactionId(transactionId);

            if (existingCA != null && isCurrentUserAuthorized(existingCA) && isDocumentForBudgetChecking(existingCA)) {
                isDocumentForCashFlowItemAssignment = true;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return isDocumentForCashFlowItemAssignment;

    }

    @Override
    public PostResponse saveCashFlowItem(CashFlowItemDto dto, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            CashAdvance existingCA = this.cashAdvanceRepo.findOneByTransactionId(dto.getTransId());

            if(existingCA.getBudgetOfficer().getAccountNo().equals(authenticationFacade.getLoggedIn().getAccountNo())){

                existingCA.setCashFlowItemBalancePOJORFP(dto.getCashFlowItemBalancePOJORFP());
                existingCA.setCashFlowItemBalanceCV(dto.getCashFlowItemBalanceCV());
                existingCA.setCashFlowItemTotal(dto.getCashFlowItemTotal());

                this.model = cashAdvanceRepo.save(existingCA);

                if (this.model != null) {

                    cashAdvanceBudgetDetailRepo.deleteByCashAdvanceId(existingCA.getId());

                    ArrayList<CashAdvanceBudgetDetail> budgetDetails = dto.getCashAdvanceBudgetDetails();
                    for (CashAdvanceBudgetDetail cashAdvanceBudgetDetail : budgetDetails){

                        CashAdvanceBudgetDetail newCashAdvanceBudgetDetail = new CashAdvanceBudgetDetail();

                        CashAdvance newCA = new CashAdvance();
                        newCA.setId(this.model.getId());
                        newCashAdvanceBudgetDetail.setCashAdvance(newCA);

                        newCashAdvanceBudgetDetail.setCashflowItem(cashAdvanceBudgetDetail.getCashflowItem());
                        newCashAdvanceBudgetDetail.setAmount(cashAdvanceBudgetDetail.getAmount());

                        cashAdvanceBudgetDetailRepo.save(newCashAdvanceBudgetDetail);

                    }

                    response.setSuccessMessage("Entries successfully saved!");
                    response.setSuccess(true);

                }

            } else {

                response.setFailureMessage("Invalid user or update is restricted!!");
                response.setSuccess(false);

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return response;

    }

    @Override
    public List<CashAdvanceBudgetDetail> getCashAdvanceBudgetDetail(Integer caId) {
        List<CashAdvanceBudgetDetail> cashAdvanceBudgetDetails = new ArrayList<>();

        try {

            cashAdvanceBudgetDetails = this.cashAdvanceBudgetDetailRepo.findAllByCashAdvanceId(caId);

            for (CashAdvanceBudgetDetail cashAdvanceBudgetDetail : cashAdvanceBudgetDetails){

                cashAdvanceBudgetDetail.setParent(StringFormatter.reverseString(StringFormatter.getParentCashflowItemName(cashAdvanceBudgetDetail.getCashflowItem())));

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return cashAdvanceBudgetDetails;
    }

    @Override
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        CashAdvance cashAdvance = cashAdvanceRepo.findById(vid).orElse(null);

        if (cashAdvance != null) {
            Employee employee = employeeRepo.findById(cashAdvance.getEmployee().getId()).orElse(null);

            params.put("CODE", cashAdvance.getCode());
            params.put("DATE", cashAdvance.getVoucherDate());
            params.put("LIQUIDATION_DATE", cashAdvance.getVoucherDate());
            params.put("AMOUNT_IN_WORDS", CurrencyIntoWords.convert(cashAdvance.getAmount()));
            params.put("AMOUNT", cashAdvance.getAmount());

            params.put("NAME", employee.getName());
            params.put("LOCATION",cashAdvance.getLocation());
            params.put("PERIOD", DateHelper.dateToLongDate(cashAdvance.getPeriodCoveredFrom()) +" to "+ DateHelper.dateToLongDate(cashAdvance.getPeriodCoveredTo()));
            params.put("PURPOSE", cashAdvance.getPurpose());

            params.put("BUDGET_LINE_ITEM", cashAdvance.getBudgetLineItemDetail().getTitle() + " - " + cashAdvance.getBudgetLineItemDetail().getCode());
            params.put("BUDGET_BALANCE_PO_JO_RFP", cashAdvance.getBudgetLineItemBalancePOJORFP());
            params.put("BUDGET_BALANCE_PCV", cashAdvance.getBudgetLineItemBalanceCV());
            
            CashAdvanceBudgetDetail cashAdvanceBudgetDetail = cashAdvanceBudgetDetailRepo.findFirstByCashAdvanceIdOrderByIdAsc(cashAdvance.getId());

            if(cashAdvanceBudgetDetail != null){
                params.put("CASH_FLOW_BALANCE_PO_JO_RFP", cashAdvanceBudgetDetail.getCashAdvance().getCashFlowItemBalancePOJORFP());
                params.put("CASH_FLOW_BALANCE_CV", cashAdvanceBudgetDetail.getCashAdvance().getCashFlowItemBalanceCV());
                params.put("CASH_FLOW_ITEM_AMOUNT", cashAdvance.getCashFlowItemBalancePOJORFP().subtract(cashAdvance.getAmount()));
                params.put("CASH_FLOW_ITEMS", new JRBeanCollectionDataSource(this.getCashAdvanceBudgetDetail(cashAdvance.getId())));
            }

            params = signatureFacade.getDocumentSignature(params, DocumentType.CA, cashAdvance);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer cashAdvanceId) {
        List<CashAdvanceParticular> particulars = cashAdvanceParticularRepo.findByCashAdvanceId(cashAdvanceId);
        if (Checker.collectionIsEmpty(particulars)) {
            particulars = new ArrayList<>();
        }
        return new JRBeanCollectionDataSource(particulars);
    }

    @Override
    public PostResponse processUpdate(
            Document v,
            BindingResult bindingResult,
            MessageSource messageSource,
            HttpServletRequest request,
            List<Map> filesToRemove) {
        PostResponse response = this.processUpdate(v, bindingResult, messageSource);

        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;

            if (this.model != null) {
                if (mRequest.getFileMap() != null) {
                    fileFacade.removeDocumentAttachment(filesToRemove, this.model.getTransaction().getId());
                    fileFacade.saveDocumentAttachment(mRequest.getFileMap(), this.model.getTransaction().getId());
                }
            }
        }

        return response;
    }

    @Override
    public PostResponse processCreate(
            Document v,
            BindingResult bindingResult,
            MessageSource messageSource,
            HttpServletRequest request) {
        PostResponse response = this.processCreate(v, bindingResult, messageSource);

        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;

            if (this.model != null) {
                if (mRequest.getFileMap() != null) {
                    fileFacade.saveDocumentAttachment(mRequest.getFileMap(), this.model.getTransaction().getId());
                }
            }
        }

        return response;
    }

    @Override
    public List<DocumentStatus> getDocumentsStatuses() {
        CashAdvance ca = cashAdvanceRepo.findFirstByOrderByIdAsc();

        if (ca != null) {
            return documentDtoer.getDocumentStatuses(ca.getWorkflow().getId());
        }

        return null;
    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        User processedBy = authenticationFacade.getLoggedIn();
        CashAdvance cashAdvance = cashAdvanceRepo.findById(postData.getDocumentId()).orElse(null);

        if (cashAdvance != null && cashAdvance.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId()) {

            // for logging
            Map oldCAMap = this.forLogMapMain(cashAdvance);

            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(cashAdvance, cashAdvance.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            cashAdvance.setDocumentStatus(afterActionDocumentStatus);
            cashAdvance.setUpdatedAt(null);

            cashAdvance = cashAdvanceRepo.save(cashAdvance);

            // for logging
            Map newCAMap = this.forLogMapMain(cashAdvance);
            newCAMap.put("remarks", postData.getRemarks());

            if (cashAdvance != null) {

                documentProcessingFacade.processAction(cashAdvance.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(cashAdvance.getTransaction(), authenticationFacade.getLoggedIn(), oldCAMap, newCAMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }
        }

        return response;
    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        CashAdvance ca = (CashAdvance) v;

        return this.processCreate(ca, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        CashAdvance ca = (CashAdvance) v;
        PostResponse response = new PostResponse();
        try {

            MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);
            CashAdvanceValidator validator = new CashAdvanceValidator();

            validator.validate(ca, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();

                response = messageFormatter.getResponse();
            } else {
                CashAdvance existingCa = null;
                User createdBy = authenticationFacade.getLoggedIn();
                User approvingOfficer = userRepo.findOneByAccountNo(ca.getApprovingOfficer().getAccountNo());
                User recommendedBy = userRepo.findOneByAccountNo(ca.getRecommendedBy().getAccountNo());
                User budgetOfficer = userRepo.findOneByAccountNo(ca.getBudgetOfficer().getAccountNo());
                Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(ca.getCashAdvanceDate()));
                boolean insertMode = !Checker.isValidId(ca.getId());
                DocumentStatus ds = new DocumentStatus();
                Workflow wf = new Workflow();

                ds.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.CA.getId());

                // Insert mode.
                if (insertMode) {
                    String offAcro = ca.getOffice().getAcronym();
                    ca.setAccountNo(generatorFacade.entityAccountNumber());
                    Object latestPcvCode = cashAdvanceRepo.findLatestCaCodeByYear(voucherYear, "%-"+offAcro+"-%");
                    ca.setCode(generatorFacade.voucherCode("CA-"+offAcro, (latestPcvCode == null ? "" : String.valueOf(latestPcvCode)), ca.getCashAdvanceDate()));
                    ca.setDocumentStatus(ds);
                    ca.setTransaction(generatorFacade.transaction());
                    ca.setCreatedBy(createdBy);
                    ca.setWorkflow(wf);

                    Employee employee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());
                    ca.setEmployee(employee);

                    existingCa = ca;
                } else {
                    List<Integer> statusAllowed = new ArrayList<>();
                    existingCa = cashAdvanceRepo.findById(ca.getId()).orElse(null);

                    statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                    statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.RETURNED_TO_CREATOR.getId());
                    statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_BUDGET_OFFICER.getId());

                    if (statusAllowed.indexOf(existingCa.getDocumentStatus().getId()) < 0) {

                        ArrayList<String> messages = new ArrayList<>();

                        messages.add("Action is not allowed");

                        response.setNotAuthorized(true);
                        response.setMessages(messages);
                        response.setSuccess(false);

                        return response;
                    }
                }

                // use for document logging
                Map oldCAMap = this.forLogMapMain(existingCa);

                // Editable fields.
                existingCa.setCashAdvanceDate(ca.getCashAdvanceDate());
                existingCa.setVoucherDate(ca.getVoucherDate());
                existingCa.setPeriodCoveredFrom(ca.getPeriodCoveredFrom());
                existingCa.setPeriodCoveredTo(ca.getPeriodCoveredTo());
                existingCa.setLocation(ca.getLocation());
                existingCa.setBudgetLineItemDetail(ca.getBudgetLineItemDetail());
                existingCa.setAmount(ca.getAmount());
                existingCa.setPurpose(ca.getPurpose());
                existingCa.setRemarks(ca.getRemarks());
                existingCa.setCreatedBy(createdBy);
                existingCa.setApprovingOfficer(approvingOfficer);
                existingCa.setRecommendedBy(recommendedBy);
                existingCa.setYear(voucherYear);
                existingCa.setOffice(ca.getOffice());
                existingCa.setBudgetOfficer(budgetOfficer);

                existingCa.setYear(voucherYear);
                existingCa.setWorkflow(wf);

                this.model = cashAdvanceRepo.save(existingCa);

                if (this.model != null) {

                    // start: update default signatories
                    signatoryFacade.ca(this.model);
                    // end: update default signatories

                    ArrayList<CashAdvanceParticular> cashAdvanceParticularDtos = ca.getCashAdvanceParticulars();

                    // Log action only when adding document.
                    if (insertMode) {
                        documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                        oldCAMap = null;
                    } else {
                        cashAdvanceParticularRepo.deleteByCashAdvanceId(this.model.getId());
                    }

                    for (CashAdvanceParticular cashAdvanceParticular : cashAdvanceParticularDtos) {
                        cashAdvanceParticular.setCashAdvance(existingCa);

                        cashAdvanceParticularRepo.save(cashAdvanceParticular);
                    }

                    // generic document logging here
                    // old value only
                    DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldCAMap, null);

                    response.setLogId(log != null ? log.getId() : 0);
                    response.setModelId(this.model.getId());

                    response.setModelId(this.model.getId());
                    response.setSuccessMessage("CA successfully saved!");
                    response.setSuccess(true);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    @Override
    public void logNewValue(Integer logId) {

        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            CashAdvance ca = cashAdvanceRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (ca != null) {
                Map map = forLogMapMain(ca);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }

    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(DocumentType.CA);
    }

    private HashMap composeHashMap(CashAdvance cashAdvance) {
        HashMap<String, Object> hm = new HashMap<>();
        HashMap<String, Object> documentStatus;
        HashMap<String, Object> transaction;
        HashMap<String, Object> employee;
        HashMap<String, Object> createdBy;
        HashMap<String, Object> approvingOfficer;
        HashMap<String, Object> recommendedBy;
        HashMap<String, Object> budgetOfficer;

        try {
            // Set document status object.
            documentStatus = new HashMap<>();

            documentStatus.put("id", cashAdvance.getDocumentStatus().getId());
            documentStatus.put("status", cashAdvance.getDocumentStatus().getStatus());

            // Set transaction object.
            transaction = new HashMap<>();

            transaction.put("id", cashAdvance.getTransaction().getId());
            transaction.put("createdAt", cashAdvance.getTransaction().getCreatedAt());
            transaction.put("createdBy", cashAdvance.getTransaction().getCreatedBy());

            // Set employee object.
            employee = new HashMap<>();

            employee.put("id", cashAdvance.getEmployee().getId());
            employee.put("accountNo", cashAdvance.getEmployee().getAccountNumber());
            employee.put("fullName", cashAdvance.getEmployee().getName());

            // Set created by user object.
            createdBy = new HashMap<>();

            createdBy.put("id", cashAdvance.getCreatedBy() != null ? cashAdvance.getCreatedBy().getId() : null);
            createdBy.put("accountNo", cashAdvance.getCreatedBy() != null ? cashAdvance.getCreatedBy().getAccountNo() : "");
            createdBy.put("fullName", cashAdvance.getCreatedBy() != null ? cashAdvance.getCreatedBy().getFullName() : "");

            // Set approved by user object.
            approvingOfficer = new HashMap<>();

            approvingOfficer.put("id", cashAdvance.getApprovingOfficer() != null ? cashAdvance.getApprovingOfficer().getId() : null);
            approvingOfficer.put("accountNo", cashAdvance.getApprovingOfficer() != null ? cashAdvance.getApprovingOfficer().getAccountNo() : "");
            approvingOfficer.put("fullName", cashAdvance.getApprovingOfficer() != null ? cashAdvance.getApprovingOfficer().getFullName() : "");

            // Set audited by user object.
            recommendedBy = new HashMap<>();

            recommendedBy.put("id", cashAdvance.getRecommendedBy() != null ? cashAdvance.getRecommendedBy().getId() : null);
            recommendedBy.put("accountNo", cashAdvance.getRecommendedBy() != null ? cashAdvance.getRecommendedBy().getAccountNo() : "");
            recommendedBy.put("fullName", cashAdvance.getRecommendedBy() != null ? cashAdvance.getRecommendedBy().getFullName() : "");

            // Set budget officer object.
            budgetOfficer = new HashMap<>();

            budgetOfficer.put("id", cashAdvance.getBudgetOfficer() != null ? cashAdvance.getBudgetOfficer().getId() : null);
            budgetOfficer.put("accountNo", cashAdvance.getBudgetOfficer() != null ? cashAdvance.getBudgetOfficer().getAccountNo() : "");
            budgetOfficer.put("fullName", cashAdvance.getBudgetOfficer() != null ? cashAdvance.getBudgetOfficer().getFullName() : "");

            Map office = new HashMap();
            office.put("id", cashAdvance.getOffice().getId());
            office.put("name", cashAdvance.getOffice().getName());
            office.put("acronym", cashAdvance.getOffice().getAcronym());
            hm.put("office", office);

            hm.put("id", cashAdvance.getId());
            hm.put("code", cashAdvance.getCode());
            hm.put("accountNo", cashAdvance.getAccountNo());
            hm.put("employee", employee);
            hm.put("cashAdvanceDate", cashAdvance.getCashAdvanceDate());
            hm.put("periodCoveredFrom", cashAdvance.getPeriodCoveredFrom());
            hm.put("periodCoveredTo", cashAdvance.getPeriodCoveredTo());
            hm.put("location", cashAdvance.getLocation());
            hm.put("budgetLineItemDetail", cashAdvance.getBudgetLineItemDetail());
            hm.put("voucherDate", cashAdvance.getVoucherDate());
            hm.put("amount", cashAdvance.getAmount());
            hm.put("purpose", cashAdvance.getPurpose());
            hm.put("remarks", cashAdvance.getRemarks());
            hm.put("createdBy", createdBy);
            hm.put("approvingOfficer", approvingOfficer);
            hm.put("recommendedBy", recommendedBy);
            hm.put("budgetOfficer", budgetOfficer);
            hm.put("documentStatus", documentStatus);
            hm.put("isLiquidated", cashAdvance.isLiquidated());
            hm.put("createdAt", cashAdvance.getCreatedAt());
            hm.put("updatedAt", cashAdvance.getUpdatedAt());
            hm.put("transaction", transaction);

            boolean isChecked = cashAdvance.getBudgetOfficer() != null;
            boolean allowCheck = false;
            hm.put("isChecked", isChecked);

            boolean allowEditAmount = false;

            User loggedInUser = authenticationFacade.getLoggedIn();
            if(loggedInUser != null){

                if(cashAdvance.getDocumentStatus().getId() == com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_BUDGET_OFFICER.getId()){

                    if(Objects.equals(loggedInUser.getId(), cashAdvance.getBudgetOfficer().getId())){

                        allowEditAmount = true;

                    }

                }

                /*Employee empBudgetOfficer = employeeRepo.findOneByAccountNumber(loggedInUser.getAccountNo());
                if(empBudgetOfficer != null){
                    if(empBudgetOfficer.getPosition() != null){

                        Map codeMap = settingFacade.getByCode("SPECIAL_SIGNATORIES");
                        if (codeMap != null) {
                            Integer budgetOfficerPositionId = Integer.parseInt(codeMap.get("budgetOfficerPositionId").toString());
                            allowCheck = Objects.equals(empBudgetOfficer.getPosition().getId(), budgetOfficerPositionId) && cashAdvance.getDocumentStatus().getId() == com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId();
                        }

                    }

                }*/

            }

            hm.put("isAllowedCheck", allowCheck && !isChecked);
            hm.put("allowEditAmount", allowEditAmount);

        } catch (Exception ex) {
            Logger.getLogger(CashAdvanceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return hm;
    }

    private Map forLogMapMain(CashAdvance ca) {
        return documentLoggerFacade.makeLog(ca);
    }

    private List<HashMap> makeCAList(List<CashAdvance> cashAdvances) {
        List<HashMap> mapList = new ArrayList<>();

        if (!Checker.collectionIsEmpty(cashAdvances)) {
            for (CashAdvance cashAdvance : cashAdvances) {
                mapList.add(composeHashMap(cashAdvance));
            }
        }

        return mapList;
    }

    private Boolean isAllowed(List<CashAdvance> list, Integer id) {
        for (CashAdvance ca : list) {
            if (ca.getId() == id) {
                return true;
            }
        }

        return false;
    }

    private boolean isCurrentUserAuthorized(CashAdvance existingCA) {
        return existingCA.getBudgetOfficer().getAccountNo().equals(this.authenticationFacade.getLoggedIn().getAccountNo());
    }

    private boolean isDocumentForBudgetChecking(CashAdvance existingCA) {
        return existingCA.getDocumentStatus().getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_BUDGET_OFFICER.getId());
    }

}
