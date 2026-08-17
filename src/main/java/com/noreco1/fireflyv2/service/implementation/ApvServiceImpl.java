package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.*;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.dtoers.LedgerDtoerImpl;
import com.noreco1.fireflyv2.dtoers.WorkflowDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.DocumentType;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.controller.response.reports.APDetail;
import com.noreco1.fireflyv2.service.ApvService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.ApvValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.noreco1.fireflyv2.common.helpers.CurrencyIntoWords;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service(value = "apvServiceImpl")
public class ApvServiceImpl implements ApvService, PrintableVoucher {

    private AccountsPayableVoucher model;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    AccountsPayableVoucherRepo apvRepo;

    @Autowired
    AccountsPayableVoucherLinkRepo apvLinkRepo;

    @Autowired
    AccountsPayableVoucherInstallmentDetailRepo accountsPayableVoucherInstallmentDetailRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    SlEntityRepo slEntityRepo;

    @Autowired
    LedgerFacadeImpl ledgerFacade;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    LedgerDtoerImpl ledgerDtoers;

    @Autowired
    FileFacade fileFacade;

    @Autowired
    MonthlyCycleRepo monthlyCycleRepo;

    @Autowired
    PaymentRequestRepo paymentRequestRepo;

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    DocumentDtoer documentDtoer;

    @Autowired
    WorkflowDtoer workflowDtoer;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    SignatureFacade signatureFacade;

    @Autowired
    AllocationFactorRepo allocationFactorRepo;

    @Autowired
    ReceivingReportRepo receivingReportRepo;

    @Autowired
    AccountsPayableVoucherLinkRepo accountsPayableVoucherLinkRepo;

    @Autowired
    ReceivingReportDetailRepo receivingReportDetailRepo;

    @Autowired
    JoAcceptanceDetailRepo joAcceptanceDetailRepo;

    @Autowired
    JoAcceptanceRepo joAcceptanceRepo;

    @Autowired
    CheckVoucherIncomePaymentRepo incomePaymentRepo;

    @Autowired
    PurchaseRequestRepo purchaseRequestRepo;

    @Autowired
    AccountsPayableVoucherIEMOPBillingRepo accountsPayableVoucherIEMOPBillingRepo;

    @Override
    @Transactional
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        AccountsPayableVoucher apv = (AccountsPayableVoucher) v;
        return this.processCreate(apv, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        AccountsPayableVoucher apv = (AccountsPayableVoucher) v;
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        ApvValidator validator = new ApvValidator();
        validator.setmonthlyCycleRepo(this.monthlyCycleRepo);
        validator.setAllocationFactorRepo(this.allocationFactorRepo);
        validator.setLegderFacade(this.ledgerFacade);
        validator.validate(apv, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            AccountsPayableVoucher existingApv = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(apv.getVoucherDate()));
            User approvingOfficer = userRepo.findOneByAccountNo(apv.getApprovingOfficer().getAccountNo());
            User checker = userRepo.findOneByAccountNo(apv.getChecker().getAccountNo());

            Boolean insertMode = apv.getId() == null;
            Boolean withPaymentRequestId = apv.getPaymentRequestId() != null && apv.getPaymentRequestId() > 0;
            Boolean withRRId = apv.getReceivingReportId() != null && apv.getReceivingReportId() > 0;
            Boolean withJobOrderAcceptanceId = apv.getJoAcceptanceId() != null && apv.getJoAcceptanceId() > 0;
            Boolean withReceivingReports = apv.getReceivingReports() != null && !apv.getReceivingReports().isEmpty();

            if (insertMode) { // insert mode

                Object latestApvCode = apvRepo.findLatestApvCodeByYear(voucherYear);
                apv.setCode(generatorFacade.voucherCodeNoOffice("APV", (latestApvCode == null ? "" : String.valueOf(latestApvCode)), apv.getVoucherDate(), GlobalConstant.COUNTER_PAD_4));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                Workflow wf = new Workflow();
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.APV.getId());

                apv.setDocumentStatus(documentStatus);
                apv.setCreatedBy(createdBy);
                apv.setTransaction(generatorFacade.transaction());
                apv.setWorkflow(wf);

                existingApv = apv;

            } else {
                existingApv = apvRepo.findById(apv.getId()).orElse(null);

                List<Integer> statusAllowed = new ArrayList();
                statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.RETURNED_TO_CREATOR.getId());

                if (statusAllowed.indexOf(existingApv.getDocumentStatus().getId()) < 0) {

                    ArrayList<String> messages = new ArrayList();
                    messages.add("Action is not allowed");

                    response.setNotAuthorized(true);
                    response.setMessages(messages);
                    response.setSuccess(false);

                    return response;
                }
            }

            // use for document logging
            Map oldJvMap = this.forLogMapMain(existingApv);

            // editable fields
            existingApv.setVendor(apv.getVendor());
            existingApv.setParticulars(apv.getParticulars());
            existingApv.setVoucherDate(apv.getVoucherDate());
            existingApv.setDueDate(apv.getDueDate());
            existingApv.setApprovingOfficer(approvingOfficer);
            existingApv.setChecker(checker);
            existingApv.setYear(voucherYear);
            existingApv.setAmount(apv.getAmount());
            existingApv.setInvoiceDate(apv.getInvoiceDate());
            existingApv.setPaymentTerm(apv.getPaymentTerm());
            existingApv.setDueDate(apv.getDueDate());
            existingApv.setUnpaidRemark(apv.getUnpaidRemark());
            existingApv.setForInstallment(apv.getForInstallment());
            existingApv.setNumberOfPayments(apv.getNumberOfPayments());
            existingApv.setInstallmentDetails(apv.getInstallmentDetails());

            this.model = apvRepo.save(existingApv);

            if (this.model != null) {
                // start: update default signatories
                signatoryFacade.apv(this.model);
                // end: update default signatories

                // try to reset linked document when updating
                if (!insertMode) {
                    apvLinkRepo.deleteByAccountsPayableVoucherId(existingApv.getId());
                    accountsPayableVoucherIEMOPBillingRepo.deleteByAccountsPayableVoucherId(existingApv.getId());
                    accountsPayableVoucherInstallmentDetailRepo.deleteAllByAccountsPayableVoucherId(existingApv.getId());
                }

                // save installment details
                if (existingApv.getForInstallment() != null && existingApv.getForInstallment()
                        && Checker.collectionIsNotEmpty(existingApv.getInstallmentDetails())) {
                    for (AccountsPayableVoucherInstallmentDetail detail : existingApv.getInstallmentDetails()) {
                        AccountsPayableVoucherInstallmentDetail installmentDetail = new AccountsPayableVoucherInstallmentDetail();
                        installmentDetail.setAccountsPayableVoucher(this.model);
                        installmentDetail.setDueDate(detail.getDueDate());
                        installmentDetail.setAmount(detail.getAmount());
                        accountsPayableVoucherInstallmentDetailRepo.save(installmentDetail);
                    }
                }

                // save iemop billing

                if (!Checker.collectionIsEmpty(apv.getIemopBillings())) {
                    for(IEMOPBilling iemopBilling : apv.getIemopBillings()) {

                        AccountsPayableVoucherIEMOPBilling accountsPayableVoucherIEMOPBilling = new AccountsPayableVoucherIEMOPBilling();
                        accountsPayableVoucherIEMOPBilling.setAccountsPayableVoucher(this.model);
                        accountsPayableVoucherIEMOPBilling.setIemopBilling(iemopBilling);

                        accountsPayableVoucherIEMOPBillingRepo.save(accountsPayableVoucherIEMOPBilling);
                    }
                }

                // link documents to APV
                if (withPaymentRequestId) { // payment request
                    PaymentRequest paymentRequest = paymentRequestRepo.findById(apv.getPaymentRequestId()).orElse(null);

                    AccountsPayableVoucherLink apvLink = new AccountsPayableVoucherLink();
                    apvLink.setAccountsPayableVoucher(this.model);
                    apvLink.setDocumentId(paymentRequest.getId());

                    DocumentType t = new DocumentType();
                    t.setId(com.noreco1.fireflyv2.model.enums.DocumentType.PR.getId());
                    apvLink.setDocumentType(t);

                    apvLinkRepo.save(apvLink);
                }
                /*if (withRRId) { // receiving report
                    ReceivingReport receivingReport = receivingReportRepo.findById(apv.getReceivingReportId()).orElse(null);

                    AccountsPayableVoucherLink apvLink = new AccountsPayableVoucherLink();
                    apvLink.setAccountsPayableVoucher(this.model);
                    apvLink.setDocumentId(receivingReport.getId());

                    DocumentType t = new DocumentType();
                    t.setId(com.noreco1.fireflyv2.model.enums.DocumentType.RR.getId());
                    apvLink.setDocumentType(t);

                    apvLinkRepo.save(apvLink);
                }*/

                if(withReceivingReports){

                    for(ReceivingReport rr : apv.getReceivingReports()){

                        ReceivingReport receivingReport = receivingReportRepo.findById(rr.getId()).orElse(null);

                        AccountsPayableVoucherLink apvLink = new AccountsPayableVoucherLink();
                        apvLink.setAccountsPayableVoucher(this.model);
                        apvLink.setDocumentId(receivingReport.getId());

                        DocumentType t = new DocumentType();
                        t.setId(com.noreco1.fireflyv2.model.enums.DocumentType.RR.getId());
                        apvLink.setDocumentType(t);

                        apvLinkRepo.save(apvLink);

                    }

                }

                if (withJobOrderAcceptanceId) { // Job Order Acceptance
                    JoAcceptance joAcceptance = joAcceptanceRepo.findById(apv.getJoAcceptanceId()).orElse(null);

                    AccountsPayableVoucherLink apvLink = new AccountsPayableVoucherLink();
                    apvLink.setAccountsPayableVoucher(this.model);
                    apvLink.setDocumentId(joAcceptance.getId());

                    DocumentType t = new DocumentType();
                    t.setId(com.noreco1.fireflyv2.model.enums.DocumentType.JOA.getId());
                    apvLink.setDocumentType(t);

                    apvLinkRepo.save(apvLink);
                }

                ledgerFacade.postGeneralLedger(this.model.getTransaction(), apv.getGeneralLedgerLines(),  apv.getSubLedgerLines(), this.model.getVoucherDate());

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldJvMap = null; // new document has no old value
                }
                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldJvMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());
                response.setSuccessMessage("APV successfully saved!");
                response.setSuccess(true);
            }
        }
        return response;
    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            AccountsPayableVoucher apv = apvRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (apv != null) {
                Map map = forLogMapMain(apv);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApvListDto> findAll() {

        List<ApvListDto> returnVouchers = new ArrayList<>();
        try {

            List<AccountsPayableVoucher> vouchers = apvRepo.findAll();
            return this.makeApvListDto(vouchers);

        }catch (Exception e) {
            e.printStackTrace();
        }
        return returnVouchers;
    }

    @Override
    public ApvDto findById(Integer id) {

        AccountsPayableVoucher accountsPayableVoucher =  apvRepo.findById(id).orElse(null);
        ApvDto apvDto = new ApvDto();

        if (accountsPayableVoucher != null) {
            apvDto.setId(accountsPayableVoucher.getId());
            apvDto.setParticulars(accountsPayableVoucher.getParticulars());
            apvDto.setLocalCode(accountsPayableVoucher.getCode());
            apvDto.setTransId(accountsPayableVoucher.getTransaction().getId());

            SlEntity approvingOfficer = slEntityRepo.findById(accountsPayableVoucher.getApprovingOfficer().getAccountNo()).orElse(null);
            SlEntity checker = slEntityRepo.findById(accountsPayableVoucher.getChecker().getAccountNo()).orElse(null);
            SlEntity createdBy = slEntityRepo.findById(accountsPayableVoucher.getCreatedBy().getAccountNo()).orElse(null);

            if(accountsPayableVoucher.getPostedBy() != null) {
                apvDto.setPostedBy(slEntityRepo.findById(accountsPayableVoucher.getPostedBy().getAccountNo()).orElse(null));
            }

            apvDto.setCreatedBy(createdBy);
            apvDto.setApprovingOfficer(approvingOfficer);
            apvDto.setChecker(checker);
            apvDto.setDueDate(accountsPayableVoucher.getDueDate());
            apvDto.setVendor(accountsPayableVoucher.getVendor());
            apvDto.setVoucherDate(accountsPayableVoucher.getVoucherDate());
            apvDto.setAmount(accountsPayableVoucher.getAmount());
            apvDto.setDocumentStatus(accountsPayableVoucher.getDocumentStatus());
            apvDto.setCreated(accountsPayableVoucher.getCreatedAt());
            apvDto.setLastUpdated(accountsPayableVoucher.getUpdatedAt());
            apvDto.setInvoiceDate(accountsPayableVoucher.getInvoiceDate());
            apvDto.setPaymentTerm(accountsPayableVoucher.getPaymentTerm());
            apvDto.setUnpaidRemark(accountsPayableVoucher.getUnpaidRemark());

            List<AccountsPayableVoucherIEMOPBilling> apvIemopBillings = accountsPayableVoucherIEMOPBillingRepo.findAllByAccountsPayableVoucherId(accountsPayableVoucher.getId());
            List<IEMOPBilling> iemopBillings = new ArrayList<>();
            if(!apvIemopBillings.isEmpty()){
                for(AccountsPayableVoucherIEMOPBilling apvIemopBilling : apvIemopBillings){

                    iemopBillings.add(apvIemopBilling.getIemopBilling());

                }
            }

            apvDto.setIemopBillings(iemopBillings);

            apvDto.setForInstallment(accountsPayableVoucher.getForInstallment());
            apvDto.setNumberOfPayments(accountsPayableVoucher.getNumberOfPayments());
            List<AccountsPayableVoucherInstallmentDetail> installmentDetails = accountsPayableVoucherInstallmentDetailRepo.findAllByAccountsPayableVoucherId(accountsPayableVoucher.getId());
            if (Checker.collectionIsNotEmpty(installmentDetails)) {
                apvDto.setInstallmentDetails(installmentDetails);
            }

            List<AccountsPayableVoucherLink> apvLinks = apvLinkRepo.findByAccountsPayableVoucherIdAndDocumentTypeId(accountsPayableVoucher.getId(), com.noreco1.fireflyv2.model.enums.DocumentType.RR.getId());
            List<Map> receivingReports = new ArrayList<>();
            if(!apvLinks.isEmpty()){
                for(AccountsPayableVoucherLink apvLink : apvLinks){

                    ReceivingReport receivingReport = receivingReportRepo.findById(apvLink.getDocumentId()).orElse(null);
                    if(receivingReport != null){
                        Map rrMap = new HashMap();

                        List<ReceivingReportDetail> details = receivingReportDetailRepo.findByReceivingReportId(receivingReport.getId());
                        ArrayList<Integer> pos = new ArrayList<>();
                        String poNos = "";
                        for(ReceivingReportDetail d : details){
                            if(d.getPoDetail() != null && d.getPoDetail().getPurchaseOrder() != null) {

                                if(pos.isEmpty()){
                                    poNos = d.getPoDetail().getPurchaseOrder().getCode();
                                    pos.add(d.getPoDetail().getPurchaseOrder().getId());
                                } else {
                                    boolean add = true;
                                    for (Integer i : pos) {
                                        if (d.getPoDetail().getPurchaseOrder().getId().equals(i)) {
                                            add = false;
                                            break;
                                        }
                                    }
                                    if(add){
                                        poNos += ", " + d.getPoDetail().getPurchaseOrder().getCode();
                                    }
                                }
                            }
                        }

                        rrMap.put("voucherDate", receivingReport.getDeliveryDate());
                        rrMap.put("localCode", receivingReport.getCode());
                        rrMap.put("particulars",  receivingReport.getCode()+", "+poNos+", "+ receivingReport.getInvoiceNumber());
                        rrMap.put("netAmount", receivingReport.getTotalAmount());
                        rrMap.put("id", receivingReport.getId());
                        rrMap.put("preparedBy", receivingReport.getCreatedBy().getFullName());
                        rrMap.put("slEntityAccountNo", receivingReport.getSupplier().getAccountNumber());
                        rrMap.put("slEntityName", receivingReport.getSupplier().getName());
                        rrMap.put("invoiceDate", receivingReport.getInvoiceDate());
                        rrMap.put("paymentTerm", details.get(0).getPoDetail().getPurchaseOrder().getPaymentTerm());

                        receivingReports.add(rrMap);
                    }

                }

                apvDto.setReceivingReports(receivingReports);

            }

        }

        return  apvDto;
    }

    @Override
    public  List<ApvListDto> findByStatusId(Integer id) {
        try {

            List<AccountsPayableVoucher> vouchers = apvRepo.findByDocumentStatusId(id);
            return this.makeApvListDto(vouchers);

        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ApvListDto> findByDateRangeAndStatusId(String from, String to, Integer id) {

        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            List<AccountsPayableVoucher> vouchers = apvRepo.findByDocumentStatusIdAndVoucherDateBetween(id, fromDate, toDate);
            return this.makeApvListDto(vouchers);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ApvListDto> findByDateRange(String from, String to) {

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

            List<AccountsPayableVoucher> vouchers = apvRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(fromDate, toDate, Arrays.asList(ids));
            return this.makeApvListDto(vouchers);

        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public Page<CvVoucherDto> findAllApprovedForCvPaged(String query, Pageable pageable) {

        org.springframework.data.domain.Page<AccountsPayableVoucher> accountsPayableVouchers;

        if(query != null){
            accountsPayableVouchers = apvRepo.findAllByQueryAndDocumentStatusForCv("%"+query+"%", com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        } else {
            accountsPayableVouchers = apvRepo.findAllByDocumentStatusForCv(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        }

        return accountsPayableVouchers.map(entity -> {
                CvVoucherDto dto = new CvVoucherDto();

                dto.setVoucherDate(entity.getVoucherDate());
                dto.setLocalCode(entity.getCode());
                dto.setId(entity.getId());
                dto.setPreparedBy(entity.getCreatedBy().getFullName());
                dto.setAmount(entity.getAmount());
                dto.setParticulars(entity.getParticulars());
                dto.setSlentityAccountNo(entity.getVendor().getAccountNo());
                dto.setSlentityName(entity.getVendor().getName());
                dto.setInvoiceDate(entity.getInvoiceDate());
                dto.setDueDate(entity.getDueDate());
                dto.setTransId(entity.getTransaction().getId());
                dto.setExtensionUrl("accounts-payable");

                List<CheckVoucherIncomePayment> incomePayments = incomePaymentRepo.findByTransactionId(entity.getTransaction().getId());
                dto.setHasTax(!incomePayments.isEmpty());

                AccountsPayableVoucherLink payableVoucherLink = accountsPayableVoucherLinkRepo.findFirstByAccountsPayableVoucherId(entity.getId());
                if(payableVoucherLink != null){

                    if( payableVoucherLink.getDocumentType().getId() == com.noreco1.fireflyv2.model.enums.DocumentType.PR.getId()){

                        PurchaseRequest purchaseRequest = purchaseRequestRepo.findById(payableVoucherLink.getDocumentId()).orElse(null);

                        if(purchaseRequest != null){
                            dto.setBudgetLineItemDetail(purchaseRequest.getBudgetLineItemDetail());
                        }

                    } else if( payableVoucherLink.getDocumentType().getId() == com.noreco1.fireflyv2.model.enums.DocumentType.RR.getId()){

                        List<ReceivingReportDetail> details = receivingReportDetailRepo.findByReceivingReportId(payableVoucherLink.getDocumentId());

                        if(!details.isEmpty()){
                            if(details.get(0).getPurchaseRequestDetail() != null){
                                if(details.get(0).getPurchaseRequestDetail().getPurchaseRequest().getBudgetLineItemDetail() != null){
                                    dto.setBudgetLineItemDetail(details.get(0).getPurchaseRequestDetail().getPurchaseRequest().getBudgetLineItemDetail());
                                }
                            }
                        }

                    } else if( payableVoucherLink.getDocumentType().getId() == com.noreco1.fireflyv2.model.enums.DocumentType.JOA.getId()){
                        List<JoAcceptanceDetail> details = joAcceptanceDetailRepo.findAllByJoAcceptanceId(payableVoucherLink.getDocumentId());
                        if(!details.isEmpty()){

                            if(details.get(0).getJoDetail() != null){

                                if(details.get(0).getJoDetail().getPurchaseRequestDetail().getPurchaseRequest().getBudgetLineItemDetail() != null){

                                    dto.setBudgetLineItemDetail(details.get(0).getJoDetail().getPurchaseRequestDetail().getPurchaseRequest().getBudgetLineItemDetail());

                                }

                            }

                        }

                    }

                }

                if (entity.getForInstallment() != null && entity.getForInstallment()) {
                    dto.setForInstallment(entity.getForInstallment());
                    dto.setNumberOfPayments(entity.getNumberOfPayments());
                    dto.setInstallmentDetails(accountsPayableVoucherInstallmentDetailRepo.findUnpaidInstallmentDetailsByApvId(entity.getId()));
                }

                return dto;
        });

    }

    @Override
    public PostResponse updateUnpaidRemarks(AccountsPayableVoucher apv) {
        PostResponse response = new PostRoleResponse();
        AccountsPayableVoucher accountsPayableVoucher = apvRepo.findById(apv.getId()).orElse(null);

        if(accountsPayableVoucher != null) {

            // use for document logging
            Map oldApvMap = this.forLogMapMain(accountsPayableVoucher);

            accountsPayableVoucher.setUnpaidRemark(apv.getUnpaidRemark());

            apvRepo.save(accountsPayableVoucher);

            Map newApvMap = this.forLogMapMain(accountsPayableVoucher);

            documentLoggerFacade.log(accountsPayableVoucher.getTransaction(), authenticationFacade.getLoggedIn(), oldApvMap, newApvMap);

            response.setSuccessMessage("Unpaid remarks successfully updated.");

        } else  {
            response.setFailureMessage("APV is not available.");
        }

        return response;
    }

    @Override
    public PostResponse updateEntries(AccountsPayableVoucher apv, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        try {

            Boolean insertMode = Checker.isValidId(apv.getId());

            if(insertMode){

                AccountsPayableVoucher existingApv = this.apvRepo.findById(apv.getId()).orElse(null);

                if(existingApv.getChecker().getAccountNo().equals(authenticationFacade.getLoggedIn().getAccountNo())){

                    existingApv.setAmount(apv.getAmount());

                    this.model = apvRepo.save(existingApv);

                    if (this.model != null) {

                        ledgerFacade.postGeneralLedger(this.model.getTransaction(), apv.getGeneralLedgerLines(), apv.getSubLedgerLines(), this.model.getVoucherDate());

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
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.APV);
    }

    @Override
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {

        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        AccountsPayableVoucher payableVoucher = apvRepo.findById(vid).orElse(null);

        if (payableVoucher != null) {

            params.put("VOUCHER_NO", payableVoucher.getCode());
            params.put("V_DATE", payableVoucher.getVoucherDate());
            params.put("AMOUNT_IN_WORDS", CurrencyIntoWords.convert(payableVoucher.getAmount()));
            params.put("TOTAL", payableVoucher.getAmount());
            params.put("SUPPLIER_NAME", payableVoucher.getVendor().getName());
            params.put("SUPPLIER_ADDR", payableVoucher.getVendor().getAddress());
            params.put("DUE_DATE", payableVoucher.getDueDate());
            params.put("PARTICULARS", payableVoucher.getParticulars());
            params.put("SUBREPORT_DIR", GlobalConstant.JASPER_BASE_PATH + "/vouchers/sub_reports/");
            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.APV, payableVoucher);

            List<Map> items = new ArrayList<>();

            if (payableVoucher != null) {

                Map references = new HashMap();

                // check if has RR items
                List<AccountsPayableVoucherLink> payableVoucherRRLinks = accountsPayableVoucherLinkRepo.findByAccountsPayableVoucherIdAndDocumentTypeId(payableVoucher.getId(), com.noreco1.fireflyv2.model.enums.DocumentType.RR.getId());

                if (Checker.collectionIsNotEmpty(payableVoucherRRLinks)) {

                    BigDecimal itemsTotal = BigDecimal.ZERO;
                    for (AccountsPayableVoucherLink link : payableVoucherRRLinks) {

                        BigDecimal totalVat = BigDecimal.ZERO;
                        String drNumber = "";

                        ReceivingReport receivingReport = receivingReportRepo.findById(link.getDocumentId()).orElse(null);
                        if (receivingReport != null) {

                            // Receiving Report
                            Map rrRef = new HashMap();
                            rrRef.put("date", receivingReport.getDeliveryDate());
                            rrRef.put("ref", receivingReport.getCode());

                            references.put(receivingReport.getCode(), rrRef);

                            List<ReceivingReportDetail> rrDetails = receivingReportDetailRepo.findByReceivingReportId(receivingReport.getId());

                            if (Checker.collectionIsNotEmpty(rrDetails)) {

                                String invoice = receivingReport.getInvoiceNumber();
                                for (ReceivingReportDetail rrDetail : rrDetails) {

                                    totalVat = totalVat.add(rrDetail.getNetAmount().subtract(rrDetail.getNetVatAmount()));
                                    drNumber = rrDetail.getDeliveryNumber();

                                    Map detail = new HashMap();

                                    detail.put("invoice", invoice);
                                    detail.put("particulars", rrDetail.getItem().getDescription());
                                    detail.put("quantity", rrDetail.getQuantityReceived());
                                    detail.put("unitCost", rrDetail.getNetVatUnitPrice());
                                    detail.put("unit", rrDetail.getItem().getUnit() != null ? rrDetail.getItem().getUnit().getCode() : "");
                                    detail.put("amount", rrDetail.getNetVatAmount());
                                    detail.put("total", rrDetail.getNetVatAmount());

                                    itemsTotal = itemsTotal.add(rrDetail.getNetVatAmount());

                                    items.add(detail);
                                    invoice = ""; // show once only

                                    // get PO details
                                    if(rrDetail.getPoDetail() != null) {

                                        PurchaseOrder purchaseOrder = rrDetail.getPoDetail().getPurchaseOrder();

                                        if (purchaseOrder != null) {

                                            Map poRef = new HashMap();
                                            poRef.put("date", purchaseOrder.getVoucherDate());
                                            poRef.put("ref", purchaseOrder.getCode());

                                            references.put(purchaseOrder.getCode(), poRef);
                                        }
                                    }
                                }
                            }

                        }

                        if (totalVat.compareTo(BigDecimal.ZERO) > 0) {

                            itemsTotal = itemsTotal.add(totalVat);

                            Map vatDetail = new HashMap();

                            vatDetail.put("invoice", "DR# " + drNumber);
                            vatDetail.put("particulars", "12% VAT");
                            vatDetail.put("total", totalVat);

                            items.add(vatDetail);
                        }

                        // blank line
                       /* Map vatDetail = new HashMap();
                        vatDetail.put("invoice", "");
                        items.add(vatDetail);
*/
                    }

                    Map totalDetail = new HashMap();

                    totalDetail.put("particulars", "Total");
                    totalDetail.put("total", itemsTotal);

                    items.add(totalDetail);

                    // blank line, separates Items & Journal
                /*    Map vatDetail = new HashMap();
                    vatDetail.put("invoice", "");
                    items.add(vatDetail);*/
                }

                // Payment Requests
                List<AccountsPayableVoucherLink> payableVoucherPRLinks = accountsPayableVoucherLinkRepo.findByAccountsPayableVoucherIdAndDocumentTypeId(payableVoucher.getId(), com.noreco1.fireflyv2.model.enums.DocumentType.PR.getId());

                if (Checker.collectionIsNotEmpty(payableVoucherPRLinks)) {

                    BigDecimal itemsTotal = BigDecimal.ZERO;
                    for (AccountsPayableVoucherLink link : payableVoucherPRLinks) {

                        BigDecimal totalVat = BigDecimal.ZERO;
                        PaymentRequest paymentRequest = paymentRequestRepo.findById(link.getDocumentId()).orElse(null);
                        if (paymentRequest != null) {

                            // Payment Request
                            Map prRef = new HashMap();
                            prRef.put("date", paymentRequest.getVoucherDate());
                            prRef.put("ref", paymentRequest.getCode());

                            references.put(paymentRequest.getCode(), prRef);

//                            List<JoAcceptanceDetail> joAcceptanceDetails = joAcceptanceDetailRepo.findByJoAcceptanceId(paymentRequest.getJoAcceptance().getId());
//
//                            if (Checker.collectionIsNotEmpty(joAcceptanceDetails)) {
//
//                                for (JoAcceptanceDetail joAcceptanceDetail : joAcceptanceDetails) {
//
//                                    totalVat = totalVat.add(joAcceptanceDetail.getVat());
//
//                                    if( joAcceptanceDetail.getJoDetail() != null) {
//                                        Map detail = new HashMap();
//                                        PurchaseRequestDetail purchaseRequestDetail = joAcceptanceDetail.getJoDetail().getPurchaseRequestDetail();
//
//                                        detail.put("particulars", purchaseRequestDetail.getJoDescription());
//                                        detail.put("quantity", joAcceptanceDetail.getQuantity());
//                                        detail.put("unitCost", joAcceptanceDetail.getUnitPrice());
//                                        detail.put("unit", purchaseRequestDetail.getUnitMeasure() != null ? purchaseRequestDetail.getUnitMeasure().getCode() : "");
//                                        detail.put("amount", joAcceptanceDetail.getAmount());
//                                        detail.put("total", joAcceptanceDetail.getNetAmount());
//
//                                        itemsTotal = itemsTotal.add(joAcceptanceDetail.getNetAmount());
//
//                                        items.add(detail);
//
//                                        // get PO details
//                                        JobOrder jobOrder = joAcceptanceDetail.getJoDetail().getJobOrder();
//
//                                        if (jobOrder != null) {
//
//                                            Map joRef = new HashMap();
//                                            joRef.put("ref", jobOrder.getCode());
//                                            joRef.put("date", jobOrder.getVoucherDate());
//
//                                            references.put(jobOrder.getCode(), joRef);
//                                        }
//                                    }
//
//                                }
//                            }

                        }

                        if (totalVat.compareTo(BigDecimal.ZERO) > 0) {

                            itemsTotal = itemsTotal.add(totalVat);

                            Map vatDetail = new HashMap();

                            vatDetail.put("particulars", "12% VAT");
                            vatDetail.put("total", totalVat);

                            items.add(vatDetail);
                        }

                        // blank line
                        Map vatDetail = new HashMap();
                        vatDetail.put("invoice", "");
                        items.add(vatDetail);

                    }

                    Map totalDetail = new HashMap();

                    totalDetail.put("particulars", "Total");
                    totalDetail.put("total", itemsTotal);

                    items.add(totalDetail);

                    // blank line, separates Items & Journal
                    Map blank = new HashMap();
                    blank.put("invoice", "");
                    items.add(blank);
                }

                //Print APV: payment of items - no need to show items - show particulars instead
                //Commented this params so that the sub report will not show on generating APV report
                //params.put("ITEMS", new JRBeanCollectionDataSource(items));

                //Set the ITEMS params to empty array so that it will not show when generating APV report
                params.put("ITEMS", new JRBeanCollectionDataSource(new ArrayList<Object>()));

                List<APDetail> accounts = new ArrayList<>();

                List<GeneralLedgerLineDto2> ledgerLineDto2s = ledgerDtoers.getGLAccountEntriesDtoByTrans(payableVoucher.getTransaction().getId(), false);

                if (!Checker.collectionIsEmpty(ledgerLineDto2s)) {
                    for (GeneralLedgerLineDto2 dto : ledgerLineDto2s) {
                        APDetail d = new APDetail();

                        d.setAccount(dto.getCode());
                        d.setDescription(dto.getDescription());
                        d.setDebit(dto.getDebit());
                        d.setCredit(dto.getCredit());

                        accounts.add(d);
                        boolean isDebit = dto.getCredit() == null || dto.getCredit().compareTo(BigDecimal.ZERO) == 0;

                        List<SubLedgerDto> subLedgerLineDtos = ledgerDtoers.getSLEntriesDtoByTransAndAccount(payableVoucher.getTransaction().getId(), dto.getAccountId(), isDebit);
                        if (!Checker.collectionIsEmpty(subLedgerLineDtos)) {

                            for (SubLedgerDto sl : subLedgerLineDtos) {
                                APDetail sld = new APDetail();

                                sld.setAccount("&nbsp;&nbsp;" + String.valueOf(sl.getAccountNo()));
                                sld.setDescription("&nbsp;&nbsp;" + sl.getName());

                                BigDecimal drAmount = sl.getAmount();
                                BigDecimal crAmount = sl.getAmount();
                                if (dto.getDebit() == null || dto.getDebit().compareTo(BigDecimal.ZERO) == 0) {
                                    drAmount = BigDecimal.ZERO;
                                } else {
                                    crAmount = BigDecimal.ZERO;
                                }

                                sld.setCreditSl(crAmount);
                                sld.setDebitSl(drAmount);

                                accounts.add(sld);
                            }
                        }
                    }
                }

                // set references
                SimpleDateFormat refDateFormat = new SimpleDateFormat("d/M/yyyy");

                int entryCnt = 0;

                Iterator iterator = references.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry refObj = (Map.Entry) iterator.next();
                    Map refMap = (Map) refObj.getValue();

                    if(entryCnt < accounts.size()) {

                        // update journal line reference
                        APDetail apDetail = accounts.get(entryCnt);

                        Date refDate = (Date) refMap.get("date");
                        apDetail.setInvoiceNumber(refDateFormat.format(refDate));
                        apDetail.setParticulars(refMap.get("ref").toString());

                        accounts.set(entryCnt, apDetail);

                    } else {    // extra lines
                        APDetail apDetail = new APDetail();

                        Date refDate = (Date) refMap.get("date");
                        apDetail.setInvoiceNumber(refDateFormat.format(refDate));
                        apDetail.setParticulars(refMap.get("ref").toString());

                        accounts.add(apDetail);
                    }
                    entryCnt++;
                }

                params.put("ACCOUNTS", new JRBeanCollectionDataSource(accounts));
                params.put("REFERENCES", new JRBeanCollectionDataSource(accounts));
            }
        }

        return params;

    }

    @Override
    public JRDataSource datasource(Integer vid) {
        List<APDetail> details = new ArrayList<>();

        /*AccountsPayableVoucher voucher = apvRepo.findById(vid).orElse(null);
        if (voucher != null) {

            // check if has RR items
            List<AccountsPayableVoucherLink> payableVoucherLinks = accountsPayableVoucherLinkRepo.findByAccountsPayableVoucherIdAndDocumentTypeId(voucher.getId(), com.noreco1.fireflyv2.model.enums.DocumentType.RR.getId());
            boolean extraSpace = false;
            BigDecimal totalVat = BigDecimal.ZERO;
            String drNumber = "";

            if(Checker.collectionIsNotEmpty(payableVoucherLinks)) {
                for(AccountsPayableVoucherLink link : payableVoucherLinks) {

                    ReceivingReport receivingReport = receivingReportRepo.findById(link.getDocumentId()).orElse(null);
                    if(receivingReport != null) {

                        List<ReceivingReportDetail> rrDetails = receivingReportDetailRepo.findByReceivingReportId(receivingReport.getId());

                        if(Checker.collectionIsNotEmpty(rrDetails)) {
                            extraSpace = true;
                            for(ReceivingReportDetail rrDetail : rrDetails) {

                                totalVat = totalVat.add(rrDetail.getNetAmount().subtract(rrDetail.getNetVatAmount()));
                                drNumber = rrDetail.getDeliveryNumber();

                                APDetail detail = new APDetail();

                                detail.setInvoiceNumber(receivingReport.getInvoiceDescription());
                                detail.setParticulars(rrDetail.getItem().getDescription());
                                detail.setAccount("");
                                detail.setDebit(rrDetail.getNetAmount());
                                detail.setCredit(BigDecimal.ZERO);

                                details.add(detail);

                            }
                        }

                    }

                }
            }

            if(extraSpace) {

                if(totalVat.compareTo(BigDecimal.ZERO) > 0) {

                    APDetail vatDetail = new APDetail();

                    vatDetail.setInvoiceNumber("DR# "+drNumber);
                    vatDetail.setParticulars("12% VAT");
                    vatDetail.setDebit(totalVat);
                    vatDetail.setCredit(BigDecimal.ZERO);

                    details.add(vatDetail);
                }
            }

            List<GeneralLedgerLineDto2> ledgerLineDto2s = ledgerDtoers.getGLAccountEntriesDtoByTrans(voucher.getTransaction().getId());

            if (!Checker.collectionIsEmpty(ledgerLineDto2s)) {
                int totalLine = ledgerLineDto2s.size();
                int rCnt = 0;
                for(GeneralLedgerLineDto2 dto : ledgerLineDto2s) {
                    rCnt++;
                    APDetail d = new APDetail();

                    d.setInvoiceNumber("");
                    d.setParticulars(dto.getDescription());
                    d.setAccount(dto.getCode());
                    d.setDebit(dto.getDebit());
                    d.setCredit(dto.getCredit());

                    details.add(d);

                    List<SubLedgerDto> subLedgerLineDtos = ledgerDtoers.getSLEntriesDtoByTransAndAccount(
                                                                        voucher.getTransaction().getId(), dto.getAccountId());
                    if (!Checker.collectionIsEmpty(subLedgerLineDtos)) {

                        for(SubLedgerDto sl : subLedgerLineDtos) {
                            APDetail sld = new APDetail();

                            sld.setAccount("&nbsp;&nbsp;&nbsp;" + String.valueOf(sl.getAccountNo()));
                            sld.setParticulars("&nbsp;&nbsp;&nbsp;" + sl.getName());

                            BigDecimal drAmount = sl.getAmount();
                            BigDecimal crAmount = sl.getAmount();
                            if (dto.getDebit() == null || dto.getDebit().compareTo(BigDecimal.ZERO) == 0) {
                                drAmount = BigDecimal.ZERO;
                            } else {
                                crAmount = BigDecimal.ZERO;
                            }

                            sld.setCreditSl(crAmount);
                            sld.setDebitSl(drAmount);

                            details.add(sld);
                        }
                    }
                }
            }
        }*/

        // just to display detail band
        APDetail vatDetail = new APDetail();
        vatDetail.setInvoiceNumber("");
        details.add(vatDetail);

        return new JRBeanCollectionDataSource(details);
    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        AccountsPayableVoucher accountsPayableVoucher =  apvRepo.findById(postData.getDocumentId()).orElse(null);

        if (accountsPayableVoucher != null && accountsPayableVoucher.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId()) {
            // for logging
            Map oldJvMap = this.forLogMapMain(accountsPayableVoucher);

            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(accountsPayableVoucher, accountsPayableVoucher.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            accountsPayableVoucher.setDocumentStatus(afterActionDocumentStatus);
            accountsPayableVoucher.setUpdatedAt(null);
            accountsPayableVoucher = apvRepo.save(accountsPayableVoucher);

            // for logging
            Map newJvMap = this.forLogMapMain(accountsPayableVoucher);
            newJvMap.put("remarks", postData.getRemarks());

            if (accountsPayableVoucher != null) {
                documentProcessingFacade.processAction(accountsPayableVoucher.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(accountsPayableVoucher.getTransaction(), authenticationFacade.getLoggedIn(), oldJvMap, newJvMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }

        }
        return response;
    }

    @Override
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
        AccountsPayableVoucher apv = apvRepo.findFirstByOrderByIdAsc();
        if (apv != null) {
            return documentDtoer.getDocumentStatuses(apv.getWorkflow().getId());
        }

        return null;
    }

    private Map forLogMapMain(AccountsPayableVoucher voucher) {
        return documentLoggerFacade.makeLog(voucher);
    }

    private List<ApvListDto> makeApvListDto(List<AccountsPayableVoucher> vouchers ) {
        List<ApvListDto> returnVouchers = new ArrayList<>();

        if (!Checker.collectionIsEmpty(vouchers)) {
            for(AccountsPayableVoucher apv : vouchers) {
                ApvListDto apvListDto = new ApvListDto();
                apvListDto.setId(apv.getId());
                apvListDto.setTransId(apv.getTransaction().getId());
                apvListDto.setSupplierAccountNo(apv.getVendor() != null ? apv.getVendor().getAccountNo():0);
                apvListDto.setAmount(apv.getAmount());
                apvListDto.setDate(apv.getVoucherDate());
                apvListDto.setLocalCode(apv.getCode());
                apvListDto.setParticulars(apv.getParticulars());
                apvListDto.setStatus(apv.getDocumentStatus().getStatus());
                apvListDto.setSupplier(apv.getVendor() != null ? apv.getVendor().getName():"");

                if(apv.getDocumentStatus().getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_APPROVAL.getId())
                        && apv.getApprovingOfficer().getAccountNo().equals(authenticationFacade.getLoggedIn().getAccountNo())){
                    apvListDto.setEnableCheckBox(Boolean.TRUE);
                    apvListDto.setSelected(Boolean.TRUE);
                } else {
                    apvListDto.setEnableCheckBox(Boolean.FALSE);
                    apvListDto.setSelected(Boolean.FALSE);
                }

                apvListDto.setDocumentCode(com.noreco1.fireflyv2.model.enums.DocumentType.APV.getCode());

                returnVouchers.add(apvListDto);
            }
        }

        return returnVouchers;
    }
}
