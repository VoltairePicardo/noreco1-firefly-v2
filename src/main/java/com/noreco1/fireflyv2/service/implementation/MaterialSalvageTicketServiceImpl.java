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
import com.noreco1.fireflyv2.model.enums.WorkflowAction;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.ItemTransactionDetailDto;
import com.noreco1.fireflyv2.controller.response.MaterialSalvageTicketDocumentDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.service.MaterialSalvageTicketService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.MaterialSalvageTicketValidator;
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

@Service(value = "materialSalvageTicketServiceImpl")
public class MaterialSalvageTicketServiceImpl implements MaterialSalvageTicketService, PrintableVoucher {

    private MaterialSalvageTicket model;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    MaterialSalvageTicketRepo materialSalvageTicketRepo;

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
    MemorandumReceiptDetailRepo memorandumReceiptDetailRepo;

    @Autowired
    private ReturnMemorandumReceiptDetailRepo returnMemorandumReceiptDetailRepo;

    @Autowired
    ItemTransactionDetailSerialNoRepo itemTransactionDetailSerialNoRepo;

    @Autowired
    SpecialEquipmentRepo specialEquipmentRepo;

    @Autowired
    StockTransactionDetailSerialNoRepo stockTransactionDetailSerialNoRepo;

    @Autowired
    ItemSerialNoFacade itemSerialNoFacade;

    @Autowired
    ItemStockDetailRepo itemStockDetailRepo;

    private Map reportMeta = new HashMap();

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public MaterialSalvageTicket findById(Integer id) {

        MaterialSalvageTicket ret = materialSalvageTicketRepo.findById(id).orElse(null);

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
                    dto.setIsUsable(d.getIsUsable());

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

            }

            ret.setDetails(detailsDto);

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return ret;

    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<MaterialSalvageTicket> findAll() {
        return materialSalvageTicketRepo.findAll();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<MaterialSalvageTicket> findAll(Pageable pageable) {
        return materialSalvageTicketRepo.findAll(pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<MaterialSalvageTicket> findByQuery(String query, Pageable pageable) {
        return materialSalvageTicketRepo.findByCode(query, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<DocumentStatus> getDocumentsStatuses() {
        return documentDtoer.getDocumentStatuses(com.noreco1.fireflyv2.model.enums.Workflow.MATERIAL_SALVAGE_TICKET.getId());
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<MaterialSalvageTicket> getListForSummaryReport(String from, String to, HttpServletRequest request) {
        List<MaterialSalvageTicket> list = new ArrayList<>();
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
                list = materialSalvageTicketRepo.findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusId(fromDate, toDate, locationInt, statusInt);
            } else if(!Checker.isStringNullAndEmpty(location)) {
                Integer locationInt = Integer.parseInt(location);
                list = materialSalvageTicketRepo.findByVoucherDateBetweenAndInventoryLocationId(fromDate, toDate, locationInt);
            } else if(!Checker.isStringNullAndEmpty(status)) {
                Integer statusInt = Integer.parseInt(status);
                list = materialSalvageTicketRepo.findByVoucherDateBetweenAndDocumentStatusId(fromDate, toDate, statusInt);
            } else {
                list = materialSalvageTicketRepo.findByVoucherDateBetween(fromDate, toDate);
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
        ArrayList<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(transId);

        if(!details.isEmpty()) {
            for (ItemTransactionDetail detail : details) {

                ItemTransactionDetailDto detailDto = new ItemTransactionDetailDto();

                detailDto.setItemId(detail.getItem().getId());
                detailDto.setUnitCode(detail.getItem().getUnit().getCode());
                detailDto.setItemCode(detail.getItem().getCode());
                detailDto.setItemDescription(detail.getItem().getDescription());
                detailDto.setQuantity(detail.getQuantity());
                detailDto.setUnitCost(detail.getUnitCost());
                detailDto.setTotalCost(detail.getTotalCost());
                detailDto.setMemorandumReceiptDetail(detail.getMemorandumReceiptDetail());
                detailDto.setIsUsable(detail.getIsUsable());

                data.add(detailDto);
            }
        }

        return data;
    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        MaterialSalvageTicket materialSalvageTicket = materialSalvageTicketRepo.findById(postData.getDocumentId()).orElse(null);

        if (materialSalvageTicket != null &&
                materialSalvageTicket.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId() &&
                materialSalvageTicket.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId()
                ) {
            List<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(materialSalvageTicket.getTransaction().getId());
            ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
            for (ItemTransactionDetail d : details) {
                detailsDto.add(d.toMSTDto());
            }

            // for logging
            Map oldMstMap = this.forLogMapMain(materialSalvageTicket);

            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(materialSalvageTicket, materialSalvageTicket.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            materialSalvageTicket.setDocumentStatus(afterActionDocumentStatus);
            materialSalvageTicket.setUpdatedAt(new Date());
            materialSalvageTicket = materialSalvageTicketRepo.save(materialSalvageTicket);

            if (materialSalvageTicket != null) {

                // for logging
                materialSalvageTicket.setDetails(detailsDto);
                Map newMstMap = this.forLogMapMain(materialSalvageTicket);
                newMstMap.put("remarks", postData.getRemarks());

                documentProcessingFacade.processAction(materialSalvageTicket.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(materialSalvageTicket.getTransaction(), authenticationFacade.getLoggedIn(), oldMstMap, newMstMap);

                // insert to itemstock & stocktrans tables if items receive
                if (postData.getWorkflowActionsDto().getActionId() == WorkflowAction.RECEIVE_ITEM.getId()) {

                    if (!Checker.collectionIsEmpty(details)) {

                        InventoryLocation inventoryLocation = materialSalvageTicket.getInventoryLocation();
                        StockTransaction stockTransaction = new StockTransaction();
                        stockTransaction.setTransaction(materialSalvageTicket.getTransaction());
                        stockTransaction.setCreatedBy(processedBy);
                        stockTransaction.setCreatedAt(new Date());
                        stockTransaction.setUpdatedAt(new Date());
                        stockTransaction = stockTransactionRepo.save(stockTransaction);

                        for (ItemTransactionDetailDto detailDto : materialSalvageTicket.getDetails()) {

                            if (detailDto.getIsUsable()) {

                                ItemStock itemStock = itemStockRepo.findByItemIdAndInventoryLocationId(detailDto.getItemId(), inventoryLocation.getId());

                                if (itemStock != null) {
                                    BigDecimal totalQty = itemStock.getTotalQuantity().add(detailDto.getQuantity());
                                    BigDecimal totalCost = itemStock.getTotalItemCost().add(detailDto.getTotalCost());
                                    itemStock.setTotalQuantity(totalQty);
                                    itemStock.setTotalItemCost(totalCost);
                                } else {
                                    itemStock = new ItemStock();

                                    Item item = new Item();
                                    item.setId(detailDto.getItemId());
                                    itemStock.setItem(item);

                                    itemStock.setInventoryLocation(inventoryLocation);
                                    itemStock.setTotalQuantity(detailDto.getQuantity());
                                    itemStock.setTotalItemCost(detailDto.getTotalCost());
                                    itemStock.setCreatedAt(new java.sql.Date(System.currentTimeMillis()));
                                    itemStock.setUpdatedAt(new java.sql.Date(System.currentTimeMillis()));
                                }
                                ItemStock newItemStock = itemStockRepo.save(itemStock);

                                if (newItemStock != null) {

                                    ItemStockDetail itemStockDetail = new ItemStockDetail();
                                    itemStockDetail.setItemStock(newItemStock);
                                    itemStockDetail.setQuantity(detailDto.getQuantity());
                                    itemStockDetail.setUnitCost(detailDto.getUnitCost());
                                    itemStockDetail.setItemCost(detailDto.getTotalCost());

                                    ItemStockDetail newItemStockDetail1 = itemStockDetailRepo.save(itemStockDetail);

                                    if(newItemStockDetail1 != null){

                                        StockTransactionDetail stockTransactionDetail = new StockTransactionDetail();
                                        stockTransactionDetail.setQuantity(newItemStockDetail1.getQuantity());
                                        stockTransactionDetail.setUnitCost(newItemStockDetail1.getUnitCost());
                                        stockTransactionDetail.setTotalCost(stockTransactionDetail.getUnitCost().multiply(stockTransactionDetail.getQuantity()));
                                        stockTransactionDetail.setVat(BigDecimal.ZERO);
                                        stockTransactionDetail.setStockTransaction(stockTransaction);
                                        stockTransactionDetail.setItemStock(newItemStock);
                                        stockTransactionDetail.setItemStockDetail(newItemStockDetail1);
                                        stockTransactionDetail.setInventoryLocation(inventoryLocation);
                                        stockTransactionDetail.setItemStockBalance(newItemStockDetail1.getQuantity());
                                        stockTransactionDetail.setItemStockAmountBalance(newItemStockDetail1.getQuantity().multiply(newItemStockDetail1.getUnitCost()).setScale(2, BigDecimal.ROUND_HALF_UP));
                                        stockTransactionDetail.setType(1); // 1 = in, 2 = out

                                        StockTransactionDetail savedStockTransactionDetail = stockTransactionDetailRepo.save(stockTransactionDetail);

                                        //Transfer serial numbers from ItemTransactionDetailSerialNo table to StockTransactionDetailSerialNo table
                                        if (savedStockTransactionDetail != null){

                                            ItemTransactionDetail itemTransactionDetail = itemTransactionDetailRepo.findTopByTransactionIdAndItemIdOrderByIdDesc(materialSalvageTicket.getTransaction().getId(), detailDto.getItemId());

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

                }

                response.setSuccessMessage("Material Salvage Ticket successfully processed");
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

        MaterialSalvageTicket materialSalvageTicket = (MaterialSalvageTicket) v;

        PostResponse response = new PostResponse();
        response.setSuccess(false);

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        MaterialSalvageTicketValidator validator = new MaterialSalvageTicketValidator();
        validator.setService(this);
        validator.validate(materialSalvageTicket, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            MaterialSalvageTicket existingMST = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(materialSalvageTicket.getVoucherDate()));

            User receivedBy = userRepo.findOneByAccountNo(materialSalvageTicket.getReceivedBy().getAccountNo());
            User returnedBy = userRepo.findOneByAccountNo(materialSalvageTicket.getReturnedBy().getAccountNo());
            Employee loggedInEmployee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());

            Boolean insertMode = materialSalvageTicket.getId() == null;
            if (insertMode) { // insert mode
//                String offAcro = materialSalvageTicket.getOffice().getAcronym();
                Object latestCode = materialSalvageTicketRepo.findLatestCodeByYear(voucherYear);
                materialSalvageTicket.setCode(generatorFacade.voucherCodeWithMonth("MST", (latestCode == null ? "" : String.valueOf(latestCode)), materialSalvageTicket.getVoucherDate()));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                materialSalvageTicket.setDocumentStatus(documentStatus);

                Workflow wf = new Workflow();
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.MATERIAL_SALVAGE_TICKET.getId());

                materialSalvageTicket.setYear(voucherYear);
                materialSalvageTicket.setTransaction(generatorFacade.transaction());
                materialSalvageTicket.setWorkflow(wf);
                materialSalvageTicket.setCreatedBy(createdBy);
                existingMST = materialSalvageTicket;
            } else {
                existingMST = materialSalvageTicketRepo.findById(materialSalvageTicket.getId()).orElse(null);
            }
            // use for document logging
            Map oldMap = this.forLogMapMain(existingMST);

            existingMST.setVoucherDate(materialSalvageTicket.getVoucherDate());
            existingMST.setPurpose(materialSalvageTicket.getPurpose());
            existingMST.setYear(voucherYear);
            existingMST.setReceivedBy(receivedBy);
            existingMST.setReturnedBy(returnedBy);
            existingMST.setOffice(loggedInEmployee.getOffice());

            existingMST.setUpdatedAt(new Date());
            this.model = materialSalvageTicketRepo.save(existingMST);

            if (this.model != null) {

                // start: update default signatories
                signatoryFacade.mst(this.model);
                // end: update default signatories

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

                ArrayList<ItemTransactionDetailDto> details = materialSalvageTicket.getDetails();
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
                    } else {
                        Item item = new Item();
                        item.setId(detailDto.getItemId());
                        detail.setItem(item);
                        inventoryLocation.setId(this.model.getInventoryLocation().getId());
                        detail.setItemStock(null);
                    }

                    detail.setTransaction(this.model.getTransaction());

                    //TODO: Set values for details
                    detail.setQuantity(detailDto.getQuantity());
                    detail.setUnitCost(detailDto.getUnitCost());
                    detail.setTotalCost(detail.getQuantity().multiply(detail.getUnitCost()));

                    detail.setInventoryLocation(inventoryLocation);
                    detail.setMemorandumReceiptDetail(detailDto.getMemorandumReceiptDetail());
                    detail.setReturnMemorandumReceiptDetail(detailDto.getReturnMemorandumReceiptDetail());
                    detail.setIsUsable(detailDto.getIsUsable());

                    if (!detailDto.getQuantity().equals(BigDecimal.ZERO)) {
                        ItemTransactionDetail newDetail = itemTransactionDetailRepo.save(detail);

                        if(newDetail != null){

                            if(detailDto.getReturnMemorandumReceiptDetail() != null){
                                ReturnMemorandumReceiptDetail returnMemorandumReceiptDetail = returnMemorandumReceiptDetailRepo.findById(detailDto.getReturnMemorandumReceiptDetail().getId()).orElse(null);

                                if(returnMemorandumReceiptDetail != null){
                                    returnMemorandumReceiptDetail.setReturnedToInventoryQuantity(detailDto.getQuantity());
                                    returnMemorandumReceiptDetailRepo.save(returnMemorandumReceiptDetail);
                                }

                            }

                            //Save Serial Numbers
                            if (Checker.collectionIsNotEmpty(detailDto.getSerialNumbers())){

                                for (SpecialEquipment specialEquipment : detailDto.getSerialNumbers()){

                                    ItemTransactionDetailSerialNo itemTransactionDetailSerialNo = new ItemTransactionDetailSerialNo();

                                    itemTransactionDetailSerialNo.setItemTransactionDetail(newDetail);
                                    itemTransactionDetailSerialNo.setSerialNo(specialEquipment.getSerialNo());

                                    itemTransactionDetailSerialNoRepo.save(itemTransactionDetailSerialNo);

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
                response.setSuccessMessage("Material Salvage Ticket successfully saved!");
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
            MaterialSalvageTicket materialSalvageTicket = materialSalvageTicketRepo.findOneByTransactionId(transId);

            if (materialSalvageTicket != null) {
                List<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(transId);
                ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
                for (ItemTransactionDetail d : details) {
                    detailsDto.add(d.toMSTDto());
                }
                materialSalvageTicket.setDetails(detailsDto);

                Map map = forLogMapMain(materialSalvageTicket);
                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.MST);
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

            List<MaterialSalvageTicket> docs = materialSalvageTicketRepo.findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(loggedIn.getId(), fromDate, toDate, docStatusId);
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
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.RECEIVED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId()
            };

            User loggedIn = authenticationFacade.getLoggedIn();

            List<MaterialSalvageTicket> docs = materialSalvageTicketRepo.findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(loggedIn.getId(), fromDate, toDate, Arrays.asList(ids));
            return this.makeMapList(docs);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Map forLogMapMain(MaterialSalvageTicket materialSalvageTicket) {
        return documentLoggerFacade.makeLog(materialSalvageTicket);
    }

    private List<Map> makeMapList(List<MaterialSalvageTicket> materialSalvageTickets) {

        List<Map> mapList = new ArrayList<>();

        if (!Checker.collectionIsEmpty(materialSalvageTickets)) {
            for (MaterialSalvageTicket c : materialSalvageTickets) {
                mapList.add(composeMap(c));
            }
        }
        return mapList;
    }

    private Map composeMap(MaterialSalvageTicket r) {
        Map map = new HashMap();

        map.put("id", r.getId());
        map.put("code", r.getCode());
        map.put("voucherDate", r.getVoucherDate());
        map.put("department", r.getDepartment().getName());
        map.put("purpose", r.getPurpose());
        map.put("createdBy", r.getCreatedBy());
        map.put("documentStatus", r.getDocumentStatus());

        return map;
    }

    @Override
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {
        this.reportMeta = new HashMap();
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        MaterialSalvageTicket materialSalvageTicket = materialSalvageTicketRepo.findById(vid).orElse(null);

        if (materialSalvageTicket != null) {

            List<Map> details = this.getDetails(vid);
            BigDecimal totalQty = BigDecimal.ZERO;
            for (Map d : details) {
                totalQty = totalQty.add(new BigDecimal(d.get("quantity")+""));
            }

            params.put("VOUCHER_NO", materialSalvageTicket.getCode());
            params.put("REMARKS", materialSalvageTicket.getPurpose());
            params.put("V_DATE", materialSalvageTicket.getVoucherDate());
            params.put("DEPARTMENT", materialSalvageTicket.getDepartment().getName());
            params.put("TOTAL_QTY", totalQty);
            params.put("INV_LOCATION", materialSalvageTicket.getInventoryLocation().getDescription());
            params.put("SUBREPORT_DIR", GlobalConstant.JASPER_BASE_PATH + "/vouchers/sub_reports/");
            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.MST, materialSalvageTicket);
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

        MaterialSalvageTicket materialSalvageTicket = materialSalvageTicketRepo.findById(id).orElse(null);
        if (materialSalvageTicket != null) {
            List<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(materialSalvageTicket.getTransaction().getId());

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
                    detailMap.put("unitCost", detail.getUnitCost());
                    detailMap.put("totalCost", detail.getTotalCost());
                    detailMap.put("mrte", detail.getMemorandumReceiptDetail() != null ? detail.getMemorandumReceiptDetail().getMemorandumReceipt().getCode() : "");
                    detailMap.put("usable", detail.getIsUsable() ? "YES" : "NO");
                    detailMap.put("stockTransactionDetailId", detail.getId());

                    data.add(detailMap);
                }
            }
        }

        return data;
    }

    @Override
    public Map getReportMeta() {
        return this.reportMeta;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<MaterialSalvageTicketDocumentDto> findAllApprovedForAccountSettingPaged(String query, Pageable pageable) {

        Page<MaterialSalvageTicket> materialSalvageTickets;

        if(query != null){
            materialSalvageTickets = materialSalvageTicketRepo.findAllByQueryForAccountSetting("%"+query+"%", com.noreco1.fireflyv2.model.enums.DocumentStatus.RECEIVED.getId(), pageable);
        } else {
            materialSalvageTickets = materialSalvageTicketRepo.findAllForAccountSetting(com.noreco1.fireflyv2.model.enums.DocumentStatus.RECEIVED.getId(), pageable);
        }

        return materialSalvageTickets.map(entity -> {

                MaterialSalvageTicketDocumentDto dto = new MaterialSalvageTicketDocumentDto();

                dto.setVoucherDate(entity.getVoucherDate());
                dto.setLocalCode(entity.getCode());
                dto.setParticulars(entity.getPurpose());
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
}

