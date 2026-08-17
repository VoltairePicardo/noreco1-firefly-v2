package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.ItemTransactionDetailDto;
import com.noreco1.fireflyv2.controller.response.MaterialCreditTicketDocumentDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.service.MaterialCreditTicketService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.MaterialCreditTicketValidator;
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

@Service(value = "materialCreditTicketServiceImpl")
public class MaterialCreditTicketServiceImpl implements MaterialCreditTicketService, PrintableVoucher {

    private MaterialCreditTicket model;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    MaterialCreditTicketRepo materialCreditTicketRepo;

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
    ItemStockDetailRepo itemStockDetailRepo;

    @Autowired
    StockTransactionDetailRepo stockTransactionDetailRepo;

    @Autowired
    StockTransactionRepo stockTransactionRepo;

    @Autowired
    SignatureFacade signatureFacade;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    StockWithdrawalRepo stockWithdrawalRepo;

    @Autowired
    ItemTransactionDetailSerialNoRepo itemTransactionDetailSerialNoRepo;

    @Autowired
    SpecialEquipmentRepo specialEquipmentRepo;

    @Autowired
    StockTransactionDetailSerialNoRepo stockTransactionDetailSerialNoRepo;

    @Autowired
    ItemSerialNoFacade itemSerialNoFacade;

    @Autowired
    InventoryLocationRepo inventoryLocationRepo;

    private Map reportMeta;

    @Override
    public MaterialCreditTicket findById(Integer id) {

        MaterialCreditTicket ret = materialCreditTicketRepo.findById(id).orElse(null);

        try {

            ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();

            ArrayList<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(ret.getTransaction().getId());

            if (Checker.collectionIsNotEmpty(details)){

                for(ItemTransactionDetail d : details){

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

    @Override
    public List<MaterialCreditTicket> findAll() {
        return materialCreditTicketRepo.findAll();
    }

    @Override
    public Page<MaterialCreditTicket> findAll(Pageable pageable) {
        return materialCreditTicketRepo.findAll(pageable);
    }

    @Override
    public Page<MaterialCreditTicket> findByQuery(String query, Pageable pageable) {
        return materialCreditTicketRepo.findByCode(query, pageable);
    }

    @Override
    public List<DocumentStatus> getDocumentsStatuses() {
        return documentDtoer.getDocumentStatuses(com.noreco1.fireflyv2.model.enums.Workflow.MATERIAL_CREDIT_TICKET.getId());
    }

    @Override
    public List<MaterialCreditTicket> getListForSummaryReport(String from, String to, HttpServletRequest request) {
        List<MaterialCreditTicket> list = new ArrayList<>();
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
                list = materialCreditTicketRepo.findByVoucherDateBetweenAndStockReleaseInventoryLocationIdAndDocumentStatusId(fromDate, toDate, locationInt, statusInt);
            } else if(!Checker.isStringNullAndEmpty(location)) {
                Integer locationInt = Integer.parseInt(location);
                list = materialCreditTicketRepo.findByVoucherDateBetweenAndStockReleaseInventoryLocationId(fromDate, toDate, locationInt);
            } else if(!Checker.isStringNullAndEmpty(status)) {
                Integer statusInt = Integer.parseInt(status);
                list = materialCreditTicketRepo.findByVoucherDateBetweenAndDocumentStatusId(fromDate, toDate, statusInt);
            } else {
                list = materialCreditTicketRepo.findByVoucherDateBetween(fromDate, toDate);
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
                detailDto.setQuantity(detail.getQuantity());
                detailDto.setUnitCost(detail.getUnitCost());
                detailDto.setTotalCost(detail.getTotalCost());

                data.add(detailDto);
            }
        }

        return data;
    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        MaterialCreditTicket materialCreditTicket =  materialCreditTicketRepo.findById(postData.getDocumentId()).orElse(null);

        if (materialCreditTicket != null &&
                materialCreditTicket.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId() &&
                materialCreditTicket.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId()
                ) {
            List<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(materialCreditTicket.getTransaction().getId());
            ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
            for(ItemTransactionDetail d : details){
                detailsDto.add(d.toDto());
            }

            // for logging
            materialCreditTicket.setDetails(detailsDto);
            Map oldRrMap = this.forLogMapMain(materialCreditTicket);

            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(materialCreditTicket, materialCreditTicket.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            materialCreditTicket.setDocumentStatus(afterActionDocumentStatus);
            materialCreditTicket.setUpdatedAt(new Date());
            materialCreditTicket = materialCreditTicketRepo.save(materialCreditTicket);

            if (materialCreditTicket != null) {

                // for logging
                materialCreditTicket.setDetails(detailsDto);
                Map newRrMap = this.forLogMapMain(materialCreditTicket);
                newRrMap.put("remarks", postData.getRemarks());

                documentProcessingFacade.processAction(materialCreditTicket.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(materialCreditTicket.getTransaction(), authenticationFacade.getLoggedIn(), oldRrMap, newRrMap);

                // insert to itemstock & stocktrans tables if approved
                if(materialCreditTicket.getDocumentStatus().getId() == 7) {

                    if (!Checker.collectionIsEmpty(details)) {

                        InventoryLocation inventoryLocation = materialCreditTicket.getStockRelease() != null ? materialCreditTicket.getStockRelease().getInventoryLocation() : materialCreditTicket.getInventoryLocation();
                        StockTransaction stockTransaction = new StockTransaction();
                        stockTransaction.setTransaction(materialCreditTicket.getTransaction());
                        stockTransaction.setCreatedBy(processedBy);
                        stockTransaction = stockTransactionRepo.save(stockTransaction);

                        for (ItemTransactionDetailDto detailDto : materialCreditTicket.getDetails()) {

                            ItemStock itemStock = itemStockRepo.findByItemIdAndInventoryLocationId(detailDto.getItemId(), inventoryLocation.getId());

                            if(itemStock != null){
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
                                    stockTransactionDetail.setItemStockBalance(itemStockDetail.getQuantity());
                                    stockTransactionDetail.setItemStockAmountBalance(itemStockDetail.getQuantity().multiply(itemStockDetail.getUnitCost()).setScale(2, BigDecimal.ROUND_HALF_UP));
                                    stockTransactionDetail.setType(1); // 1 = in, 2 = out
                                    StockTransactionDetail savedStockTransactionDetail = stockTransactionDetailRepo.save(stockTransactionDetail);

                                    //Transfer serial numbers from ItemTransactionDetailSerialNo table to StockTransactionDetailSerialNo table
                                    if (savedStockTransactionDetail != null){

                                        ItemTransactionDetail itemTransactionDetail = itemTransactionDetailRepo.findTopByTransactionIdAndItemIdOrderByIdDesc(materialCreditTicket.getTransaction().getId(), detailDto.getItemId());

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

                response.setSuccessMessage("MCT successfully processed");
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

        MaterialCreditTicket materialCreditTicket = (MaterialCreditTicket)v;

        PostResponse response = new PostResponse();
        response.setSuccess(false);

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        MaterialCreditTicketValidator validator = new MaterialCreditTicketValidator();
        validator.setService(this);
        validator.validate(materialCreditTicket, bindingResult);

        if(bindingResult.hasErrors()){
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else{
            User createdBy = authenticationFacade.getLoggedIn();
            MaterialCreditTicket existingMCT = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(materialCreditTicket.getVoucherDate()));

            User approvedBy = userRepo.findOneByAccountNo(materialCreditTicket.getApprovingOfficer().getAccountNo());
            Employee loggedInEmployee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());

            Boolean insertMode = materialCreditTicket.getId() == null;
            if (insertMode) { // insert mode
//                String offAcro = materialCreditTicket.getOffice().getAcronym();
                Object latestCode = materialCreditTicketRepo.findLatestCodeByYear(voucherYear);
                materialCreditTicket.setCode(generatorFacade.voucherCodeWithMonth("MCRT", (latestCode == null ? "" : String.valueOf(latestCode)), materialCreditTicket.getVoucherDate()));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                materialCreditTicket.setDocumentStatus(documentStatus);

                Workflow wf = new Workflow();
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.MATERIAL_CREDIT_TICKET.getId());

                materialCreditTicket.setYear(voucherYear);
                materialCreditTicket.setTransaction(generatorFacade.transaction());
                materialCreditTicket.setWorkflow(wf);
                materialCreditTicket.setCreatedBy(createdBy);

                existingMCT = materialCreditTicket;
            } else {
                existingMCT = materialCreditTicketRepo.findById(materialCreditTicket.getId()).orElse(null);
            }
            // use for document logging
            Map oldMap = this.forLogMapMain(existingMCT);

            existingMCT.setRequester(materialCreditTicket.getStockRelease() != null ? materialCreditTicket.getStockRelease().getReceivedBy() : createdBy);
            existingMCT.setVoucherDate(materialCreditTicket.getVoucherDate());
            existingMCT.setRemarks(materialCreditTicket.getRemarks());
            existingMCT.setYear(voucherYear);
            existingMCT.setApprovingOfficer(approvedBy);
            existingMCT.setOffice(loggedInEmployee.getOffice());
            existingMCT.setInventoryLocation(materialCreditTicket.getInventoryLocation());

            existingMCT.setUpdatedAt(new Date());
            this.model = materialCreditTicketRepo.save(existingMCT);

            if (this.model != null) {

                // start: update default signatories
                signatoryFacade.mct(this.model);
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

                if (Checker.collectionIsNotEmpty(materialCreditTicket.getDetails())){

                    ArrayList<ItemTransactionDetailDto> details = materialCreditTicket.getDetails();

                    for(ItemTransactionDetailDto detailDto: details) {

//                        ItemStock itemStock = itemStockRepo.findByItemIdAndInventoryLocationId(detailDto.getItemId(),this.model.getStockRelease().getInventoryLocation().getId());
                        ItemTransactionDetail detail = new ItemTransactionDetail();

                        detail.setTransaction(this.model.getTransaction());

                        //TODO: Set values for details
                        Item item = new Item();
                        item.setId(detailDto.getItemId());
                        detail.setItem(item);

                        if(detailDto.getQuantity() == null){
                            detailDto.setQuantity(BigDecimal.ZERO);
                        }

                        detail.setQuantity(detailDto.getQuantity());
                        detail.setUnitCost(detailDto.getUnitCost());
                        detail.setTotalCost(detail.getQuantity().multiply(detail.getUnitCost()));

                        if(!detailDto.getQuantity().equals(BigDecimal.ZERO)) {

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

                }

                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());
                response.setSuccessMessage("Material Credit Ticket successfully saved!");
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
            MaterialCreditTicket materialCreditTicket = materialCreditTicketRepo.findOneByTransactionId(transId);

            if (materialCreditTicket != null) {
                List<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(transId);
                ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
                for(ItemTransactionDetail d : details){
                    detailsDto.add(d.toDto());
                }
                materialCreditTicket.setDetails(detailsDto);

                Map map = forLogMapMain(materialCreditTicket);
                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.MCT);
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

            List<MaterialCreditTicket> docs = materialCreditTicketRepo.findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(loggedIn.getId(), fromDate, toDate, docStatusId);
            return this.makeMCTMapList(docs);
        }catch (Exception ex) {
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

            List<MaterialCreditTicket> docs = materialCreditTicketRepo.findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(loggedIn.getId(), fromDate, toDate, Arrays.asList(ids));
            return this.makeMCTMapList(docs);

        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Map forLogMapMain(MaterialCreditTicket materialCreditTicket) {
        return documentLoggerFacade.makeLog(materialCreditTicket);
    }

    private List<Map> makeMCTMapList(List<MaterialCreditTicket> materialCreditTickets ) {

        List<Map> mapList = new ArrayList<>();

        if (!Checker.collectionIsEmpty(materialCreditTickets)) {
            for(MaterialCreditTicket c:materialCreditTickets) {
                mapList.add(composeMap(c));
            }
        }
        return mapList;
    }

    private Map composeMap(MaterialCreditTicket r) {
        Map map = new HashMap();

        map.put("id", r.getId());
        map.put("code", r.getCode());
        map.put("voucherDate", r.getVoucherDate());
        map.put("remarks", r.getRemarks());
        map.put("createdBy", r.getCreatedBy());
        map.put("requester", r.getRequester());
        map.put("approvingOfficer", r.getApprovingOfficer());
        map.put("documentStatus", r.getDocumentStatus());

        return map;
    }

    @Override
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {

        this.reportMeta = new HashMap();
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        MaterialCreditTicket materialCreditTicket = materialCreditTicketRepo.findById(vid).orElse(null);

        if (materialCreditTicket != null) {
            List<Map> details = this.getDetails(vid);
            BigDecimal totalQty = BigDecimal.ZERO;
            String invLoc = materialCreditTicket.getStockRelease() != null ? materialCreditTicket.getStockRelease().getInventoryLocation().getDescription() : materialCreditTicket.getInventoryLocation().getDescription();
            String mchtNo = materialCreditTicket.getStockRelease() != null ? materialCreditTicket.getStockRelease().getCode() : "";
            String woNo = "";

            if(materialCreditTicket.getStockRelease() != null){
                StockWithdrawal stockWithdrawal = stockWithdrawalRepo.findOneByTransactionId(materialCreditTicket.getStockRelease().getDocumentTransaction().getId());
                if (stockWithdrawal != null) {
                    woNo = stockWithdrawal.getWorkOrder() != null ? stockWithdrawal.getWorkOrder().getCode() : "";
                }
            }

            for (Map d : details) {
                totalQty = totalQty.add(new BigDecimal(d.get("quantity")+""));
            }

            params.put("VOUCHER_NO", materialCreditTicket.getCode());
            params.put("REMARKS", materialCreditTicket.getRemarks());
            params.put("V_DATE", materialCreditTicket.getVoucherDate());
            params.put("INV_LOCATION", invLoc);
            params.put("MCHT_NO", mchtNo);
            params.put("TOTAL_QTY", totalQty);
            params.put("WO_NO", woNo);
            params.put("SUBREPORT_DIR", GlobalConstant.JASPER_BASE_PATH + "/vouchers/sub_reports/");

            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.MCT, materialCreditTicket);
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

        MaterialCreditTicket materialCreditTicket = materialCreditTicketRepo.findById(id).orElse(null);
        if (materialCreditTicket != null) {
            List<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(materialCreditTicket.getTransaction().getId());

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

    @Override
    public List<InventoryLocation> getAllInventoryLocations() {
        return inventoryLocationRepo.findAll();
    }

    @Override
    public Page<MaterialCreditTicketDocumentDto> findAllApprovedForAccountSettingPaged(String query, Pageable pageable) {

        Page<MaterialCreditTicket> materialCreditTickets;

        if(query != null){
            materialCreditTickets = materialCreditTicketRepo.findAllByQueryForAccountSetting("%"+query+"%", com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        } else {
            materialCreditTickets = materialCreditTicketRepo.findAllForAccountSetting(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        }

        return materialCreditTickets.map(entity -> {

            MaterialCreditTicketDocumentDto dto = new MaterialCreditTicketDocumentDto();

            dto.setVoucherDate(entity.getVoucherDate());
            dto.setLocalCode(entity.getCode());
            dto.setParticulars(entity.getRemarks());
            dto.setId(entity.getId());
            dto.setPreparedBy(entity.getCreatedBy().getFullName());
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
}
