package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.InventoryDocumentDto;
import com.noreco1.fireflyv2.controller.response.ItemTransactionDetailDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.StockTransferService;
import com.noreco1.fireflyv2.validator.StockTransferValidator;
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

@Service(value = "stockTransferServiceImpl")
public class StockTransferServiceImpl implements StockTransferService, PrintableVoucher {

    private StockTransfer model;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    StockTransferRepo stockTransferRepo;

    @Autowired
    ItemTransactionDetailRepo itemTransactionDetailRepo;

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

    @Override
    public StockTransfer findById(Integer id) {

        StockTransfer ret = stockTransferRepo.findById(id).orElse(null);
        ArrayList<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(ret.getTransaction().getId());

        ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
        for(ItemTransactionDetail d : details){
            detailsDto.add(d.toMSTDto());
        }

        ret.setDetails(detailsDto);

        return ret;
    }

    @Override
    public List<StockTransfer> findAll() {
        return stockTransferRepo.findAll();
    }

    @Override
    public Page<StockTransfer> findAll(Pageable pageable) {
        return stockTransferRepo.findAll(pageable);
    }

    @Override
    public Page<StockTransfer> findByQuery(String query, Pageable pageable) {
        return null;
    }

    @Override
    public List<Map> getDetails(Integer id) {
        List<Map> data = new ArrayList<>();

        StockTransfer stockTransfer = stockTransferRepo.findById(id).orElse(null);
        if (stockTransfer != null) {

            List<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(stockTransfer.getTransaction().getId());

            if(! details.isEmpty() ) {

                int counter = 1;
                for (ItemTransactionDetail detail:details) {

                    Map detailMap = new HashMap();

                    detailMap.put("id", counter++);
                    detailMap.put("itemId", detail.getItem().getId());
                    detailMap.put("itemCode", detail.getItem().getCode());
                    detailMap.put("description", detail.getItem().getDescription());
                    detailMap.put("quantity", detail.getQuantity());
                    detailMap.put("unitId", detail.getItem().getUnit().getId());
                    detailMap.put("unitCode", detail.getItem().getUnit().getCode());
                    detailMap.put("quantityReleased", detail.getQuantity());
                    detailMap.put("inventoryCategoryId", detail.getItem().getInventoryCategory().getId());
                    detailMap.put("unitCost", detail.getUnitCost());
                    detailMap.put("totalCost", detail.getTotalCost());

                    data.add(detailMap);
                }
            }
        }

        return data;
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

            List<StockTransfer> docs = stockTransferRepo.findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(loggedIn.getId(), fromDate, toDate, docStatusId);
            return this.makeStockTransferListMap(docs);
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

            List<StockTransfer> docs = stockTransferRepo.findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(loggedIn.getId(), fromDate, toDate, Arrays.asList(ids));
            return this.makeStockTransferListMap(docs);

        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<DocumentStatus> getDocumentsStatuses() {
        return documentDtoer.getDocumentStatuses(com.noreco1.fireflyv2.model.enums.Workflow.STOCK_TRANSFER.getId());
    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        StockTransfer stockStockTransferRepoOne =  stockTransferRepo.findById(postData.getDocumentId()).orElse(null);

        if (stockStockTransferRepoOne != null) {
            // for logging
            List<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(stockStockTransferRepoOne.getTransaction().getId());
            ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
            for(ItemTransactionDetail d : details){
                detailsDto.add(d.toDto());
            }
            stockStockTransferRepoOne.setDetails(detailsDto);

            Map oldMap = this.forLogMapMain(stockStockTransferRepoOne);
            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(stockStockTransferRepoOne, stockStockTransferRepoOne.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            stockStockTransferRepoOne.setDocumentStatus(afterActionDocumentStatus);
            stockStockTransferRepoOne.setUpdatedAt(null);
            stockStockTransferRepoOne = stockTransferRepo.save(stockStockTransferRepoOne);

            // for logging
            Map newMap = this.forLogMapMain(stockStockTransferRepoOne);
            newMap.put("remarks", postData.getRemarks());

            if (stockStockTransferRepoOne != null) {
                documentProcessingFacade.processAction(stockStockTransferRepoOne.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(stockStockTransferRepoOne.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, newMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }

        }
        return response;
    }

    @Override
    public Page<InventoryDocumentDto> findAllForReleasingByQuery(String query, Pageable pageable) {
        Page<StockTransfer> stockTransfers;

        Employee employee = employeeRepo.findOneByAccountNumber(authenticationFacade.getLoggedIn().getAccountNo());

        Integer invLocId = employee.getOffice().getInventoryLocation().getId();

        if(query != null){
            stockTransfers = stockTransferRepo.findAllByCodeContainingIgnoreCaseOrRemarksContainingIgnoreCaseAndDocumentStatusAndIdNotIn("%"+query.toUpperCase()+"%", com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), invLocId, pageable);

            return stockTransfers.map(entity -> {
                    return toDto(entity, 1);
            });
        } else {
            stockTransfers = stockTransferRepo.findAllByDocumentStatusAndIdNotInAndFromInventoryLocationId(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), invLocId, pageable);

            return stockTransfers.map(entity -> {
                    return toDto(entity, 1);
            });
        }
    }

    @Override
    public Page<InventoryDocumentDto> findAllForReleasing(Pageable pageable) {
        Page<StockTransfer> stockTransfers;
        stockTransfers = stockTransferRepo.findAllByDocumentStatusAndIdNotIn(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);

        return stockTransfers.map(entity -> {
                return toDto(entity, 1);
        });
    }

    @Override
    public Page<InventoryDocumentDto> findAllForReceivingByQuery(String query, Integer invLocId, Pageable pageable) {
        Page<StockTransfer> stockTransfers;
        if(query != null) {
            stockTransfers = stockTransferRepo.findForReceivingByQuery("%"+query+"%", invLocId, com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        } else {
            stockTransfers = stockTransferRepo.findForReceiving(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), invLocId, pageable);
        }

        return stockTransfers.map(entity -> {
                return toDto(entity,2);
        });
    }

    @Override
    public List<StockTransfer> getListForSummaryReport(String from, String to, HttpServletRequest request) {
        List<StockTransfer> list = new ArrayList<>();
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
                list = stockTransferRepo.findByVoucherDateBetweenAndFromInventoryLocationIdAndDocumentStatusIdOrderByFromInventoryLocationIdAsc(fromDate, toDate, locationInt, statusInt);
            } else if(!Checker.isStringNullAndEmpty(location)) {
                Integer locationInt = Integer.parseInt(location);
                list = stockTransferRepo.findByVoucherDateBetweenAndFromInventoryLocationIdOrderByFromInventoryLocationIdAsc(fromDate, toDate, locationInt);
            } else if(!Checker.isStringNullAndEmpty(status)) {
                Integer statusInt = Integer.parseInt(status);
                list = stockTransferRepo.findByVoucherDateBetweenAndDocumentStatusIdOrderByFromInventoryLocationIdAsc(fromDate, toDate, statusInt);
            } else {
                list = stockTransferRepo.findByVoucherDateBetween(fromDate, toDate);
            }

            if(!Checker.collectionIsEmpty(list)){
                for(StockTransfer l : list){
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
                detailDto.setQuantity(detail.getQuantity());
                detailDto.setUnitCost(detail.getUnitCost());
                detailDto.setTotalCost(detail.getTotalCost());

                data.add(detailDto);
            }
        }

        return data;
    }

    private InventoryDocumentDto toDto(StockTransfer entity, Integer type) {
        InventoryDocumentDto dto = new InventoryDocumentDto();
        System.out.println(entity.getTransaction().getId());
        ArrayList<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(entity.getTransaction().getId());

        ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
        for (ItemTransactionDetail d : details) {
            if(type == 1){ //1 for releasing 2 = receiving
                detailsDto.add(d.toReleaseDto());
            } else {
                detailsDto.add(d.toReceiveDto());
            }
        }

        dto.setDate(entity.getVoucherDate());
        dto.setCode(entity.getCode());
        dto.setPurpose(entity.getRemarks());
        dto.setDetails(detailsDto);
        dto.setTransId(entity.getTransaction().getId());
        dto.setCreatedBy(entity.getCreatedBy().getFullName());
        dto.setCreatedByUser(entity.getCreatedBy());

        return dto;
    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        StockTransfer stockTransfer = (StockTransfer)v;
        return this.processCreate(stockTransfer, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {

        StockTransfer stockTransfer = (StockTransfer)v;

        PostResponse response = new PostResponse();
        response.setSuccess(false);

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        StockTransferValidator validator = new StockTransferValidator();
        validator.setService(this);
        validator.validate(stockTransfer, bindingResult);

        if(bindingResult.hasErrors()){

            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();

        } else{

            User createdBy = authenticationFacade.getLoggedIn();
            StockTransfer existingStockTransfer = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(stockTransfer.getVoucherDate()));

            User approvedBy = userRepo.findOneByAccountNo(stockTransfer.getApprovingOfficer().getAccountNo());
            Employee loggedInEmployee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());

            Boolean insertMode = stockTransfer.getId() == null;
            if (insertMode) { // insert mode
//                String offAcro = stockTransfer.getOffice().getAcronym();
                Object latestCanvassCode = stockTransferRepo.findLatestCodeByYear(voucherYear);
                stockTransfer.setCode(generatorFacade.voucherCodeNoOffice("ST", (latestCanvassCode == null ? "" : String.valueOf(latestCanvassCode)), stockTransfer.getVoucherDate(), GlobalConstant.COUNTER_PAD_4));
                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                stockTransfer.setDocumentStatus(documentStatus);

                Workflow wf = new Workflow();
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.STOCK_TRANSFER.getId());

                stockTransfer.setYear(voucherYear);
                stockTransfer.setTransaction(generatorFacade.transaction());
                stockTransfer.setWorkflow(wf);
                stockTransfer.setCreatedBy(createdBy);
                existingStockTransfer = stockTransfer;
            } else {
                existingStockTransfer = stockTransferRepo.findById(stockTransfer.getId()).orElse(null);
            }
            // use for document logging
            Map oldMap = this.forLogMapMain(existingStockTransfer);

            existingStockTransfer.setVoucherDate(stockTransfer.getVoucherDate());
            existingStockTransfer.setRemarks(stockTransfer.getRemarks());
            existingStockTransfer.setFromInventoryLocation(stockTransfer.getFromInventoryLocation());
            existingStockTransfer.setToInventoryLocation(stockTransfer.getToInventoryLocation());
            existingStockTransfer.setYear(voucherYear);
            existingStockTransfer.setApprovingOfficer(approvedBy);
            existingStockTransfer.setOffice(loggedInEmployee.getOffice());

            existingStockTransfer.setUpdatedAt(new Date());
            this.model = stockTransferRepo.save(existingStockTransfer);

            if (this.model != null) {

                // start: update default signatories
                signatoryFacade.st(this.model);
                // end: update default signatories

                if (!insertMode) {
                    itemTransactionDetailRepo.deleteByTransactionId(existingStockTransfer.getTransaction().getId());
                }

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldMap = null;
                }

                ArrayList<ItemTransactionDetailDto> details = stockTransfer.getDetails();
                for(ItemTransactionDetailDto detailDto: details) {
                    ItemStock itemStock = itemStockRepo.findById(detailDto.getItemStockId()).orElse(null);
                    ItemTransactionDetail detail = new ItemTransactionDetail();

                    detail.setTransaction(this.model.getTransaction());

                    //TODO: Set values for details
                    detail.setItem(itemStock.getItem());
                    detail.setQuantity(detailDto.getQuantity());
                    detail.setUnitCost(itemStock.getTotalItemCost().divide(itemStock.getTotalQuantity(), BigDecimal.ROUND_HALF_UP));
                    detail.setTotalCost(detail.getQuantity().multiply(detail.getUnitCost()));
                    detail.setQuantityReleased(BigDecimal.ZERO);
                    detail.setQuantityReceived(BigDecimal.ZERO);

                    detail.setItemStock(itemStock);

                    detail.setInventoryLocation(itemStock.getInventoryLocation());
                    detail.setAdjustment(detailDto.getQuantity().multiply(new BigDecimal(-1)));

                    if (!detailDto.getQuantity().equals(BigDecimal.ZERO)) {
                        ItemTransactionDetail newDetail = itemTransactionDetailRepo.save(detail);
                    }
                }
                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());
                response.setSuccessMessage("Stock Transfer successfully saved!");
                response.setSuccess(true);
            }
        }

        return response;
    }

    @Override
    public void logNewValue(Integer logId) {

        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);
        if (documentLog != null) {
            StockTransfer doc = stockTransferRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (doc != null) {

                List<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(doc.getTransaction().getId());
                ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
                for(ItemTransactionDetail d : details){
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
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.ST);
    }

    @Override
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {

        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        StockTransfer stockTransfer = stockTransferRepo.findById(vid).orElse(null);

        if (stockTransfer != null) {

            List<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(stockTransfer.getTransaction().getId());
            BigDecimal totalQty = BigDecimal.ZERO;
            for(ItemTransactionDetail d : details){
                totalQty = totalQty.add(d.getQuantity());
            }

            params.put("VOUCHER_NO", stockTransfer.getCode());
            params.put("REMARKS", stockTransfer.getRemarks());
            params.put("V_DATE", stockTransfer.getVoucherDate());
            params.put("FROM_OFFICE", stockTransfer.getFromInventoryLocation().getDescription());
            params.put("TO_OFFICE", stockTransfer.getToInventoryLocation().getDescription());
            params.put("TOTAL_QTY", totalQty);

            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.ST, stockTransfer);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer vid) {

        List<Map> rrDetails = this.getDetails(vid);
        return new JRBeanCollectionDataSource(rrDetails);
    }

    private Map forLogMapMain(StockTransfer stockTransfer) {
        return documentLoggerFacade.makeLog(stockTransfer);
    }

    private List<Map> makeStockTransferListMap(List<StockTransfer> cs ) {

        List<Map> mapList = new ArrayList<>();

        if (!Checker.collectionIsEmpty(cs)) {
            for(StockTransfer c:cs) {
                mapList.add(composeStockTransferMap(c));
            }
        }
        return mapList;
    }

    private Map composeStockTransferMap(StockTransfer w) {
        Map map = new HashMap();

        map.put("id", w.getId());
        map.put("code", w.getCode());
        map.put("voucherDate", w.getVoucherDate());
        map.put("fromOffice", w.getFromInventoryLocation().getDescription());
        map.put("toOffice", w.getToInventoryLocation().getDescription());
        map.put("remarks", w.getRemarks());
        map.put("createdBy", w.getCreatedBy());
        map.put("approvingOfficer", w.getApprovingOfficer());
        map.put("documentStatus", w.getDocumentStatus());

        return map;
    }
}

