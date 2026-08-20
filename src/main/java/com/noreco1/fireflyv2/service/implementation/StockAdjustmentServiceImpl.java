package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.ItemTransactionDetailDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.controller.response.StockAdjustmentDocumentDto;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.StockAdjustmentService;
import com.noreco1.fireflyv2.validator.StockAdjustmentValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

@Service(value = "stockAdjustmentServiceImpl")
public class StockAdjustmentServiceImpl implements StockAdjustmentService, PrintableVoucher {

    private StockAdjustment model;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    StockAdjustmentRepo stockAdjustmentRepo;

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
    ItemStockDetailRepo itemStockDetailRepo;

    @Override
    public StockAdjustment findById(Integer id) {
        StockAdjustment ret = stockAdjustmentRepo.findById(id).orElse(null);
        ArrayList<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(ret.getTransaction().getId());

        ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
        for (ItemTransactionDetail d : details) {
            detailsDto.add(d.toAdjustmentDto());
        }

        ret.setDetails(detailsDto);

        return ret;
    }

    @Override
    public List<StockAdjustment> findAll() {
        return stockAdjustmentRepo.findAll();
    }

    @Override
    public Page<StockAdjustment> findAll(Pageable pageable) {
        return stockAdjustmentRepo.findAll(pageable);
    }

    @Override
    public Page<StockAdjustment> findByQuery(String query, Pageable pageable) {
        return stockAdjustmentRepo.findByCode(query, pageable);
    }

    @Override
    public List<DocumentStatus> getDocumentsStatuses() {
        return documentDtoer.getDocumentStatuses(com.noreco1.fireflyv2.model.enums.Workflow.STOCK_ADJUSTMENT.getId());
    }

    @Override
    public List<StockAdjustment> getListForSummaryReport(String from, String to, HttpServletRequest request) {
        List<StockAdjustment> list = new ArrayList<>();
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
                list = stockAdjustmentRepo.findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusId(fromDate, toDate, locationInt, statusInt);
            } else if(!Checker.isStringNullAndEmpty(location)) {
                Integer locationInt = Integer.parseInt(location);
                list = stockAdjustmentRepo.findByVoucherDateBetweenAndInventoryLocationId(fromDate, toDate, locationInt);
            } else if(!Checker.isStringNullAndEmpty(status)) {
                Integer statusInt = Integer.parseInt(status);
                list = stockAdjustmentRepo.findByVoucherDateBetweenAndDocumentStatusId(fromDate, toDate, statusInt);
            } else {
                list = stockAdjustmentRepo.findByVoucherDateBetween(fromDate, toDate);
            }

            if(!Checker.collectionIsEmpty(list)){
                for(StockAdjustment l : list){
                    l.setDetails(new ArrayList<>(this.getItems(l.getTransaction().getId())));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public List<ItemTransactionDetailDto> getItems(Integer transId) {
        List<ItemTransactionDetailDto> data = new ArrayList<>();
        ArrayList<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(transId);

        if(!details.isEmpty()) {
            for (ItemTransactionDetail detail : details) {

                ItemTransactionDetailDto detailDto = new ItemTransactionDetailDto();

                detailDto.setItemId(detail.getItem().getId());
                detailDto.setUnitCode(detail.getItem().getUnit().getCode());
                detailDto.setItemCode(detail.getItem().getCode());
                detailDto.setItemDescription(detail.getItem().getDescription());
                detailDto.setQuantity(detail.getAdjustment());
                detailDto.setUnitCost(detail.getUnitCost());
                detailDto.setTotalCost(detail.getTotalCost());

                data.add(detailDto);
            }
        }

        return data;
    }

    @Override
    public Page<StockAdjustmentDocumentDto> findAllApprovedForAccountSettingPaged(String query, Pageable pageable) {

        Page<StockAdjustment> stockAdjustments;

        if(query != null){
            stockAdjustments = stockAdjustmentRepo.findAllByQueryForAccountSetting("%"+query+"%", com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        } else {
            stockAdjustments = stockAdjustmentRepo.findAllForAccountSetting(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        }

        return stockAdjustments.map(entity -> {

                StockAdjustmentDocumentDto dto = new StockAdjustmentDocumentDto();

                dto.setVoucherDate(entity.getVoucherDate());
                dto.setLocalCode(entity.getCode());
                dto.setParticulars(entity.getRemarks());
                dto.setId(entity.getId());
                dto.setPreparedBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getFullName() : null);
                dto.setTransactionId(entity.getTransaction().getId());

                ArrayList<StockTransactionDetail> stockTransactionDetails = stockTransactionDetailRepo.findByStockTransactionTransactionId(entity.getTransaction().getId());

                if(Checker.collectionIsNotEmpty(stockTransactionDetails)){

                    BigDecimal quantity = BigDecimal.ZERO;

                    for (StockTransactionDetail stockTransactionDetail : stockTransactionDetails){

                        quantity = quantity.add(stockTransactionDetail.getQuantity());

                    }

                    dto.setNetAmount(BigDecimal.ZERO);
                    dto.setQuantity(quantity);

                }

                return dto;
        });

    }

    @Override
    public Page<StockAdjustmentDocumentDto> findAllApprovedForJVPaged(String query, Pageable pageable) {

        Page<StockAdjustment> stockAdjustments;

        if(query != null){
            stockAdjustments = stockAdjustmentRepo.findAllByQueryForJV("%"+query+"%", com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        } else {
            stockAdjustments = stockAdjustmentRepo.findAllForJV(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        }

        return stockAdjustments.map(entity -> {

                StockAdjustmentDocumentDto dto = new StockAdjustmentDocumentDto();

                dto.setVoucherDate(entity.getVoucherDate());
                dto.setLocalCode(entity.getCode());
                dto.setParticulars(entity.getRemarks());
                dto.setId(entity.getId());
                dto.setPreparedBy(entity.getCreatedBy().getFullName());
                dto.setTransactionId(entity.getTransaction().getId());
                dto.setExtensionUrl("adjustment");

                ArrayList<StockTransactionDetail> stockTransactionDetails = stockTransactionDetailRepo.findByStockTransactionTransactionId(entity.getTransaction().getId());

                if(Checker.collectionIsNotEmpty(stockTransactionDetails)){

                    BigDecimal quantity = BigDecimal.ZERO;

                    for (StockTransactionDetail stockTransactionDetail : stockTransactionDetails){

                        quantity = quantity.add(stockTransactionDetail.getQuantity());

                    }

                    dto.setNetAmount(BigDecimal.ZERO);
                    dto.setQuantity(quantity);

                }

                return dto;
        });

    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        StockAdjustment stockAdjustment = stockAdjustmentRepo.findById(postData.getDocumentId()).orElse(null);

        if (stockAdjustment != null &&
                stockAdjustment.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId() &&
                stockAdjustment.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId()
                ) {
            List<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(stockAdjustment.getTransaction().getId());
            ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
            for (ItemTransactionDetail d : details) {
                detailsDto.add(d.toAdjustmentDto());
            }
            stockAdjustment.setDetails(detailsDto);

            // for logging
            Map oldRrMap = this.forLogMapMain(stockAdjustment);

            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(stockAdjustment, stockAdjustment.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            stockAdjustment.setDocumentStatus(afterActionDocumentStatus);
            stockAdjustment.setUpdatedAt(new Date());
            stockAdjustment = stockAdjustmentRepo.save(stockAdjustment);

            if (stockAdjustment != null) {

                // for logging
                stockAdjustment.setDetails(detailsDto);
                Map newRrMap = this.forLogMapMain(stockAdjustment);
                newRrMap.put("remarks", postData.getRemarks());

                documentProcessingFacade.processAction(stockAdjustment.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(stockAdjustment.getTransaction(), authenticationFacade.getLoggedIn(), oldRrMap, newRrMap);

                // insert to itemstock & stocktrans tables if approved
                if (postData.getWorkflowActionsDto().getActionId() == 5) {
                    if (!Checker.collectionIsEmpty(details)) {

                        InventoryLocation inventoryLocation = model.getInventoryLocation();

                        StockTransaction stockTransaction = new StockTransaction();
                        stockTransaction.setTransaction(stockAdjustment.getTransaction());
                        stockTransaction.setCreatedBy(processedBy);

                        stockTransaction = stockTransactionRepo.save(stockTransaction);

                        for (ItemTransactionDetailDto detailDto : stockAdjustment.getDetails()) {

                            ItemStock itemStock = itemStockRepo.findFirstByItemIdAndInventoryLocationIdOrderByIdAsc(detailDto.getItemId(), inventoryLocation.getId());

                            BigDecimal receivedCost = detailDto.getAdjustment().multiply(itemStock.getTotalItemCost().divide(itemStock.getTotalQuantity(), BigDecimal.ROUND_HALF_UP));
                            BigDecimal totalQty = itemStock.getTotalQuantity().add(detailDto.getAdjustment());
                            BigDecimal totalCost = itemStock.getTotalItemCost().add(receivedCost);

                            itemStock.setTotalQuantity(totalQty);
                            itemStock.setTotalItemCost(totalCost);

                            ItemStock newItemStock = itemStockRepo.save(itemStock);

                            if(Checker.isValidId(newItemStock.getId())){

                                Integer type = detailDto.getAdjustment().compareTo(BigDecimal.ZERO) == 1 ? 1 : 2;

                                if(type == 1){//Add item stock detail

                                    ItemStockDetail itemStockDetail = new ItemStockDetail();

                                    itemStockDetail.setItemStock(newItemStock);
                                    itemStockDetail.setQuantity(detailDto.getAdjustment());
                                    itemStockDetail.setUnitCost(detailDto.getUnitCost());
                                    itemStockDetail.setItemCost(detailDto.getUnitCost().multiply(detailDto.getAdjustment()));

                                    ItemStockDetail newItemStockDetail = itemStockDetailRepo.save(itemStockDetail);

                                    if (newItemStockDetail != null) {

                                        StockTransactionDetail stockTransactionDetail = new StockTransactionDetail();

                                        stockTransactionDetail.setQuantity(newItemStockDetail.getQuantity());
                                        stockTransactionDetail.setUnitCost(newItemStockDetail.getUnitCost());
                                        stockTransactionDetail.setTotalCost(stockTransactionDetail.getUnitCost().multiply(stockTransactionDetail.getQuantity()));
                                        stockTransactionDetail.setVat(BigDecimal.ZERO);
                                        stockTransactionDetail.setStockTransaction(stockTransaction);
                                        stockTransactionDetail.setItemStock(newItemStock);
                                        stockTransactionDetail.setItemStockDetail(newItemStockDetail);
                                        stockTransactionDetail.setInventoryLocation(inventoryLocation);
                                        stockTransactionDetail.setItemStockBalance(newItemStockDetail.getQuantity());
                                        stockTransactionDetail.setItemStockAmountBalance(newItemStockDetail.getQuantity().multiply(newItemStockDetail.getUnitCost()));
                                        stockTransactionDetail.setType(type); // 1 = in, 2 = out

                                        stockTransactionDetailRepo.save(stockTransactionDetail);

                                    }

                                } else {

                                    BigDecimal totalRelease = BigDecimal.ZERO;
                                    BigDecimal quantityPerItemStock;
                                    BigDecimal unitCostPerItemStock;

                                    List<ItemStockDetail> itemStockDetails = itemStockDetailRepo.findAllByItemStockIdAndQuantityGreaterThanOrderByIdAsc(newItemStock.getId(), BigDecimal.ZERO);

                                    if(Checker.collectionIsNotEmpty(itemStockDetails)){

                                        BigDecimal itemStockDiff = BigDecimal.ZERO;

                                        for (ItemStockDetail itemStockDetail : itemStockDetails){

                                            if(itemStockDiff.compareTo(BigDecimal.ZERO) == 1){
                                                detailDto.setAdjustment(itemStockDiff);
                                            }

                                            BigDecimal quantity = BigDecimal.ZERO;
                                            BigDecimal itemCost = BigDecimal.ZERO;

                                            Boolean isQuantityIsGreaterThanEqualZero = itemStockDetail.getQuantity().compareTo(detailDto.getAdjustment().abs()) == 1 || itemStockDetail.getQuantity().compareTo(detailDto.getAdjustment().abs()) == 0;

                                            if(isQuantityIsGreaterThanEqualZero){
                                                quantity = itemStockDetail.getQuantity().subtract(detailDto.getAdjustment().abs());
                                                itemCost = itemStockDetail.getItemCost().subtract(itemStockDetail.getUnitCost().multiply(detailDto.getAdjustment().abs()));
                                                totalRelease = totalRelease.add(detailDto.getAdjustment().abs());
                                                quantityPerItemStock = detailDto.getAdjustment().abs();
                                                unitCostPerItemStock = itemStockDetail.getUnitCost();
                                            } else {
                                                itemStockDiff = detailDto.getAdjustment().abs().subtract(itemStockDetail.getQuantity());
                                                totalRelease = totalRelease.add(itemStockDetail.getQuantity());
                                                quantityPerItemStock = itemStockDetail.getQuantity();
                                                unitCostPerItemStock = itemStockDetail.getUnitCost();
                                            }

                                            itemStockDetail.setQuantity(quantity);
                                            itemStockDetail.setItemCost(itemCost);

                                            ItemStockDetail newItemStockDetail = itemStockDetailRepo.save(itemStockDetail);

                                            if (newItemStockDetail != null) {

                                                StockTransactionDetail stockTransactionDetail = new StockTransactionDetail();

                                                stockTransactionDetail.setQuantity(quantityPerItemStock);
                                                stockTransactionDetail.setUnitCost(unitCostPerItemStock);
                                                stockTransactionDetail.setTotalCost(stockTransactionDetail.getUnitCost().multiply(stockTransactionDetail.getQuantity()));
                                                stockTransactionDetail.setVat(BigDecimal.ZERO);
                                                stockTransactionDetail.setStockTransaction(stockTransaction);
                                                stockTransactionDetail.setItemStock(newItemStock);
                                                stockTransactionDetail.setItemStockDetail(newItemStockDetail);
                                                stockTransactionDetail.setInventoryLocation(inventoryLocation);
                                                stockTransactionDetail.setItemStockBalance(newItemStockDetail.getQuantity());
                                                stockTransactionDetail.setItemStockAmountBalance(newItemStockDetail.getQuantity().multiply(newItemStockDetail.getUnitCost()));
                                                stockTransactionDetail.setType(type); // 1 = in, 2 = out

                                                stockTransactionDetailRepo.save(stockTransactionDetail);

                                                if(isQuantityIsGreaterThanEqualZero){
                                                    break;
                                                }

                                            }

                                        }

                                    }

                                }

                            }

                        }

                    }

                }

                response.setSuccessMessage("Stock Adjustment successfully processed");
                response.setSuccess(true);

            }

        }

        return response;

    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        return this.processCreate(v, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {

        StockAdjustment stockAdjustment = (StockAdjustment) v;

        PostResponse response = new PostResponse();
        response.setSuccess(false);

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        StockAdjustmentValidator validator = new StockAdjustmentValidator();
        validator.setService(this);
        validator.validate(stockAdjustment, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            StockAdjustment existingSA = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(stockAdjustment.getVoucherDate()));

            User checkedBy = userRepo.findOneByAccountNo(stockAdjustment.getChecker().getAccountNo());
            User approvedBy = userRepo.findOneByAccountNo(stockAdjustment.getApprovingOfficer().getAccountNo());
            Employee loggedInEmployee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());

            Boolean insertMode = stockAdjustment.getId() == null;
            if (insertMode) { // insert mode
//                String offAcro = stockAdjustment.getOffice().getAcronym();
                Object latestCode = stockAdjustmentRepo.findLatestCodeByYear(voucherYear);
                stockAdjustment.setCode(generatorFacade.voucherCodeWithMonth("SA", (latestCode == null ? "" : String.valueOf(latestCode)), stockAdjustment.getVoucherDate()));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                stockAdjustment.setDocumentStatus(documentStatus);

                Workflow wf = new Workflow();
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.STOCK_ADJUSTMENT.getId());

                stockAdjustment.setYear(voucherYear);
                stockAdjustment.setTransaction(generatorFacade.transaction());
                stockAdjustment.setWorkflow(wf);
                stockAdjustment.setCreatedBy(createdBy);
                existingSA = stockAdjustment;
            } else {
                existingSA = stockAdjustmentRepo.findById(stockAdjustment.getId()).orElse(null);
            }
            // use for document logging
            Map oldMap = this.forLogMapMain(existingSA);

            existingSA.setVoucherDate(stockAdjustment.getVoucherDate());
            existingSA.setRemarks(stockAdjustment.getRemarks());
            existingSA.setYear(voucherYear);
            existingSA.setChecker(checkedBy);
            existingSA.setApprovingOfficer(approvedBy);
            existingSA.setOffice(loggedInEmployee.getOffice());

            existingSA.setUpdatedAt(new Date());
            this.model = stockAdjustmentRepo.save(existingSA);

            if (this.model != null) {

                // start: update default signatories
                signatoryFacade.sa(this.model);
                // end: update default signatories

                if (!insertMode) {
                    itemTransactionDetailRepo.deleteByTransactionId(model.getTransaction().getId());
                }

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldMap = null;
                }

                ArrayList<ItemTransactionDetailDto> details = stockAdjustment.getDetails();
                for (ItemTransactionDetailDto detailDto : details) {
                    ItemStock itemStock = itemStockRepo.findById(detailDto.getItemStockId()).orElse(null);
                    ItemTransactionDetail detail = new ItemTransactionDetail();

                    detail.setTransaction(this.model.getTransaction());

                    //TODO: Set values for details
                    detail.setItem(itemStock.getItem());
                    detail.setQuantity(itemStock.getTotalQuantity());
                    detail.setUnitCost(itemStock.getTotalItemCost().divide(itemStock.getTotalQuantity(), BigDecimal.ROUND_HALF_UP));
                    detail.setTotalCost(detailDto.getAdjustment().abs().multiply(detail.getUnitCost()));

                    detail.setItemStock(itemStock);
                    detail.setAdjustment(detailDto.getAdjustment());

                    if (detailDto.getAdjustment().compareTo(BigDecimal.ZERO) != 0) {
                        ItemTransactionDetail newDetail = itemTransactionDetailRepo.save(detail);
                    }
                }

                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());
                response.setSuccessMessage("Stock Adjustment successfully saved!");
                response.setSuccess(true);
            }
        }

        return response;
    }

    @Override
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        StockAdjustment stockAdjustment = stockAdjustmentRepo.findById(vid).orElse(null);

        if (stockAdjustment != null) {

            List<Map> details = this.getDetails(vid);
            BigDecimal totalQty = BigDecimal.ZERO;
            for (Map d : details) {
                totalQty = totalQty.add(new BigDecimal(d.get("quantity")+""));
            }

            params.put("VOUCHER_NO", stockAdjustment.getCode());
            params.put("REMARKS", stockAdjustment.getRemarks());
            params.put("V_DATE", stockAdjustment.getVoucherDate());
            params.put("INV_LOCATION", stockAdjustment.getInventoryLocation().getDescription());
            params.put("TOTAL_QTY", totalQty);

            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.SA, stockAdjustment);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer vid) {
        List<Map> details = this.getDetails(vid);
        return new JRBeanCollectionDataSource(details);
    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);
        if (documentLog != null) {
            Integer transId = documentLog.getTransaction().getId();
            StockAdjustment stockAdjustment = stockAdjustmentRepo.findOneByTransactionId(transId);

            if (stockAdjustment != null) {
                List<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(transId);
                ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
                for (ItemTransactionDetail d : details) {
                    detailsDto.add(d.toAdjustmentDto());
                }
                stockAdjustment.setDetails(detailsDto);

                Map map = forLogMapMain(stockAdjustment);
                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.SA);
    }

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

            List<StockAdjustment> docs = stockAdjustmentRepo.findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(loggedIn.getId(), fromDate, toDate, docStatusId);
            return this.makeMapList(docs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

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
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId()
            };

            User loggedIn = authenticationFacade.getLoggedIn();

            List<StockAdjustment> docs = stockAdjustmentRepo.findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(loggedIn.getId(), fromDate, toDate, Arrays.asList(ids));
            return this.makeMapList(docs);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public List<Map> getDetails(int id) {
        List<Map> data = new ArrayList<>();

        StockAdjustment stockAdjustment = stockAdjustmentRepo.findById(id).orElse(null);
        if (stockAdjustment != null) {
            List<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(stockAdjustment.getTransaction().getId());

            if (!details.isEmpty()) {
                int counter = 1;
                for (ItemTransactionDetail detail : details) {
                    Map detailMap = new HashMap();

                    detailMap.put("id", counter++);
                    detailMap.put("itemId", detail.getItemStock().getItem().getId());
                    detailMap.put("code", detail.getItemStock().getItem().getCode());
                    detailMap.put("description", detail.getItemStock().getItem().getDescription());
                    detailMap.put("quantity", detail.getAdjustment());
                    detailMap.put("unitCode", detail.getItemStock().getItem().getUnit().getCode());
                    detailMap.put("unitId", detail.getItemStock().getItem().getUnit().getId());
                    detailMap.put("unitCost", detail.getUnitCost());
                    detailMap.put("totalCost", detail.getTotalCost());

                    data.add(detailMap);
                }
            }
        }

        return data;
    }

    private Map forLogMapMain(StockAdjustment stockAdjustment) {
        return documentLoggerFacade.makeLog(stockAdjustment);
    }

    private List<Map> makeMapList(List<StockAdjustment> stockAdjustments) {

        List<Map> mapList = new ArrayList<>();

        if (!Checker.collectionIsEmpty(stockAdjustments)) {
            for (StockAdjustment c : stockAdjustments) {
                mapList.add(composeMap(c));
            }
        }
        return mapList;
    }

    private Map composeMap(StockAdjustment r) {
        Map map = new HashMap();

        map.put("id", r.getId());
        map.put("code", r.getCode());
        map.put("voucherDate", r.getVoucherDate());
        map.put("remarks", r.getRemarks());
        map.put("createdBy", r.getCreatedBy());
        map.put("documentStatus", r.getDocumentStatus());

        return map;
    }

}
