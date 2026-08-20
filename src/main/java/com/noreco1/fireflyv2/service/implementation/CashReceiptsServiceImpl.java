package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.dtoers.LedgerDtoerImpl;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.controller.response.reports.CommonLedgerDetail;
import com.noreco1.fireflyv2.service.CashReceiptsService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.CashReceiptsValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import com.noreco1.fireflyv2.common.helpers.CurrencyIntoWords;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@Service(value = "cashReceiptsServiceImpl")
public class CashReceiptsServiceImpl implements CashReceiptsService, PrintableVoucher {

    private CashReceipts model;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    CashReceiptsRepo crRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    SlEntityRepo slEntityRepo;

    @Autowired
    TemporaryBatchRepo temporaryBatchRepo;

    @Autowired
    LedgerFacadeImpl ledgerFacade;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    LedgerDtoerImpl ledgerDtoers;

    @Autowired
    MonthlyCycleRepo monthlyCycleRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    SignatureFacade signatureFacade;

    @Autowired
    DocumentDtoer documentDtoer;

    @Autowired
    AllocationFactorRepo allocationFactorRepo;

    @Autowired
    EmployeeRepo employeeRepo;

    @Override
    @Transactional
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        CashReceipts cashReceipts = (CashReceipts) v;
        return this.processCreate(cashReceipts, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        CashReceipts cashReceipts = (CashReceipts) v;
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        CashReceiptsValidator validator = new CashReceiptsValidator();
        validator.setLegderFacade(this.ledgerFacade);
        validator.setmonthlyCycleRepo(this.monthlyCycleRepo);
        validator.setAllocationFactorRepo(this.allocationFactorRepo);
        validator.validate(cashReceipts, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            CashReceipts existingCashReceipts = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(cashReceipts.getVoucherDate()));
            User approvingOfficer = userRepo.findOneByAccountNo(cashReceipts.getApprovingOfficer().getAccountNo());

            Boolean insertMode = cashReceipts.getId() == null;
            Boolean withTempBatch = cashReceipts.getTempBatchId() != null && cashReceipts.getTempBatchId() > 0;
            Transaction transaction = null;

            if (withTempBatch) {
                TemporaryBatch temporaryBatch = temporaryBatchRepo.findById(cashReceipts.getTempBatchId()).orElse(null);
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

                Object latestCrCode = crRepo.findLatestCrCodeByYear(voucherYear);
                cashReceipts.setCode(generatorFacade.voucherCodeNoOffice("CRV", (latestCrCode == null ? "" : String.valueOf(latestCrCode)), cashReceipts.getVoucherDate(), GlobalConstant.COUNTER_PAD_4));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                Workflow wf = new Workflow();
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.CASH_RECEIPTS.getId());

                cashReceipts.setDocumentStatus(documentStatus);
                cashReceipts.setCreatedBy(createdBy);
                cashReceipts.setTransaction(transaction);
                cashReceipts.setWorkflow(wf);

                existingCashReceipts = cashReceipts;

            } else {
                existingCashReceipts = crRepo.findById(cashReceipts.getId()).orElse(null);

                List<Integer> statusAllowed = new ArrayList();
                statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.RETURNED_TO_CREATOR.getId());

                if (statusAllowed.indexOf(existingCashReceipts.getDocumentStatus().getId()) < 0) {

                    ArrayList<String> messages = new ArrayList();
                    messages.add("Action is not allowed");

                    response.setNotAuthorized(true);
                    response.setMessages(messages);
                    response.setSuccess(false);

                    return response;
                }
            }

            // use for document logging
            Map oldCashReceiptsMap = this.forLogMapMain(existingCashReceipts);

            // editable fields
            existingCashReceipts.setParticulars(cashReceipts.getParticulars());
            existingCashReceipts.setVoucherDate(cashReceipts.getVoucherDate());
            existingCashReceipts.setApprovingOfficer(approvingOfficer);
            existingCashReceipts.setYear(voucherYear);
            existingCashReceipts.setAmount(cashReceipts.getAmount());

            this.model = crRepo.save(existingCashReceipts);

            if (this.model != null) {

                // start: update default signatories
                signatoryFacade.crv(this.model);
                // end: update default signatories

                ledgerFacade.postGeneralLedger(this.model.getTransaction(), cashReceipts.getGeneralLedgerLines(), cashReceipts.getSubLedgerLines(), this.model.getVoucherDate());

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldCashReceiptsMap = null;
                }

                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldCashReceiptsMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());
                response.setSuccessMessage("CRV successfully saved!");
                response.setSuccess(true);
            }
        }
        return response;
    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            CashReceipts cashReceipts = crRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (cashReceipts != null) {
                Map map = forLogMapMain(cashReceipts);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.CRV);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashReceiptsListDto> findAll() {

        List<CashReceipts> vouchers = crRepo.findAll();
        return this.makeCRVListDto(vouchers);
    }

    @Override
    public CashReceiptsDto findById(Integer id) {

        CashReceipts cashReceipts =  crRepo.findById(id).orElse(null);
        CashReceiptsDto cashReceiptsDto = new CashReceiptsDto();

        if (cashReceipts != null) {
            cashReceiptsDto.setId(cashReceipts.getId());
            cashReceiptsDto.setParticulars(cashReceipts.getParticulars());
            cashReceiptsDto.setLocalCode(cashReceipts.getCode());
            cashReceiptsDto.setTransId(cashReceipts.getTransaction().getId());

            SlEntity approvingOfficer = slEntityRepo.findById(cashReceipts.getApprovingOfficer().getAccountNo()).orElse(null);

            cashReceiptsDto.setApprovingOfficer(approvingOfficer);
            cashReceiptsDto.setVoucherDate(cashReceipts.getVoucherDate());
            cashReceiptsDto.setAmount(cashReceipts.getAmount());
            cashReceiptsDto.setDocumentStatus(cashReceipts.getDocumentStatus());
            cashReceiptsDto.setCreated(cashReceipts.getCreatedAt());
            cashReceiptsDto.setLastUpdated(cashReceipts.getUpdatedAt());
            cashReceiptsDto.setPreparedBy(cashReceipts.getCreatedBy());
            cashReceiptsDto.setOffice(cashReceipts.getOffice());

            if(cashReceipts.getPostedBy() != null) {
                cashReceiptsDto.setPostedBy(slEntityRepo.findById(cashReceipts.getPostedBy().getAccountNo()).orElse(null));
            }

            cashReceiptsDto.setJournalEntries(ledgerDtoers.getVoucherLedgerLines(cashReceipts.getTransaction().getId()));
        }

        return  cashReceiptsDto;
    }

    @Override
    public  List<CashReceiptsListDto> findByStatusId(Integer id) {
        List<CashReceipts> vouchers = crRepo.findByDocumentStatusId(id);

        List<CashReceiptsListDto> returnVouchers = new ArrayList<>();
        if (!Checker.collectionIsEmpty(vouchers)) {
            for(CashReceipts sv : vouchers) {
                CashReceiptsListDto cashReceiptsListDto = new CashReceiptsListDto();
                cashReceiptsListDto.setId(sv.getId());
                cashReceiptsListDto.setTransId(sv.getTransaction().getId());
                cashReceiptsListDto.setAmount(sv.getAmount());
                cashReceiptsListDto.setDate(sv.getVoucherDate());
                cashReceiptsListDto.setLocalCode(sv.getCode());
                cashReceiptsListDto.setParticulars(sv.getParticulars());
                cashReceiptsListDto.setStatus(sv.getDocumentStatus().getStatus());

                returnVouchers.add(cashReceiptsListDto);
            }

            return returnVouchers;
        }
        return null;
    }

    @Override
    public List<CashReceiptsListDto> findByDateRangeAndStatusId(String from, String to, Integer id) {
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }
            
            List<CashReceipts> vouchers = crRepo.findByDocumentStatusIdAndVoucherDateBetween(id, fromDate, toDate);
            return this.makeCRVListDto(vouchers);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<CashReceiptsListDto> findByDateRange(String from, String to) {
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

            List<CashReceipts> vouchers = crRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(fromDate, toDate, Arrays.asList(ids));
            return this.makeCRVListDto(vouchers);

        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public PostResponse updateEntries(CashReceipts cr, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            Boolean insertMode = Checker.isValidId(cr.getId());

            if(insertMode){

                CashReceipts existingCr = this.crRepo.findById(cr.getId()).orElse(null);

                if(existingCr.getChecker().getAccountNo().equals(authenticationFacade.getLoggedIn().getAccountNo())){

                    existingCr.setAmount(cr.getAmount());

                    this.model = crRepo.save(existingCr);

                    if (this.model != null) {

                        ledgerFacade.postGeneralLedger(this.model.getTransaction(), cr.getGeneralLedgerLines(), cr.getSubLedgerLines(), this.model.getVoucherDate());

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
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        CashReceipts cashReceipts = crRepo.findById(vid).orElse(null);

        if (cashReceipts != null) {
            params.put("VOUCHER_NO", cashReceipts.getCode());
            params.put("V_DATE", cashReceipts.getVoucherDate());
            params.put("AMOUNT_IN_WORDS", CurrencyIntoWords.convert(cashReceipts.getAmount()));
            params.put("TOTAL", cashReceipts.getAmount());
            params.put("EXPLANATION", cashReceipts.getParticulars());
            params.put("REMARKS","");

            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.CRV, cashReceipts);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer vid) {
        List<CommonLedgerDetail> details = new ArrayList<>();

        CashReceipts voucher = crRepo.findById(vid).orElse(null);
        if (voucher != null) {
            details = ledgerDtoers.getVoucherLedgerLines(voucher.getTransaction().getId());
        }
        return new JRBeanCollectionDataSource(details);
    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        CashReceipts cashReceipts =  crRepo.findById(postData.getDocumentId()).orElse(null);

        if (cashReceipts != null && cashReceipts.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId()) {

            // for logging
            Map oldCashReceiptsMap = this.forLogMapMain(cashReceipts);

            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(cashReceipts, cashReceipts.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            cashReceipts.setDocumentStatus(afterActionDocumentStatus);
            cashReceipts.setUpdatedAt(null);
            cashReceipts = crRepo.save(cashReceipts);

            // for logging
            Map newCashReceiptsMap = this.forLogMapMain(cashReceipts);
            newCashReceiptsMap.put("remarks", postData.getRemarks());

            if (cashReceipts != null) {
                documentProcessingFacade.processAction(cashReceipts.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(cashReceipts.getTransaction(), authenticationFacade.getLoggedIn(), oldCashReceiptsMap, newCashReceiptsMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }

        }
        return response;
    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource,
                                      HttpServletRequest request, List<Map> fileToRemove) {
        return this.processUpdate(v, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request) {
        return this.processCreate(v, bindingResult, messageSource);
    }

    @Override
    public List<DocumentStatus> getDocumentsStatuses() {
        CashReceipts voucher = crRepo.findFirstByOrderByIdAsc();
        if (voucher != null) {
            return documentDtoer.getDocumentStatuses(voucher.getWorkflow().getId());
        }
        return null;
    }

    private List<CashReceiptsListDto> makeCRVListDto(List<CashReceipts> vouchers ) {
        List<CashReceiptsListDto> returnVouchers = new ArrayList<>();

        if (!Checker.collectionIsEmpty(vouchers)) {
            for(CashReceipts cr : vouchers) {
                CashReceiptsListDto cashReceiptsListDto = new CashReceiptsListDto();
                cashReceiptsListDto.setId(cr.getId());
                cashReceiptsListDto.setTransId(cr.getTransaction().getId());
                cashReceiptsListDto.setAmount(cr.getAmount());
                cashReceiptsListDto.setDate(cr.getVoucherDate());
                cashReceiptsListDto.setLocalCode(cr.getCode());
                cashReceiptsListDto.setParticulars(cr.getParticulars());
                cashReceiptsListDto.setStatus(cr.getDocumentStatus().getStatus());

                if(cr.getDocumentStatus().getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_APPROVAL.getId())
                        && cr.getApprovingOfficer().getAccountNo().equals(authenticationFacade.getLoggedIn().getAccountNo())){
                    cashReceiptsListDto.setEnableCheckBox(Boolean.TRUE);
                    cashReceiptsListDto.setSelected(Boolean.TRUE);
                } else {
                    cashReceiptsListDto.setEnableCheckBox(Boolean.FALSE);
                    cashReceiptsListDto.setSelected(Boolean.FALSE);
                }

                cashReceiptsListDto.setDocumentCode(com.noreco1.fireflyv2.model.enums.DocumentType.CRV.getCode());

                returnVouchers.add(cashReceiptsListDto);
            }
        }

        return returnVouchers;
    }

    private Map forLogMapMain(CashReceipts cashReceipts) {
        return documentLoggerFacade.makeLog(cashReceipts);
    }
}
