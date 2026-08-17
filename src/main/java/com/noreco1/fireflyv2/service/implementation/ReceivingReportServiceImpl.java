package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.InventoryLocation;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.model.enums.InventoryCategory;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.ReceivingReportService;
import com.noreco1.fireflyv2.validator.ReceivingReportValidator;
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
import java.math.BigDecimal;
import java.util.*;

@Service(value = "receivingReportServiceImpl")
public class ReceivingReportServiceImpl implements ReceivingReportService, PrintableVoucher {

    private ReceivingReport model;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private DocumentLogRepo documentLogRepo;

    @Autowired
    private SignatoryFacade signatoryFacade;

    @Autowired
    private ReceivingReportRepo receivingReportRepo;

    @Autowired
    private ReceivingReportDetailRepo receivingReportDetailRepo;

    @Autowired
    private SignatureFacade signatureFacade;

    @Autowired
    private DocumentDtoer documentDtoer;

    @Autowired
    private DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    private SupplierRepo supplierRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PoDetailRepo poDetailRepo;

    @Autowired
    private DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    ItemStockRepo itemStockRepo;

    @Autowired
    StockTransactionRepo stockTransactionRepo;

    @Autowired
    StockTransactionDetailRepo stockTransactionDetailRepo;
    @Autowired
    SettingFacade settingFacade;

    @Autowired
    InventoryLocationRepo inventoryLocationRepo;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    ItemTransactionDetailRepo itemTransactionDetailRepo;

    @Autowired
    ItemsForRepairRepo itemsForRepairRepo;

    @Autowired
    FileFacade fileFacade;

    @Autowired
    PurchaseRequestDetailRepo PurchaseRequestDetailRepo;

    @Autowired
    ItemStockDetailRepo itemStockDetailRepo;

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
    @Transactional
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        ReceivingReport rr = (ReceivingReport) v;
        return this.processCreate(rr, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        ReceivingReport receivingReport = (ReceivingReport) v;

        PostResponse response = new PostResponse();
        response.setSuccess(false);

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        ReceivingReportValidator validator = new ReceivingReportValidator();
        validator.setService(this);
        validator.validate(receivingReport, bindingResult);

        // use for document logging
        Map oldJvMap = null;

        User createdBy = authenticationFacade.getLoggedIn();

        try {

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
            } else {
                boolean insertMode = receivingReport.getId() == null;
                ReceivingReport existingReceivingReport = null;

                User approvedBy = userRepo.findOneByAccountNo(receivingReport.getApprovingOfficer().getAccountNo());
                User checkedBy = userRepo.findOneByAccountNo(receivingReport.getChecker().getAccountNo());
                Supplier supplier = supplierRepo.findOneByAccountNumber(receivingReport.getSupplier().getAccountNumber());

                Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(DateHelper.getServerDate()));

                if (insertMode) { // create new

                    receivingReport.setApprovingOfficer(approvedBy);
                    receivingReport.setChecker(checkedBy);
                    receivingReport.setSupplier(supplier);
                    receivingReport.setRemarks(receivingReport.getRemarks());

                    receivingReport.setYear(voucherYear);
                    receivingReport.setCreatedBy(createdBy);
                    receivingReport.setCode(this.getCode(receivingReport, voucherYear));

                    DocumentStatus documentStatus = new DocumentStatus();
                    documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());

                    Workflow wf = new Workflow();
                    wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.RR.getId());

                    receivingReport.setDocumentStatus(documentStatus);
                    receivingReport.setTransaction(generatorFacade.transaction());
                    receivingReport.setWorkflow(wf);

                    existingReceivingReport = receivingReport;

                } else { // update

                    List<Integer> statusAllowed = new ArrayList();
                    statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                    statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.RETURNED_TO_CREATOR.getId());

                    existingReceivingReport = receivingReportRepo.findById(receivingReport.getId()).orElse(null);

                    // editing is authorized
                    if (existingReceivingReport != null && statusAllowed.indexOf(existingReceivingReport.getDocumentStatus().getId()) >= 0) {

                        // RR details
                        List<ReceivingReportDetail> rrDetails = receivingReportDetailRepo.findByReceivingReportId(receivingReport.getId());
                        existingReceivingReport.setRrDetails(rrDetails);
                        oldJvMap = this.forLogMapMain(existingReceivingReport);

                        existingReceivingReport.setApprovingOfficer(approvedBy);
                        existingReceivingReport.setChecker(checkedBy);
                        existingReceivingReport.setSupplier(supplier);

                        existingReceivingReport.setTotalAmount(receivingReport.getTotalAmount());
                        existingReceivingReport.setDeliveryDate(receivingReport.getDeliveryDate());
                        existingReceivingReport.setDeliveryNumber(receivingReport.getDeliveryNumber());
                        existingReceivingReport.setTotalQuantity(receivingReport.getTotalQuantity());
                        existingReceivingReport.setInvoiceDate(receivingReport.getInvoiceDate());
                        existingReceivingReport.setInvoiceNumber(receivingReport.getInvoiceNumber());
                        existingReceivingReport.setInventoryLocation(receivingReport.getInventoryLocation());
                        existingReceivingReport.setRemarks(receivingReport.getRemarks());
                        existingReceivingReport.setYear(voucherYear);
                        existingReceivingReport.setOffice(receivingReport.getOffice());
                        existingReceivingReport.setIsRepairedItems(receivingReport.getIsRepairedItems());
                        existingReceivingReport.setIsJO(receivingReport.getIsJO());
                        existingReceivingReport.setDepartment(receivingReport.getDepartment());

                    } else {

                        response.setFailureMessage("Unauthorized");
                        return response;
                    }

                }

                existingReceivingReport.setUpdatedAt(new Date());
                this.model = receivingReportRepo.save(existingReceivingReport);

                if (this.model != null) {

                    // start: update default signatories
                    signatoryFacade.rr(this.model);
                    // end: update default signatories

                    // start: save details
                    if (! insertMode) { // update: reset details
                        receivingReportDetailRepo.deleteByReceivingReportId(this.model.getId());
                    }

                    List<ReceivingReportDetail> rrDetails = receivingReport.getRrDetails(); // data from post
                    for (ReceivingReportDetail detail: rrDetails) {
                        if(detail.getQuantityReceived().compareTo(BigDecimal.ZERO) == 1) {

                            if(detail.getPoDetail() == null || !Checker.isValidId(detail.getPoDetail().getId())) {  // receive item without PO

                                if (supplier.isVatable() && detail.getAmount().compareTo(BigDecimal.ZERO) > 0) {

                                    // amount / [(100+12%) * 100]
                                    BigDecimal vat = detail.getAmount().divide(new BigDecimal(1.12), 2, BigDecimal.ROUND_HALF_UP);

                                    detail.setVat(vat);
                                }
                            }

                            detail.setReceivingReport(this.model);

                            // netVatUnitPrice = { netAmount / [ (100+VAT) / 100] } / quantityReceived
                            BigDecimal netVatUnitPrice = detail.getNetAmount().divide(new BigDecimal(1.12), 2, BigDecimal.ROUND_HALF_UP);
                            netVatUnitPrice = netVatUnitPrice.divide(detail.getQuantityReceived(),  2, BigDecimal.ROUND_HALF_UP);

                            detail.setNetVatUnitPrice(netVatUnitPrice);
                            detail.setNetVatAmount(netVatUnitPrice.multiply(detail.getQuantityReceived()));

                            detail.setVat(detail.getNetAmount().subtract(detail.getNetVatAmount()));    // to fix round-off difference

                            receivingReportDetailRepo.save(detail);
                        }
                    } // end: save details

                    if (insertMode) { // log action only when adding document
                        documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    }

                    // generic document logging here
                    // old value only
                    DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldJvMap, null);
                    response.setLogId(log != null ? log.getId() : 0);
                }

                response.setSuccess(true);
                response.setModelId(this.model.getId());
                response.setSuccessMessage("Receiving report successfully received");
            }

        }catch (Exception e) {
            e.printStackTrace();
            response.setFailureMessage("Internal server error");
        }
        return response;
    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            ReceivingReport rr = receivingReportRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (rr != null) {
                // RR details
                List<ReceivingReportDetail> rrDetails = receivingReportDetailRepo.findByReceivingReportId(rr.getId());
                rr.setRrDetails(rrDetails);

                Map map = forLogMapMain(rr);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(DocumentType.RR);
    }

    @Override
    public List<Map> findByDateRangeAndStatusId(String from, String to, Integer statusId) {

        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            List<ReceivingReport> docs = receivingReportRepo.findByDocumentStatusIdAndDeliveryDateBetween(statusId, fromDate, toDate);
            return this.makeRRListMap(docs);
        }catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Map> findByDateRangePending(String from, String to) {

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

            List<ReceivingReport> docs = receivingReportRepo.findByDeliveryDateBetweenAndDocumentStatusIdNotIn(fromDate, toDate, Arrays.asList(ids));
            return this.makeRRListMap(docs);

        }catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Map> getRRDetails(Integer rrId) {

        List<Map> data = new ArrayList<>();

        ReceivingReport receivingReport = receivingReportRepo.findById(rrId).orElse(null);
        if (receivingReport != null) {

            List<ReceivingReportDetail> rrDetails = receivingReportDetailRepo.findByReceivingReportId(receivingReport.getId());

            if(! rrDetails.isEmpty() ) {

                int counter = 1;
                for (ReceivingReportDetail detail:rrDetails) {

                    Map detailMap = new HashMap();

                    detailMap.put("id", counter++);
                    detailMap.put("description", detail.getItem().getDescription());

                    if(detail.getPurchaseRequestDetail() != null) {
                        detailMap.put("rvNo", detail.getPurchaseRequestDetail().getPurchaseRequest().getCode());
                    } else {
                        detailMap.put("rvNo", "");
                    }
                    detailMap.put("quantity", detail.getQuantityOrdered());
                    detailMap.put("unitCode", detail.getItem().getUnit().getCode());
                    detailMap.put("unitPrice", detail.getUnitPrice());
                    detailMap.put("netVatAmount", detail.getNetVatAmount());
                    detailMap.put("netVatUnitPrice", detail.getNetVatUnitPrice());
                    detailMap.put("prevReceivedQty", detail.getQuantityReceived());
                    detailMap.put("vat", detail.getNetAmount().subtract(detail.getNetVatAmount()));
                    if (detail.getReceivingReport().getIsRepairedItems()) {
                        detailMap.put("quantity", detail.getDeliveredQuantity());
                    }
                    detailMap.put("netAmount", detail.getNetAmount());

                    data.add(detailMap);
                }
            }
        }

        return data;
    }

    @Override
    public Map findById(Integer id) {
        Map rr = new HashMap();

        ReceivingReport receivingReport = receivingReportRepo.findById(id).orElse(null);
        if (receivingReport != null) {
            rr.put("id", receivingReport.getId());
            rr.put("code", receivingReport.getCode());
            rr.put("deliveryDate", receivingReport.getDeliveryDate());
            rr.put("deliveryNumber", receivingReport.getDeliveryNumber());
            rr.put("invoiceDate", receivingReport.getDeliveryDate());
            rr.put("invoiceNumber", receivingReport.getInvoiceNumber());
            rr.put("remarks", receivingReport.getRemarks());
            rr.put("documentStatus", receivingReport.getDocumentStatus());
            rr.put("inventoryLocation", receivingReport.getInventoryLocation());
            rr.put("totalAmount", receivingReport.getTotalAmount());
            rr.put("lastUpdated", receivingReport.getUpdatedAt());

            Map createdByMap = new HashMap();
            createdByMap.put("accountNo", receivingReport.getCreatedBy().getAccountNo());
            createdByMap.put("name", receivingReport.getCreatedBy().getFullName());
            rr.put("createdBy", createdByMap);

            Map checkerMap = new HashMap();
            checkerMap.put("accountNo", receivingReport.getChecker().getAccountNo());
            checkerMap.put("name", receivingReport.getChecker().getFullName());

            rr.put("checker", checkerMap);

            Map approvedByMap = new HashMap();
            approvedByMap.put("accountNo", receivingReport.getApprovingOfficer().getAccountNo());
            approvedByMap.put("name", receivingReport.getApprovingOfficer().getFullName());

            rr.put("approvingOfficer", approvedByMap);
            rr.put("workflow", receivingReport.getWorkflow());
            rr.put("transaction", receivingReport.getTransaction());

            // delivery number is in RR details
            ArrayList<Object> rrDetailsList = new ArrayList<>();
            List<ReceivingReportDetail> rrDetails = receivingReportDetailRepo.findByReceivingReportId(receivingReport.getId());
            rr.put("rrDetails", rrDetailsList);

            if(! rrDetails.isEmpty() ) {
                ReceivingReportDetail rrDetail = rrDetails.get(0);

                rr.put("deliveryNumber", rrDetail.getDeliveryNumber());

                if(receivingReport.getIsJO()) {
                    rr.put("isJO", receivingReport.getIsJO());

                    Map joMap = new HashMap();
                    joMap.put("id", rrDetail.getJoDetail().getJobOrder().getId());
                    joMap.put("joDesc", rrDetail.getJoDetail().getJobOrder().getCode() + " : " + rrDetail.getJoDetail().getJobOrder().getVendor().getName());

                    rr.put("jobOrder", joMap);
                }

                if (rrDetail.getPoDetail() != null) {
                    PurchaseOrder purchaseOrder = rrDetail.getPoDetail().getPurchaseOrder();

                    Map poMap = new HashMap();
                    poMap.put("id", purchaseOrder.getId());
                    poMap.put("poDesc", purchaseOrder.getCode() + " : " + purchaseOrder.getVendor().getName());
                    poMap.put("vendor", purchaseOrder.getVendor());

                    rr.put("purchaseOrder", poMap);
                }
                if (rrDetail.getItemTransactionDetail() != null) {
                    rr.put("isRepairedItems", receivingReport.getIsRepairedItems());
                    ItemsForRepair itemsForRepair = itemsForRepairRepo.findOneByTransactionId(rrDetail.getItemTransactionDetail().getTransaction().getId());

                    Map ifrMap = new HashMap();
                    ifrMap.put("id", itemsForRepair.getId());
                    ifrMap.put("ifrDesc", itemsForRepair.getCode() + " : " + itemsForRepair.getSupplier().getName());
                    ifrMap.put("supplier", itemsForRepair.getSupplier());

                    rr.put("itemsForRepair", ifrMap);
                }
                if (receivingReport.getIsRV() && rrDetail.getPurchaseRequestDetail() != null) {
                    PurchaseRequest purchaseRequest = rrDetail.getPurchaseRequestDetail().getPurchaseRequest();
                    Map rvMap = new HashMap();
                    rvMap.put("id", purchaseRequest.getId());
                    rvMap.put("rvDesc", purchaseRequest.getCode());

                    rr.put("requisitionVoucher", rvMap);
                    rr.put("isRV", true);
                }
                rr.put("vendor", receivingReport.getSupplier());
                rr.put("supplier", receivingReport.getSupplier());

                for (ReceivingReportDetail detail: rrDetails) {

                    Map detailMap = new HashMap();

                    if(receivingReport.getIsJO()) {
                        detailMap.put("id", detail.getJoDetail() != null ? detail.getJoDetail().getId() : 0);
                    } else if(detail.getPoDetail() != null) {
                        detailMap.put("id", detail.getPoDetail().getId());
                    } else {
                        detailMap.put("id", 0);
                    }

                    ItemStockDetail itemStockDetail = this.itemStockDetailRepo.findFirstByReceivingReportDetailId(detail.getId());

                    detailMap.put("itemId", detail.getItem().getId());
                    detailMap.put("itemDescription", detail.getItem().getDescription());
                    detailMap.put("quantity", detail.getQuantityOrdered());
                    detailMap.put("unitCode", detail.getItem().getUnit().getCode());
                    detailMap.put("unitPrice", detail.getUnitPrice());
                    detailMap.put("itemAmount", detail.getAmount());
                    detailMap.put("deliveredQuantity", detail.getPoDetail() != null ? detail.getPoDetail().getDeliveredQuantity() : 0);
                    detailMap.put("quantityReceived", detail.getQuantityReceived());
                    detailMap.put("adjustment", detail.getAdjustment());
                    detailMap.put("netAmount", detail.getNetAmount());
                    detailMap.put("discount", detail.getDiscount());
                    detailMap.put("vat", detail.getVat());
                    detailMap.put("accountId", itemStockDetail != null && itemStockDetail.getDebitAccount() != null ? itemStockDetail.getDebitAccount().getId() : "");
                    detailMap.put("code", itemStockDetail != null && itemStockDetail.getDebitAccount() != null ? itemStockDetail.getDebitAccount().getCode() : "");
                    detailMap.put("credit", 0);
                    detailMap.put("creditStr", "0.00");
                    detailMap.put("debit", detail.getNetAmount());
                    detailMap.put("debitStr", detail.getNetAmount()+"");
                    detailMap.put("description", itemStockDetail != null && itemStockDetail.getDebitAccount() != null ? itemStockDetail.getDebitAccount().getTitle() : "");
                    detailMap.put("distribution", new ArrayList<>());
                    detailMap.put("hasSL", false);
                    detailMap.put("searchText", (itemStockDetail != null && itemStockDetail.getDebitAccount() != null ? itemStockDetail.getDebitAccount().getCode() : "") + " " + (itemStockDetail != null && itemStockDetail.getDebitAccount() != null ? itemStockDetail.getDebitAccount().getTitle() : ""));
                    detailMap.put("deliveredQuantity", detail.getDeliveredQuantity());

                    if(detail.getPurchaseRequestDetail() != null) {
                        Map rvDetail = new HashMap();
                        rvDetail.put("id", detail.getPurchaseRequestDetail().getId());
                        detailMap.put("rvDetailId", detail.getPurchaseRequestDetail().getId());
                        detailMap.put("rvDetail", rvDetail);
                    }

                    if (detail.getItemTransactionDetail() != null) {
                        detailMap.put("itemTransactionDetailId", detail.getItemTransactionDetail().getId());
                        detailMap.put("hasItem", detail.getItemTransactionDetail().getDeliveredQuantity().compareTo(BigDecimal.ZERO) == 1);

                        Map ifrMap = new HashMap();
                        ifrMap.put("id", detail.getItemTransactionDetail().getItem().getId());
                        ifrMap.put("description", detail.getItemTransactionDetail().getItem().getDescription());
                        ifrMap.put("quantity", detail.getItemTransactionDetail().getQuantity());
                        ifrMap.put("deliveredQuantity", detail.getItemTransactionDetail().getDeliveredQuantity());

                        detailMap.put("ifrItem", ifrMap);
                    }

                    rrDetailsList.add(detailMap);
                }

            }

        }

        return rr;

    }

    @Override
    public HashMap reportParameters(Integer id, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        ReceivingReport receivingReport = receivingReportRepo.findById(id).orElse(null);

        if (receivingReport != null) {

            List<ReceivingReportDetail> rrDetails = receivingReportDetailRepo.findByReceivingReportId(receivingReport.getId());

            if(!rrDetails.isEmpty() ) {
                ReceivingReportDetail rrDetail = rrDetails.get(0);
                params.put("DELIVERY_NUMBER", rrDetail.getDeliveryNumber());
                params.put("PO_NO", rrDetail.getPoDetail() != null ? rrDetail.getPoDetail().getPurchaseOrder().getCode() : "");
                params.put("RV_NO", rrDetail.getPurchaseRequestDetail() != null ? rrDetail.getPurchaseRequestDetail().getPurchaseRequest().getCode() : "");
                params.put("WO_NO", rrDetail.getPurchaseRequestDetail() != null ? (rrDetail.getPurchaseRequestDetail().getPurchaseRequest().getWorkOrder() != null ? rrDetail.getPurchaseRequestDetail().getPurchaseRequest().getWorkOrder().getCode() : "") : "");

                params.put("QTY_2", "Received");
                if (rrDetail.getPurchaseRequestDetail() != null) {
                    params.put("QTY_1", "Requested");
                }
                if (rrDetail.getPoDetail() != null) {
                    params.put("QTY_1", "Ordered");
                }
            }

            Supplier supplier = supplierRepo.findById(receivingReport.getSupplier().getId()).orElse(null);

            params.put("VOUCHER_NO", receivingReport.getCode());
            params.put("V_DATE", receivingReport.getDeliveryDate());
            params.put("SUPPLIER", supplier.getName());
            params.put("SUPPLIER_ADDRESS", supplier.getAddress());
            params.put("AMOUNT", receivingReport.getTotalAmount());
            params.put("INV_LOCATION", receivingReport.getInventoryLocation().getDescription());
            params.put("INVOICE_DESC", receivingReport.getInvoiceNumber());
            params.put("TITLE", "RECEIVING REPORT");
            if (receivingReport.getIsRepairedItems()) {
                params.put("TITLE", "RECEIVING REPORT - WHSE");
                params.put("QTY_1", "Delivered");
                params.put("QTY_2", "Accepted");
            }
            params.put("PURPOSE", receivingReport.getRemarks() != null ? receivingReport.getRemarks() : "");

            params = signatureFacade.getDocumentSignature(params, DocumentType.RR, receivingReport);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer id) {
        List<Map> rrDetails = this.getRRDetails(id);
        return new JRBeanCollectionDataSource(rrDetails);
    }

    @Transactional
    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        ReceivingReport receivingReport =  receivingReportRepo.findById(postData.getDocumentId()).orElse(null);

        if (receivingReport != null &&
                receivingReport.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId() &&
                receivingReport.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId()
                ) {
            List<ReceivingReportDetail> reportDetails = receivingReportDetailRepo.findByReceivingReportId(receivingReport.getId());
            receivingReport.setRrDetails(reportDetails);

            // for logging
            Map oldRrMap = this.forLogMapMain(receivingReport);

            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(receivingReport, receivingReport.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            receivingReport.setDocumentStatus(afterActionDocumentStatus);
            receivingReport.setUpdatedAt(new Date());
            receivingReport = receivingReportRepo.save(receivingReport);

            if (receivingReport != null) {

                // for logging
                Map newRrMap = this.forLogMapMain(receivingReport);
                receivingReport.setRrDetails(reportDetails);
                newRrMap.put("remarks", postData.getRemarks());

                documentProcessingFacade.processAction(receivingReport.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(receivingReport.getTransaction(), authenticationFacade.getLoggedIn(), oldRrMap, newRrMap);

                // insert to itemstock & stocktrans tables
                if(postData.getWorkflowActionsDto().getActionId() == com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_APPROVAL.getId()) {

                    if (!Checker.collectionIsEmpty(reportDetails)) {

                        InventoryLocation inventoryLocation = receivingReport.getInventoryLocation();
                        StockTransaction stockTransaction = new StockTransaction();
                        stockTransaction.setTransaction(receivingReport.getTransaction());
                        stockTransaction.setCreatedBy(processedBy);
                        stockTransaction = stockTransactionRepo.save(stockTransaction);

                        for (ReceivingReportDetail rrd : reportDetails) {

                            ItemStock itemStock = itemStockRepo.findByItemIdAndInventoryLocationId(rrd.getItem().getId(), inventoryLocation.getId());

                            if (itemStock != null) {
                                BigDecimal totalQty = itemStock.getTotalQuantity().add(rrd.getQuantityReceived());
                                BigDecimal totalItemCost = itemStock.getTotalItemCost().add(rrd.getAmount()).add(rrd.getAdjustment());
                                itemStock.setTotalQuantity(totalQty);
                                itemStock.setTotalItemCost(totalItemCost);
                            } else {
                                itemStock = new ItemStock();
                                itemStock.setItem(rrd.getItem());
                                itemStock.setInventoryLocation(receivingReport.getInventoryLocation());
                                itemStock.setTotalQuantity(rrd.getQuantityReceived());
                                itemStock.setTotalItemCost(rrd.getAmount().add(rrd.getAdjustment()));
                            }

                            ItemStock newItemStock = itemStockRepo.save(itemStock);

                            if (newItemStock != null) {

                                ItemStockDetail itemStockDetail = new ItemStockDetail();
                                itemStockDetail.setItemStock(newItemStock);
                                itemStockDetail.setQuantity(rrd.getQuantityReceived());
                                itemStockDetail.setUnitCost(rrd.getUnitPrice().add(rrd.getAdjustment().divide(rrd.getQuantityReceived(),2, BigDecimal.ROUND_HALF_UP)));
                                itemStockDetail.setItemCost(itemStockDetail.getUnitCost().multiply(itemStockDetail.getQuantity()));
                                itemStockDetail.setReceivingReportDetail(rrd);

                                if(this.createdByWarehouseStaff(rrd) && (rrd.getItem().getInventoryCategory().getId().equals(InventoryCategory.LINE_MATERIALS.getId()) || rrd.getItem().getInventoryCategory().getId().equals(InventoryCategory.SPECIAL_EQUIPMENT.getId()))){
                                    itemStockDetail.setDepartment(null);
                                } else {
                                    itemStockDetail.setDepartment(receivingReport.getDepartment());
                                }

                                ItemStockDetail newItemStockDetail1 = itemStockDetailRepo.save(itemStockDetail);

                                if(newItemStockDetail1 != null){

                                    StockTransactionDetail stockTransactionDetail = new StockTransactionDetail();
                                    stockTransactionDetail.setQuantity(rrd.getQuantityReceived());
                                    stockTransactionDetail.setUnitCost(itemStockDetail.getUnitCost());
                                    stockTransactionDetail.setTotalCost(itemStockDetail.getUnitCost().multiply(itemStockDetail.getQuantity()).setScale(2, BigDecimal.ROUND_HALF_UP));
                                    stockTransactionDetail.setVat(BigDecimal.ZERO);
                                    stockTransactionDetail.setStockTransaction(stockTransaction);
                                    stockTransactionDetail.setItemStock(newItemStock);
                                    stockTransactionDetail.setItemStockDetail(newItemStockDetail1);
                                    stockTransactionDetail.setInventoryLocation(inventoryLocation);
                                    stockTransactionDetail.setItemStockBalance(itemStockDetail.getQuantity());
                                    stockTransactionDetail.setItemStockAmountBalance(itemStockDetail.getQuantity().multiply(itemStockDetail.getUnitCost()).setScale(2, BigDecimal.ROUND_HALF_UP));
                                    stockTransactionDetail.setType(1); // 1 = in, 2 = out
                                    stockTransactionDetailRepo.save(stockTransactionDetail);

                                }

                            }

                            // update PO detail deliveredQuantity
                            if (rrd.getPoDetail() != null) {
                                PoDetail poDetail = poDetailRepo.findById(rrd.getPoDetail().getId()).orElse(null);
                                if (poDetail != null) {
                                    poDetail.setDeliveredQuantity(rrd.getQuantityReceived().add(poDetail.getDeliveredQuantity()));
                                    // save
                                    poDetailRepo.save(poDetail);
                                }
                            }
                            if (rrd.getItemTransactionDetail() != null) {
                                ItemTransactionDetail itemTransactionDetail = itemTransactionDetailRepo.findById(rrd.getItemTransactionDetail().getId()).orElse(null);
                                if (itemTransactionDetail != null) {
                                    itemTransactionDetail.setNewItem(rrd.getItem());
                                    itemTransactionDetail.setDeliveredQuantity(itemTransactionDetail.getDeliveredQuantity().add(rrd.getDeliveredQuantity()));
                                    itemTransactionDetailRepo.save(itemTransactionDetail);
                                }
                            }
                            if (rrd.getPurchaseRequestDetail() != null) {
                                PurchaseRequestDetail purchaseRequestDetail = rrd.getPurchaseRequestDetail();
                                purchaseRequestDetail.setRrQuantity(rrd.getQuantityReceived().add(purchaseRequestDetail.getRrQuantity()));
                                PurchaseRequestDetailRepo.save(purchaseRequestDetail);
                            }

                        }

                    }

                }

                response.setSuccessMessage("RR successfully processed");
                response.setSuccess(true);

            }

        }

        return response;

    }

    @Override
    public List<DocumentStatus> getDocumentsStatuses() {
        ReceivingReport rr = receivingReportRepo.findFirstByOrderByIdAsc();
        if (rr != null) {
            return documentDtoer.getDocumentStatuses(rr.getWorkflow().getId());
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map> findAllApprovedForApv() {
        List<Map> data = new ArrayList<>();
        List<Object[]> objects = receivingReportRepo.findAllForApv(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), DocumentType.RR.getId());

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
    public Page<ApvPurchasingDocumentDto> findAllApprovedForApvPaged(String query, Pageable pageable) {
        Page<ReceivingReport> receivingReports;
        if(query != null){
            receivingReports = receivingReportRepo.findAllByQueryForApv("%"+query+"%", pageable);
        } else {
            receivingReports = receivingReportRepo.findAllForApv(pageable);
        }

        return receivingReports.map(entity -> {
                ApvPurchasingDocumentDto dto = new ApvPurchasingDocumentDto();
                String poNos = "";
                ArrayList<Integer> pos = new ArrayList<>();

                List<ReceivingReportDetail> details = receivingReportDetailRepo.findByReceivingReportId(entity.getId());
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

                ReceivingReportDetail firstPoDetail = details.stream()
                        .filter(d -> d.getPoDetail() != null && d.getPoDetail().getPurchaseOrder() != null)
                        .findFirst().orElse(null);

                dto.setVoucherDate(entity.getDeliveryDate());
                dto.setLocalCode(entity.getCode());
                dto.setParticulars( entity.getCode()+", "+poNos+", "+ entity.getInvoiceNumber());
                dto.setNetAmount(entity.getTotalAmount());
                dto.setId(entity.getId());
                dto.setPreparedBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getFullName() : "");
                dto.setSlentityAccountNo(entity.getSupplier().getAccountNumber());
                dto.setSlentityName(entity.getSupplier().getName());
                dto.setInvoiceDate(entity.getInvoiceDate());
                dto.setPaymentTerm(firstPoDetail != null ? firstPoDetail.getPoDetail().getPurchaseOrder().getPaymentTerm() : null);
                dto.setTransactionId(entity.getTransaction().getId());
                dto.setQuantity(entity.getTotalQuantity());

                return dto;
        });
    }

    @Override
    public Page<ApvPurchasingDocumentDto> findAllApprovedForApvWithAccountSettingPaged(String query, Integer supplierAcctNo, Pageable pageable) {

        Page<ReceivingReport> receivingReports;

        Supplier supplier = supplierRepo.findOneByAccountNumber(supplierAcctNo);

        if(query != null){
            receivingReports = receivingReportRepo.findAllByQueryForApvWithAccountSetting("%"+query+"%", com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), DocumentType.RR.getId(), supplier.getId(), pageable);
        } else {
            receivingReports = receivingReportRepo.findAllForApvWithAccountSetting(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), DocumentType.RR.getId(), supplier.getId(), pageable);
        }

        return receivingReports.map(entity -> {
                ApvPurchasingDocumentDto dto = new ApvPurchasingDocumentDto();
                String poNos = "";
                ArrayList<Integer> pos = new ArrayList<>();

                List<ReceivingReportDetail> details = receivingReportDetailRepo.findByReceivingReportId(entity.getId());
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

                dto.setVoucherDate(entity.getDeliveryDate());
                dto.setLocalCode(entity.getCode());
                dto.setParticulars( entity.getCode()+", "+poNos+", "+ entity.getInvoiceNumber());
                dto.setNetAmount(entity.getTotalAmount());
                dto.setId(entity.getId());
                dto.setPreparedBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getFullName() : "");
                dto.setSlentityAccountNo(entity.getSupplier().getAccountNumber());
                dto.setSlentityName(entity.getSupplier().getName());
                dto.setInvoiceDate(entity.getInvoiceDate());
                dto.setPaymentTerm(details.get(0).getPoDetail().getPurchaseOrder().getPaymentTerm());

                return dto;
        });

    }

    @Override
    public List<InventoryLocation> getAllInventoryLocations() {
        return inventoryLocationRepo.findAll();
    }

    @Override
    public List<ReceivingReportDetail> getAllReceivingReportDetail(Integer id) {
        return receivingReportDetailRepo.findByReceivingReportId(id);
    }

    @Override
    public Page<CvVoucherDto> findAllApprovedForCvPaged(String query, Pageable pageable) {

        org.springframework.data.domain.Page<ReceivingReport> receivingReports;

        if(query != null){
            receivingReports = receivingReportRepo.findAllByQueryAndDocumentStatusForCv("%"+query+"%", com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        } else {
            receivingReports = receivingReportRepo.findAllByDocumentStatusForCv(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        }

        return receivingReports.map(entity -> {
                CvVoucherDto dto = new CvVoucherDto();

                dto.setVoucherDate(entity.getDeliveryDate());
                dto.setLocalCode(entity.getCode());
                dto.setId(entity.getId());
                dto.setPreparedBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getFullName() : "");
                dto.setAmount(entity.getTotalAmount());
                dto.setParticulars(entity.getCode() + " - " + entity.getRemarks());
                dto.setTransId(entity.getTransaction().getId());
                dto.setSlentityAccountNo(entity.getSupplier().getAccountNumber());
                dto.setSlentityName(entity.getSupplier().getName());
                dto.setExtensionUrl("receiving-report");
                dto.setBudgetLineItemDetail(null);

                List<ReceivingReportDetail> details = receivingReportDetailRepo.findByReceivingReportId(entity.getId());

                if(!details.isEmpty()){
                    if(details.get(0).getPurchaseRequestDetail() != null){
                        if(details.get(0).getPurchaseRequestDetail().getPurchaseRequest().getBudgetLineItemDetail() != null){
                            dto.setBudgetLineItemDetail(details.get(0).getPurchaseRequestDetail().getPurchaseRequest().getBudgetLineItemDetail());
                        }
                    }
                }

                return dto;
        });

    }

    @Override
    public Page<ReceivingReportDocumentDto> findAllForJv(String query, Pageable pageable) {
        Page<ReceivingReport> receivingReports;

        if(query != null){
            receivingReports = receivingReportRepo.findAllByQueryForJV("%"+query+"%", com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        } else {
            receivingReports = receivingReportRepo.findAllForJV(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        }

        return receivingReports.map(entity -> {

                ReceivingReportDocumentDto dto = new ReceivingReportDocumentDto();

                dto.setVoucherDate(entity.getDeliveryDate());
                dto.setLocalCode(entity.getCode());
                dto.setParticulars(entity.getRemarks());
                dto.setId(entity.getId());
                dto.setPreparedBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getFullName() : "");
                dto.setTransactionId(entity.getTransaction().getId());
                dto.setExtensionUrl("receiving-report");
                dto.setNetAmount(entity.getTotalAmount());

                return dto;
        });

    }

    @Override
    public Map findByApvId(Integer apvId) {
        Map m = null;

        List<Object[]> rows = receivingReportRepo.findByApvId(apvId, DocumentType.RR.getId());

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

    private Map forLogMapMain(ReceivingReport rr) {
        return documentLoggerFacade.makeLog(rr);
    }

    private List<Map> makeRRListMap(List<ReceivingReport> cs ) {

        List<Map> mapList = new ArrayList<>();

        if (!Checker.collectionIsEmpty(cs)) {
            for(ReceivingReport c:cs) {
                mapList.add(composeRRMap(c));
            }
        }
        return mapList;
    }

    private Map composeRRMap(ReceivingReport r) {
        Map map = new HashMap();

        map.put("id", r.getId());
        map.put("localCode", r.getCode());
        map.put("deliveryDate", r.getDeliveryDate());
        map.put("supplier", r.getSupplier().getName());
        map.put("totalAmount", r.getTotalAmount());
        map.put("totalQuantity", r.getTotalQuantity());
        map.put("invoiceDescription", r.getInvoiceNumber());
        map.put("remarks", r.getRemarks());
        map.put("documentStatus", r.getDocumentStatus().getStatus());
        map.put("receivedBy", r.getCreatedBy().getFullName());
        map.put("createdAt", r.getCreatedAt());

        return map;
    }

    private String getCode(ReceivingReport receivingReport, Integer voucherYear) {

        String rrCode = "";

        try {

            Object latestRRCode = receivingReportRepo.findLatestRRCodeByYear(voucherYear);
            rrCode = generatorFacade.voucherCodeNoOffice("RR", (latestRRCode == null ? "" : String.valueOf(latestRRCode)), receivingReport.getDeliveryDate(), GlobalConstant.COUNTER_PAD_4);

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return rrCode;

    }

    private Boolean createdByWarehouseStaff(ReceivingReportDetail receivingReportDetail){

        Boolean isCreatedByWarehouse = Boolean.FALSE;

        try {

            Map codeMap = settingFacade.getByCode(SettingCode.WAREHOUSE_POSITIONS.toString());

            if(codeMap.size() > 0){
                List<Integer> createdByWarehousePositionIds = (List<Integer>) codeMap.get("createdByWarehousePositionIds");

                User createdBy = receivingReportDetail.getPurchaseRequestDetail().getPurchaseRequest().getCreatedBy();
                Employee employee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());

                for (Integer positionId : createdByWarehousePositionIds){

                    if (employee.getPosition().getId().equals(positionId)){
                        isCreatedByWarehouse = true;
                        break;
                    }

                }
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return isCreatedByWarehouse;

    }
}
