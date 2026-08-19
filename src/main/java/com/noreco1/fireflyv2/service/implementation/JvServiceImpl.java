package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.dtoers.LedgerDtoer;
import com.noreco1.fireflyv2.dtoers.LedgerDtoerImpl;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.controller.response.reports.CommonLedgerDetail;
import com.noreco1.fireflyv2.service.JvService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.JvValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service(value = "jvServiceImpl")
public class JvServiceImpl implements JvService, PrintableVoucher {

    private JournalVoucher model;

    @Autowired
    JournalVoucherRepo jvRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    UserRepo userRepo;

    @Autowired
    SlEntityRepo slEntityRepo;

    @Autowired
    LedgerFacadeImpl ledgerFacade;

    @Autowired
    LedgerDtoerImpl ledgerDtoers;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    TemporaryBatchRepo temporaryBatchRepo;

    @Autowired
    FileFacade fileFacade;

    @Autowired
    MonthlyCycleRepo monthlyCycleRepo;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    LedgerDtoer ledgerDtoer;

    @Autowired
    private FileUploadRepo fileRepo;

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    DocumentDtoer documentDtoer;

    @Autowired
    SignatureFacade signatureFacade;

    @Autowired
    AllocationFactorRepo allocationFactorRepo;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    StockReceiveRepo stockReceiveRepo;

    @Autowired
    private StockAdjustmentRepo stockAdjustmentRepo;

    @Autowired
    private StockTransactionDetailRepo stockTransactionDetailRepo;

    @Autowired
    ReceivingReportRepo receivingReportRepo;

    @Autowired
    private JournalVoucherBudgetLineItemDetailRepo journalVoucherBudgetLineItemDetailRepo;

    @Autowired
    private JournalVoucherBudgetSubItemDetailRepo journalVoucherBudgetSubItemDetailRepo;

    @Autowired
    private JournalVoucherCashFlowBudgetDetailRepo journalVoucherCashFlowBudgetDetailRepo;

    @Override
    @Transactional
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        JournalVoucher journalVoucher =  jvRepo.findById(postData.getDocumentId()).orElse(null);

        if (journalVoucher != null && journalVoucher.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId()) {
            // for logging
            Map oldJvMap = this.forLogMapMain(journalVoucher);

            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(journalVoucher, journalVoucher.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            journalVoucher.setDocumentStatus(afterActionDocumentStatus);
            journalVoucher.setUpdatedAt(null);
            journalVoucher = jvRepo.save(journalVoucher);

            // for logging
            Map newJvMap = this.forLogMapMain(journalVoucher);
            newJvMap.put("remarks", postData.getRemarks());

            if (journalVoucher != null) {
                documentProcessingFacade.processAction(journalVoucher.getTransaction(), actionMap, null, processedBy, DocumentToTableMap.JV.toString());
                documentLoggerFacade.log(journalVoucher.getTransaction(), authenticationFacade.getLoggedIn(), oldJvMap, newJvMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }
        }
        return response;
    }

    @Override
    @Transactional
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        JournalVoucher jv = (JournalVoucher) v;
        return this.processCreate(jv, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        JournalVoucher jv = (JournalVoucher) v;
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        try {

            JvValidator validator = new JvValidator();
            validator.setmonthlyCycleRepo(this.monthlyCycleRepo);
            validator.setAllocationFactorRepo(this.allocationFactorRepo);
            validator.setLegderFacade(this.ledgerFacade);
            validator.validate(jv, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
            } else {
                User createdBy = authenticationFacade.getLoggedIn();
                JournalVoucher existingJv = null;

                Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(jv.getVoucherDate()));
                User approvingOfficer = userRepo.findOneByAccountNo(jv.getApprovingOfficer().getAccountNo());
                User checker = userRepo.findOneByAccountNo(jv.getChecker().getAccountNo());
                User recApp = userRepo.findOneByAccountNo(jv.getRecommendingOfficer().getAccountNo());
                User auditor = userRepo.findOneByAccountNo(jv.getAuditingOfficer().getAccountNo());
                User budgetOfficer = userRepo.findOneByAccountNo(jv.getBudgetOfficer().getAccountNo());

                Boolean insertMode = jv.getId() == null;
                Boolean withTempBatch = jv.getTempBatchId() != null && jv.getTempBatchId() > 0;
                Transaction transaction = null;

                if (withTempBatch) {
                    TemporaryBatch temporaryBatch = temporaryBatchRepo.findById(jv.getTempBatchId()).orElse(null);
                    if (temporaryBatch != null) {
                        transaction = temporaryBatch.getTransaction();
                        temporaryBatch.setVoucherCreated(true);
                        temporaryBatchRepo.save(temporaryBatch);
                    } else {
                        transaction = generatorFacade.transaction();
                    }
                } else {
                    transaction = generatorFacade.transaction();
                }

                if (insertMode) { // insert mode

                    Object latestApvCode = jvRepo.findLatestVvCodeByYear(voucherYear);
                    String voucherCode = generatorFacade.voucherCodeNoOffice("JV", (latestApvCode == null ? "" : String.valueOf(latestApvCode)), jv.getVoucherDate(), GlobalConstant.COUNTER_PAD_4);
                    jv.setCode(voucherCode);

                    DocumentStatus documentStatus = new DocumentStatus();
                    documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                    Workflow wf = new Workflow();
                    wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.JV.getId());

                    jv.setDocumentStatus(documentStatus);
                    jv.setCreatedBy(createdBy);
                    jv.setTransaction(transaction);
                    jv.setWorkflow(wf);

                    existingJv = jv;
                } else {
                    existingJv = jvRepo.findById(jv.getId()).orElse(null);

                    if (existingJv == null) {
                        ArrayList<String> messages = new ArrayList();
                        messages.add("JV is not available!");
                        response.setMessages(messages);
                        return response;
                    }

                    List<Integer> statusAllowed = new ArrayList();
                    statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                    statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.RETURNED_TO_CREATOR.getId());

                    if (statusAllowed.indexOf(existingJv.getDocumentStatus().getId()) < 0) {

                        ArrayList<String> messages = new ArrayList();
                        messages.add("Action is not allowed");

                        response.setNotAuthorized(true);
                        response.setMessages(messages);
                        response.setSuccess(false);

                        return response;
                    }
                }
                // use for document logging
                Map oldJvMap = this.forLogMapMain(existingJv);

                // editable fields
                existingJv.setExplanation(jv.getExplanation());
                existingJv.setVoucherDate(jv.getVoucherDate());
                existingJv.setApprovingOfficer(approvingOfficer);
                existingJv.setChecker(checker);
                existingJv.setRecommendingOfficer(recApp);
                existingJv.setAuditingOfficer(auditor);
                existingJv.setBudgetOfficer(budgetOfficer);
                existingJv.setYear(voucherYear);
                existingJv.setAmount(jv.getAmount());
                existingJv.setPayable(jv.getPayable());
                existingJv.setInvDocTransactionId(jv.getInvDocTransactionId());
                existingJv.setCashAdvanceLiquidation(jv.getCashAdvanceLiquidation());
                existingJv.setBatch(jv.getBatch());

                this.model = jvRepo.save(existingJv);

                if (this.model != null) {

                    // start: update default signatories
                    signatoryFacade.jv(this.model);
                    // end: update default signatories

                    ledgerFacade.postGeneralLedger(this.model.getTransaction(), jv.getGeneralLedgerLines(), jv.getSubLedgerLines(), this.model.getVoucherDate());

                    if (!insertMode) {
                        journalVoucherBudgetLineItemDetailRepo.deleteByJournalVoucherId(existingJv.getId());
                        journalVoucherBudgetSubItemDetailRepo.deleteByJournalVoucherId(existingJv.getId());
                    }

                    if (insertMode) { // document processing logging only when adding document
                        documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                        oldJvMap = null; // new document has no old value
                    }

//                    ArrayList<BudgetLineItemDetail> budgetLineItemDetails = jv.getBudgetLineItemDetails();
//                    for (BudgetLineItemDetail budgetLineItemDetail : budgetLineItemDetails){
//
//                        JournalVoucherBudgetLineItemDetail newJournalVoucherBudgetLineItemDetail = new JournalVoucherBudgetLineItemDetail();
//
//                        JournalVoucher journalVoucher = new JournalVoucher();
//                        journalVoucher.setId(this.model.getId());
//                        newJournalVoucherBudgetLineItemDetail.setJournalVoucher(journalVoucher);
//
//                        newJournalVoucherBudgetLineItemDetail.setBudgetLineItemDetail(budgetLineItemDetail);
//                        newJournalVoucherBudgetLineItemDetail.setBudgetAmountBalanceCV(budgetLineItemDetail.getBudgetAmountBalanceCV());
//                        newJournalVoucherBudgetLineItemDetail.setBudgetAmountBalancePOJORFP(budgetLineItemDetail.getBudgetAmountBalancePOJORFP());
//
//                        journalVoucherBudgetLineItemDetailRepo.save(newJournalVoucherBudgetLineItemDetail);
//
//                    }
//
//                    ArrayList<BudgetSubItem> budgetSubItems = jv.getBudgetSubItems();
//                    for (BudgetSubItem budgetSubItem : budgetSubItems){
//
//                        JournalVoucherBudgetSubItemDetail newJournalVoucherBudgetSubItemDetail = new JournalVoucherBudgetSubItemDetail();
//
//                        JournalVoucher journalVoucher = new JournalVoucher();
//                        journalVoucher.setId(this.model.getId());
//                        newJournalVoucherBudgetSubItemDetail.setJournalVoucher(journalVoucher);
//
//                        newJournalVoucherBudgetSubItemDetail.setBudgetSubItem(budgetSubItem);
//                        newJournalVoucherBudgetSubItemDetail.setAmount(budgetSubItem.getAmount());
//                        newJournalVoucherBudgetSubItemDetail.setBudgetSubItemAmountBalanceCV(budgetSubItem.getBudgetSubItemAmountBalanceCV());
//                        newJournalVoucherBudgetSubItemDetail.setBudgetSubItemAmountBalancePOJO(budgetSubItem.getBudgetSubItemAmountBalancePOJO());
//
//                        journalVoucherBudgetSubItemDetailRepo.save(newJournalVoucherBudgetSubItemDetail);
//
//                    }

                    // generic document logging here
                    // old value only
                    DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldJvMap, null);

                    response.setLogId(log != null ? log.getId() : 0);
                    response.setModelId(this.model.getId());
                    response.setSuccessMessage("JV successfully saved!");
                    response.setSuccess(true);
                }
            }
        }catch (Exception ex) {
            Logger.getLogger(JvServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return response;
    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            JournalVoucher jv = jvRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (jv != null) {
                Map map = forLogMapMain(jv);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public List<Map> findAll() {
        List<JournalVoucher> vouchers = jvRepo.findAll();
        return this.makeJvListMap(vouchers);
    }

    @Override
    public Map findById(Integer id) {
        Map map = new HashMap();

        JournalVoucher journalVoucher = jvRepo.findById(id).orElse(null);
        if (journalVoucher != null) {
            map = composeJvMap(journalVoucher);
        }

        return map;
    }

    @Override
    public List<Map> findByDateRangeAndStatusId(String from, String to, Integer id) {
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            List<JournalVoucher> vouchers = jvRepo.findByDocumentStatusIdAndVoucherDateBetween(id, fromDate, toDate);
            return this.makeJvListMap(vouchers);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Map> findByDateRange(String from, String to) {
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

            List<JournalVoucher> vouchers = jvRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(fromDate, toDate, Arrays.asList(ids));
            return this.makeJvListMap(vouchers);

        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public PostResponse updateEntries(JournalVoucher jv, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            Boolean insertMode = Checker.isValidId(jv.getId());

            if(insertMode){

                JournalVoucher existingJv = this.jvRepo.findById(jv.getId()).orElse(null);

                if(existingJv.getChecker().getAccountNo().equals(authenticationFacade.getLoggedIn().getAccountNo())){

                    existingJv.setAmount(jv.getAmount());

                    this.model = jvRepo.save(existingJv);

                    if (this.model != null) {

                        ledgerFacade.postGeneralLedger(this.model.getTransaction(), jv.getGeneralLedgerLines(), jv.getSubLedgerLines(), this.model.getVoucherDate());

                        response.setSuccessMessage("Entries successfully saved!");
                        response.setSuccess(true);

                    }

                } else {

                    response.setFailureMessage("Invalid user or update is restricted!!");
                    response.setSuccess(false);

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return response;

    }

    @Override
    public Page<CvVoucherDto> findAllApprovedForCvPaged(String query, Pageable pageable) {

        org.springframework.data.domain.Page<JournalVoucher> journalVouchers;

        if(query != null){
            journalVouchers = jvRepo.findAllByQueryAndDocumentStatusForCv("%"+query+"%", com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        } else {
            journalVouchers = jvRepo.findAllByDocumentStatusForCv(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        }

        return journalVouchers.map(entity -> {
                CvVoucherDto dto = new CvVoucherDto();

                dto.setVoucherDate(entity.getVoucherDate());
                dto.setLocalCode(entity.getCode());
                dto.setId(entity.getId());
                dto.setPreparedBy(entity.getCreatedBy().getFullName());
                dto.setAmount(entity.getAmount());
                dto.setParticulars(entity.getCode() + " - " + entity.getExplanation());
                dto.setTransId(entity.getTransaction().getId());
                dto.setExtensionUrl("journal-voucher");
                dto.setBudgetLineItemDetail(null);

                return dto;
        });

    }

    @Override
    public List<JournalVoucherBudgetLineItemDetail> getJournalVoucherBudgetLineItemDetails(Integer jvId) {

        List<JournalVoucherBudgetLineItemDetail> journalVoucherBudgetLineItemDetails = new ArrayList<>();

        try {

            journalVoucherBudgetLineItemDetails = this. journalVoucherBudgetLineItemDetailRepo.findAllByJournalVoucherId(jvId);

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return journalVoucherBudgetLineItemDetails;

    }

    @Override
    public List<JournalVoucherBudgetSubItemDetail> getJournalVoucherBudgetSubItemDetails(Integer jvId) {

        List<JournalVoucherBudgetSubItemDetail> journalVoucherBudgetSubItemDetails = new ArrayList<>();

        try {

            journalVoucherBudgetSubItemDetails = this. journalVoucherBudgetSubItemDetailRepo.findAllByJournalVoucherId(jvId);

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return journalVoucherBudgetSubItemDetails;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostResponse additionalBudgetLineItemDetail(
            JournalVoucherAdditionalBudgetLineItemDetailDto detailDto,
            BindingResult bindingResult,
            MessageSource messageSource) {

        PostResponse response = new PostResponse();

        if (bindingResult.hasErrors()) {

            String errors = bindingResult.getFieldErrors()
                    .stream()
                    .map(error -> messageSource.getMessage(error, LocaleContextHolder.getLocale()))
                    .collect(Collectors.joining(", "));

            response.setSuccess(false);
            response.setFailureMessage(errors);
            return response;
        }

        JournalVoucher journalVoucher = jvRepo.findOneByTransactionId(detailDto.getTransactionId());

        if (journalVoucher == null) {
            response.setSuccess(false);
            response.setFailureMessage("Journal voucher not found.");
            return response;
        }

        List<BudgetDetail> budgetDetails = detailDto.getBudgetDetails();
        List<BudgetLineItemDetail> budgetLineItemDetails = detailDto.getBudgetLineItemDetails();

        boolean hasBudgetDetails = budgetDetails != null && !budgetDetails.isEmpty();
        boolean hasBudgetLineItemDetails = budgetLineItemDetails != null && !budgetLineItemDetails.isEmpty();

        if (!hasBudgetDetails && !hasBudgetLineItemDetails) {
            response.setSuccess(false);
            response.setFailureMessage("Please add at least one budget detail or budget line item detail.");
            return response;
        }

        try {

            // Replace existing budget details
            journalVoucherCashFlowBudgetDetailRepo.deleteByJournalVoucherId(journalVoucher.getId());

            List<JournalVoucherCashFlowBudgetDetail> detailsToSaveBudgetDetail = new ArrayList<>();

            for (BudgetDetail budgetDetail : budgetDetails) {

                if (budgetDetail == null) {
                    continue;
                }

                JournalVoucherCashFlowBudgetDetail detail = new JournalVoucherCashFlowBudgetDetail();
                detail.setJournalVoucher(journalVoucher);
                detail.setBudgetDetail(budgetDetail);
                detail.setTotalBalance(budgetDetail.getTotalBalance());
                detail.setTotalAmount(budgetDetail.getTotalAmount());

                detailsToSaveBudgetDetail.add(detail);

            }

            if (!detailsToSaveBudgetDetail.isEmpty()) {
                journalVoucherCashFlowBudgetDetailRepo.saveAll(detailsToSaveBudgetDetail);
            }

            // Replace existing budget line item details
            journalVoucherBudgetLineItemDetailRepo.deleteByJournalVoucherId(journalVoucher.getId());

            List<JournalVoucherBudgetLineItemDetail> detailsToSaveBudgetLineItemDetail = new ArrayList<>();

            for (BudgetLineItemDetail budgetLineItemDetail : budgetLineItemDetails) {

                if (budgetLineItemDetail == null) {
                    continue;
                }

                JournalVoucherBudgetLineItemDetail detail = new JournalVoucherBudgetLineItemDetail();
                detail.setJournalVoucher(journalVoucher);
                detail.setBudgetLineItemDetail(budgetLineItemDetail);
                detail.setBudgetAmountBalanceCV(budgetLineItemDetail.getBudgetAmountBalanceCV());
                detail.setBudgetAmountBalancePOJORFP(budgetLineItemDetail.getBudgetAmountBalancePOJORFP());

                detailsToSaveBudgetLineItemDetail.add(detail);
            }

            if (!detailsToSaveBudgetLineItemDetail.isEmpty()) {
                journalVoucherBudgetLineItemDetailRepo.saveAll(detailsToSaveBudgetLineItemDetail);
            }

            response.setSuccess(true);
            response.setSuccessMessage("Additional budget line item detail successfully saved!");

        } catch (Exception ex) {
            response.setSuccess(false);
            response.setFailureMessage("An unexpected error occurred. Please try again.");
            throw ex; // rethrow so @Transactional(rollbackFor = Exception.class) actually rolls back
        }

        return response;
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.JV);
    }

    private Map composeJvMap(JournalVoucher journalVoucher) {
        Map map = new HashMap();

        map.put("id", journalVoucher.getId());
        map.put("code", journalVoucher.getCode());
        map.put("voucherDate", journalVoucher.getVoucherDate());
        map.put("year", journalVoucher.getYear());
        map.put("transId", journalVoucher.getTransaction().getId());
        map.put("documentStatus", journalVoucher.getDocumentStatus());
        map.put("amount", journalVoucher.getAmount());
        map.put("explanation", journalVoucher.getExplanation());
        map.put("remarks", journalVoucher.getRemarks());
        map.put("createdAt", journalVoucher.getCreatedAt());
        map.put("updatedAt", journalVoucher.getUpdatedAt());
        map.put("postedBy", journalVoucher.getPostedBy() != null ? journalVoucher.getPostedBy().getFullName():"");
        map.put("payable", journalVoucher.getPayable());
        map.put("cashAdvanceLiquidation", journalVoucher.getCashAdvanceLiquidation());

        Map crUser = new HashMap();
        crUser.put("accountNo", journalVoucher.getCreatedBy().getAccountNo());
        crUser.put("id", journalVoucher.getCreatedBy().getId());
        crUser.put("name", journalVoucher.getCreatedBy().getFullName());
        map.put("createdBy", crUser);

        Map chUser = new HashMap();
        chUser.put("accountNo", journalVoucher.getChecker().getAccountNo());
        chUser.put("id", journalVoucher.getChecker().getId());
        chUser.put("name", journalVoucher.getChecker().getFullName());
        map.put("checker", chUser);

        Map recUser = new HashMap();
        recUser.put("accountNo", journalVoucher.getRecommendingOfficer() != null ? journalVoucher.getRecommendingOfficer().getAccountNo() : "");
        recUser.put("id", journalVoucher.getRecommendingOfficer() != null ? journalVoucher.getRecommendingOfficer().getId() : null);
        recUser.put("name", journalVoucher.getRecommendingOfficer() != null ? journalVoucher.getRecommendingOfficer().getFullName() : "");
        map.put("recommendingOfficer", recUser);

        Map apUser = new HashMap();
        apUser.put("accountNo", journalVoucher.getApprovingOfficer() != null ? journalVoucher.getApprovingOfficer().getAccountNo() : "");
        apUser.put("id", journalVoucher.getApprovingOfficer() != null ? journalVoucher.getApprovingOfficer().getId() : null);
        apUser.put("name", journalVoucher.getApprovingOfficer() != null ? journalVoucher.getApprovingOfficer().getFullName() : "");
        map.put("approvingOfficer", apUser);

        Map auditUser = new HashMap();
        auditUser.put("accountNo", journalVoucher.getAuditingOfficer() != null ? journalVoucher.getAuditingOfficer().getAccountNo() : "");
        auditUser.put("id", journalVoucher.getAuditingOfficer() != null ? journalVoucher.getAuditingOfficer().getId() : null);
        auditUser.put("name", journalVoucher.getAuditingOfficer() != null ? journalVoucher.getAuditingOfficer().getFullName() : "");
        map.put("auditingOfficer", auditUser);

        Map budgetUser = new HashMap();
        budgetUser.put("accountNo", journalVoucher.getBudgetOfficer() != null ? journalVoucher.getBudgetOfficer().getAccountNo() : "");
        budgetUser.put("id", journalVoucher.getBudgetOfficer() != null ? journalVoucher.getBudgetOfficer().getId() : null);
        budgetUser.put("name", journalVoucher.getBudgetOfficer() != null ? journalVoucher.getBudgetOfficer().getFullName() : "");
        map.put("budgetOfficer", budgetUser);

        if(Checker.isValidId(journalVoucher.getInvDocTransactionId())){

            StockReceive stockReceive = stockReceiveRepo.findOneByTransactionId(journalVoucher.getInvDocTransactionId());

            if(stockReceive != null){

                StockReceiveDocumentDto stockReceiveDocumentDto = new StockReceiveDocumentDto();

                stockReceiveDocumentDto.setVoucherDate(stockReceive.getVoucherDate());
                stockReceiveDocumentDto.setLocalCode(stockReceive.getCode());
                stockReceiveDocumentDto.setParticulars(stockReceive.getDescription());
                stockReceiveDocumentDto.setId(stockReceive.getId());
                stockReceiveDocumentDto.setPreparedBy(stockReceive.getCreatedBy().getFullName());
                stockReceiveDocumentDto.setTransactionId(stockReceive.getTransaction().getId());
                stockReceiveDocumentDto.setExtensionUrl("receiving");

                ArrayList<StockTransactionDetail> stockTransactionDetails = stockTransactionDetailRepo.findByStockTransactionTransactionId(stockReceive.getTransaction().getId());

                if(Checker.collectionIsNotEmpty(stockTransactionDetails)){

                    BigDecimal quantity = BigDecimal.ZERO;

                    for (StockTransactionDetail stockTransactionDetail : stockTransactionDetails){

                        quantity = quantity.add(stockTransactionDetail.getQuantity());

                    }

                    stockReceiveDocumentDto.setNetAmount(BigDecimal.ZERO);
                    stockReceiveDocumentDto.setQuantity(quantity);

                }

                map.put("document", stockReceiveDocumentDto);

            }

            StockAdjustment stockAdjustment = stockAdjustmentRepo.findOneByTransactionId(journalVoucher.getInvDocTransactionId());

            if(stockAdjustment != null){

                StockAdjustmentDocumentDto stockAdjustmentDocumentDto = new StockAdjustmentDocumentDto();

                stockAdjustmentDocumentDto.setVoucherDate(stockAdjustment.getVoucherDate());
                stockAdjustmentDocumentDto.setLocalCode(stockAdjustment.getCode());
                stockAdjustmentDocumentDto.setParticulars(stockAdjustment.getRemarks());
                stockAdjustmentDocumentDto.setId(stockAdjustment.getId());
                stockAdjustmentDocumentDto.setPreparedBy(stockAdjustment.getCreatedBy().getFullName());
                stockAdjustmentDocumentDto.setTransactionId(stockAdjustment.getTransaction().getId());
                stockAdjustmentDocumentDto.setExtensionUrl("receiving");

                ArrayList<StockTransactionDetail> stockTransactionDetails = stockTransactionDetailRepo.findByStockTransactionTransactionId(stockAdjustment.getTransaction().getId());

                if(Checker.collectionIsNotEmpty(stockTransactionDetails)){

                    BigDecimal quantity = BigDecimal.ZERO;

                    for (StockTransactionDetail stockTransactionDetail : stockTransactionDetails){

                        quantity = quantity.add(stockTransactionDetail.getQuantity());

                    }

                    stockAdjustmentDocumentDto.setNetAmount(BigDecimal.ZERO);
                    stockAdjustmentDocumentDto.setQuantity(quantity);

                }

                map.put("document", stockAdjustmentDocumentDto);

            }

            ReceivingReport receivingReport = receivingReportRepo.findOneByTransactionId(journalVoucher.getInvDocTransactionId());

            if(receivingReport != null){

                ReceivingReportDocumentDto receivingReportDocumentDto = new ReceivingReportDocumentDto();

                receivingReportDocumentDto.setVoucherDate(receivingReport.getDeliveryDate());
                receivingReportDocumentDto.setLocalCode(receivingReport.getCode());
                receivingReportDocumentDto.setParticulars(receivingReport.getRemarks());
                receivingReportDocumentDto.setId(receivingReport.getId());
                receivingReportDocumentDto.setPreparedBy(receivingReport.getCreatedBy().getFullName());
                receivingReportDocumentDto.setTransactionId(receivingReport.getTransaction().getId());
                receivingReportDocumentDto.setExtensionUrl("receiving-report");
                receivingReportDocumentDto.setNetAmount(receivingReport.getTotalAmount());

                map.put("document", receivingReportDocumentDto);

            }

        }

        if(journalVoucher.getDocumentStatus().getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_APPROVAL.getId())
                && journalVoucher.getApprovingOfficer().getAccountNo().equals(authenticationFacade.getLoggedIn().getAccountNo())){
            map.put("enableCheckBox", Boolean.TRUE);
            map.put("selected", Boolean.TRUE);
        } else {
            map.put("enableCheckBox", Boolean.FALSE);
            map.put("selected", Boolean.FALSE);
        }

        map.put("documentCode", DocumentType.JV.getCode());

        List<JournalVoucherCashFlowBudgetDetail> journalVoucherCashFlowBudgetDetails = journalVoucherCashFlowBudgetDetailRepo.findAllByJournalVoucherId(journalVoucher.getId());

        if(Checker.collectionIsNotEmpty(journalVoucherCashFlowBudgetDetails)){
            map.put("journalVoucherCashFlowBudgetDetails", journalVoucherCashFlowBudgetDetails);
        }

        List<JournalVoucherBudgetLineItemDetail> journalVoucherBudgetLineItemDetails = journalVoucherBudgetLineItemDetailRepo.findAllByJournalVoucherId(journalVoucher.getId());

        if(Checker.collectionIsNotEmpty(journalVoucherBudgetLineItemDetails)){
            map.put("journalVoucherBudgetLineItemDetails", journalVoucherBudgetLineItemDetails);
        }

        List<JournalVoucherBudgetSubItemDetail> journalVoucherBudgetSubItemDetails = journalVoucherBudgetSubItemDetailRepo.findAllByJournalVoucherId(journalVoucher.getId());

        if(Checker.collectionIsNotEmpty(journalVoucherBudgetSubItemDetails)){
            map.put("journalVoucherBudgetSubItemDetails", journalVoucherBudgetSubItemDetails);
        }

        map.put("forAddingBudgetDetail", authenticationFacade.getLoggedIn().getAccountNo().equals(journalVoucher.getBudgetOfficer().getAccountNo()) && journalVoucher.getDocumentStatus().getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_BUDGET_OFFICER.getId()));

        if (journalVoucher.getTransaction() != null) {
            map.put("journalEntries", ledgerDtoers.getVoucherLedgerLines(journalVoucher.getTransaction().getId()));
        }

        return map;
    }

    @Override
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        JournalVoucher journalVoucher = jvRepo.findById(vid).orElse(null);

        if (journalVoucher != null) {
            params.put("TRANS_ID", journalVoucher.getTransaction().getId());
            params.put("VOUCHER_NO", journalVoucher.getCode());
            params.put("V_DATE", journalVoucher.getVoucherDate());
            params.put("APPROVAR", journalVoucher.getApprovingOfficer().getFullName());
            params.put("CHECKER", journalVoucher.getChecker().getFullName());
            params.put("PREPARAR", journalVoucher.getCreatedBy().getFullName());
            params.put("RECOMMENDAR", journalVoucher.getRecommendingOfficer().getFullName());
            params.put("AUDITOR", journalVoucher.getAuditingOfficer().getFullName());
            params.put("EXPLANATION", journalVoucher.getExplanation().trim());
            params.put("PAYABLE", journalVoucher.getPayable() ? "Yes" : "No");
            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.JV, journalVoucher);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer vid) {
        List<CommonLedgerDetail> details = new ArrayList<>();

        JournalVoucher voucher = jvRepo.findById(vid).orElse(null);
        if (voucher != null) {
            details = ledgerDtoers.getVoucherLedgerLines(voucher.getTransaction().getId());
        }
        return new JRBeanCollectionDataSource(details);
    }

    @Override
    @Transactional
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource,
                                      HttpServletRequest request, List<Map> fileToRemove) {

        PostResponse response = this.processUpdate(v, bindingResult, messageSource);

        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
            if (this.model != null) {
                if (mRequest.getFileMap() != null) {
                    fileFacade.removeDocumentAttachment(fileToRemove, this.model.getTransaction().getId());
                    fileFacade.saveDocumentAttachment(mRequest.getFileMap(), this.model.getTransaction().getId());
                }
            }
        }

        return response;
    }

    @Override
    @Transactional
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request) {
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
        JournalVoucher voucher = jvRepo.findFirstByOrderByIdAsc();
        if (voucher != null) {
            return documentDtoer.getDocumentStatuses(voucher.getWorkflow().getId());
        }

        return null;
    }

    private Map forLogMapMain(JournalVoucher jv) {
        return documentLoggerFacade.makeLog(jv);
    }

    private List<Map> makeJvListMap(List<JournalVoucher> vouchers ) {

        List<Map> mapList = new ArrayList<>();

        if (!Checker.collectionIsEmpty(vouchers)) {
            for(JournalVoucher jv:vouchers) {
                mapList.add(composeJvMap(jv));
            }
        }
        return mapList;
    }
}
