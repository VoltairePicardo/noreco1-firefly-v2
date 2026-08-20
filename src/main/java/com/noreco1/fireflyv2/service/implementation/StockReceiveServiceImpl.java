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
import com.noreco1.fireflyv2.controller.response.StockReceiveDocumentDto;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.StockReceiveService;
import com.noreco1.fireflyv2.validator.StockReceiveValidator;
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

@Service(value = "stockReceiveServiceImpl")
public class StockReceiveServiceImpl implements StockReceiveService, PrintableVoucher {

    private StockReceive model;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    StockTransferRepo stockTransferRepo;

    @Autowired
    StockWithdrawalDetailRepo stockWithdrawalDetailRepo;

    @Autowired
    StockReceiveRepo stockReceiveRepo;

    @Autowired
    StockReleaseRepo stockReleaseRepo;

    @Autowired
    DepartmentRepo departmentRepo;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    UserRepo userRepo;

    @Autowired
    SlEntityRepo slEntityRepo;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

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
    ItemStockRepo itemStockRepo;

    @Autowired
    StockTransactionRepo stockTransactionRepo;

    @Autowired
    StockTransactionDetailRepo stockTransactionDetailRepo;

    @Autowired
    ItemTransactionDetailRepo itemTransactionDetailRepo;

    @Autowired
    ItemStockDetailRepo itemStockDetailRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public StockReceive findById(Integer id) {
        StockReceive ret = stockReceiveRepo.findById(id).orElse(null);
        ArrayList<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(ret.getTransaction().getId());
        StockTransfer stockTransfer = stockTransferRepo.findOneByTransactionId(ret.getDocumentTransaction().getId());

        ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
        ArrayList<ItemTransactionDetail> transDetails = itemTransactionDetailRepo.findByTransactionId(stockTransfer.getTransaction().getId());
        for (StockTransactionDetail d : details) {
            ItemTransactionDetailDto dto = new ItemTransactionDetailDto();
            dto.setItemId(d.getItemStock().getItem().getId());
            dto.setItemCode(d.getItemStock().getItem().getCode());
            dto.setUnitId(d.getItemStock().getItem().getUnit().getId());
            dto.setUnitCode(d.getItemStock().getItem().getUnit().getCode());
            dto.setItemDescription(d.getItemStock().getItem().getDescription());
            for (ItemTransactionDetail swd : transDetails) {
                if (d.getItemStock().getItem().getId().equals(swd.getItem().getId())) {
                    dto.setQuantityOrdered(swd.getQuantity());
                    break;
                }
            }
            dto.setQuantityReceived(d.getQuantity());
            dto.setUnitCost(d.getUnitCost());
            dto.setTotalCost(d.getTotalCost());
            detailsDto.add(dto);
        }

        ret.setDetails(detailsDto);

        return ret;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public StockReceive findByCode(String code) {

        List<StockReceive> v = stockReceiveRepo.findByCode(code);

        if (!Checker.collectionIsEmpty(v)) {
            return v.get(0);
        } else return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<StockReceive> findAll() {
        return stockReceiveRepo.findAll();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<StockReceive> findAll(Pageable pageable) {
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<StockReceive> findByQuery(String query, Pageable pageable) {
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Map> getDetails(int id) {
        List<Map> data = new ArrayList<>();

        StockReceive receive = stockReceiveRepo.findById(id).orElse(null);
        if (receive != null) {

            List<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(receive.getTransaction().getId());

            if (!details.isEmpty()) {

                int counter = 1;
                for (StockTransactionDetail detail : details) {
                    Map detailMap = new HashMap();

                    detailMap.put("id", counter++);
                    detailMap.put("itemId", detail.getItemStock().getItem().getId());
                    detailMap.put("code", detail.getItemStock().getItem().getCode());
                    detailMap.put("description", detail.getItemStock().getItem().getDescription());
                    detailMap.put("quantity", detail.getQuantity());
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

            List<StockReceive> docs = stockReceiveRepo.findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(loggedIn.getId(), fromDate, toDate, docStatusId);
            return this.makeReleasingMapList(docs);
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
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId()
            };

            User loggedIn = authenticationFacade.getLoggedIn();

            List<StockReceive> docs = stockReceiveRepo.findByVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(fromDate, toDate, Arrays.asList(ids));
            return this.makeReleasingMapList(docs);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<StockReceive> getListForSummaryReport(String from, String to, HttpServletRequest request) {
        List<StockReceive> list = new ArrayList<>();
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
                list = stockReceiveRepo.findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusId(fromDate, toDate, locationInt, statusInt);
            } else if(!Checker.isStringNullAndEmpty(location)) {
                Integer locationInt = Integer.parseInt(location);
                list = stockReceiveRepo.findByVoucherDateBetweenAndInventoryLocationId(fromDate, toDate, locationInt);
            } else if(!Checker.isStringNullAndEmpty(status)) {
                Integer statusInt = Integer.parseInt(status);
                list = stockReceiveRepo.findByVoucherDateBetweenAndDocumentStatusId(fromDate, toDate, statusInt);
            } else {
                list = stockReceiveRepo.findByVoucherDateBetween(fromDate, toDate);
            }

            if(!Checker.collectionIsEmpty(list)){
                for(StockReceive l : list){
                    l.setDetails(new ArrayList<>(this.getItems(l.getTransaction().getId())));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<ItemTransactionDetailDto> getItems(Integer transId) {
        List<ItemTransactionDetailDto> data = new ArrayList<>();
        ArrayList<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(transId);

        if(!details.isEmpty()) {
            for (StockTransactionDetail detail : details) {

                ItemTransactionDetailDto detailDto = new ItemTransactionDetailDto();

                detailDto.setItemId(detail.getItemStock().getItem().getId());
                detailDto.setUnitCode(detail.getItemStock().getItem().getUnit().getCode());
                detailDto.setItemCode(detail.getItemStock().getItem().getCode());
                detailDto.setItemDescription(detail.getItemStock().getItem().getDescription());
                detailDto.setQuantity(detail.getQuantity());
                detailDto.setUnitCost(detail.getUnitCost());
                detailDto.setTotalCost(detail.getTotalCost());

                data.add(detailDto);
            }
        }

        return data;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<StockReceiveDocumentDto> findAllApprovedForAccountSettingPaged(String query, Pageable pageable) {

        Page<StockReceive> stockReceives;

        if(query != null){
            stockReceives = stockReceiveRepo.findAllByQueryForAccountSetting("%"+query+"%", com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        } else {
            stockReceives = stockReceiveRepo.findAllForAccountSetting(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        }

        return stockReceives.map(entity -> {

            StockReceiveDocumentDto dto = new StockReceiveDocumentDto();

            dto.setVoucherDate(entity.getVoucherDate());
            dto.setLocalCode(entity.getCode());
            dto.setParticulars(entity.getDescription());
            dto.setId(entity.getId());
            dto.setPreparedBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getFullName() : null);
            dto.setTransactionId(entity.getTransaction().getId());

            ArrayList<StockTransactionDetail> stockTransactionDetails = stockTransactionDetailRepo.findByStockTransactionTransactionId(entity.getTransaction().getId());

            if (Checker.collectionIsNotEmpty(stockTransactionDetails)) {

                BigDecimal quantity = BigDecimal.ZERO;

                for (StockTransactionDetail stockTransactionDetail : stockTransactionDetails) {
                    quantity = quantity.add(stockTransactionDetail.getQuantity());
                }

                dto.setNetAmount(BigDecimal.ZERO);
                dto.setQuantity(quantity);

            }

            return dto;

        });

    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<StockReceiveDocumentDto> findAllApprovedForJVPaged(String query, Pageable pageable) {

        Page<StockReceive> stockReceives;

        if(query != null){
            stockReceives = stockReceiveRepo.findAllByQueryForJV("%"+query+"%", com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        } else {
            stockReceives = stockReceiveRepo.findAllForJV(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        }

        return stockReceives.map(entity -> {

            StockReceiveDocumentDto dto = new StockReceiveDocumentDto();

            dto.setVoucherDate(entity.getVoucherDate());
            dto.setLocalCode(entity.getCode());
            dto.setParticulars(entity.getDescription());
            dto.setId(entity.getId());
            dto.setPreparedBy(entity.getCreatedBy().getFullName());
            dto.setTransactionId(entity.getTransaction().getId());
            dto.setExtensionUrl("receiving");

            ArrayList<StockTransactionDetail> stockTransactionDetails = stockTransactionDetailRepo.findByStockTransactionTransactionId(entity.getTransaction().getId());

            if (Checker.collectionIsNotEmpty(stockTransactionDetails)) {

                BigDecimal quantity = BigDecimal.ZERO;

                for (StockTransactionDetail stockTransactionDetail : stockTransactionDetails) {
                    quantity = quantity.add(stockTransactionDetail.getQuantity());
                }

                dto.setNetAmount(BigDecimal.ZERO);
                dto.setQuantity(quantity);

            }

            return dto;

        });

    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> filesToRemove) {
        return this.processUpdate(v, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request) {
        return this.processCreate(v, bindingResult, messageSource);
    }

    @Override
    public List<DocumentStatus> getDocumentsStatuses() {
        return documentDtoer.getDocumentStatuses(com.noreco1.fireflyv2.model.enums.Workflow.STOCK_RECEIVE.getId());
    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        StockReceive stockReceiveRepoOne = stockReceiveRepo.findById(postData.getDocumentId()).orElse(null);

        if (stockReceiveRepoOne != null) {
            // for logging
            List<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(stockReceiveRepoOne.getTransaction().getId());
            ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
            for (StockTransactionDetail d : details) {
                detailsDto.add(d.toDto());
            }
            stockReceiveRepoOne.setDetails(detailsDto);

            Map oldMap = this.forLogMapMain(stockReceiveRepoOne);
            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(stockReceiveRepoOne, stockReceiveRepoOne.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            stockReceiveRepoOne.setDocumentStatus(afterActionDocumentStatus);
            stockReceiveRepoOne.setUpdatedAt(null);
            stockReceiveRepoOne = stockReceiveRepo.save(stockReceiveRepoOne);

            // for logging
            Map newMap = this.forLogMapMain(stockReceiveRepoOne);
            newMap.put("remarks", postData.getRemarks());

            if (stockReceiveRepoOne != null) {
                documentProcessingFacade.processAction(stockReceiveRepoOne.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(stockReceiveRepoOne.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, newMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }

        }
        return response;
    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        StockReceive receive = (StockReceive) v;
        return this.processCreate(receive, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {

        StockReceive stockReceive = (StockReceive) v;

        PostResponse response = new PostResponse();
        response.setSuccess(false);

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        //approvedby workaround
        User approvedBy = authenticationFacade.getLoggedIn();
        stockReceive.setApprovingOfficer(approvedBy);

        StockReceiveValidator validator = new StockReceiveValidator();
        validator.setService(this);
        validator.validate(stockReceive, bindingResult);

        if (bindingResult.hasErrors()) {

            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();

        } else {

            User createdBy = authenticationFacade.getLoggedIn();
            StockReceive existingReceive = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(stockReceive.getVoucherDate()));

            User checkedBy = userRepo.findOneByAccountNo(stockReceive.getCheckedBy().getAccountNo());
//            User approvedBy = userRepo.findOneByAccountNo(stockReceive.getApprovingOfficer().getAccountNo());
            Employee loggedInEmployee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());

            Boolean insertMode = stockReceive.getId() == null;
            if (insertMode) { // insert mode
//                String offAcro = stockReceive.getOffice().getAcronym();
                Object latestCanvassCode = stockReceiveRepo.findLatestCodeByYear(voucherYear);
                stockReceive.setCode(generatorFacade.voucherCode("SRC", (latestCanvassCode == null ? "" : String.valueOf(latestCanvassCode)), stockReceive.getVoucherDate()));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                stockReceive.setDocumentStatus(documentStatus);

                Workflow wf = new Workflow();
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.STOCK_RECEIVE.getId());

                stockReceive.setYear(voucherYear);
                stockReceive.setTransaction(generatorFacade.transaction());
                stockReceive.setWorkflow(wf);
                stockReceive.setCreatedBy(createdBy);
                existingReceive = stockReceive;
            } else {
                existingReceive = stockReceiveRepo.findById(stockReceive.getId()).orElse(null);
            }
            // use for document logging
            Map oldMap = this.forLogMapMain(existingReceive);

            existingReceive.setDocumentTransaction(stockReceive.getDocumentTransaction());
            existingReceive.setInventoryLocation(stockReceive.getInventoryLocation());
            existingReceive.setVoucherDate(stockReceive.getVoucherDate());
            existingReceive.setDescription(stockReceive.getDescription());
            existingReceive.setYear(voucherYear);
            existingReceive.setCheckedBy(checkedBy);
            existingReceive.setApprovingOfficer(approvedBy);
            existingReceive.setOffice(loggedInEmployee.getOffice());

            existingReceive.setUpdatedAt(new Date());
            this.model = stockReceiveRepo.save(existingReceive);

            if (this.model != null) {

                // start: update default signatories
                signatoryFacade.src(this.model);
                // end: update default signatories

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldMap = null;
                }

                ArrayList<ItemTransactionDetailDto> details = stockReceive.getDetails();
                //save to stockTrans tables
                if (!Checker.collectionIsEmpty(details)) {
                    InventoryLocation inventoryLocation = model.getInventoryLocation();
                    StockTransaction stockTransaction = new StockTransaction();
                    stockTransaction.setTransaction(model.getTransaction());
                    stockTransaction.setCreatedBy(model.getCreatedBy());
                    stockTransaction = stockTransactionRepo.save(stockTransaction);
                    for (ItemTransactionDetailDto itemTransactionDetailDto : details) {
                        if(itemTransactionDetailDto.getReceiveQuantity() != null && itemTransactionDetailDto.getReceiveQuantity().compareTo(BigDecimal.ZERO) == 1) {
                            ItemStock itemStock = itemStockRepo.findByItemIdAndInventoryLocationId(itemTransactionDetailDto.getItemId(), inventoryLocation.getId());
                            //receive cost = rec. qty * netUnitPrice
                            if(itemStock != null) {
                                BigDecimal totalQty = itemStock.getTotalQuantity().add(itemTransactionDetailDto.getReceiveQuantity());
                                BigDecimal totalCost = itemStock.getTotalItemCost().add(itemTransactionDetailDto.getUnitCost().multiply(itemTransactionDetailDto.getReceiveQuantity()));
                                itemStock.setTotalQuantity(totalQty);
                                itemStock.setTotalItemCost(totalCost);
                            } else {
                                itemStock = new ItemStock();
                                itemStock.setTotalQuantity(itemTransactionDetailDto.getReceiveQuantity());
                                itemStock.setTotalItemCost(itemTransactionDetailDto.getUnitCost().multiply(itemTransactionDetailDto.getReceiveQuantity()));
                                Item item = new Item();
                                item.setId(itemTransactionDetailDto.getItemId());
                                itemStock.setItem(item);
                                itemStock.setInventoryLocation(inventoryLocation);
                            }

                            ItemStock newItemStock = itemStockRepo.save(itemStock);

                            if (newItemStock != null) {

                                ItemStockDetail itemStockDetail = new ItemStockDetail();

                                itemStockDetail.setItemStock(newItemStock);
                                itemStockDetail.setQuantity(itemTransactionDetailDto.getReceiveQuantity());
                                itemStockDetail.setUnitCost(itemTransactionDetailDto.getUnitCost());
                                itemStockDetail.setItemCost(itemStockDetail.getQuantity().multiply(itemStockDetail.getUnitCost()));
                                itemStockDetail.setDepartment(loggedInEmployee.getDepartment());//set ItemStockDetail.FK_departmentId to the receiving Department

                                ItemStockDetail newItemStockDetail = itemStockDetailRepo.save(itemStockDetail);

                                if(newItemStockDetail != null){
                                    StockTransactionDetail stockTransactionDetail = new StockTransactionDetail();
                                    stockTransactionDetail.setQuantity(itemTransactionDetailDto.getReceiveQuantity());
                                    stockTransactionDetail.setUnitCost(itemTransactionDetailDto.getUnitCost());
                                    stockTransactionDetail.setTotalCost(stockTransactionDetail.getUnitCost().multiply(stockTransactionDetail.getQuantity()));
                                    stockTransactionDetail.setVat(BigDecimal.ZERO);
                                    stockTransactionDetail.setStockTransaction(stockTransaction);
                                    stockTransactionDetail.setItemStock(newItemStock);
                                    stockTransactionDetail.setItemStockDetail(newItemStockDetail);
                                    stockTransactionDetail.setInventoryLocation(inventoryLocation);
                                    stockTransactionDetail.setItemStockBalance(newItemStockDetail.getQuantity());
                                    stockTransactionDetail.setItemStockAmountBalance(newItemStockDetail.getQuantity().multiply(newItemStockDetail.getUnitCost()));
                                    stockTransactionDetail.setType(1); // 1 = in, 2 = out
                                    stockTransactionDetailRepo.save(stockTransactionDetail);
                                }

                            }

                            //update transfer details
                            List<ItemTransactionDetail> stockTransferDetails = itemTransactionDetailRepo.findByTransactionId(model.getDocumentTransaction().getId());
                            for (ItemTransactionDetail itemTransactionDetail : stockTransferDetails) {
                                if (itemTransactionDetail.getItem().getId().equals(itemTransactionDetailDto.getItemId())) {
                                    itemTransactionDetail.setQuantityReceived(itemTransactionDetail.getQuantityReceived().add(itemTransactionDetailDto.getReceiveQuantity()));
                                    itemTransactionDetailRepo.save(itemTransactionDetail);
                                    break;
                                }
                            }
                        }
                    }
                }

                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());
                response.setSuccessMessage("Stock Receive successfully saved!");
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
            StockReceive doc = stockReceiveRepo.findOneByTransactionId(transId);

            if (doc != null) {
                List<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(transId);
                ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
                for (StockTransactionDetail d : details) {
                    detailsDto.add(d.toDto());
                }
                doc.setDetails(detailsDto);

                Map map = forLogMapMain(doc);
                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.SRC);
    }

    @Override
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {

        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        StockReceive stockReceive = stockReceiveRepo.findById(vid).orElse(null);

        if (stockReceive != null) {

            List<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(stockReceive.getTransaction().getId());
            BigDecimal totalQty = BigDecimal.ZERO;
            for (StockTransactionDetail d : details) {
                totalQty = totalQty.add(d.getQuantity());
            }
            StockTransfer stockTransfer = stockTransferRepo.findOneByTransactionId(stockReceive.getDocumentTransaction().getId());

            params.put("VOUCHER_NO", stockReceive.getCode());
            params.put("PURPOSE", stockReceive.getDescription());
            params.put("V_DATE", stockReceive.getVoucherDate());
            params.put("INV_LOCATION", stockReceive.getInventoryLocation().getDescription());
            params.put("DESCRIPTION", stockReceive.getDescription());
            params.put("TOTAL_QTY", totalQty);
            params.put("SOURCE_LOC", stockTransfer.getFromInventoryLocation().getDescription());
            params.put("ST_NO", stockTransfer.getCode());

            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.SRC, stockReceive);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer vid) {

        List<Map> rrDetails = this.getDetails(vid);
        return new JRBeanCollectionDataSource(rrDetails);
    }

    private Map forLogMapMain(StockReceive src) {
        return documentLoggerFacade.makeLog(src);
    }

    private List<Map> makeReleasingMapList(List<StockReceive> cs) {

        List<Map> mapList = new ArrayList<>();

        if (!Checker.collectionIsEmpty(cs)) {
            for (StockReceive c : cs) {
                mapList.add(composeMap(c));
            }
        }
        return mapList;
    }

    private Map composeMap(StockReceive r) {
        Map map = new HashMap();

        map.put("id", r.getId());
        map.put("code", r.getCode());
        map.put("voucherDate", r.getVoucherDate());
        map.put("description", r.getDescription());
        map.put("createdBy", r.getCreatedBy());
        map.put("checkedBy", r.getCheckedBy());
        map.put("approvingOfficer", r.getApprovingOfficer());
        map.put("documentStatus", r.getDocumentStatus());

        return map;
    }
}
