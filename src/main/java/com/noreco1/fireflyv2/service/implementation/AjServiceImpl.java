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
import com.noreco1.fireflyv2.controller.response.GeneralLedgerLineDto2;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.controller.response.reports.CommonLedgerDetail;
import com.noreco1.fireflyv2.service.AjService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.AjValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by User on 11/15/2016.
 */
@Service(value = "ajServiceImpl")
public class AjServiceImpl implements AjService, PrintableVoucher {

    private AdjustmentJournal model;

    @Autowired
    AdjustmentJournalRepo ajRepo;

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
    OfficeRepo officeRepo;

    @Autowired
    EmployeeRepo employeeRepo;

    @Override
    @Transactional
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        AdjustmentJournal adjustmentJournal =  ajRepo.findById(postData.getDocumentId()).orElse(null);

        if (adjustmentJournal != null && adjustmentJournal.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId()) {
            // for logging
            Map oldAjMap = this.forLogMapMain(adjustmentJournal);

            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(adjustmentJournal, adjustmentJournal.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            adjustmentJournal.setDocumentStatus(afterActionDocumentStatus);
            adjustmentJournal.setUpdatedAt(null);
            adjustmentJournal = ajRepo.save(adjustmentJournal);

            // for logging
            Map newAjMap = this.forLogMapMain(adjustmentJournal);
            newAjMap.put("remarks", postData.getRemarks());

            if (adjustmentJournal != null) {
                documentProcessingFacade.processAction(adjustmentJournal.getTransaction(), actionMap, null, processedBy, DocumentToTableMap.AJ.toString());
                documentLoggerFacade.log(adjustmentJournal.getTransaction(), authenticationFacade.getLoggedIn(), oldAjMap, newAjMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }
        }
        return response;
    }

    @Override
    @Transactional
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        AdjustmentJournal aj = (AdjustmentJournal) v;
        return this.processCreate(aj, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        AdjustmentJournal aj = (AdjustmentJournal) v;
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        try {
            AjValidator validator = new AjValidator();
            validator.setmonthlyCycleRepo(this.monthlyCycleRepo);
            validator.setAllocationFactorRepo(this.allocationFactorRepo);
            validator.setLegderFacade(this.ledgerFacade);
            validator.validate(aj, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
            } else {
                User createdBy = authenticationFacade.getLoggedIn();
                AdjustmentJournal existingAj = null;

                Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(aj.getVoucherDate()));
                User approvingOfficer = userRepo.findOneByAccountNo(aj.getApprovingOfficer().getAccountNo());
                User checker = userRepo.findOneByAccountNo(aj.getChecker().getAccountNo());
                User recApp = userRepo.findOneByAccountNo(aj.getRecommendingOfficer().getAccountNo());
//                User auditor = userRepo.findOneByAccountNo(aj.getAuditor().getAccountNo());

                Boolean insertMode = aj.getId() == null;
                Transaction transaction = generatorFacade.transaction();

                if (insertMode) { // insert mode

                    Object latestAjCode = ajRepo.findLatestAjCodeByYear(voucherYear);
                    aj.setCode(generatorFacade.voucherCodeNoOffice("AV", (latestAjCode == null ? "" : String.valueOf(latestAjCode)), aj.getVoucherDate(), GlobalConstant.COUNTER_PAD_4));

                    DocumentStatus documentStatus = new DocumentStatus();
                    documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                    Workflow wf = new Workflow();
                    wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.AJ.getId());

                    aj.setDocumentStatus(documentStatus);
                    aj.setCreatedBy(createdBy);
                    aj.setTransaction(transaction);
                    aj.setWorkflow(wf);

                    existingAj = aj;
                } else {
                    existingAj = ajRepo.findById(aj.getId()).orElse(null);

                    if (existingAj == null) {
                        ArrayList<String> messages = new ArrayList();
                        messages.add("AJ is not available!");
                        response.setMessages(messages);
                        return response;
                    }

                    List<Integer> statusAllowed = new ArrayList();
                    statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                    statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.RETURNED_TO_CREATOR.getId());

                    if (statusAllowed.indexOf(existingAj.getDocumentStatus().getId()) < 0) {
                        ArrayList<String> messages = new ArrayList();
                        messages.add("Action is not allowed");

                        response.setNotAuthorized(true);
                        response.setMessages(messages);
                        response.setSuccess(false);

                        return response;
                    }
                }
                // use for document logging
                Map oldAjMap = this.forLogMapMain(existingAj);

                // editable fields
                existingAj.setExplanation(aj.getExplanation());
                existingAj.setVoucherDate(aj.getVoucherDate());
                existingAj.setApprovingOfficer(approvingOfficer);
                existingAj.setChecker(checker);
                existingAj.setRecommendingOfficer(recApp);
//                existingAj.setAuditor(auditor);
                existingAj.setYear(voucherYear);
                existingAj.setAmount(aj.getAmount());

                this.model = ajRepo.save(existingAj);

                if (this.model != null) {

                    // start: update default signatories
                    signatoryFacade.aj(this.model);
                    // end: update default signatories

                    ledgerFacade.postGeneralLedger(this.model.getTransaction(), aj.getGeneralLedgerLines(), aj.getSubLedgerLines(), this.model.getVoucherDate());

                    if (insertMode) { // document processing logging only when adding document
                        documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                        oldAjMap = null; // new document has no old value
                    }

                    // generic document logging here
                    // old value only
                    DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldAjMap, null);

                    response.setLogId(log != null ? log.getId() : 0);
                    response.setModelId(this.model.getId());
                    response.setSuccessMessage("AJ successfully saved!");
                    response.setSuccess(true);
                }
            }
        }catch (Exception ex) {
            Logger.getLogger(AjServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return response;
    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            AdjustmentJournal aj = ajRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (aj != null) {
                Map map = forLogMapMain(aj);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public List<Map> findAll() {
        List<AdjustmentJournal> vouchers = ajRepo.findAll();
        return this.makeAjListMap(vouchers);
    }

    @Override
    public Map findById(Integer id) {
        Map map = new HashMap();

        AdjustmentJournal adjustmentJournal = ajRepo.findById(id).orElse(null);
        if (adjustmentJournal != null) {
            map = composeAjMap(adjustmentJournal);
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

            List<AdjustmentJournal> vouchers = ajRepo.findByDocumentStatusIdAndVoucherDateBetween(id, fromDate, toDate);
            return this.makeAjListMap(vouchers);
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

            List<AdjustmentJournal> vouchers = ajRepo.findByVoucherDateBetweenAndDocumentStatusIdNotInOrderByCode(fromDate, toDate, Arrays.asList(ids));
            return this.makeAjListMap(vouchers);

        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<GeneralLedgerLineDto2> closingDefaultEntries(String asOfDate) {

        List<GeneralLedgerLineDto2> ledgerLineDtos = new ArrayList<>();
        try {
            List<Object[]> generalLedgerLines = ajRepo.getLedgerEntriesByAsOfDate(asOfDate);

            if (generalLedgerLines != null) {
                for (Object[] line : generalLedgerLines) {
                    GeneralLedgerLineDto2 lineDto = new GeneralLedgerLineDto2();

                    BigDecimal debit = new BigDecimal(line[0].toString());
                    BigDecimal credit = new BigDecimal(line[1].toString());
                    Integer accountId = (Integer)line[2];
                    String code = (String)line[3];
                    String title = (String)line[4];
                    Boolean hasSl = (Boolean)line[5];

                    lineDto.setDescription(title);
                    lineDto.setAccountId(accountId);
                    lineDto.setCode(code);
                    lineDto.setHasSL(hasSl);

                    
                    /**
                     *  when an account has both debit and credit balances.
                     *  In this case:
                     *   If debit > credit then entry = debit - credit, side=debit
                     *   if debit < credit then entry = credit - debit, side = credit
                     *
                     */
                    if (debit.compareTo(credit) > 0) {
                        debit = debit.subtract(credit);
                        credit = BigDecimal.ZERO;
                    } else if (credit.compareTo(debit) > 0) {
                        credit = credit.subtract(debit);
                        debit = BigDecimal.ZERO;
                    } else {
                        continue;
                    }
                    lineDto.setDebit(credit); // reverse
                    lineDto.setCredit(debit); // reverse

                    // format debit & credit
                    lineDto.setDebitStr(new DecimalFormat("#,##0.00").format(lineDto.getDebit()));
                    lineDto.setCreditStr(new DecimalFormat("#,##0.00").format(lineDto.getCredit()));

                    lineDto.setSearchText(StringFormatter.accountSearchText(code, title));
                    
                    ledgerLineDtos.add(lineDto);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException(e);
        }

        return ledgerLineDtos;
    }

    @Override
    public List<GeneralLedgerLineDto2> reopeningDefaultEntries(String year) {
        List<GeneralLedgerLineDto2> ledgerLineDtos = new ArrayList<>();
        try {
            List<Object[]> generalLedgerLines = ajRepo.getLedgerEntriesClosing(Integer.parseInt(year));

            if (generalLedgerLines != null) {
                for (Object[] line : generalLedgerLines) {
                    GeneralLedgerLineDto2 lineDto = new GeneralLedgerLineDto2();

                    BigDecimal debit = new BigDecimal(line[0].toString());
                    BigDecimal credit = new BigDecimal(line[1].toString());
                    Integer accountId = (Integer)line[2];
                    String code = (String)line[3];
                    String title = (String)line[4];
                    Boolean hasSl = (Boolean)line[5];

//                    lineDto.setDebit(debit);
//                    lineDto.setCredit(credit);
                    /**
                     *  when an account has both debit and credit balances.
                     *  In this case:
                     *   If debit > credit then entry = debit - credit, side=debit
                     *   if debit < credit then entry = credit - debit, side = credit
                     *
                     */
                    if (debit.compareTo(credit) > 0) {
                        debit = debit.subtract(credit);
                        credit = BigDecimal.ZERO;
                    } else if (credit.compareTo(debit) > 0) {
                        credit = credit.subtract(debit);
                        debit = BigDecimal.ZERO;
                    } else {
                        continue;
                    }
                    lineDto.setDebit(credit); // reverse
                    lineDto.setCredit(debit); // reverse

                    lineDto.setDescription(title);
                    lineDto.setAccountId(accountId);
                    lineDto.setCode(code);
                    lineDto.setHasSL(hasSl);

                    // format debit & credit
                    lineDto.setDebitStr(new DecimalFormat("#,##0.00").format(lineDto.getDebit()));
                    lineDto.setCreditStr(new DecimalFormat("#,##0.00").format(lineDto.getCredit()));

                    lineDto.setSearchText(StringFormatter.accountSearchText(code, title));

                    ledgerLineDtos.add(lineDto);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException(e);
        }

        return ledgerLineDtos;
    }

    @Override
    public PostResponse updateEntries(AdjustmentJournal aj, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            Boolean insertMode = Checker.isValidId(aj.getId());

            if(insertMode){

                AdjustmentJournal existingAj = this.ajRepo.findById(aj.getId()).orElse(null);

                if(existingAj.getChecker().getAccountNo().equals(authenticationFacade.getLoggedIn().getAccountNo())){

                    existingAj.setAmount(aj.getAmount());

                    this.model = ajRepo.save(existingAj);

                    if (this.model != null) {

                        ledgerFacade.postGeneralLedger(this.model.getTransaction(), aj.getGeneralLedgerLines(), aj.getSubLedgerLines(), this.model.getVoucherDate());

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
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.AJ);
    }

    private Map composeAjMap(AdjustmentJournal adjustmentJournal) {
        Map map = new HashMap();

        map.put("id", adjustmentJournal.getId());
        map.put("code", adjustmentJournal.getCode());
        map.put("voucherDate", adjustmentJournal.getVoucherDate());
        map.put("year", adjustmentJournal.getYear());
        map.put("transId", adjustmentJournal.getTransaction().getId());
        map.put("documentStatus", adjustmentJournal.getDocumentStatus());
        map.put("amount", adjustmentJournal.getAmount());
        map.put("explanation", adjustmentJournal.getExplanation());
        map.put("remarks", adjustmentJournal.getRemarks());
        map.put("createdAt", adjustmentJournal.getCreatedAt());
        map.put("updatedAt", adjustmentJournal.getUpdatedAt());
        map.put("transactionType", adjustmentJournal.getTransactionType());

        Map crUser = new HashMap();
        crUser.put("accountNo", adjustmentJournal.getCreatedBy().getAccountNo());
        crUser.put("id", adjustmentJournal.getCreatedBy().getId());
        crUser.put("name", adjustmentJournal.getCreatedBy().getFullName());
        map.put("createdBy", crUser);

        Map chUser = new HashMap();
        chUser.put("accountNo", adjustmentJournal.getChecker().getAccountNo());
        chUser.put("id", adjustmentJournal.getChecker().getId());
        chUser.put("name", adjustmentJournal.getChecker().getFullName());
        map.put("checker", chUser);

        Map recUser = new HashMap();
        recUser.put("accountNo", adjustmentJournal.getRecommendingOfficer().getAccountNo());
        recUser.put("id", adjustmentJournal.getRecommendingOfficer().getId());
        recUser.put("name", adjustmentJournal.getRecommendingOfficer().getFullName());
        map.put("recommendingOfficer", recUser);

        /*Map auUser = new HashMap();
        auUser.put("accountNo", adjustmentJournal.getAuditor().getAccountNo());
        auUser.put("id", adjustmentJournal.getAuditor().getId());
        auUser.put("name", adjustmentJournal.getAuditor().getFullName());
        map.put("auditor", auUser);*/

        Map apUser = new HashMap();
        apUser.put("accountNo", adjustmentJournal.getApprovingOfficer().getAccountNo());
        apUser.put("id", adjustmentJournal.getApprovingOfficer().getId());
        apUser.put("name", adjustmentJournal.getApprovingOfficer().getFullName());
        map.put("approvingOfficer", apUser);

        Map postedBy = new HashMap();
        if(adjustmentJournal.getPostedBy() != null) {

            postedBy.put("accountNo", adjustmentJournal.getPostedBy().getAccountNo());
            postedBy.put("id", adjustmentJournal.getPostedBy().getId());
            postedBy.put("name", adjustmentJournal.getPostedBy().getFullName());
            map.put("postedBy", postedBy);
        } else {
            postedBy.put("name", "");
        }

        map.put("postedBy", postedBy);

        if(adjustmentJournal.getDocumentStatus().getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_APPROVAL.getId())
                && adjustmentJournal.getApprovingOfficer().getAccountNo().equals(authenticationFacade.getLoggedIn().getAccountNo())){
            map.put("enableCheckBox", Boolean.TRUE);
            map.put("selected", Boolean.TRUE);
        } else {
            map.put("enableCheckBox", Boolean.FALSE);
            map.put("selected", Boolean.FALSE);
        }

        map.put("documentCode", DocumentType.AJ.getCode());

        if (adjustmentJournal.getTransaction() != null) {
            map.put("journalEntries", ledgerDtoers.getVoucherLedgerLines(adjustmentJournal.getTransaction().getId()));
        }

        return map;
    }

    @Override
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        AdjustmentJournal adjustmentJournal = ajRepo.findById(vid).orElse(null);

        if (adjustmentJournal != null) {
            params.put("TRANS_ID", adjustmentJournal.getTransaction().getId());
            params.put("VOUCHER_NO", adjustmentJournal.getCode());
            params.put("V_DATE", adjustmentJournal.getVoucherDate());
//            params.put("AUDITOR", adjustmentJournal.getAuditor().getFullName());
            params.put("EXPLANATION", adjustmentJournal.getExplanation().trim());
            params = signatureFacade.getDocumentSignature(params, DocumentType.AJ, adjustmentJournal);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer vid) {
        List<CommonLedgerDetail> details = new ArrayList<>();

        AdjustmentJournal voucher = ajRepo.findById(vid).orElse(null);
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
        AdjustmentJournal voucher = ajRepo.findFirstByOrderByIdAsc();
        if (voucher != null) {
            return documentDtoer.getDocumentStatuses(voucher.getWorkflow().getId());
        }

        return null;
    }

    private Map forLogMapMain(AdjustmentJournal aj) {
        return documentLoggerFacade.makeLog(aj);
    }

    private List<Map> makeAjListMap(List<AdjustmentJournal> vouchers ) {
        List<Map> mapList = new ArrayList<>();

        if (!Checker.collectionIsEmpty(vouchers)) {
            for(AdjustmentJournal aj:vouchers) {
                mapList.add(composeAjMap(aj));
            }
        }
        return mapList;
    }
}

