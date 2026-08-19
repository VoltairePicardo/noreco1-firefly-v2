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
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.controller.response.SalesVoucherDto;
import com.noreco1.fireflyv2.controller.response.SalesVoucherListDto;
import com.noreco1.fireflyv2.controller.response.reports.CommonLedgerDetail;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.SalesVoucherService;
import com.noreco1.fireflyv2.validator.SalesVoucherValidator;
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

@Service(value = "salesVoucherServiceImpl")
public class SalesVoucherServiceImpl implements SalesVoucherService, PrintableVoucher {

    private SalesVoucher model;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    SalesVoucherRepo svRepo;

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
    LedgerDtoerImpl ledgerDtoers;

    @Autowired
    MonthlyCycleRepo monthlyCycleRepo;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    SignatoryFacade  signatoryFacade;

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
        SalesVoucher sv = (SalesVoucher) v;
        return this.processCreate(sv, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {

        SalesVoucher sv = (SalesVoucher) v;
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        SalesVoucherValidator validator = new SalesVoucherValidator();
        validator.setLegderFacade(this.ledgerFacade);
        validator.setmonthlyCycleRepo(this.monthlyCycleRepo);
        validator.setAllocationFactorRepo(this.allocationFactorRepo);
        validator.validate(sv, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            SalesVoucher existingSv = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(sv.getVoucherDate()));
            User approvingOfficer = userRepo.findOneByAccountNo(sv.getApprovingOfficer().getAccountNo());

            Boolean insertMode = sv.getId() == null;
            Boolean withTempBatch = sv.getTempBatchId() != null && sv.getTempBatchId() > 0;
            Transaction transaction = null;

            if (withTempBatch) {
                TemporaryBatch temporaryBatch = temporaryBatchRepo.findById(sv.getTempBatchId()).orElse(null);
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

                Object latestSvCode = svRepo.findLatestSvCodeByYear(voucherYear);
                sv.setCode(generatorFacade.voucherCodeNoOffice("SV", (latestSvCode == null ? "" : String.valueOf(latestSvCode)), sv.getVoucherDate(), GlobalConstant.COUNTER_PAD_4));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                Workflow wf = new Workflow();
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.SALES_VOUCHER.getId());

                sv.setDocumentStatus(documentStatus);
                sv.setCreatedBy(createdBy);
                sv.setTransaction(transaction);
                sv.setWorkflow(wf);

                existingSv = sv;

            } else {
                existingSv = svRepo.findById(sv.getId()).orElse(null);

                List<Integer> statusAllowed = new ArrayList();
                statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.RETURNED_TO_CREATOR.getId());

                if (statusAllowed.indexOf(existingSv.getDocumentStatus().getId()) < 0) {

                    ArrayList<String> messages = new ArrayList();
                    messages.add("Action is not allowed");

                    response.setNotAuthorized(true);
                    response.setMessages(messages);
                    response.setSuccess(false);

                    return response;
                }
            }

            // use for document logging
            Map oldSalesVoucherMap = this.forLogMapMain(existingSv);

            // editable fields
            existingSv.setParticulars(sv.getParticulars());
            existingSv.setVoucherDate(sv.getVoucherDate());
            existingSv.setApprovingOfficer(approvingOfficer);
            existingSv.setYear(voucherYear);
            existingSv.setAmount(sv.getAmount());

            this.model = svRepo.save(existingSv);

            if (this.model != null) {
                // start: update default signatories
                signatoryFacade.sv(this.model);
                // end: update default signatories

                ledgerFacade.postGeneralLedger(this.model.getTransaction(), sv.getGeneralLedgerLines(), sv.getSubLedgerLines(), this.model.getVoucherDate());

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldSalesVoucherMap = null;
                }

                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldSalesVoucherMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());

                response.setModelId(this.model.getId());
                response.setSuccessMessage("Sales Voucher successfully saved!");
                response.setSuccess(true);
            }
        }
        return response;
    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            SalesVoucher salesVoucher = svRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (salesVoucher != null) {
                Map map = forLogMapMain(salesVoucher);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.SV);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesVoucherListDto> findAll() {
        List<SalesVoucher> vouchers = svRepo.findAll();
        return this.makeSVListDto(vouchers);
    }

    @Override
    public SalesVoucherDto findById(Integer id) {

        SalesVoucher salesVoucher =  svRepo.findById(id).orElse(null);
        SalesVoucherDto svDto = new SalesVoucherDto();

        if (salesVoucher != null) {
            svDto.setId(salesVoucher.getId());
            svDto.setParticulars(salesVoucher.getParticulars());
            svDto.setLocalCode(salesVoucher.getCode());
            svDto.setTransId(salesVoucher.getTransaction().getId());

            SlEntity approvingOfficer = slEntityRepo.findById(salesVoucher.getApprovingOfficer().getAccountNo()).orElse(null);

            svDto.setApprovingOfficer(approvingOfficer);
            svDto.setVoucherDate(salesVoucher.getVoucherDate());
            svDto.setAmount(salesVoucher.getAmount());
            svDto.setDocumentStatus(salesVoucher.getDocumentStatus());
            svDto.setCreated(salesVoucher.getCreatedAt());
            svDto.setLastUpdated(salesVoucher.getUpdatedAt());
            svDto.setPreparedBy(salesVoucher.getCreatedBy());
            svDto.setOffice(salesVoucher.getOffice());

            if(salesVoucher.getPostedBy() != null) {
                svDto.setPostedBy(slEntityRepo.findById(salesVoucher.getPostedBy().getAccountNo()).orElse(null));
            }

            svDto.setJournalEntries(ledgerDtoers.getVoucherLedgerLines(salesVoucher.getTransaction().getId()));
        }

        return  svDto;
    }

    @Override
    public  List<SalesVoucherListDto> findByStatusId(Integer id) {
        List<SalesVoucher> vouchers = svRepo.findByDocumentStatusId(id);

        List<SalesVoucherListDto> returnVouchers = new ArrayList<>();
        if (!Checker.collectionIsEmpty(vouchers)) {
            for(SalesVoucher sv : vouchers) {
                SalesVoucherListDto svListDto = new SalesVoucherListDto();
                svListDto.setId(sv.getId());
                svListDto.setTransId(sv.getTransaction().getId());
                svListDto.setAmount(sv.getAmount());
                svListDto.setDate(sv.getVoucherDate());
                svListDto.setLocalCode(sv.getCode());
                svListDto.setParticulars(sv.getParticulars());
                svListDto.setStatus(sv.getDocumentStatus().getStatus());

                returnVouchers.add(svListDto);
            }

            return returnVouchers;
        }
        return null;
    }

    @Override
    public List<SalesVoucherListDto> findByDateRangeAndStatusId(String from, String to, Integer id) {
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            List<SalesVoucher> vouchers = svRepo.findByDocumentStatusIdAndVoucherDateBetween(id, fromDate, toDate);
            return this.makeSVListDto(vouchers);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<SalesVoucherListDto> findByDateRange(String from, String to) {
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

            List<SalesVoucher> vouchers = svRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(fromDate, toDate, Arrays.asList(ids));
            return this.makeSVListDto(vouchers);

        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public PostResponse updateEntries(SalesVoucher sv, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            Boolean insertMode = Checker.isValidId(sv.getId());

            if(insertMode){

                SalesVoucher existingSv = this.svRepo.findById(sv.getId()).orElse(null);

                if(existingSv.getChecker().getAccountNo().equals(authenticationFacade.getLoggedIn().getAccountNo())){

                    existingSv.setAmount(sv.getAmount());

                    this.model = svRepo.save(existingSv);

                    if (this.model != null) {

                        ledgerFacade.postGeneralLedger(this.model.getTransaction(), sv.getGeneralLedgerLines(), sv.getSubLedgerLines(), this.model.getVoucherDate());

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

        SalesVoucher salesVoucher = svRepo.findById(vid).orElse(null);

        if (salesVoucher != null) {
            params.put("VOUCHER_NO", salesVoucher.getCode());
            params.put("V_DATE", salesVoucher.getVoucherDate());
            params.put("AMOUNT_IN_WORDS", CurrencyIntoWords.convert(salesVoucher.getAmount()));
            params.put("TOTAL", salesVoucher.getAmount());
            params.put("EXPLANATION", salesVoucher.getParticulars());
            params.put("REMARKS","");
            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.SV, salesVoucher);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer vid) {
        List<CommonLedgerDetail> details = new ArrayList<>();

        SalesVoucher voucher = svRepo.findById(vid).orElse(null);
        if (voucher != null) {
            details = ledgerDtoers.getVoucherLedgerLines(voucher.getTransaction().getId());
        }
        return new JRBeanCollectionDataSource(details);
    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        SalesVoucher salesVoucher =  svRepo.findById(postData.getDocumentId()).orElse(null);

        if (salesVoucher != null && salesVoucher.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId()) {

            // for logging
            Map oldSalesVoucherMap = this.forLogMapMain(salesVoucher);

            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(salesVoucher, salesVoucher.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            salesVoucher.setDocumentStatus(afterActionDocumentStatus);
            salesVoucher.setUpdatedAt(null);
            salesVoucher = svRepo.save(salesVoucher);

            // for logging
            Map newSalesVoucherMap = this.forLogMapMain(salesVoucher);
            newSalesVoucherMap.put("remarks", postData.getRemarks());

            if (salesVoucher != null) {
                documentProcessingFacade.processAction(salesVoucher.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(salesVoucher.getTransaction(), authenticationFacade.getLoggedIn(), oldSalesVoucherMap, newSalesVoucherMap);

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
        SalesVoucher voucher = svRepo.findFirstByOrderByIdAsc();
        if (voucher != null) {
            return documentDtoer.getDocumentStatuses(voucher.getWorkflow().getId());
        }

        return null;
    }

    private Map forLogMapMain(SalesVoucher salesVoucher) {
        return documentLoggerFacade.makeLog(salesVoucher);
    }

    private List<SalesVoucherListDto> makeSVListDto(List<SalesVoucher> vouchers ) {
        List<SalesVoucherListDto> returnVouchers = new ArrayList<>();

        if (!Checker.collectionIsEmpty(vouchers)) {
            for(SalesVoucher sv : vouchers) {
                SalesVoucherListDto svListDto = new SalesVoucherListDto();
                svListDto.setId(sv.getId());
                svListDto.setTransId(sv.getTransaction().getId());
                svListDto.setAmount(sv.getAmount());
                svListDto.setDate(sv.getVoucherDate());
                svListDto.setLocalCode(sv.getCode());
                svListDto.setParticulars(sv.getParticulars());
                svListDto.setStatus(sv.getDocumentStatus().getStatus());

                if(sv.getDocumentStatus().getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_APPROVAL.getId())
                        && sv.getApprovingOfficer().getAccountNo().equals(authenticationFacade.getLoggedIn().getAccountNo())){
                    svListDto.setEnableCheckBox(Boolean.TRUE);
                    svListDto.setSelected(Boolean.TRUE);
                } else {
                    svListDto.setEnableCheckBox(Boolean.FALSE);
                    svListDto.setSelected(Boolean.FALSE);
                }

                svListDto.setDocumentCode(com.noreco1.fireflyv2.model.enums.DocumentType.SV.getCode());

                returnVouchers.add(svListDto);
            }
        }

        return returnVouchers;
    }
}
