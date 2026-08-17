package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.controller.response.reports.PayReqDetail;
import com.noreco1.fireflyv2.service.PaymentRequestService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.PayReqValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.noreco1.fireflyv2.common.helpers.CurrencyIntoWords;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by Personal on 7/21/2015.
 */
@Service(value = "paymentRequestServiceImpl")
public class PaymentRequestServiceImpl implements PaymentRequestService, PrintableVoucher {

    private PaymentRequest model;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    PaymentRequestRepo payReqRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    SlEntityRepo slEntityRepo;

    @Autowired
    JoAcceptanceRepo joAcceptanceRepo;

    @Autowired
    JoAcceptanceDetailRepo joAcceptanceDetailRepo;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    JoAcceptanceDetailServiceImpl joAcceptanceDetailDto;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    DocumentDtoer documentDtoer;

    @Autowired
    SignatureFacade signatureFacade;

    @Autowired
    FileFacade fileFacade;

    @Autowired
    PaymentRequestDetailRepo paymentRequestDetailRepo;

    @Autowired
    PaymentRequestBudgetDetailRepo paymentRequestBudgetDetailRepo;

    @Autowired
    PaymentRequestBudgetLineItemDetailRepo paymentRequestBudgetLineItemDetailRepo;

    @Override
    @Transactional(readOnly = true)
    public PaymentRequest findOneByCode(String code) {
        List<PaymentRequest> prs = payReqRepo.findOneByCode(code);

        if (!Checker.collectionIsEmpty(prs)) {
            return prs.get(0);
        } else return null;
    }

    @Override
    @Transactional
    public PostResponse processUpdate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource) {
        PaymentRequest paymentRequest = (PaymentRequest) v;
        return this.processCreate(paymentRequest, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource) {
        PaymentRequest paymentRequest = (PaymentRequest) v;
        PostResponse response = new PostResponse();
        response.setSuccess(false);

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        PayReqValidator validator = new PayReqValidator();
        validator.setService(this);
        validator.validate(paymentRequest, bindingResult);

        try {

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
            } else {
                User createdBy = authenticationFacade.getLoggedIn();
                PaymentRequest existingPayReq = null;

                Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(paymentRequest.getVoucherDate()));

                Boolean insertMode = paymentRequest.getId() == null;
                if (insertMode) { // insert mode

                    Object latestPayReqCode = payReqRepo.findLatestPaymentRequestCodeByYear(voucherYear);
                    paymentRequest.setCode(generatorFacade.voucherCodeNoOffice("RFP", (latestPayReqCode == null ? "" : String.valueOf(latestPayReqCode)), paymentRequest.getVoucherDate(), GlobalConstant.COUNTER_PAD_4));

                    DocumentStatus documentStatus = new DocumentStatus();
                    documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                    paymentRequest.setDocumentStatus(documentStatus);

                    Workflow wf = new Workflow();
                    wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.RP.getId());

                    paymentRequest.setTransaction(generatorFacade.transaction());
                    paymentRequest.setWorkflow(wf);
                    paymentRequest.setCreatedBy(createdBy);
                    existingPayReq = paymentRequest;
                } else {
                    existingPayReq = payReqRepo.findById(paymentRequest.getId()).orElse(null);
                }
                // use for document logging
                Map oldMap = this.forLogMapMain(existingPayReq);

                existingPayReq.setVendor(paymentRequest.getVendor());
                existingPayReq.setVoucherDate(paymentRequest.getVoucherDate());
                existingPayReq.setYear(voucherYear);
                existingPayReq.setAmount(paymentRequest.getAmount());
                existingPayReq.setInvoiceDate(paymentRequest.getInvoiceDate());
                existingPayReq.setInvoiceNumber(paymentRequest.getInvoiceNumber());
                existingPayReq.setDueDate(paymentRequest.getDueDate());
//                existingPayReq.setBudgetAmountBalanceCV(paymentRequest.getBudgetAmountBalanceCV());
//                existingPayReq.setBudgetAmountBalancePOJORFP(paymentRequest.getBudgetAmountBalancePOJORFP());
                existingPayReq.setCashFlowAmountBalancePOJORFP(paymentRequest.getCashFlowAmountBalancePOJORFP());
                existingPayReq.setCashFlowAmountBalanceCV(paymentRequest.getCashFlowAmountBalanceCV());

                this.model = payReqRepo.save(existingPayReq);

                if (this.model != null) {

                    // start: update default signatories
//                    signatoryFacade.pr(this.model);
                    // end: update default signatories

                    if (!insertMode) {
                        paymentRequestDetailRepo.deleteByPaymentRequestId(existingPayReq.getId());
                        paymentRequestBudgetDetailRepo.deleteByPaymentRequestId(existingPayReq.getId());
                        paymentRequestBudgetLineItemDetailRepo.deleteByPaymentRequestId(existingPayReq.getId());
                    }

                    if (insertMode) { // log action only when adding document
                        documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                        oldMap = null;
                    }

                    ArrayList<PaymentRequestDetail> paymentRequestDetails = paymentRequest.getPaymentRequestDetails();
                    for(PaymentRequestDetail paymentRequestDetail: paymentRequestDetails) {

                        PaymentRequestDetail detail = new PaymentRequestDetail();

                        detail.setPaymentRequest(this.model);
                        detail.setDescription(paymentRequestDetail.getDescription());
                        detail.setAmount(paymentRequestDetail.getAmount());

                        paymentRequestDetailRepo.save(detail);

                    }

                    ArrayList<PaymentRequestBudgetDetail> budgetDetails = paymentRequest.getBudgetDetails();
                    for (PaymentRequestBudgetDetail paymentRequestBudgetDetail : budgetDetails){

                        PaymentRequestBudgetDetail newPaymentRequestBudgetDetail = new PaymentRequestBudgetDetail();

                        PaymentRequest pr = new PaymentRequest();
                        pr.setId(this.model.getId());
                        newPaymentRequestBudgetDetail.setPaymentRequest(pr);

                        newPaymentRequestBudgetDetail.setBudgetSubItem(paymentRequestBudgetDetail.getBudgetSubItem());
                        newPaymentRequestBudgetDetail.setAmount(paymentRequestBudgetDetail.getAmount());

                        paymentRequestBudgetDetailRepo.save(newPaymentRequestBudgetDetail);

                    }

                    ArrayList<BudgetLineItemDetail> budgetLineItemDetails = paymentRequest.getBudgetLineItemDetails();
                    for (BudgetLineItemDetail budgetLineItemDetail : budgetLineItemDetails){

                        PaymentRequestBudgetLineItemDetail newPaymentRequestBudgetLineItemDetail = new PaymentRequestBudgetLineItemDetail();

                        PaymentRequest pr = new PaymentRequest();
                        pr.setId(this.model.getId());
                        newPaymentRequestBudgetLineItemDetail.setPaymentRequest(pr);

                        newPaymentRequestBudgetLineItemDetail.setBudgetLineItemDetail(budgetLineItemDetail);
                        newPaymentRequestBudgetLineItemDetail.setBudgetAmountBalanceCV(budgetLineItemDetail.getBudgetAmountBalanceCV() != null ? budgetLineItemDetail.getBudgetAmountBalanceCV() : BigDecimal.ZERO);
                        newPaymentRequestBudgetLineItemDetail.setBudgetAmountBalancePOJORFP(budgetLineItemDetail.getBudgetAmountBalancePOJORFP() != null ? budgetLineItemDetail.getBudgetAmountBalancePOJORFP() : BigDecimal.ZERO);

                        paymentRequestBudgetLineItemDetailRepo.save(newPaymentRequestBudgetLineItemDetail);

                    }

                    // generic document logging here
                    // old value only
                    DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, null);

                    response.setLogId(log != null ? log.getId() : 0);
                    response.setModelId(this.model.getId());
                    response.setSuccessMessage("Payment Request successfully saved!");
                    response.setSuccess(true);
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException(e);
        }
        return response;
    }

    @Override
    public PostResponse processCreate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request) {
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
    public PostResponse processUpdate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> filesToRemove) {
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
    public List<PaymentRequestBudgetDetail> getPaymentRequestBudgetDetail(Integer prId) {

        List<PaymentRequestBudgetDetail> paymentRequestBudgetDetails = new ArrayList<>();

        try {

            paymentRequestBudgetDetails = this. paymentRequestBudgetDetailRepo.findAllByPaymentRequestId(prId);

            for (PaymentRequestBudgetDetail paymentRequestBudgetDetail : paymentRequestBudgetDetails){

//                paymentRequestBudgetDetail.setParent(StringFormatter.reverseString(StringFormatter.getParentCashflowItemName(paymentRequestBudgetDetail.getBudgetSubItem())));

                String subItem = paymentRequestBudgetDetail.getBudgetSubItem().getBudgetLineItemDetail().getCashflowItem().getName() +
                        " -> " +
                        paymentRequestBudgetDetail.getBudgetSubItem().getBudgetLineItemDetail().getTitle() +
                        " -> " +
                        paymentRequestBudgetDetail.getBudgetSubItem().getDescription();

                paymentRequestBudgetDetail.setParent(subItem);

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return paymentRequestBudgetDetails;

    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            PaymentRequest doc = payReqRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (doc != null) {
                Map map = forLogMapMain(doc);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.PR);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayReqListDto> findAll() {
        List<PaymentRequest> vouchers = payReqRepo.findAll();

        List<PayReqListDto> returnVouchers = new ArrayList<>();
        if (!Checker.collectionIsEmpty(vouchers)) {
            for(PaymentRequest payReq : vouchers) {
                PayReqListDto payReqListDto = new PayReqListDto();
                payReqListDto.setId(payReq.getId());
                payReqListDto.setVoucherDate(payReq.getVoucherDate());
                payReqListDto.setLocalCode(payReq.getCode());
                payReqListDto.setParticulars("");
                payReqListDto.setAmount(BigDecimal.ZERO);
                payReqListDto.setStatus(payReq.getDocumentStatus().getStatus());

                SlEntity createdBy = slEntityRepo.findById(payReq.getCreatedBy().getAccountNo()).orElse(null);
                payReqListDto.setPreparedBy(createdBy.getName());

                SlEntity vendor = slEntityRepo.findById(payReq.getVendor().getAccountNo()).orElse(null);
                payReqListDto.setVendor(vendor.getName());

                returnVouchers.add(payReqListDto);
            }

            return returnVouchers;
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map> findAllApprovedForApv() {
        List<Map> data = new ArrayList<>();
        List<Object[]> objects = payReqRepo.findAllForApv(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), com.noreco1.fireflyv2.model.enums.DocumentType.PR.getId());

        if (!Checker.collectionIsEmpty(objects)) {
            for(Object[] row:objects) {
                Map m = new HashMap();
                m.put("id", row[0]);
                m.put("localCode", row[1]);
                m.put("netAmount", row[2]);
                m.put("particulars", row[3]);
                m.put("voucherDate", row[4]);
                m.put("preparedBy", row[5]);

                m.put("slentityAccountNo", row[6]);
                m.put("slentityName", row[7]);

                data.add(m);
            }
        }
        return data;
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ApvPurchasingDocumentDto> findAllApprovedForApvPaged(String query, Pageable pageable) {
        org.springframework.data.domain.Page<PaymentRequest> paymentRequests;
        if(query != null){
            paymentRequests = payReqRepo.findAllByQueryForApv("%"+query+"%", com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), com.noreco1.fireflyv2.model.enums.DocumentType.PR.getId(), pageable);
        } else {
            paymentRequests = payReqRepo.findAllForApv(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), com.noreco1.fireflyv2.model.enums.DocumentType.PR.getId(), pageable);
        }

        return paymentRequests.map(entity -> {
                ApvPurchasingDocumentDto dto = new ApvPurchasingDocumentDto();

                dto.setVoucherDate(entity.getVoucherDate());
                dto.setLocalCode(entity.getCode());
                dto.setId(entity.getId());
                dto.setPreparedBy(entity.getCreatedBy().getFullName());
                dto.setNetAmount(entity.getAmount());
                dto.setParticulars(entity.getCode() + " - " + entity.getVendor().getName());
                dto.setSlentityAccountNo(entity.getVendor().getAccountNo());
                dto.setSlentityName(entity.getVendor().getName());
                dto.setInvoiceDate(entity.getInvoiceDate());
                dto.setDueDate(entity.getDueDate());

                return dto;
        });
    }

    @Override
    public PayReqDto findById(Integer id) {

        PayReqDto jpayReqDtoaDto = new PayReqDto();

        try  {

            PaymentRequest paymentRequest =  payReqRepo.findById(id).orElse(null);

            if (paymentRequest != null) {
                jpayReqDtoaDto.setId(paymentRequest.getId());
                jpayReqDtoaDto.setLocalCode(paymentRequest.getCode());
                jpayReqDtoaDto.setTransId(paymentRequest.getTransaction().getId());
                jpayReqDtoaDto.setAmount(BigDecimal.ZERO);

                SlEntity createdBy = slEntityRepo.findById(paymentRequest.getCreatedBy().getAccountNo()).orElse(null);
                SlEntity vendor = slEntityRepo.findById(paymentRequest.getVendor().getAccountNo()).orElse(null);

                jpayReqDtoaDto.setCreatedBy(createdBy);
                jpayReqDtoaDto.setVendor(vendor);
                jpayReqDtoaDto.setVoucherDate(paymentRequest.getVoucherDate());
                jpayReqDtoaDto.setDocumentStatus(paymentRequest.getDocumentStatus());
                jpayReqDtoaDto.setCreated(paymentRequest.getCreatedAt());
                jpayReqDtoaDto.setLastUpdated(paymentRequest.getUpdatedAt());
                jpayReqDtoaDto.setAmount(paymentRequest.getAmount());
                jpayReqDtoaDto.setInvoiceDate(paymentRequest.getInvoiceDate());
                jpayReqDtoaDto.setInvoiceNumber(paymentRequest.getInvoiceNumber());
                jpayReqDtoaDto.setDueDate(paymentRequest.getDueDate());
                jpayReqDtoaDto.setCashFlowAmountBalanceCV(paymentRequest.getCashFlowAmountBalanceCV());
                jpayReqDtoaDto.setCashFlowAmountBalancePOJORFP(paymentRequest.getCashFlowAmountBalancePOJORFP());

                List<PaymentRequestDetail> paymentRequestDetails = paymentRequestDetailRepo.findAllByPaymentRequestId(paymentRequest.getId());

                if(Checker.collectionIsNotEmpty(paymentRequestDetails)){
                    jpayReqDtoaDto.setPaymentRequestDetails(paymentRequestDetails);
                }

                List<PaymentRequestBudgetLineItemDetail> paymentRequestBudgetLineItemDetails = paymentRequestBudgetLineItemDetailRepo.findAllByPaymentRequestId(paymentRequest.getId());

                if(Checker.collectionIsNotEmpty(paymentRequestBudgetLineItemDetails)){
                    jpayReqDtoaDto.setPaymentRequestBudgetLineItemDetails(paymentRequestBudgetLineItemDetails);
                }

            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        return  jpayReqDtoaDto;

    }

    @Override
    public HashMap reportParameters(Integer id, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        PaymentRequest payReq = payReqRepo.findById(id).orElse(null);

        if (payReq != null) {

            params.put("VOUCHER_NO", payReq.getCode());
            params.put("INVOICE_DATE", payReq.getInvoiceDate());
            params.put("INVOICE_NUMBER", payReq.getInvoiceNumber());
            params.put("DUE_DATE", payReq.getDueDate());
            params.put("V_DATE", payReq.getVoucherDate());
            params.put("VENDOR_TYPE", payReq.getVendor().getSlEntityClassification());
            params.put("VENDOR", payReq.getVendor().getName());
            params.put("VENDOR_ADDRESS", payReq.getVendor().getAddress());
            params.put("AMOUNT", payReq.getAmount());
            params.put("AMOUNT_IN_WORDS", CurrencyIntoWords.convert(payReq.getAmount()) + " ("+new DecimalFormat("#,##0.00").format(payReq.getAmount()) + ")");
            params.put("BUDGET_LINE_ITEMS", new JRBeanCollectionDataSource(this.paymentRequestBudgetLineItemDetailRepo.findAllByPaymentRequestId(payReq.getId())));

            PaymentRequestBudgetDetail paymentRequestBudgetDetail = paymentRequestBudgetDetailRepo.findFirstByPaymentRequestIdOrderByIdAsc(payReq.getId());

            if(paymentRequestBudgetDetail != null){
                params.put("CASH_FLOW_BALANCE_PO_JO_RFP", paymentRequestBudgetDetail.getPaymentRequest().getCashFlowAmountBalancePOJORFP());
                params.put("CASH_FLOW_BALANCE_CV", paymentRequestBudgetDetail.getPaymentRequest().getCashFlowAmountBalanceCV());
                params.put("CASH_FLOW_ITEM_AMOUNT", payReq.getCashFlowAmountBalancePOJORFP().subtract(payReq.getAmount()));
                params.put("CASH_FLOW_ITEMS", new JRBeanCollectionDataSource(this.getPaymentRequestBudgetDetail(payReq.getId())));
            }

            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.PR, payReq);

        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer id) {
        List<PayReqDetail> details = new ArrayList<>();

        PaymentRequest voucher = payReqRepo.findById(id).orElse(null);
        if (voucher != null) {
            List<PaymentRequestDetail> paymentRequestDetails = paymentRequestDetailRepo.findAllByPaymentRequestId(voucher.getId());

            if (Checker.collectionIsNotEmpty(paymentRequestDetails)) {
                for(PaymentRequestDetail dto : paymentRequestDetails) {
                    PayReqDetail d = new PayReqDetail();

                    d.setDescription(dto.getDescription());
                    d.setAmount(dto.getAmount());

                    details.add(d);
                }
            }
        }
        return new JRBeanCollectionDataSource(details);
    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        PaymentRequest payReq =  payReqRepo.findById(postData.getDocumentId()).orElse(null);

        if (payReq != null) {
            // for logging
            Map oldMap = this.forLogMapMain(payReq);
            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(payReq, payReq.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            payReq.setDocumentStatus(afterActionDocumentStatus);
            payReq.setUpdatedAt(null);
            payReq = payReqRepo.save(payReq);

            // for logging
            Map newMap = this.forLogMapMain(payReq);
            newMap.put("remarks", postData.getRemarks());

            if (payReq != null) {
                documentProcessingFacade.processAction(payReq.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(payReq.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, newMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }

        }
        return response;
    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        return null;
    }

    @Override
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        return null;
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
        PaymentRequest pr = payReqRepo.findFirstByOrderByIdAsc();
        if (pr != null) {
            return documentDtoer.getDocumentStatuses(pr.getWorkflow().getId());
        }
        return null;
    }

    @Override
    public Map findByApvId(Integer apvId) {
        Map m = null;

        List<Object[]> rows = payReqRepo.findByApvId(apvId, com.noreco1.fireflyv2.model.enums.DocumentType.PR.getId());

        if (!Checker.collectionIsEmpty(rows)) {
            Object[] row = rows.get(0);

            m = new HashMap();
            m.put("id", row[0]);
            m.put("localCode", row[1]);
            m.put("netAmount", row[2]);
            m.put("particulars", row[3]);
            m.put("voucherDate", row[4]);
            m.put("preparedBy", row[5]);
        }

        return m;
    }

    @Override
    public List<Map> findByDateRangeAndStatusId(String from, String to, Integer id) {
        try {
            java.util.Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            java.util.Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new java.util.Date(0);
            }

            if (toDate == null) {
                toDate = new java.util.Date();
            }

            List<PaymentRequest> docs = payReqRepo.findByDocumentStatusIdAndVoucherDateBetween(id, fromDate, toDate);
            return this.makePaymentRequestListMap(docs);
        }catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Map> findByDateRangePending(String from, String to) {
        try {
            java.util.Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            java.util.Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new java.util.Date(0);
            }

            if (toDate == null) {
                toDate = new java.util.Date();
            }

            Integer[] ids = {
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId()
            };

            List<PaymentRequest> docs = payReqRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(fromDate, toDate, Arrays.asList(ids));
            return this.makePaymentRequestListMap(docs);

        }catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private Map forLogMapMain(PaymentRequest pr) {
        return  documentLoggerFacade.makeLog(pr);
    }

    private List<Map> makePaymentRequestListMap(List<PaymentRequest> cs ) {

        List<Map> mapList = new ArrayList<>();

        if (!Checker.collectionIsEmpty(cs)) {
            for(PaymentRequest c:cs) {
                mapList.add(composePaymentRequestMap(c));
            }
        }
        return mapList;
    }

    private Map composePaymentRequestMap(PaymentRequest r) {
        Map map = new HashMap();

        map.put("id", r.getId());
        map.put("localCode", r.getCode());
        map.put("voucherDate", r.getVoucherDate());
        map.put("vendor", r.getVendor().getName());

        String joaCode = "";
        String supplier = "";
        map.put("particulars", joaCode + " - " + supplier);
        map.put("amount", r.getAmount());
        map.put("preparedBy", r.getCreatedBy().getFullName());
        map.put("status", r.getDocumentStatus().getStatus());

        return map;
    }

}