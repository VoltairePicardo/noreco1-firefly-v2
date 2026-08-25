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
import com.noreco1.fireflyv2.controller.response.reports.PODetail;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.PurchaseOrderService;
import com.noreco1.fireflyv2.validator.PoValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Personal on 5/15/2015.
 */
@Service(value = "poServiceImpl")
public class PurchaseOrderServiceImpl implements PurchaseOrderService, PrintableVoucher {

    private PurchaseOrder model;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    PurchaseOrderRepo purchaseOrderRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    SlEntityRepo slEntityRepo;

    @Autowired
    PoDetailRepo poDetailRepo;

    @Autowired
    PurchaseRequestDetailRepo purchaseRequestDetailRepo;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    PoDetailServiceImpl poDetailDto;

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
    SettingFacade settingFacade;

    @Autowired
    SupplierRepo supplierRepo;

    @Autowired
    QuotationItemDetailRepo quotationItemDetailRepo;

    @Autowired
    PurchaseOrderBudgetDetailRepo purchaseOrderBudgetDetailRepo;

    @Autowired
    Environment env;

    @Autowired
    FileFacade fileFacade;

    private Map reportMeta;

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrder findByCode(String code) {
        List<PurchaseOrder> pos = purchaseOrderRepo.findByCode(code);

        if (!Checker.collectionIsEmpty(pos)) {
            return pos.get(0);
        } else return null;
    }

    @Override
    @Transactional
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        PurchaseOrder purchaseOrder = (PurchaseOrder) v;
        return this.processCreate(purchaseOrder, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        PurchaseOrder purchaseOrder = (PurchaseOrder) v;
        PostResponse response = new PostResponse();
        response.setSuccess(false);

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        PoValidator validator = new PoValidator();
        validator.setService(this);
        validator.validate(purchaseOrder, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            PurchaseOrder existingPo = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(purchaseOrder.getVoucherDate()));

            User budgetCheckedBy = userRepo.findOneByAccountNo(purchaseOrder.getBudgetCheckedBy().getAccountNo());
            User checkedBy = userRepo.findOneByAccountNo(purchaseOrder.getCheckedBy().getAccountNo());
            User approvedBy = userRepo.findOneByAccountNo(purchaseOrder.getApprovingOfficer().getAccountNo());

            Boolean insertMode = purchaseOrder.getId() == null;
            if (insertMode) { // insert mode
                Object latestCanvassCode = purchaseOrderRepo.findLatestPoCodeByYear(voucherYear);
                purchaseOrder.setCode(generatorFacade.voucherCodeNoOffice("PO", (latestCanvassCode == null ? "" : String.valueOf(latestCanvassCode)), purchaseOrder.getVoucherDate(), GlobalConstant.COUNTER_PAD_4));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                purchaseOrder.setDocumentStatus(documentStatus);

                purchaseOrder.setTransaction(generatorFacade.transaction());
                purchaseOrder.setCreatedBy(createdBy);
                existingPo = purchaseOrder;
            } else {
                existingPo = purchaseOrderRepo.findById(purchaseOrder.getId()).orElse(null);
            }
            // use for document logging
            Map oldMap = this.forLogMapMain(existingPo);

            Workflow wf = new Workflow();
            wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.PO.getId());

            existingPo.setWorkflow(wf);
            existingPo.setVendor(slEntityRepo.getReferenceById(purchaseOrder.getVendor().getAccountNo()));
            existingPo.setVoucherDate(purchaseOrder.getVoucherDate());
            existingPo.setYear(voucherYear);
            existingPo.setBudgetCheckedBy(budgetCheckedBy);
            existingPo.setCheckedBy(checkedBy);
            existingPo.setApprovingOfficer(approvedBy);
            existingPo.setAmount(purchaseOrder.getAmount());
            existingPo.setDeliveryAddress(purchaseOrder.getDeliveryAddress());
            existingPo.setDeliveryTerm(purchaseOrder.getDeliveryTerm());
            existingPo.setPaymentTerm(purchaseOrder.getPaymentTerm());
            existingPo.setPurpose(purchaseOrder.getPurpose());
            existingPo.setCashAdvance(purchaseOrder.getCashAdvance());
            existingPo.setUseCreditCard(purchaseOrder.getUseCreditCard());
            existingPo.setDepartment(purchaseOrder.getDepartment());
            existingPo.setBudgetLineItemDetail(purchaseOrder.getBudgetLineItemDetail());
            existingPo.setBudgetLineItemBalancePOJORFP(purchaseOrder.getBudgetLineItemBalancePOJORFP());
            existingPo.setBudgetLineItemBalanceCV(purchaseOrder.getBudgetLineItemBalanceCV());
            existingPo.setDeliveryTimeAndCompletion(purchaseOrder.getDeliveryTimeAndCompletion());

            this.model = purchaseOrderRepo.save(existingPo);

            if (this.model != null) {

                // start: update default signatories
                signatoryFacade.po(this.model);
                // end: update default signatories

                if (!insertMode) {
                    poDetailRepo.deleteByPurchaseOrderId(existingPo.getId());
                }

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldMap = null;
                }

                ArrayList<PoDetailDto> poDetails = purchaseOrder.getPoDetails();
                for(PoDetailDto poDetailLine: poDetails) {
                    PoDetail poDetail = new PoDetail();

                    PurchaseOrder po1 = new PurchaseOrder();
                    po1.setId(this.model.getId());
                    poDetail.setPurchaseOrder(po1);

                    PurchaseRequestDetail purchaseRequestDetail = new PurchaseRequestDetail();
                    purchaseRequestDetail.setId(poDetailLine.getRvDetailId());
                    purchaseRequestDetail.setPoQuantity(poDetailLine.getPoQuantity());
                    poDetail.setPurchaseRequestDetail(purchaseRequestDetail);

                    poDetail.setQuantity(poDetailLine.getQuantity());
                    poDetail.setUnitPrice(poDetailLine.getUnitPrice());
                    poDetail.setVat(poDetailLine.getVat());
                    poDetail.setDiscount(poDetailLine.getDiscount());
                    poDetail.setAmount(poDetailLine.getItemAmount());

                    if(poDetailLine.getBrand() != null && poDetailLine.getBrand().getId() != null) poDetail.setBrand(poDetailLine.getBrand());

                    if(!poDetailLine.getQuantity().equals(BigDecimal.ZERO)) {
                        PoDetail newPod = poDetailRepo.save(poDetail);
                    }
                    purchaseRequestDetailRepo.updatePoQuantityById(purchaseRequestDetail.getId(), purchaseRequestDetail.getPoQuantity());
                }

                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());
                response.setSuccessMessage("Purchase Order successfully saved!");
                response.setSuccess(true);
            }
        }

        return response;
    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            PurchaseOrder doc = purchaseOrderRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (doc != null) {
                Map map = forLogMapMain(doc);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.PO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PoListDto> findAll() {
        List<PurchaseOrder> vouchers = purchaseOrderRepo.findAll();

        List<PoListDto> returnVouchers = new ArrayList<>();
        if (!Checker.collectionIsEmpty(vouchers)) {
            for(PurchaseOrder po : vouchers) {
                PoListDto poListDto = new PoListDto();
                poListDto.setId(po.getId());
                poListDto.setVoucherDate(po.getVoucherDate());
                poListDto.setLocalCode(po.getCode());
                poListDto.setSupplier(po.getVendor().getName());
                poListDto.setAmount(po.getAmount());
                poListDto.setStatus(po.getDocumentStatus().getStatus());

                SlEntity createdBy = slEntityRepo.findById(po.getCreatedBy().getAccountNo()).orElse(null);
                poListDto.setPreparedBy(createdBy.getName());

                returnVouchers.add(poListDto);
            }

            return returnVouchers;
        }
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
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

            List<PurchaseOrder> docs = purchaseOrderRepo.findByDocumentStatusIdAndVoucherDateBetween(id, fromDate, toDate);
            return this.makePOListMap(docs);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Map> findByDateRangeAndStatusIdAndForEditing(String from, String to, Integer id, Integer officeId) {
        try {
            java.util.Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            java.util.Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new java.util.Date(0);
            }

            if (toDate == null) {
                toDate = new java.util.Date();
            }

            List<PurchaseOrder> docs = purchaseOrderRepo.findByDocumentStatusIdAndVoucherDateBetweenAndOfficeId(id, fromDate, toDate, officeId);
            return this.makePOListMap(docs);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<PODetail> poDetails(Integer id) {
        List<PODetail> details = new ArrayList<>();

        PurchaseOrder voucher = purchaseOrderRepo.findById(id).orElse(null);
        if (voucher != null) {
            List<PoDetailDto> poDetailLineDtos = poDetailDto.getPoDetails(id);

            if (!Checker.collectionIsEmpty(poDetailLineDtos)) {

                PurchaseRequest purchaseRequest = null;
                Quotation abstractOfQuotation = null;

                for(PoDetailDto dto : poDetailLineDtos) {
                    PODetail d = new PODetail();

                    d.setId(poDetailLineDtos.indexOf(dto) + 1);
                    d.setDescription(dto.getItemDescription());
                    d.setUnitCode(dto.getUnitCode());
                    d.setQuantity(dto.getQuantity());
                    d.setUnitPrice(dto.getUnitPrice());
                    d.setAmount(dto.getItemAmount());
                    d.setBrand(dto.getBrand() == null ? "":dto.getBrand().getName());

                    details.add(d);

                    if(purchaseRequest == null) {
                        PurchaseRequestDetail purchaseRequestDetail = this.purchaseRequestDetailRepo.findById(dto.getRvDetailId()).orElse(null);
                        if(purchaseRequestDetail != null) {
                            purchaseRequest = purchaseRequestDetail.getPurchaseRequest();
                        }
                    }

                    if(abstractOfQuotation == null) {
                        QuotationItemDetail quotationItemDetail = this.quotationItemDetailRepo.findOneByQuotationItemPurchaseRequestDetailIdAndSupplierAccountNumber(dto.getRvDetailId(), voucher.getVendor().getAccountNo());
                        if(quotationItemDetail != null) {
                            abstractOfQuotation = quotationItemDetail.getQuotationItem().getQuotation();
                        }
                    }
                }

                if(purchaseRequest != null) {
                    this.reportMeta.put("PR_NO", purchaseRequest.getCode());

                    Employee requestedBy = employeeRepo.findOneByAccountNumber(purchaseRequest.getCreatedBy().getAccountNo());

                    this.reportMeta.put("REQUESTED_BY", requestedBy.getName());
                    this.reportMeta.put("REQUESTED_BY_POS", requestedBy.getPosition().getName());

                    if (requestedBy.getSignature() != null) {
                        this.reportMeta.put("REQUESTED_BY_SIGN", env.getProperty("path.attachments") + requestedBy.getSignature().getFilename());
                    }

                    if (purchaseRequest.getBudgetLineItemDetail() != null) {
                        this.reportMeta.put("BUDGET_LINE_ITEM", purchaseRequest.getBudgetLineItemDetail().getTitle() + " - " + purchaseRequest.getBudgetLineItemDetail().getCode());
                        this.reportMeta.put("BUDGET_LINE_ITEM_BALANCE", voucher.getBudgetLineItemBalancePOJORFP());
                    }
                }

                if(abstractOfQuotation != null) {
                    try {
                        this.reportMeta.put("MODE_OF_PROCUREMENT", purchaseRequest.getModeOfProcurement() != null ? purchaseRequest.getModeOfProcurement().getName() : "");
                    } catch (Exception e) {}
                }

                PurchaseOrderBudgetDetail purchaseOrderBudgetDetail = purchaseOrderBudgetDetailRepo.findFirstByPurchaseOrderIdOrderByIdAsc(voucher.getId());

                if(purchaseOrderBudgetDetail != null){
                    this.reportMeta.put("CASH_FLOW_ITEM", purchaseOrderBudgetDetail.getBudgetSubItem().getBudgetLineItemDetail().getTitle());
//                    this.reportMeta.put("CASH_FLOW_ITEM_AMOUNT", voucher.getCashFlowItemBalancePOJORFP().subtract(voucher.getCashFlowItemTotal()));
                    this.reportMeta.put("CASH_FLOW_ITEM_AMOUNT", BigDecimal.ZERO);
                }

            }
        }

        return details;
    }

    @Override
    public Map reportMeta() {
        return this.reportMeta;
    }

    @Override
    public Page<PurchaseOrder> findPurchaseOrderForItemTestingByStatusAndFilter(Integer statusId, String filter, Pageable pageable) {
        if (filter == null) {
            filter = "";
        }
        return purchaseOrderRepo.findPurchaseOrderForItemTestingByFilter(statusId, "%"+filter+"%", pageable);
    }

    @Override
    public Page<PurchaseOrder> findPurchaseOrderWithItemTestingForRRByFilter(String filter, Pageable pageable) {
        if (filter == null) {
            filter = "";
        }
        return purchaseOrderRepo.findPurchaseOrderWithItemTestingForRRByFilter(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), "%"+filter+"%", pageable);
    }

    @Override
    public List<PoListDto> findForCV() {
        List<PurchaseOrder> vouchers = purchaseOrderRepo.findPurchaseOrdersForCV(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());

        List<PoListDto> returnVouchers = new ArrayList<>();
        if (!Checker.collectionIsEmpty(vouchers)) {
            for(PurchaseOrder po : vouchers) {
                PoListDto poListDto = new PoListDto();
                poListDto.setId(po.getId());
                poListDto.setVoucherDate(po.getVoucherDate());
                poListDto.setLocalCode(po.getCode());
                poListDto.setSupplier(po.getVendor().getName());
                poListDto.setAmount(po.getAmount());
                poListDto.setStatus(po.getDocumentStatus().getStatus());

                SlEntity createdBy = slEntityRepo.findById(po.getCreatedBy().getAccountNo()).orElse(null);
                poListDto.setPreparedBy(createdBy.getName());

                poListDto.setBudgetLineItemDetail(po.getBudgetLineItemDetail());

                returnVouchers.add(poListDto);
            }

            return returnVouchers;
        }
        return null;
    }

    @Override
    public boolean isDocumentForCashFlowItemAssignment(Integer transactionId) {

        boolean isDocumentForCashFlowItemAssignment = false;

        try {
            PurchaseOrder existingPO = this.purchaseOrderRepo.findOneByTransactionId(transactionId);

            if (existingPO != null && isCurrentUserAuthorized(existingPO) && isDocumentForBudgetChecking(existingPO)) {
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

            PurchaseOrder existingPo = this.purchaseOrderRepo.findOneByTransactionId(dto.getTransId());

            if(existingPo.getBudgetCheckedBy().getAccountNo().equals(authenticationFacade.getLoggedIn().getAccountNo())){

                existingPo.setCashFlowItemBalancePOJORFP(dto.getCashFlowItemBalancePOJORFP());
                existingPo.setCashFlowItemBalanceCV(dto.getCashFlowItemBalanceCV());
                existingPo.setCashFlowItemTotal(dto.getCashFlowItemTotal());

                this.model = purchaseOrderRepo.save(existingPo);

                if (this.model != null) {

                    purchaseOrderBudgetDetailRepo.deleteByPurchaseOrderId(existingPo.getId());

                    ArrayList<BudgetSubItem> budgetDetails = dto.getBudgetSubItems();
                    for (BudgetSubItem budgetSubItem : budgetDetails){

                        PurchaseOrderBudgetDetail newPurchaseOrderBudgetDetail = new PurchaseOrderBudgetDetail();

                        PurchaseOrder po1 = new PurchaseOrder();
                        po1.setId(this.model.getId());
                        newPurchaseOrderBudgetDetail.setPurchaseOrder(po1);

                        newPurchaseOrderBudgetDetail.setBudgetSubItem(budgetSubItem);
                        newPurchaseOrderBudgetDetail.setAmount(budgetSubItem.getAmount());
                        newPurchaseOrderBudgetDetail.setBudgetSubItemAmountBalanceCV(budgetSubItem.getBudgetSubItemAmountBalanceCV());
                        newPurchaseOrderBudgetDetail.setBudgetSubItemAmountBalancePOJO(budgetSubItem.getBudgetSubItemAmountBalancePOJO());

                        purchaseOrderBudgetDetailRepo.save(newPurchaseOrderBudgetDetail);

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
    public Page<PurchaseOrder> findAllForCreditCardPurchaseRequestByStatusAndFilter(Integer statusId, String filter, Pageable pageable) {
        if (filter == null) {
            filter = "";
        }
        return purchaseOrderRepo.findAllForCreditCardPurchaseRequestByStatusAndFilter(statusId, "%"+filter+"%", pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
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
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId()
            };

            List<PurchaseOrder> docs = purchaseOrderRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(fromDate, toDate, Arrays.asList(ids));
            return this.makePOListMap(docs);

        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<PurchaseOrder> findByStatusAndFilter(Integer statusId, String filter, Pageable pageable) {
        if (filter == null) {
            filter = "";
        }
        return purchaseOrderRepo.findByStatusAndFilter(statusId, "%"+filter+"%", pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<PurchaseOrder> findByStatusId(Integer statusId) {
        return this.purchaseOrderRepo.findByDocumentStatusIdOrderByIdDesc(statusId);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public PoDto findById(Integer id) {
        PurchaseOrder po =  purchaseOrderRepo.findById(id).orElse(null);
        PoDto poDto = new PoDto();

        if (po != null) {
            poDto.setId(po.getId());
            poDto.setLocalCode(po.getCode());
            poDto.setTransId(po.getTransaction().getId());
            poDto.setAmount(po.getAmount());

            SlEntity createdBy = slEntityRepo.findById(po.getCreatedBy().getAccountNo()).orElse(null);

            SlEntity budgetCheckedBy = slEntityRepo.findById(po.getBudgetCheckedBy().getAccountNo()).orElse(null);
            SlEntity checkedBy = slEntityRepo.findById(po.getCheckedBy().getAccountNo()).orElse(null);
            SlEntity approvedBy = slEntityRepo.findById(po.getApprovingOfficer().getAccountNo()).orElse(null);

            poDto.setCreatedBy(createdBy);
            poDto.setBudgetCheckedBy(budgetCheckedBy);
            poDto.setCheckedBy(checkedBy);
            poDto.setApprovedBy(approvedBy);
            poDto.setPaymentTerm(po.getPaymentTerm());
            poDto.setVendor(po.getVendor());
            poDto.setVoucherDate(po.getVoucherDate());
            poDto.setDocumentStatus(po.getDocumentStatus());
            poDto.setCreated(po.getCreatedAt());
            poDto.setLastUpdated(po.getUpdatedAt());

            poDto.setDeliveryAddress(po.getDeliveryAddress());
            poDto.setDeliveryTerm(po.getDeliveryTerm());
            poDto.setDeliveryTermPretty(DeliveryTerm.valueOf(po.getDeliveryTerm()).getDescription());
            poDto.setPurpose(po.getPurpose());
            poDto.setDeliveryTimeAndCompletion(po.getDeliveryTimeAndCompletion());
//            poDto.setVehicle(po.getVehicle());
            poDto.setCashAdvance(po.getCashAdvance());
            poDto.setUseCreditCard(po.getUseCreditCard());
            poDto.setBudgetLineItemBalancePOJORFP(po.getBudgetLineItemBalancePOJORFP());
            poDto.setBudgetLineItemBalanceCV(po.getBudgetLineItemBalanceCV());
            poDto.setCashFlowItemBalancePOJORFP(po.getCashFlowItemBalancePOJORFP());
            poDto.setCashFlowItemBalanceCV(po.getCashFlowItemBalanceCV());
            poDto.setCashFlowItemTotal(po.getCashFlowItemTotal());
            poDto.setReceivedDate(po.getReceivedDate());
            poDto.setReceivedBy(po.getReceivedBy());
            poDto.setExpectedDeliveryDate(po.getExpectedDeliveryDate());

            List<PoDetail> poDetails = poDetailRepo.findByPurchaseOrderId(po.getId());

            if(!poDetails.isEmpty()){

                for(PoDetail poDetail : poDetails){

                    if(poDetail.getPurchaseRequestDetail() != null){

                        poDto.setPurchaseRequest(poDetail.getPurchaseRequestDetail().getPurchaseRequest());

                    }

                }

            }

        }

        return  poDto;
    }

    @Override
    public HashMap reportParameters(Integer id, HttpServletRequest request) {
        this.reportMeta = new HashMap();

        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        PurchaseOrder purchaseOrder = purchaseOrderRepo.findById(id).orElse(null);

        if (purchaseOrder != null) {
            params.put("VOUCHER_NO", purchaseOrder.getCode());
            params.put("V_DATE", purchaseOrder.getVoucherDate());
            params.put("APPROVEDBY", purchaseOrder.getApprovingOfficer().getFullName());
            params.put("CHECKEDBY", purchaseOrder.getCheckedBy().getFullName());
            params.put("PREPAREDBY", purchaseOrder.getCreatedBy().getFullName());

            Supplier supplier = supplierRepo.findOneByAccountNumber(purchaseOrder.getVendor().getAccountNo());

            if (supplier != null) {
                params.put("SUPPLIER", supplier.getName());
                params.put("SUPPLIER_ADDRESS", supplier.getAddress());
                params.put("SUPPLIER_CONTACT", supplier.getPhone());
                params.put("SUPPLIER_TIN", supplier.getTin());
            }

            if(Checker.isAmountGreaterThanZero(purchaseOrder.getPaymentTerm())) {
                String termInWords = NumberToWord.convert(new BigDecimal(purchaseOrder.getPaymentTerm()));
                params.put("PAYMENT_TERM", "Within "+termInWords.toLowerCase()+"("+purchaseOrder.getPaymentTerm()+") calendar days after complete delivery");
            } else {
                params.put("PAYMENT_TERM", purchaseOrder.getPaymentTerm()+" DAYS");
            }

            String deliveryTerm;
            try {
                // if old values then load it
                deliveryTerm = DeliveryTerm.valueOf(purchaseOrder.getDeliveryTerm()).getDescription();
            } catch (Exception e) {
                deliveryTerm = purchaseOrder.getDeliveryTerm();
            }

            params.put("DELIVERY_PLACE", purchaseOrder.getDeliveryAddress());
            params.put("DELIVERY_TIME_AND_COMPLETION", purchaseOrder.getDeliveryTimeAndCompletion());
            params.put("DELIVERY_TERM", deliveryTerm);

            Map map = settingFacade.getByCode("WAREHOUSE_CONTACT_PERSON");
            params.put("WAREHOUSE_CONTACT_PERSON", String.valueOf(map.get("warehouseContactPerson")));

            params.put("AMOUNT", purchaseOrder.getAmount());
            params.put("PURPOSE", Checker.isStringNullOrEmpty(purchaseOrder.getPurpose()) ? "" : purchaseOrder.getPurpose());

            params.put("RECEIVED_DATE", purchaseOrder.getReceivedDate());
            params.put("RECEIVED_BY", purchaseOrder.getReceivedBy());
            params.put("EXPECTED_DELIVERY_DATE", purchaseOrder.getExpectedDeliveryDate());

            params = signatureFacade.getDocumentSignature(params, DocumentType.PO, purchaseOrder);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer id) {
        List<PODetail> details = new ArrayList<>();

        PurchaseOrder voucher = purchaseOrderRepo.findById(id).orElse(null);
        if (voucher != null) {
            List<PoDetailDto> poDetailLineDtos = poDetailDto.getPoDetails(id);

            if (!Checker.collectionIsEmpty(poDetailLineDtos)) {
                for(PoDetailDto dto : poDetailLineDtos) {
                    PODetail d = new PODetail();

                    d.setId(poDetailLineDtos.indexOf(dto) + 1);
                    d.setDescription(dto.getItemDescription());
                    d.setUnitCode(dto.getUnitCode());
                    d.setQuantity(dto.getQuantity());
                    d.setUnitPrice(dto.getUnitPrice());
                    d.setAmount(dto.getItemAmount());

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
        PurchaseOrder purchaseOrder =  purchaseOrderRepo.findById(postData.getDocumentId()).orElse(null);

        if (purchaseOrder != null) {
            // for logging
            Map oldMap = this.forLogMapMain(purchaseOrder);
            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(purchaseOrder, purchaseOrder.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            purchaseOrder.setDocumentStatus(afterActionDocumentStatus);
            purchaseOrder.setUpdatedAt(null);
            purchaseOrder = purchaseOrderRepo.save(purchaseOrder);

            // for logging
            Map newMap = this.forLogMapMain(purchaseOrder);
            newMap.put("remarks", postData.getRemarks());

            if (purchaseOrder != null) {
                documentProcessingFacade.processAction(purchaseOrder.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(purchaseOrder.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, newMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }

        }
        return response;
    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource,
                                      HttpServletRequest request, List<Map> fileToRemove) {
//        return this.processUpdate(v, bindingResult, messageSource);
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
//        return this.processCreate(v, bindingResult, messageSource);

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
        List<DocumentStatus> statuses1 = documentDtoer.getDocumentStatuses(com.noreco1.fireflyv2.model.enums.Workflow.PO.getId());
        List<DocumentStatus> statuses2 = documentDtoer.getDocumentStatuses(com.noreco1.fireflyv2.model.enums.Workflow.PO_FORBIDDING.getId());

        List<DocumentStatus> statuses = new ArrayList<>();
        if(!statuses1.isEmpty()) {

            statuses.addAll(statuses1);
            statuses = this.getExtraStatuses(statuses, statuses2);

        } else if(!statuses2.isEmpty()) {

            statuses.addAll(statuses2);
            statuses = this.getExtraStatuses(statuses, statuses1);
        }

        return statuses;
    }

    @Override
    @Transactional
    public PostResponse processSupplierReceived(Document v, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        response.setSuccess(false);
        try{
            PurchaseOrder purchaseOrder = (PurchaseOrder) v;
            PurchaseOrder existingPo = purchaseOrderRepo.findById(purchaseOrder.getId()).orElse(null);

            if (existingPo.getDocumentStatus().getId() == com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId()) {
                existingPo.setReceivedDate(purchaseOrder.getReceivedDate());
                existingPo.setReceivedBy(purchaseOrder.getReceivedBy());
                existingPo.setExpectedDeliveryDate(purchaseOrder.getExpectedDeliveryDate());
                purchaseOrderRepo.save(existingPo);
            } else {
                response.setSuccess(false);
                response.setFailureMessage("This is not allowed.");
                return response;
            }

            response.setSuccessMessage("Changes saved");
            response.setSuccess(true);

            return response;
        } catch (Exception ex) {
            response.setSuccess(false);
            response.setFailureMessage(ex.getMessage());
            return response;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map> findApprovedVendors() {
        List<Map> mapList = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();

        List<PurchaseOrder> vouchers = purchaseOrderRepo.findApprovedPurchaseOrderVendors(
                com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());
        if (!Checker.collectionIsEmpty(vouchers)) {
            for (PurchaseOrder po : vouchers) {
                Integer accountNo = po.getVendor().getAccountNo();
                if (seen.add(accountNo)) {
                    Map map = new HashMap();
                    map.put("accountNo", po.getVendor().getAccountNo());
                    map.put("name",      po.getVendor().getName());
                    map.put("address",   po.getVendor().getAddress());
                    map.put("marker",    po.getVendor().getMarker());
                    mapList.add(map);
                }
            }
        }
        return mapList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PoListDto> findBySupplierAccountNo(Integer accountNo) {
        List<PurchaseOrder> vouchers = purchaseOrderRepo.findApprovedPurchaseOrdersByVendor(
                accountNo, com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());

        List<PoListDto> result = new ArrayList<>();
        if (!Checker.collectionIsEmpty(vouchers)) {
            for (PurchaseOrder po : vouchers) {
                PoListDto dto = new PoListDto();
                dto.setId(po.getId());
                dto.setLocalCode(po.getCode());
                dto.setVoucherDate(po.getVoucherDate());
                dto.setSupplier(po.getVendor().getName());
                dto.setAmount(po.getAmount());
                dto.setStatus(po.getDocumentStatus().getStatus());

                SlEntity createdBy = slEntityRepo.findById(po.getCreatedBy().getAccountNo()).orElse(null);
                if (createdBy != null) dto.setPreparedBy(createdBy.getName());

                result.add(dto);
            }
        }
        return result;
    }

    private List<DocumentStatus> getExtraStatuses(List<DocumentStatus> returnStatuses, List<DocumentStatus> newStatuses) {

        if(!newStatuses.isEmpty()) {

            for(DocumentStatus status:newStatuses) {

                boolean inReturnStatuses = false;
                for(DocumentStatus exStatus:returnStatuses) {
                    if(status.getId().equals(exStatus.getId())) {
                        inReturnStatuses = true;
                        break;
                    }
                }

                if(!inReturnStatuses) returnStatuses.add(status);
            }
        }

        return returnStatuses;

    }

    private Map forLogMapMain(PurchaseOrder po) {
        return documentLoggerFacade.makeLog(po);
    }

    private List<Map> makePOListMap(List<PurchaseOrder> cs ) {

        List<Map> mapList = new ArrayList<>();

        if (!Checker.collectionIsEmpty(cs)) {
            for(PurchaseOrder c:cs) {
                mapList.add(composePOMap(c));
            }
        }
        return mapList;
    }

    private Map composePOMap(PurchaseOrder r) {
        Map map = new HashMap();

        map.put("id", r.getId());
        map.put("localCode", r.getCode());
        map.put("voucherDate", r.getVoucherDate());
        map.put("supplier", r.getVendor().getName());
        map.put("amount", r.getAmount());
        map.put("preparedBy", r.getCreatedBy().getFullName());
        map.put("status", r.getDocumentStatus().getStatus());

        return map;
    }

    private boolean isCurrentUserAuthorized(PurchaseOrder existingPO) {
        return existingPO.getBudgetCheckedBy().getAccountNo().equals(this.authenticationFacade.getLoggedIn().getAccountNo());
    }

    private boolean isDocumentForBudgetChecking(PurchaseOrder existingPO) {
        return existingPO.getDocumentStatus().getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_BUDGET_CHECKING.getId());
    }

}
