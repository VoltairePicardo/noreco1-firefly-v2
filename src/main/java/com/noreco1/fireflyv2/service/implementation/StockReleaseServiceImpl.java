package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.controller.form.StockReleaseForm;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.model.enums.StockReleaseType;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.StockReleaseService;
import com.noreco1.fireflyv2.service.strategy.inventory_document.InventoryDocumentReleasingStrategyRegistry;
import com.noreco1.fireflyv2.validator.StockReleaseValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by lenovo on 5/4/2017.
 */
@Service(value = "stockReleaseServiceImpl")
public class StockReleaseServiceImpl implements StockReleaseService, PrintableVoucher {

    private StockRelease model;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    StockWithdrawalRepo stockWithdrawalRepo;

    @Autowired
    StockTransferRepo stockTransferRepo;

    @Autowired
    StockWithdrawalDetailRepo stockWithdrawalDetailRepo;

    @Autowired
    StockReleaseRepo stockReleaseRepo;

    @Autowired
    DepartmentRepo departmentRepo;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    UserRepo userRepo;

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
    ItemStockDetailRepo itemStockDetailRepo;

    @Autowired
    StockTransactionRepo stockTransactionRepo;

    @Autowired
    StockTransactionDetailRepo stockTransactionDetailRepo;

    @Autowired
    ItemTransactionDetailRepo itemTransactionDetailRepo;

    @Autowired
    SpecialEquipmentRepo specialEquipmentRepo;

    @Autowired
    StockTransactionDetailSerialNoRepo stockTransactionDetailSerialNoRepo;

    @Autowired
    ItemRepo itemRepo;

    private Map reportMeta;

    @Autowired
    MemorandumReceiptRepo memorandumReceiptRepo;

    @Autowired
    InventoryDocumentReleasingStrategyRegistry releasingStrategyRegistry;

    @Override
    @Transactional(readOnly = true)
    public StockRelease findById(Integer id) {

        StockRelease ret = stockReleaseRepo.findById(id).orElse(null);

        try {

            ArrayList<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(ret.getTransaction().getId());
            StockWithdrawal stockWithdrawal = stockWithdrawalRepo.findOneByTransactionId(ret.getDocumentTransaction().getId());

            ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
            if(stockWithdrawal != null) {
                ArrayList<StockWithdrawalDetail> widDetails = stockWithdrawalDetailRepo.findByStockWithdrawalId(stockWithdrawal.getId());
                for (StockTransactionDetail d : details) {
                    ItemTransactionDetailDto dto = new ItemTransactionDetailDto();
                    dto.setItemId(d.getItemStock().getItem().getId());
                    dto.setItemCode(d.getItemStock().getItem().getCode());
                    dto.setUnitId(d.getItemStock().getItem().getUnit().getId());
                    dto.setUnitCode(d.getItemStock().getItem().getUnit().getCode());
                    dto.setItemDescription(d.getItemStock().getItem().getDescription());
                    for (StockWithdrawalDetail swd : widDetails) {
                        if (d.getItemStock().getItem().getId().equals(swd.getItem().getId())) {
                            dto.setQuantityOrdered(swd.getQuantity());
                            break;
                        }
                    }
                    dto.setQuantityReleased(d.getQuantity());
                    dto.setUnitCost(d.getUnitCost());
                    dto.setTotalCost(d.getTotalCost());

                    List<StockTransactionDetailSerialNo> serialNumbers = stockTransactionDetailSerialNoRepo.findAllByStockTransactionDetailId(d.getId());
                    List<SpecialEquipment> specialEquipments = new ArrayList<>();
                    if(!serialNumbers.isEmpty()){
                        for(StockTransactionDetailSerialNo detail : serialNumbers){
                            SpecialEquipment specialEquipment = new SpecialEquipment();

                            if(detail.getSpecialEquipment() != null){
                                specialEquipment = detail.getSpecialEquipment();
                            } else {
                                specialEquipment.setSerialNo(detail.getSerialNo());
                            }

                            specialEquipments.add(specialEquipment);

                        }
                    }


                    dto.setSerialNumbers(specialEquipments);

                    detailsDto.add(dto);

                }

            } else {

                ArrayList<ItemTransactionDetail> transDetails = itemTransactionDetailRepo.findByTransactionId(ret.getTransaction().getId());

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

                    dto.setQuantityReleased(d.getQuantity());
                    dto.setUnitCost(d.getUnitCost());
                    dto.setTotalCost(d.getTotalCost());
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
    @Transactional(readOnly = true)
    public StockRelease findByCode(String code) {

        List<StockRelease> releases = stockReleaseRepo.findByCode(code);

        if (!Checker.collectionIsEmpty(releases)) {
            return releases.get(0);
        } else return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockRelease> findAll() {
        return stockReleaseRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockRelease> findAll(Pageable pageable) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockRelease> findByQuery(String query, Pageable pageable) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map> getDetails(int id) {
        List<Map> data = new ArrayList<>();

        StockRelease release = stockReleaseRepo.findById(id).orElse(null);
        if (release != null) {

            List<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(release.getTransaction().getId());

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
                    detailMap.put("stockTransactionDetailId", detail.getId());

                    data.add(detailMap);
                }
            }
        }

        return data;
    }

    @Override
    @Transactional(readOnly = true)
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

            List<StockRelease> docs = stockReleaseRepo.findByAllowedUserVoucherDateBetweenAndDocumentStatusIdAndOfficeId(loggedIn.getId(), fromDate, toDate, docStatusId);
            return this.makeReleasingMapList(docs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
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


            List<StockRelease> docs = stockReleaseRepo.findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(loggedIn.getId(), fromDate, toDate, Arrays.asList(ids));
            return this.makeReleasingMapList(docs);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockWithdrawal> findStockWithdrawalByDocumentStatusId(Integer documentStatusId) {
        return stockReleaseRepo.findStockWithdrawalByDocumentStatusId(documentStatusId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map> getAvailableItemStock(Integer itemId) {

        List<Map> ret = new ArrayList<>();

        Employee employee = employeeRepo.findOneByAccountNumber(authenticationFacade.getLoggedIn().getAccountNo());

        Integer invLocId = employee.getOffice().getInventoryLocation().getId();
        Integer departmentId = employee.getDepartment().getId();

        List<Object[]> availableStocks = stockReleaseRepo.getAvailableItemStock(itemId, invLocId, departmentId);
        for (Object[] stock : availableStocks) {

            Map data = new HashMap();
            data.put("quantity", stock[1]);

            ret.add(data);
        }

        return ret;
    }

    @Override
    @Transactional
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> filesToRemove) {
        return this.processUpdate(v, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request) {
        return this.processCreate(v, bindingResult, messageSource);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentStatus> getDocumentsStatuses() {
        return documentDtoer.getDocumentStatuses(com.noreco1.fireflyv2.model.enums.Workflow.RELEASING_OFE.getId());
    }

    @Override
    @Transactional
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        StockRelease stockReleaseRepoOne = stockReleaseRepo.findById(postData.getDocumentId()).orElse(null);

        if (stockReleaseRepoOne != null) {
            // for logging
            List<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(stockReleaseRepoOne.getTransaction().getId());
            ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
            for (StockTransactionDetail d : details) {
                detailsDto.add(d.toDto());
            }
            stockReleaseRepoOne.setDetails(detailsDto);

            Map oldMap = this.forLogMapMain(stockReleaseRepoOne);
            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(stockReleaseRepoOne, stockReleaseRepoOne.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            stockReleaseRepoOne.setDocumentStatus(afterActionDocumentStatus);
            stockReleaseRepoOne.setUpdatedAt(null);
            stockReleaseRepoOne = stockReleaseRepo.save(stockReleaseRepoOne);

            // for logging
            Map newMap = this.forLogMapMain(stockReleaseRepoOne);
            newMap.put("remarks", postData.getRemarks());

            if (stockReleaseRepoOne != null) {
                documentProcessingFacade.processAction(stockReleaseRepoOne.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(stockReleaseRepoOne.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, newMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }

        }
        return response;
    }

    @Override
    @Transactional
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        return null;
    }

    @Override
    @Transactional
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        return null;
    }

    @Override
    @Transactional
    public PostResponse processUpdate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource) {
        StockRelease release = (StockRelease) v;
        return this.processCreate(release, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource) {

        StockRelease stockRelease = (StockRelease) v;

        PostResponse response = new PostResponse();
        response.setSuccess(false);

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        StockReleaseValidator validator = new StockReleaseValidator();
        validator.setService(this);
        validator.setItemRepo(itemRepo);
        validator.setSpecialEquipmentRepo(specialEquipmentRepo);
        validator.validate(stockRelease, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            StockRelease existingRelease = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(stockRelease.getVoucherDate()));

            User receivedBy = userRepo.findOneByAccountNo(stockRelease.getReceivedBy().getAccountNo());
            Employee loggedInEmployee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());

            Boolean insertMode = stockRelease.getId() == null;
            if (insertMode) { // insert mode
                String typeCode = "";
                StockReleaseType type = StockReleaseType.typeFromInt(stockRelease.getType());
                switch (type) {
                    case MCT:
                        typeCode = StockReleaseType.MCT.getCode();
                        break;
                    case OFE:
                        typeCode = StockReleaseType.OFE.getCode();
                        break;
                    case OSSP:
                        typeCode = StockReleaseType.OSSP.getCode();
                        break;
                    case STRL:
                        typeCode = StockReleaseType.STRL.getCode();
                        break;
                }
//                String offAcro = stockRelease.getOffice().getAcronym();
                Object latestCanvassCode = stockReleaseRepo.findLatestCodeByYear(voucherYear, stockRelease.getType());
                stockRelease.setCode(generatorFacade.voucherCodeNoOffice(typeCode, (latestCanvassCode == null ? "" : String.valueOf(latestCanvassCode)), stockRelease.getVoucherDate(), GlobalConstant.COUNTER_PAD_4));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());

                Workflow wf = new Workflow();
                if(typeCode.equals(StockReleaseType.MCT.getCode()) || typeCode.equals(StockReleaseType.STRL.getCode())) {
                    wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.RELEASING.getId());
                } else {
                    wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.RELEASING_OFE.getId());
                }
                stockRelease.setDocumentStatus(documentStatus);
                stockRelease.setYear(voucherYear);
                stockRelease.setTransaction(generatorFacade.transaction());
                stockRelease.setWorkflow(wf);
                stockRelease.setCreatedBy(createdBy);
                existingRelease = stockRelease;
            } else {
                existingRelease = stockReleaseRepo.findById(stockRelease.getId()).orElse(null);
            }
            // use for document logging
            Map oldMap = this.forLogMapMain(existingRelease);

            existingRelease.setDocumentTransaction(stockRelease.getDocumentTransaction());
            existingRelease.setInventoryLocation(loggedInEmployee.getOffice().getInventoryLocation());
            existingRelease.setVoucherDate(stockRelease.getVoucherDate());
            existingRelease.setDescription(stockRelease.getDescription());
            existingRelease.setYear(voucherYear);
            existingRelease.setType(stockRelease.getType());
            existingRelease.setReceivedBy(receivedBy);
            existingRelease.setAuditor(null);
            existingRelease.setOffice(loggedInEmployee.getOffice());

            existingRelease.setUpdatedAt(new Date());
            this.model = stockReleaseRepo.save(existingRelease);

            if (this.model != null) {

                // start: update default signatories
                signatoryFacade.sr(this.model);
                // end: update default signatories

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldMap = null;
                }

                ArrayList<ItemTransactionDetailDto> details = stockRelease.getDetails();

                //save to stockTrans tables
                if (!Checker.collectionIsEmpty(details)) {

                    InventoryLocation inventoryLocation = model.getInventoryLocation();

                    StockTransaction stockTransaction = new StockTransaction();
                    stockTransaction.setTransaction(model.getTransaction());
                    stockTransaction.setCreatedBy(model.getCreatedBy());

                    stockTransaction = stockTransactionRepo.save(stockTransaction);

                    for (ItemTransactionDetailDto itemTransactionDetailDto : details) {

                        if(itemTransactionDetailDto.getReleaseQuantity() != null && itemTransactionDetailDto.getReleaseQuantity().compareTo(BigDecimal.ZERO) == 1) {

                            ItemStock itemStock = itemStockRepo.findFirstByItemIdAndInventoryLocationIdOrderByIdAsc(itemTransactionDetailDto.getItemId(), inventoryLocation.getId());

                            BigDecimal receivedCost = itemTransactionDetailDto.getReleaseQuantity().multiply(itemStock.getTotalItemCost().divide(itemStock.getTotalQuantity(), BigDecimal.ROUND_HALF_UP));
                            BigDecimal totalQty = itemStock.getTotalQuantity().subtract(itemTransactionDetailDto.getReleaseQuantity());
                            BigDecimal totalCost = itemStock.getTotalItemCost().subtract(receivedCost);

                            itemStock.setTotalQuantity(totalQty);
                            itemStock.setTotalItemCost(totalCost);

                            ItemStock newItemStock = itemStockRepo.save(itemStock);

                            BigDecimal totalRelease = BigDecimal.ZERO;
                            BigDecimal quantityPerItemStock = BigDecimal.ZERO;
                            BigDecimal unitCostPerItemStock = BigDecimal.ZERO;

                            if(Checker.isValidId(newItemStock.getId())){

                                List<ItemStockDetail> itemStockDetails = itemStockDetailRepo.findAllByItemStockIdAndQuantityGreaterThanOrderByIdAsc(newItemStock.getId(), BigDecimal.ZERO);

                                if(Checker.collectionIsNotEmpty(itemStockDetails)){

                                    BigDecimal itemStockDiff = BigDecimal.ZERO;

                                    for (ItemStockDetail itemStockDetail : itemStockDetails){

                                        if(itemStockDiff.compareTo(BigDecimal.ZERO) == 1){
                                            itemTransactionDetailDto.setReleaseQuantity(itemStockDiff);
                                        }

                                        BigDecimal quantity = BigDecimal.ZERO;
                                        BigDecimal itemCost = BigDecimal.ZERO;

                                        Boolean isQuantityIsGreaterThanEqualZero = itemStockDetail.getQuantity().compareTo(itemTransactionDetailDto.getReleaseQuantity()) == 1 || itemStockDetail.getQuantity().compareTo(itemTransactionDetailDto.getReleaseQuantity()) == 0;

                                        if(isQuantityIsGreaterThanEqualZero){
                                            quantity = itemStockDetail.getQuantity().subtract(itemTransactionDetailDto.getReleaseQuantity());
                                            itemCost = itemStockDetail.getItemCost().subtract(itemStockDetail.getUnitCost().multiply(itemTransactionDetailDto.getReleaseQuantity()));
                                            totalRelease = totalRelease.add(itemTransactionDetailDto.getReleaseQuantity());
                                            quantityPerItemStock = itemTransactionDetailDto.getReleaseQuantity();
                                            unitCostPerItemStock = itemStockDetail.getUnitCost();
                                        } else {
                                            itemStockDiff = itemTransactionDetailDto.getReleaseQuantity().subtract(itemStockDetail.getQuantity());
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
                                            stockTransactionDetail.setType(2); // 1 = in, 2 = out

                                            stockTransactionDetailRepo.save(stockTransactionDetail);

                                            //Save Serial Numbers
                                            if(!itemTransactionDetailDto.getSerialNumbers().isEmpty()){

                                                for(SpecialEquipment specialEquipment : itemTransactionDetailDto.getSerialNumbers()){

                                                    if(!Checker.isStringNullOrEmpty(specialEquipment.getSerialNo())){

                                                        StockTransactionDetailSerialNo stockTransactionDetailSerialNo = new StockTransactionDetailSerialNo();

                                                        SpecialEquipment existingSpecialEquipment = specialEquipmentRepo.findBySerialNo(specialEquipment.getSerialNo());

                                                        if(existingSpecialEquipment != null){
                                                            stockTransactionDetailSerialNo.setSpecialEquipment(existingSpecialEquipment);
                                                        }

                                                        stockTransactionDetailSerialNo.setSerialNo(specialEquipment.getSerialNo());
                                                        stockTransactionDetailSerialNo.setStockTransactionDetail(stockTransactionDetail);

                                                        stockTransactionDetailSerialNoRepo.save(stockTransactionDetailSerialNo);

                                                    }

                                                }

                                            }

                                            if(isQuantityIsGreaterThanEqualZero){
                                                break;
                                            }

                                        }

                                    }

                                }

                            }

                            //update withdrawal details
                            if (stockRelease.getDocumentType() == 1 || stockRelease.getDocumentType() == 3) {

                                StockWithdrawal stockWithdrawal = stockRelease.getDocumentType() == 3 ? memorandumReceiptRepo.findByTransactionId(model.getDocumentTransaction().getId()).getStockWithdrawal() : stockWithdrawalRepo.findOneByTransactionId(model.getDocumentTransaction().getId());
                                List<StockWithdrawalDetail> stockWithdrawalDetails = stockWithdrawalDetailRepo.findByStockWithdrawalId(stockWithdrawal.getId());

                                for (StockWithdrawalDetail stockWithdrawalDetail : stockWithdrawalDetails) {

                                    if (stockWithdrawalDetail.getItem().getId().equals(itemTransactionDetailDto.getItemId())) {
                                        stockWithdrawalDetail.setQuantityReleased(stockWithdrawalDetail.getQuantityReleased().add(totalRelease));
                                        stockWithdrawalDetailRepo.save(stockWithdrawalDetail);
                                        break;

                                    }

                                }

                            } else {

                                List<ItemTransactionDetail> stockTransferDetails = itemTransactionDetailRepo.findByTransactionId(model.getDocumentTransaction().getId());

                                for (ItemTransactionDetail itemTransactionDetail : stockTransferDetails) {

                                    if (itemTransactionDetail.getItem().getId().equals(itemTransactionDetailDto.getItemId())) {
                                        itemTransactionDetail.setQuantityReleased(itemTransactionDetail.getQuantityReleased().add(itemTransactionDetailDto.getReleaseQuantity()));
                                        itemTransactionDetailRepo.save(itemTransactionDetail);
                                        break;
                                    }

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
                response.setSuccessMessage("Stock Release successfully saved!");
                response.setSuccess(true);

            }

        }

        if (Checker.documentSaved(response)) {
            this.logNewValue(response.getLogId());
        }

        return response;

    }

    @Override
    @Transactional(readOnly = true)
    public List<StockRelease> getListForSummaryReport(String from, String to, HttpServletRequest request) {
        List<StockRelease> list = new ArrayList<>();

        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            String type = request.getParameter("type");
            String location = request.getParameter("location");
            String status = request.getParameter("status");

            if(!Checker.isStringNullAndEmpty(type) && !Checker.isStringNullAndEmpty(location) && !Checker.isStringNullAndEmpty(status)) {
                Integer typeInt = Integer.parseInt(type);
                Integer locationInt = Integer.parseInt(location);
                Integer statusInt = Integer.parseInt(status);
                list = stockReleaseRepo.findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusIdAndTypeOrderByTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, locationInt, statusInt, typeInt);
            } else if(!Checker.isStringNullAndEmpty(type) && !Checker.isStringNullAndEmpty(location)) {
                Integer typeInt = Integer.parseInt(type);
                Integer locationInt = Integer.parseInt(location);
                list = stockReleaseRepo.findByVoucherDateBetweenAndInventoryLocationIdAndTypeOrderByTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, locationInt, typeInt);
            } else if(!Checker.isStringNullAndEmpty(type) && !Checker.isStringNullAndEmpty(status)) {
                Integer typeInt = Integer.parseInt(type);
                Integer statusInt = Integer.parseInt(status);
                list = stockReleaseRepo.findByVoucherDateBetweenAndDocumentStatusIdAndTypeOrderByTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, statusInt, typeInt);
            } else if(!Checker.isStringNullAndEmpty(location) && !Checker.isStringNullAndEmpty(status)) {
                Integer locationInt = Integer.parseInt(location);
                Integer statusInt = Integer.parseInt(status);
                list = stockReleaseRepo.findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusIdOrderByTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, locationInt, statusInt);
            } else if(!Checker.isStringNullAndEmpty(location)) {
                Integer locationInt = Integer.parseInt(location);
                list = stockReleaseRepo.findByVoucherDateBetweenAndInventoryLocationIdOrderByTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, locationInt);
            } else if(!Checker.isStringNullAndEmpty(status)) {
                Integer statusInt = Integer.parseInt(status);
                list = stockReleaseRepo.findByVoucherDateBetweenAndDocumentStatusIdOrderByTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, statusInt);
            }  else if(!Checker.isStringNullAndEmpty(type)) {
                Integer typeInt = Integer.parseInt(type);
                list = stockReleaseRepo.findByVoucherDateBetweenAndTypeOrderByTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, typeInt);
            } else {
                list = stockReleaseRepo.findByVoucherDateBetweenOrderByTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockWithdrawalDetailDto> getItems(Integer docTransId) {
        List<StockWithdrawalDetailDto> data = new ArrayList<>();
        ArrayList<StockWithdrawalDetail> details = stockWithdrawalDetailRepo.findByStockWithdrawalTransactionId(docTransId);

        if(!details.isEmpty()) {
            for (StockWithdrawalDetail detail : details) {

                StockWithdrawalDetailDto detailDto = new StockWithdrawalDetailDto();

                detailDto.setItemId(detail.getItem().getId());
                detailDto.setUnitCode(detail.getUnit().getCode());
                detailDto.setItemCode(detail.getItem().getCode());
                detailDto.setItemDescription(detail.getItem().getDescription());
                detailDto.setQuantity(detail.getQuantity());
                detailDto.setQuantityReleased(detail.getQuantityReleased());

                data.add(detailDto);
            }
        } else {
            ArrayList<ItemTransactionDetail> transDetails = itemTransactionDetailRepo.findByTransactionId(docTransId);
            for (ItemTransactionDetail detail : transDetails) {

                StockWithdrawalDetailDto detailDto = new StockWithdrawalDetailDto();

                detailDto.setItemId(detail.getItem().getId());
                detailDto.setUnitCode(detail.getItem().getUnit().getCode());
                detailDto.setItemCode(detail.getItem().getCode());
                detailDto.setItemDescription(detail.getItem().getDescription());
                detailDto.setQuantity(detail.getQuantity());
                detailDto.setQuantityReleased(detail.getQuantityReleased());

                data.add(detailDto);
            }
        }

        return data;
    }

    @Override
    @Transactional
    public PostResponse processCreateMultiple(StockReleaseForm v, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        response.setSuccess(false);
        ArrayList<Integer> modelIds = new ArrayList<>();
        DocumentLog log = null;
        for (StockRelease stockRelease : v.getDocuments()) {
            MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

            StockReleaseValidator validator = new StockReleaseValidator();
            validator.setService(this);
            validator.validate(stockRelease, bindingResult);

            if (bindingResult.hasErrors()) {

                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();

            } else {

                User createdBy = authenticationFacade.getLoggedIn();
                StockRelease existingRelease = null;

                Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(stockRelease.getVoucherDate()));

                User receivedBy = userRepo.findOneByAccountNo(stockRelease.getReceivedBy().getAccountNo());
                Employee loggedInEmployee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());

                Boolean insertMode = stockRelease.getId() == null;
                if (insertMode) { // insert mode
                    String typeCode = "";
                    StockReleaseType type = StockReleaseType.typeFromInt(stockRelease.getType());
                    switch (type) {
                        case MCT:
                            typeCode = StockReleaseType.MCT.getCode();
                            break;
                        case OFE:
                            typeCode = StockReleaseType.OFE.getCode();
                            break;
                        case OSSP:
                            typeCode = StockReleaseType.OSSP.getCode();
                            break;
                        case STRL:
                            typeCode = StockReleaseType.STRL.getCode();
                            break;
                    }
//                    String offAcro = stockRelease.getOffice().getAcronym();
                    Object latestCanvassCode = stockReleaseRepo.findLatestCodeByYear(voucherYear, stockRelease.getType());
                    stockRelease.setCode(generatorFacade.voucherCodeWithMonth(typeCode, (latestCanvassCode == null ? "" : String.valueOf(latestCanvassCode)), stockRelease.getVoucherDate()));

                    DocumentStatus documentStatus = new DocumentStatus();
                    documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                    stockRelease.setDocumentStatus(documentStatus);

                    Workflow wf = new Workflow();
                    if(typeCode.equals(StockReleaseType.MCT.getCode()) || typeCode.equals(StockReleaseType.STRL.getCode())) {
                        wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.RELEASING.getId());
                    } else {
                        wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.RELEASING_OFE.getId());
                    }
                    stockRelease.setYear(voucherYear);
                    stockRelease.setTransaction(generatorFacade.transaction());
                    stockRelease.setWorkflow(wf);
                    stockRelease.setCreatedBy(createdBy);
                    existingRelease = stockRelease;
                } else {
                    existingRelease = stockReleaseRepo.findById(stockRelease.getId()).orElse(null);
                }
                // use for document logging
                Map oldMap = this.forLogMapMain(existingRelease);

                existingRelease.setDocumentTransaction(stockRelease.getDocumentTransaction());
                existingRelease.setInventoryLocation(stockRelease.getInventoryLocation());
                existingRelease.setVoucherDate(stockRelease.getVoucherDate());
                existingRelease.setDescription(stockRelease.getDescription());
                existingRelease.setYear(voucherYear);
                existingRelease.setType(stockRelease.getType());
                existingRelease.setReceivedBy(receivedBy);
                existingRelease.setAuditor(null);
                existingRelease.setOffice(loggedInEmployee.getOffice());

                existingRelease.setUpdatedAt(new Date());
                this.model = stockReleaseRepo.save(existingRelease);

                if (this.model != null) {

                    // start: update default signatories
                    signatoryFacade.sr(this.model);
                    // end: update default signatories

                    if (insertMode) { // log action only when adding document
                        documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                        oldMap = null;
                    }

                    ArrayList<ItemTransactionDetailDto> details = stockRelease.getDetails();
                    //save to stockTrans tables
                    if (!Checker.collectionIsEmpty(details)) {
                        InventoryLocation inventoryLocation = model.getInventoryLocation();
                        StockTransaction stockTransaction = new StockTransaction();
                        stockTransaction.setTransaction(model.getTransaction());
                        stockTransaction.setCreatedBy(model.getCreatedBy());
                        stockTransaction = stockTransactionRepo.save(stockTransaction);
                        for (ItemTransactionDetailDto itemTransactionDetailDto : details) {
                            if(itemTransactionDetailDto.getReleaseQuantity() != null && itemTransactionDetailDto.getReleaseQuantity().compareTo(BigDecimal.ZERO) == 1) {
                                ItemStock itemStock = itemStockRepo.findByItemIdAndInventoryLocationId(itemTransactionDetailDto.getItemId(), inventoryLocation.getId());
                                //receive cost = rec. qty * netUnitPrice
                                BigDecimal receivedCost = itemTransactionDetailDto.getReleaseQuantity().multiply(itemStock.getUnitCost());
                                BigDecimal totalQty = itemStock.getQuantity().subtract(itemTransactionDetailDto.getReleaseQuantity());
                                BigDecimal totalCost = itemStock.getUnitCost().multiply(itemStock.getQuantity()).subtract(receivedCost);
                                BigDecimal averageAmount = totalQty.compareTo(BigDecimal.ZERO) == 1 ? totalCost.divide(totalQty, BigDecimal.ROUND_HALF_UP) : itemStock.getUnitCost();
                                itemStock.setQuantity(totalQty);
                                itemStock.setUnitCost(averageAmount);
                                ItemStock newItemStock = itemStockRepo.save(itemStock);
                                if (newItemStock != null) {
                                    StockTransactionDetail stockTransactionDetail = new StockTransactionDetail();
                                    stockTransactionDetail.setQuantity(itemTransactionDetailDto.getReleaseQuantity());
                                    stockTransactionDetail.setUnitCost(itemStock.getUnitCost());
                                    stockTransactionDetail.setTotalCost(stockTransactionDetail.getUnitCost().multiply(stockTransactionDetail.getQuantity()));
                                    stockTransactionDetail.setVat(BigDecimal.ZERO);
                                    stockTransactionDetail.setStockTransaction(stockTransaction);
                                    stockTransactionDetail.setItemStock(newItemStock);
                                    stockTransactionDetail.setInventoryLocation(inventoryLocation);
                                    stockTransactionDetail.setItemStockBalance(newItemStock.getQuantity());
                                    stockTransactionDetail.setItemStockAmountBalance(newItemStock.getQuantity().multiply(newItemStock.getUnitCost()));
                                    stockTransactionDetail.setType(2); // 1 = in, 2 = out
                                    stockTransactionDetailRepo.save(stockTransactionDetail);
                                }
                                //update withdrawal details
                                if (stockRelease.getDocumentType() == 1) {
                                    StockWithdrawal stockWithdrawal = stockWithdrawalRepo.findOneByTransactionId(model.getDocumentTransaction().getId());
                                    List<StockWithdrawalDetail> stockWithdrawalDetails = stockWithdrawalDetailRepo.findByStockWithdrawalId(stockWithdrawal.getId());
                                    for (StockWithdrawalDetail stockWithdrawalDetail : stockWithdrawalDetails) {
                                        if (stockWithdrawalDetail.getItem().getId().equals(itemTransactionDetailDto.getItemId())) {
                                            stockWithdrawalDetail.setQuantityReleased(stockWithdrawalDetail.getQuantityReleased().add(itemTransactionDetailDto.getReleaseQuantity()));
                                            stockWithdrawalDetailRepo.save(stockWithdrawalDetail);
                                            break;
                                        }
                                    }
                                } else {
                                    List<ItemTransactionDetail> stockTransferDetails = itemTransactionDetailRepo.findByTransactionId(model.getDocumentTransaction().getId());
                                    for (ItemTransactionDetail itemTransactionDetail : stockTransferDetails) {
                                        if (itemTransactionDetail.getItem().getId().equals(itemTransactionDetailDto.getItemId())) {
                                            itemTransactionDetail.setQuantityReleased(itemTransactionDetail.getQuantityReleased().add(itemTransactionDetailDto.getReleaseQuantity()));
                                            itemTransactionDetailRepo.save(itemTransactionDetail);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // generic document logging here
                    // old value only
                    log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, null);
                    modelIds.add(model.getId());
                }
            }
        }
        if(!modelIds.isEmpty()) {
            response.setLogId(log != null ? log.getId() : 0);
            response.setModelIds(modelIds);
            response.setSuccessMessage("Stock Release successfully saved!");
            response.setSuccess(true);
        } else {
            response.setFailureMessage("No Stock Release Document created!");
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockRelease> findByDateRangeAndCodeAndType(String from, String to, Integer type, String query, Pageable pageable) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        if(query != null){
            return stockReleaseRepo.findAllByVoucherDateBetweenAndCodeContainingIgnoreCaseAndTypeOrderByVoucherDateAscCodeAsc(start, end, query, type, pageable);
        } else {
            return stockReleaseRepo.findAllByVoucherDateBetweenAndTypeOrderByVoucherDateAscCodeAsc(start, end, type, pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getStockReleasePaged(String from, String to, Integer statusId, String query, Pageable pageable) {
        User loggedIn = authenticationFacade.getLoggedIn();
        Integer[] ids = {
                com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(),
                com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId(),
                com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId()
        };

        return stockReleaseRepo.getStockReleasePaged(from, to, statusId, query, loggedIn.getId(), Arrays.asList(ids), pageable);
    }

    @Override
    @Transactional
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);
        if (documentLog != null) {
            Integer transId = documentLog.getTransaction().getId();
            StockRelease doc = stockReleaseRepo.findOneByTransactionId(transId);

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
    @Transactional(readOnly = true)
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(DocumentType.SRL);
    }

    @Override
    @Transactional(readOnly = true)
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {

        this.reportMeta = new HashMap();
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        StockRelease stockRelease = stockReleaseRepo.findById(vid).orElse(null);

        if (stockRelease != null) {

            List<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(stockRelease.getTransaction().getId());
            BigDecimal totalQty = BigDecimal.ZERO;
            for (StockTransactionDetail d : details) {
                totalQty = totalQty.add(d.getQuantity());
            }
            String department = "";
            String location = "";
            String docTransNo = "";
            String woNo = "";
            Integer invLoc = 0;
            if(stockRelease.getType() != StockReleaseType.STRL.getId()){
                StockWithdrawal stockWithdrawal = stockWithdrawalRepo.findOneByTransactionId(stockRelease.getDocumentTransaction().getId());
                department = stockWithdrawal.getDepartment().getName();
                location = stockRelease.getInventoryLocation().getDescription();
                docTransNo = stockWithdrawal.getCode();
                woNo = stockWithdrawal.getWorkOrder() != null ? stockWithdrawal.getWorkOrder().getCode() : "";
                invLoc = null;
            } else {
                StockTransfer stockTransfer = stockTransferRepo.findOneByTransactionId(stockRelease.getDocumentTransaction().getId());
                department = stockTransfer.getFromInventoryLocation().getDescription();
                docTransNo = stockTransfer.getCode();
                invLoc = stockRelease.getInventoryLocation().getId();
            }
            String title = "";
            String additionalNote = "."; //End note with a period by default
            StockReleaseType type = StockReleaseType.typeFromInt(stockRelease.getType());
            switch (type) {
                case MCT:
                    title = StockReleaseType.MCT.getDescription();
                    break;
                case OFE:
                    title = StockReleaseType.OFE.getDescription();
                    break;
                case OSSP:
                    title = StockReleaseType.OSSP.getDescription();
                    additionalNote = " and that these above items have been received in complete quantity and in good condition.";
                    break;
                case STRL:
                    title = StockReleaseType.STRL.getDescription();
                    break;
            }

            params.put("VOUCHER_NO", stockRelease.getCode());
            params.put("REF_NO", docTransNo);
            params.put("DEPARTMENT", department);
            params.put("V_DATE", stockRelease.getVoucherDate());
            params.put("INV_LOCATION", invLoc);
            params.put("LOCATION", location);
            params.put("DESCRIPTION", stockRelease.getDescription());
            params.put("TOTAL_QTY", totalQty);
            params.put("TITLE", title);
            params.put("TYPE", stockRelease.getType());
            params.put("WO_NO", woNo);
            params.put("ADDITIONAL_NOTE", additionalNote);
            params.put("SUBREPORT_DIR", GlobalConstant.JASPER_BASE_PATH + "/vouchers/sub_reports/");

            params = signatureFacade.getDocumentSignature(params, stockRelease.getType() == 2 || stockRelease.getType() == 3 ? DocumentType.SRL_OFE_OSSP : DocumentType.SRL, stockRelease);
        }

        return params;
    }

    @Override
    @Transactional(readOnly = true)
    public JRDataSource datasource(Integer vid) {
        List<Map> rrDetails = this.getDetails(vid);

        if(!rrDetails.isEmpty()) {

            List<Map> itemLines = new ArrayList<>();

            int counter = 1;
            for(Map detail: rrDetails) {
                Integer stockTransactionDetailId = (Integer)detail.get("stockTransactionDetailId");
                List<StockTransactionDetailSerialNo> serialNos = this.stockTransactionDetailSerialNoRepo.findAllByStockTransactionDetailId(stockTransactionDetailId);

                if(Checker.collectionIsNotEmpty(serialNos)) {

                    Map line = new HashMap();
                    line.put("code", detail.get("code"));
                    line.put("name", detail.get("description"));
                    line.put("quantity", detail.get("quantity"));
                    line.put("unit", detail.get("unitCode"));

                    for(StockTransactionDetailSerialNo serialNo: serialNos) {
                        line.put("count", counter++);
                        line.put("serial", serialNo.getSerialNo());
                        itemLines.add(line);

                        line = new HashMap(); // reset line
                    }
                }
            }

            this.reportMeta.put("SERIALS", new JRBeanCollectionDataSource(itemLines));
        }

        return new JRBeanCollectionDataSource(rrDetails);
    }

    @Override
    public Map getReportMeta() {
        return this.reportMeta;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockReleaseDocumentDto> findAllApprovedForAccountSettingPaged(String query, Pageable pageable) {

        Page<StockRelease> stockReleases;

        if(query != null){
            stockReleases = stockReleaseRepo.findAllByQueryForAccountSetting("%"+query+"%", com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        } else {
            stockReleases = stockReleaseRepo.findAllForAccountSetting(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        }

        return stockReleases.map(entity -> {

            StockReleaseDocumentDto dto = new StockReleaseDocumentDto();

            dto.setVoucherDate(entity.getVoucherDate());
            dto.setLocalCode(entity.getCode());
            dto.setParticulars(entity.getDescription());
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
    @Transactional(readOnly = true)
    public Optional<Page<InventoryDocumentDto>> findInventoryDocumentsForReleasing(String type, String query, Pageable pageable) {
        if (type == null) {
            return Optional.empty();
        }
        return releasingStrategyRegistry.get(type)
                .map(strategy -> strategy.findAllForReleasingByQuery(query, pageable));
    }

    private Map forLogMapMain(StockRelease sr) {
        return documentLoggerFacade.makeLog(sr);
    }

    private List<Map> makeReleasingMapList(List<StockRelease> cs) {

        List<Map> mapList = new ArrayList<>();

        if (!Checker.collectionIsEmpty(cs)) {
            for (StockRelease c : cs) {
                mapList.add(composeMap(c));
            }
        }
        return mapList;
    }

    private Map composeMap(StockRelease r) {
        Map map = new HashMap();

        map.put("id", r.getId());
        map.put("code", r.getCode());
        map.put("voucherDate", r.getVoucherDate());
        map.put("description", r.getDescription());
        map.put("createdBy", r.getCreatedBy());
        map.put("receivedBy", r.getReceivedBy());
        map.put("documentStatus", r.getDocumentStatus());

        return map;
    }

}
