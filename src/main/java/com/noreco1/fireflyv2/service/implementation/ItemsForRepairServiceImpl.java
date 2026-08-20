package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.InventoryLocation;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.WorkflowAction;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.ItemTransactionDetailDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.service.ItemsForRepairService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.ItemsForRepairValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by tonyc on 9/22/2020.
 */
@Service(value = "itemsForRepairServiceImpl")
public class ItemsForRepairServiceImpl implements ItemsForRepairService, PrintableVoucher {

    private ItemsForRepair model;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    ItemsForRepairRepo itemsForRepairRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    ItemTransactionDetailRepo itemTransactionDetailRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    DocumentDtoer documentDtoer;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    ItemStockRepo itemStockRepo;

    @Autowired
    StockTransactionDetailRepo stockTransactionDetailRepo;

    @Autowired
    StockTransactionRepo stockTransactionRepo;

    @Autowired
    SignatureFacade signatureFacade;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    SupplierRepo supplierRepo;

    @Autowired
    SettingFacade settingFacade;

    @Autowired
    ItemTransactionDetailSerialNoRepo itemTransactionDetailSerialNoRepo;

    @Autowired
    SpecialEquipmentRepo specialEquipmentRepo;

    @Autowired
    StockTransactionDetailSerialNoRepo stockTransactionDetailSerialNoRepo;

    @Autowired
    ItemSerialNoFacade itemSerialNoFacade;
    private Map reportMeta = new HashMap();

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public ItemsForRepair findById(Integer id) {

        ItemsForRepair ret = itemsForRepairRepo.findById(id).orElse(null);

        try {

            ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();

            ArrayList<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(ret.getTransaction().getId());

            if (Checker.collectionIsNotEmpty(details)){

                for (ItemTransactionDetail d : details) {

                    ItemTransactionDetailDto dto = new ItemTransactionDetailDto();

                    dto.setItemId(d.getItem().getId());
                    dto.setItemCode(d.getItem().getCode());
                    dto.setUnitId(d.getItem().getUnit().getId());
                    dto.setUnitCode(d.getItem().getUnit().getCode());
                    dto.setItemDescription(d.getItem().getDescription());
                    dto.setQuantityReleased(d.getQuantity());
                    dto.setUnitCost(d.getUnitCost());
                    dto.setTotalCost(d.getTotalCost());
                    dto.setQuantity(d.getQuantity());
                    dto.setDeductFromStock(d.getDeductFromStock());
                    dto.setItemStockId(d.getItemStock().getId());
                    dto.setInventoryLocationId(d.getInventoryLocation().getId());

                    List<SpecialEquipment> specialEquipments = new ArrayList<>();

                    List<ItemTransactionDetailSerialNo> serialNumbers = itemTransactionDetailSerialNoRepo.findAllByItemTransactionDetailId(d.getId());

                    if(!serialNumbers.isEmpty()){

                        for(ItemTransactionDetailSerialNo detail : serialNumbers){
                            SpecialEquipment specialEquipment = new SpecialEquipment();

                            specialEquipment.setSerialNo(detail.getSerialNo());

                            specialEquipments.add(specialEquipment);

                        }

                    }

                    dto.setSerialNumbers(specialEquipments);

                    detailsDto.add(dto);

                }

                ret.setDetails(detailsDto);

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return ret;

    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<ItemsForRepair> findAll() {
        return itemsForRepairRepo.findAll();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<ItemsForRepair> findAll(Pageable pageable) {
        return itemsForRepairRepo.findAll(pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<ItemsForRepair> findByQuery(String query, Pageable pageable) {
        return itemsForRepairRepo.findByCode(query, pageable);
    }

    @Override
    public List<DocumentStatus> getDocumentsStatuses() {
        return documentDtoer.getDocumentStatuses(com.noreco1.fireflyv2.model.enums.Workflow.ITEMS_FOR_REPAIR.getId());
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<ItemsForRepair> getListForSummaryReport(String from, String to, HttpServletRequest request) {
        List<ItemsForRepair> list = new ArrayList<>();
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            String location = request.getParameter("location");
            String status = request.getParameter("status");

            if(!Checker.isStringNullAndEmpty(location) && !Checker.isStringNullAndEmpty(status)) {
                Integer locationInt = Integer.parseInt(location);
                Integer statusInt = Integer.parseInt(status);
                list = itemsForRepairRepo.findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusId(fromDate, toDate, locationInt, statusInt);
            } else if(!Checker.isStringNullAndEmpty(location)) {
                Integer locationInt = Integer.parseInt(location);
                list = itemsForRepairRepo.findByVoucherDateBetweenAndInventoryLocationId(fromDate, toDate, locationInt);
            } else if(!Checker.isStringNullAndEmpty(status)) {
                Integer statusInt = Integer.parseInt(status);
                list = itemsForRepairRepo.findByVoucherDateBetweenAndDocumentStatusId(fromDate, toDate, statusInt);
            } else {
                list = itemsForRepairRepo.findByVoucherDateBetween(fromDate, toDate);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<ItemTransactionDetailDto> getItemDetails(Integer transId) {
        List<ItemTransactionDetailDto> data = new ArrayList<>();
        ArrayList<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(transId);

        if(!details.isEmpty()) {
            for (ItemTransactionDetail detail : details) {

                ItemTransactionDetailDto detailDto = new ItemTransactionDetailDto();

                detailDto.setId(detail.getId());
                detailDto.setItemId(detail.getItem().getId());
                detailDto.setUnitCode(detail.getItem().getUnit().getCode());
                detailDto.setItemCode(detail.getItem().getCode());
                detailDto.setItemDescription(detail.getItem().getDescription());
                detailDto.setQuantity(detail.getQuantity());
                detailDto.setDeliveredQuantity(detail.getDeliveredQuantity());
                if (detail.getNewItem() != null) {
                    detailDto.setNewItemId(detail.getNewItem().getId());
                    detailDto.setNewItemDescription(detail.getNewItem().getDescription());
                }

                data.add(detailDto);
            }
        }

        return data;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Object[]> getForRR(String query, Integer invLocId, Pageable pageable) {
        if (Checker.isStringNullAndEmpty(query)) {
            return itemsForRepairRepo.findForRR(invLocId, pageable);
        } else {
            return itemsForRepairRepo.findForRR("%"+query+"%", invLocId, pageable);
        }
    }

    @Override
    public Map getReportMeta() {
        return this.reportMeta;
    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        ItemsForRepair itemsForRepair = itemsForRepairRepo.findById(postData.getDocumentId()).orElse(null);

        if (itemsForRepair != null &&
                itemsForRepair.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId() &&
                itemsForRepair.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId()
                ) {
            List<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(itemsForRepair.getTransaction().getId());
            ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
            for (ItemTransactionDetail d : details) {
                detailsDto.add(d.toIFRDto());
            }

            // for logging
            Map oldMstMap = this.forLogMapMain(itemsForRepair);

            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(itemsForRepair, itemsForRepair.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            itemsForRepair.setDocumentStatus(afterActionDocumentStatus);
            itemsForRepair.setUpdatedAt(new Date());
            itemsForRepair = itemsForRepairRepo.save(itemsForRepair);

            if (itemsForRepair != null) {

                // for logging
                itemsForRepair.setDetails(detailsDto);
                Map newMstMap = this.forLogMapMain(itemsForRepair);
                newMstMap.put("remarks", postData.getRemarks());

                documentProcessingFacade.processAction(itemsForRepair.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(itemsForRepair.getTransaction(), authenticationFacade.getLoggedIn(), oldMstMap, newMstMap);

                // insert to itemstock & stocktrans tables if items receive
                if (postData.getWorkflowActionsDto().getActionId() == WorkflowAction.SEND_FOR_REPAIR.getId()) {
                    if (!Checker.collectionIsEmpty(details)) {
                        StockTransaction stockTransaction = new StockTransaction();
                        stockTransaction.setTransaction(itemsForRepair.getTransaction());
                        stockTransaction.setCreatedBy(processedBy);
                        stockTransaction = stockTransactionRepo.save(stockTransaction);
                        for (ItemTransactionDetailDto detailDto : itemsForRepair.getDetails()) {
                            if (detailDto.getDeductFromStock()) {
                                InventoryLocation inventoryLocation = new InventoryLocation();
                                inventoryLocation.setId(detailDto.getInventoryLocationId());
                                ItemStock itemStock = itemStockRepo.findByItemIdAndInventoryLocationId(detailDto.getItemId(), inventoryLocation.getId());
                                BigDecimal vat = BigDecimal.ZERO;
                                BigDecimal unitPrice = detailDto.getUnitCost();
                                if (itemStock != null) {
                                    BigDecimal totalQty = itemStock.getQuantity().subtract(detailDto.getQuantity());
                                    itemStock.setQuantity(totalQty);
                                }
                                ItemStock newItemStock = itemStockRepo.save(itemStock);
                                if (newItemStock != null) {
                                    StockTransactionDetail stockTransactionDetail = new StockTransactionDetail();
                                    stockTransactionDetail.setQuantity(detailDto.getQuantity());
                                    stockTransactionDetail.setUnitCost(itemStock.getUnitCost());
                                    stockTransactionDetail.setTotalCost(stockTransactionDetail.getUnitCost().multiply(stockTransactionDetail.getQuantity()));
                                    stockTransactionDetail.setVat(vat);
                                    stockTransactionDetail.setStockTransaction(stockTransaction);
                                    stockTransactionDetail.setItemStock(newItemStock);
                                    stockTransactionDetail.setInventoryLocation(inventoryLocation);
                                    stockTransactionDetail.setItemStockBalance(newItemStock.getQuantity());
                                    stockTransactionDetail.setItemStockAmountBalance(newItemStock.getQuantity().multiply(newItemStock.getUnitCost()));
                                    stockTransactionDetail.setType(2); // 1 = in, 2 = out
                                    StockTransactionDetail savedStockTransactionDetail = stockTransactionDetailRepo.save(stockTransactionDetail);

                                    //Transfer serial numbers from ItemTransactionDetailSerialNo table to StockTransactionDetailSerialNo table
                                    if (savedStockTransactionDetail != null){

                                        ItemTransactionDetail itemTransactionDetail = itemTransactionDetailRepo.findTopByTransactionIdAndItemIdOrderByIdDesc(itemsForRepair.getTransaction().getId(), detailDto.getItemId());

                                        if (itemTransactionDetail != null){

                                            List<ItemTransactionDetailSerialNo> itemTransactionDetailSerialNos = itemTransactionDetailSerialNoRepo.findAllByItemTransactionDetailId(itemTransactionDetail.getId());

                                            if (Checker.collectionIsNotEmpty(itemTransactionDetailSerialNos)){

                                                for (ItemTransactionDetailSerialNo itemTransactionDetailSerialNo : itemTransactionDetailSerialNos){

                                                    StockTransactionDetailSerialNo stockTransactionDetailSerialNo = new StockTransactionDetailSerialNo();

                                                    SpecialEquipment existingSpecialEquipment = specialEquipmentRepo.findBySerialNo(itemTransactionDetailSerialNo.getSerialNo());

                                                    if(existingSpecialEquipment != null){
                                                        stockTransactionDetailSerialNo.setSpecialEquipment(existingSpecialEquipment);
                                                    }

                                                    stockTransactionDetailSerialNo.setSerialNo(itemTransactionDetailSerialNo.getSerialNo());
                                                    stockTransactionDetailSerialNo.setStockTransactionDetail(stockTransactionDetail);
                                                    stockTransactionDetailSerialNoRepo.save(stockTransactionDetailSerialNo);

                                                }

                                            }

                                        }

                                    }
                                }
                            }
                        }
                    }
                }

                response.setSuccessMessage("Items For Repair successfully processed");
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
    public PostResponse processUpdate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource) {
        return this.processCreate(v, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource) {

        ItemsForRepair itemsForRepair = (ItemsForRepair) v;

        PostResponse response = new PostResponse();
        response.setSuccess(false);

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        ItemsForRepairValidator validator = new ItemsForRepairValidator();
        validator.setService(this);
        validator.validate(itemsForRepair, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            Employee loggedInEmployee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());

            ItemsForRepair existingIFR = null;
            Supplier supplier = null;
            if (itemsForRepair.getSupplier() == null) {
                Map codeMap = settingFacade.getByCode("COOP_SUPPLIER");
                if (codeMap != null) {
                    supplier = supplierRepo.findOneByAccountNumber(Integer.parseInt(codeMap.get("accountNo").toString()));
                }
                if (supplier == null) {
                    response.setFailureMessage("COOP as a supplier not found!");
                    response.setSuccess(false);
                    return response;
                }
            } else {
               supplier = supplierRepo.findOneByAccountNumber(itemsForRepair.getSupplier().getAccountNumber());
            }
            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(itemsForRepair.getVoucherDate()));

            Boolean insertMode = itemsForRepair.getId() == null;
            if (insertMode) { // insert mode
//                String offAcro = itemsForRepair.getOffice().getAcronym();
                Object latestCode = itemsForRepairRepo.findLatestCodeByYear(voucherYear);
                itemsForRepair.setCode(generatorFacade.voucherCodeWithMonth("IFR", (latestCode == null ? "" : String.valueOf(latestCode)), itemsForRepair.getVoucherDate()));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                itemsForRepair.setDocumentStatus(documentStatus);

                Workflow wf = new Workflow();
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.ITEMS_FOR_REPAIR.getId());

                itemsForRepair.setSupplier(supplier);
                itemsForRepair.setYear(voucherYear);
                itemsForRepair.setTransaction(generatorFacade.transaction());
                itemsForRepair.setWorkflow(wf);
                itemsForRepair.setCreatedBy(createdBy);
                existingIFR = itemsForRepair;
            } else {
                existingIFR = itemsForRepairRepo.findById(itemsForRepair.getId()).orElse(null);
            }
            // use for document logging
            Map oldMap = this.forLogMapMain(existingIFR);

            existingIFR.setSupplier(supplier);
            existingIFR.setVoucherDate(itemsForRepair.getVoucherDate());
            existingIFR.setParticulars(itemsForRepair.getParticulars());
            existingIFR.setYear(voucherYear);
            existingIFR.setOffice(loggedInEmployee.getOffice());

            existingIFR.setUpdatedAt(new Date());
            this.model = itemsForRepairRepo.save(existingIFR);

            if (this.model != null) {
                if (!insertMode) {
                    List<ItemTransactionDetail> itemTransactionDetails = itemTransactionDetailRepo.findByTransactionId(this.model.getTransaction().getId());
                    for(ItemTransactionDetail itemTransactionDetail : itemTransactionDetails){
                        itemTransactionDetailSerialNoRepo.deleteByItemTransactionDetailId(itemTransactionDetail.getId());
                    }
                    itemTransactionDetailRepo.deleteByTransactionId(model.getTransaction().getId());
                }

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldMap = null;
                }

                ArrayList<ItemTransactionDetailDto> details = itemsForRepair.getDetails();
                for (ItemTransactionDetailDto detailDto : details) {

                    ItemStock itemStock = null;
                    if(detailDto.getItemStockId() != null) {
                        itemStock = itemStockRepo.findById(detailDto.getItemStockId()).orElse(null);
                    }

                    ItemTransactionDetail detail = new ItemTransactionDetail();
                    InventoryLocation inventoryLocation = new InventoryLocation();
                    if(itemStock != null){
                        detail.setItem(itemStock.getItem());
                        inventoryLocation.setId(detailDto.getInventoryLocationId());
                        detail.setItemStock(itemStock);
                    }

                    detail.setTransaction(this.model.getTransaction());

                    //TODO: Set values for details
                    detail.setQuantity(detailDto.getQuantity());
                    detail.setUnitCost(itemStock.getUnitCost());
                    detail.setTotalCost(detail.getQuantity().multiply(detail.getUnitCost()));
                    detail.setInventoryLocation(inventoryLocation);
                    detail.setDeductFromStock(detailDto.getDeductFromStock());
                    detail.setDeliveredQuantity(BigDecimal.ZERO);

                    if (!detailDto.getQuantity().equals(BigDecimal.ZERO)) {

                        ItemTransactionDetail newDetail = itemTransactionDetailRepo.save(detail);

                        //Save Serial Numbers
                        if (newDetail != null && Checker.collectionIsNotEmpty(detailDto.getSerialNumbers())){

                            for (SpecialEquipment specialEquipment : detailDto.getSerialNumbers()){

                                ItemTransactionDetailSerialNo itemTransactionDetailSerialNo = new ItemTransactionDetailSerialNo();

                                itemTransactionDetailSerialNo.setItemTransactionDetail(newDetail);
                                itemTransactionDetailSerialNo.setSerialNo(specialEquipment.getSerialNo());

                                itemTransactionDetailSerialNoRepo.save(itemTransactionDetailSerialNo);

                            }

                        }

                    }

                }

                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), createdBy, oldMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());
                response.setSuccessMessage("Items For Repair successfully saved!");
                response.setSuccess(true);
            }
        }

        return response;
    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);
        if (documentLog != null) {
            Integer transId = documentLog.getTransaction().getId();
            ItemsForRepair itemsForRepair = itemsForRepairRepo.findOneByTransactionId(transId);

            if (itemsForRepair != null) {
                List<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(transId);
                ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
                for (ItemTransactionDetail d : details) {
                    detailsDto.add(d.toIFRDto());
                }
                itemsForRepair.setDetails(detailsDto);

                Map map = forLogMapMain(itemsForRepair);
                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.IFR);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Map> findByDateRangeAndStatusId(String from, String to, Integer docStatusId, Integer officeId) {
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            User loggedIn = authenticationFacade.getLoggedIn();

            List<ItemsForRepair> docs = itemsForRepairRepo.findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(loggedIn.getId(), fromDate, toDate, docStatusId);
            return this.makeMapList(docs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Map> findByDateRangePending(String from, String to, Integer officeId) {
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
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_REPAIR.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId()
            };

            User loggedIn = authenticationFacade.getLoggedIn();

            List<ItemsForRepair> docs = itemsForRepairRepo.findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(loggedIn.getId(), fromDate, toDate, Arrays.asList(ids));
            return this.makeMapList(docs);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Map forLogMapMain(ItemsForRepair itemsForRepair) {
        return documentLoggerFacade.makeLog(itemsForRepair);
    }

    private List<Map> makeMapList(List<ItemsForRepair> itemsForRepairs) {

        List<Map> mapList = new ArrayList<>();

        if (!Checker.collectionIsEmpty(itemsForRepairs)) {
            for (ItemsForRepair c : itemsForRepairs) {
                mapList.add(composeMap(c));
            }
        }
        return mapList;
    }

    private Map composeMap(ItemsForRepair r) {
        Map map = new HashMap();

        map.put("id", r.getId());
        map.put("code", r.getCode());
        map.put("voucherDate", r.getVoucherDate());
        map.put("supplier", r.getSupplier() != null ? r.getSupplier().getName() : "In COOP");
        map.put("particulars", r.getParticulars());
        map.put("createdBy", r.getCreatedBy());
        map.put("documentStatus", r.getDocumentStatus());

        return map;
    }

    @Override
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {
        this.reportMeta = new HashMap();
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        ItemsForRepair itemsForRepair = itemsForRepairRepo.findById(vid).orElse(null);

        if (itemsForRepair != null) {

            List<Map> details = this.getDetails(vid);
            BigDecimal totalQty = BigDecimal.ZERO;
            for (Map d : details) {
                totalQty = totalQty.add(new BigDecimal(d.get("quantity")+""));
            }

            params.put("VOUCHER_NO", itemsForRepair.getCode());
            params.put("REMARKS", itemsForRepair.getParticulars());
            params.put("V_DATE", itemsForRepair.getVoucherDate());
            params.put("SUPPLIER", itemsForRepair.getSupplier() != null ? itemsForRepair.getSupplier().getName() : "In COOP");
            params.put("TOTAL_QTY", totalQty);
            params.put("INV_LOCATION", itemsForRepair.getInventoryLocation().getDescription());

            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.IFR, itemsForRepair);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer vid) {
        List<Map> details = this.getDetails(vid);
        this.reportMeta.put("SERIALS", this.itemSerialNoFacade.itemsInPrint(details));
        return new JRBeanCollectionDataSource(details);
    }

    public List<Map> getDetails(int id) {
        List<Map> data = new ArrayList<>();

        ItemsForRepair itemsForRepair = itemsForRepairRepo.findById(id).orElse(null);
        if (itemsForRepair != null) {
            List<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(itemsForRepair.getTransaction().getId());

            if (!details.isEmpty()) {
                int counter = 1;
                for (ItemTransactionDetail detail : details) {
                    Map detailMap = new HashMap();

                    detailMap.put("id", counter++);
                    detailMap.put("itemId", detail.getItem().getId());
                    detailMap.put("code", detail.getItem().getCode());
                    detailMap.put("description", detail.getItem().getDescription());
                    detailMap.put("quantity", detail.getQuantity());
                    detailMap.put("unitCode", detail.getItem().getUnit().getCode());
                    detailMap.put("unitId", detail.getItem().getUnit().getId());
                    detailMap.put("deduct", detail.getDeductFromStock() ? "YES" : "NO");
                    detailMap.put("stockTransactionDetailId", detail.getId());

                    data.add(detailMap);
                }
            }
        }

        return data;
    }
}
