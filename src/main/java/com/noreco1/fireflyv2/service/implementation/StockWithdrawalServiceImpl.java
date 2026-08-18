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
import com.noreco1.fireflyv2.model.enums.InventoryCategory;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.StockWithdrawalService;
import com.noreco1.fireflyv2.mysql_model.TurnOnOrderWithdrawal;
import com.noreco1.fireflyv2.mysql_repo.TurnOnOrderWithdrawalRepo;
import com.noreco1.fireflyv2.validator.WithdrawalValidator;
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
@Service(value = "withdrawalServiceImpl")
public class StockWithdrawalServiceImpl implements StockWithdrawalService, PrintableVoucher {

    private StockWithdrawal model;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    StockWithdrawalRepo stockWithdrawalRepo;

    @Autowired
    StockWithdrawalDetailRepo stockWithdrawalDetailRepo;

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
    SpecialEquipmentWithdrawalDetailRepo specialEquipmentWithdrawalDetailRepo;

    @Autowired
    StockWithdrawalSpecialEquipmentDetailRepo stockWithdrawalSpecialEquipmentDetailRepo;

    @Autowired
    TurnOnOrderWithdrawalRepo turnOnOrderWithdrawalRepo;

    @Autowired
    PurchaseRequestDetailRepo PurchaseRequestDetailRepo;

    @Autowired
    StockWithdrawalEmployeeRepo stockWithdrawalEmployeeRepo;

    @Override
    public StockWithdrawal findById(Integer id) {

        StockWithdrawal ret = stockWithdrawalRepo.findById(id).orElse(null);
        ArrayList<StockWithdrawalDetail> details = stockWithdrawalDetailRepo.findByStockWithdrawalId(id);
        ArrayList<StockWithdrawalEmployee> stockWithdrawalEmployees = stockWithdrawalEmployeeRepo.findAllByStockWithdrawalId(id);

        ArrayList<StockWithdrawalDetailDto> detailsDto = new ArrayList<>();
        for (StockWithdrawalDetail d : details) {
            StockWithdrawalDetailDto dto = d.toDto();
            if (ret.getPurchaseRequest() != null) {
                List<Object[]> rows = PurchaseRequestDetailRepo.findForWithdrawalUpdate(ret.getPurchaseRequest().getId(), d.getItem().getId());

                if (!Checker.collectionIsEmpty(rows)) {
                    Object[] row = rows.get(0);
                    dto.setInventoryBalance(new BigDecimal(row[0]+""));
                    dto.setItemStockId(Integer.parseInt(row[1]+""));
                    dto.setRvBalance(new BigDecimal(row[10]+"").add(d.getQuantity()));
                }
            }

            detailsDto.add(dto);
        }

        ret.setDetails(detailsDto);

        ArrayList<EmployeeDto> employeeDtos = new ArrayList<>();

        for (StockWithdrawalEmployee stockWithdrawalEmployee : stockWithdrawalEmployees){

            EmployeeDto dto = new EmployeeDto();

            dto.setName(stockWithdrawalEmployee.getEmployee().getName());
            dto.setAccountNo(stockWithdrawalEmployee.getEmployee().getAccountNumber());

            employeeDtos.add(dto);

        }

        ret.setEmployees(employeeDtos);

        TurnOnOrderWithdrawalDto turnOnOrderWithdrawalDto = new TurnOnOrderWithdrawalDto();
        if (Checker.isValidId(ret.getTurnOnOrderWithdrawalId())) {
            TurnOnOrderWithdrawal toWithdrawal = turnOnOrderWithdrawalRepo.findById(ret.getTurnOnOrderWithdrawalId()).orElse(null);

            if (toWithdrawal != null) {
                turnOnOrderWithdrawalDto.setId(toWithdrawal.getId());
                turnOnOrderWithdrawalDto.setDate(toWithdrawal.getDate());
                turnOnOrderWithdrawalDto.setTotalTurnOnOrders(turnOnOrderWithdrawalRepo.getTotalTurnOnOrderByTurnOnOrderWithdrawalId(toWithdrawal.getId()));
            }
        }
        ret.setTurnOnOrderWithdrawal(turnOnOrderWithdrawalDto);

        return ret;
    }

    @Override
    public StockWithdrawal findByCode(String code) {

        List<StockWithdrawal> withdrawals = stockWithdrawalRepo.findByCode(code);

        if (!Checker.collectionIsEmpty(withdrawals)) {
            return withdrawals.get(0);
        } else return null;
    }

    @Override
    @Transactional
    public PostResponse create(Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        StockWithdrawal sw = new StockWithdrawal();
        Object dateObj = payload.get("voucherDate");
        if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
            Date voucherDate = java.sql.Date.valueOf(dateStr);
            sw.setVoucherDate(voucherDate);
            int year = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(voucherDate));
            sw.setYear(year);
            sw.setType(1);
            Object latestCode = stockWithdrawalRepo.findLatestCodeByYear(year, 1);
            String code = generatorFacade.voucherCodeNoOffice("MRS",
                    latestCode == null ? "" : String.valueOf(latestCode),
                    voucherDate, GlobalConstant.COUNTER_PAD_4);
            sw.setCode(code);
        }
        sw.setDescription(payload.get("description") != null ? String.valueOf(payload.get("description")) : null);
        Date now = new Date();
        sw.setCreatedAt(now);
        sw.setUpdatedAt(now);
        sw.setCreatedBy(authenticationFacade.getLoggedIn());
        DocumentStatus ds = new DocumentStatus();
        ds.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
        sw.setDocumentStatus(ds);
        Workflow wf = new Workflow();
        wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.WITHDRAWAL.getId());
        sw.setWorkflow(wf);
        sw.setTransaction(generatorFacade.transaction());
        StockWithdrawal saved = stockWithdrawalRepo.save(sw);
        response.setSuccessMessage("Stock Withdrawal saved.");
        response.setModelId(saved.getId());
        return response;
    }

    @Override
    @Transactional
    public PostResponse update(Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        Object idObj = payload.get("id");
        if (idObj == null) {
            response.setFailureMessage("ID is required.");
            return response;
        }
        Integer id = ((Number) idObj).intValue();
        StockWithdrawal sw = stockWithdrawalRepo.findById(id).orElse(null);
        if (sw == null) {
            response.setFailureMessage("Record not found.");
            return response;
        }
        Object dateObj = payload.get("voucherDate");
        if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
            sw.setVoucherDate(java.sql.Date.valueOf(dateStr));
        }
        sw.setDescription(payload.get("description") != null ? String.valueOf(payload.get("description")) : null);
        sw.setUpdatedAt(new Date());
        StockWithdrawal saved = stockWithdrawalRepo.save(sw);
        response.setSuccessMessage("Stock Withdrawal updated.");
        response.setModelId(saved.getId());
        return response;
    }

    @Override
    public List<StockWithdrawal> findAll() {
        return stockWithdrawalRepo.findAll();
    }

    @Override
    public Page<StockWithdrawal> findAll(Pageable pageable) {
        return stockWithdrawalRepo.findAll(pageable);
    }

    @Override
    public Page<StockWithdrawal> findByQuery(String query, Pageable pageable) {
        return null;
    }

    @Override
    public List<Map> getDetails(int id) {
        List<Map> data = new ArrayList<>();

        StockWithdrawal withdrawal = stockWithdrawalRepo.findById(id).orElse(null);
        if (withdrawal != null) {

            List<StockWithdrawalDetail> details = stockWithdrawalDetailRepo.findByStockWithdrawalId(withdrawal.getId());

            if (!details.isEmpty()) {

                int counter = 1;
                for (StockWithdrawalDetail detail : details) {

                    Map detailMap = new HashMap();

                    detailMap.put("id", counter++);
                    detailMap.put("itemId", detail.getItem().getId());
                    detailMap.put("itemCode", detail.getItem().getCode());
                    detailMap.put("description", detail.getItem().getDescription());
                    detailMap.put("quantity", detail.getQuantity());
                    detailMap.put("unitId", detail.getUnit().getId());
                    detailMap.put("unitCode", detail.getUnit().getCode());
                    detailMap.put("quantityReleased", detail.getQuantityReleased());
                    detailMap.put("inventoryCategoryId", detail.getItem().getInventoryCategory().getId());

                    data.add(detailMap);
                }
            }
        }

        return data;
    }

    @Override
    public Page<Map<String, Object>> getStockWithdrawalPaged(String from, String to, Integer statusId, String query, Pageable pageable) {
        User loggedIn = authenticationFacade.getLoggedIn();
        Integer[] ids = {
                com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(),
                com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId(),
                com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId(),
        };

        return stockWithdrawalRepo.getStockWithdrawalPaged(from, to, statusId, query, loggedIn.getId(), Arrays.asList(ids), pageable);
    }

    @Override
    public Page<InventoryDocumentDto> findAllForReleasingByQuery(String query, Pageable pageable) {
        Page<StockWithdrawal> stockWithdrawals;

        Employee employee = employeeRepo.findOneByAccountNumber(authenticationFacade.getLoggedIn().getAccountNo());

        Integer invLocId = employee.getOffice().getInventoryLocation().getId();

        if (query != null) {
            stockWithdrawals = stockWithdrawalRepo.findAllByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndDocumentStatusAndIdNotIn("%" + query.toUpperCase() + "%", com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), invLocId, pageable);
        } else {
            stockWithdrawals = stockWithdrawalRepo.findAllByDocumentStatusAndIdNotIn(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), invLocId, pageable);
        }

        return stockWithdrawals.map(entity -> {
                InventoryDocumentDto dto = new InventoryDocumentDto();
                ArrayList<StockWithdrawalDetail> details = stockWithdrawalDetailRepo.findByStockWithdrawalId(entity.getId());

                ArrayList<StockWithdrawalDetailDto> detailsDto = new ArrayList<>();
                for (StockWithdrawalDetail d : details) {
                    detailsDto.add(d.toDto());
                }

                dto.setDate(entity.getVoucherDate());
                dto.setCode(entity.getCode());
                dto.setPurpose(Checker.isStringNullOrEmpty(entity.getDescription()) ? entity.getPurpose().getDescription() : entity.getDescription());
                dto.setWithdrawalDetails(detailsDto);
                dto.setTransId(entity.getTransaction().getId());
                dto.setCreatedBy(entity.getCreatedBy().getFullName());

                User user = new User();
                user.setId(entity.getCreatedBy().getId());
                user.setFullName(entity.getCreatedBy().getFullName());
                user.setAccountNo(entity.getCreatedBy().getAccountNo());

                dto.setCreatedByUser(user);

                dto.setDepartmentName(entity.getDepartment().getName());
                dto.setInventoryCategoryTypeId(entity.getInventoryCategory().getType());

                return dto;
        });
    }

    @Override
    public List<StockWithdrawal> getListForSummaryReport(String from, String to, HttpServletRequest request) {
        List<StockWithdrawal> list = new ArrayList<>();

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

            if (!Checker.isStringNullAndEmpty(type) && !Checker.isStringNullAndEmpty(location) && !Checker.isStringNullAndEmpty(status)) {
                Integer typeInt = Integer.parseInt(type);
                Integer locationInt = Integer.parseInt(location);
                Integer statusInt = Integer.parseInt(status);
                list = stockWithdrawalRepo.findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusIdAndInventoryCategoryTypeOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, locationInt, statusInt, typeInt);
            } else if (!Checker.isStringNullAndEmpty(type) && !Checker.isStringNullAndEmpty(location)) {
                Integer typeInt = Integer.parseInt(type);
                Integer locationInt = Integer.parseInt(location);
                list = stockWithdrawalRepo.findByVoucherDateBetweenAndInventoryLocationIdAndInventoryCategoryTypeOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, locationInt, typeInt);
            } else if (!Checker.isStringNullAndEmpty(type) && !Checker.isStringNullAndEmpty(status)) {
                Integer typeInt = Integer.parseInt(type);
                Integer statusInt = Integer.parseInt(status);
                list = stockWithdrawalRepo.findByVoucherDateBetweenAndDocumentStatusIdAndInventoryCategoryTypeOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, statusInt, typeInt);
            } else if (!Checker.isStringNullAndEmpty(location) && !Checker.isStringNullAndEmpty(status)) {
                Integer locationInt = Integer.parseInt(location);
                Integer statusInt = Integer.parseInt(status);
                list = stockWithdrawalRepo.findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusIdOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, locationInt, statusInt);
            } else if (!Checker.isStringNullAndEmpty(location)) {
                Integer locationInt = Integer.parseInt(location);
                list = stockWithdrawalRepo.findByVoucherDateBetweenAndInventoryLocationIdOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, locationInt);
            } else if (!Checker.isStringNullAndEmpty(status)) {
                Integer statusInt = Integer.parseInt(status);
                list = stockWithdrawalRepo.findByVoucherDateBetweenAndDocumentStatusIdOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, statusInt);
            } else if (!Checker.isStringNullAndEmpty(type)) {
                Integer typeInt = Integer.parseInt(type);
                list = stockWithdrawalRepo.findByVoucherDateBetweenAndInventoryCategoryTypeOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, typeInt);
            } else {
                list = stockWithdrawalRepo.findByVoucherDateBetweenOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public List<StockWithdrawalDetailDto> getItems(Integer withdrawalId) {
        List<StockWithdrawalDetailDto> data = new ArrayList<>();
        ArrayList<StockWithdrawalDetail> details = stockWithdrawalDetailRepo.findByStockWithdrawalId(withdrawalId);

        if (!details.isEmpty()) {
            for (StockWithdrawalDetail detail : details) {

                StockWithdrawalDetailDto detailDto = new StockWithdrawalDetailDto();

                detailDto.setItemId(detail.getItem().getId());
                detailDto.setUnitCode(detail.getUnit().getCode());
                detailDto.setItemCode(detail.getItem().getCode());
                detailDto.setItemDescription(detail.getItem().getDescription());
                detailDto.setQuantity(detail.getQuantity());
                detailDto.setQuantityReleased(detail.getQuantityReleased());
                detailDto.setIsSpecialEquipment(detail.getIsSpecialEquipment());

                data.add(detailDto);
            }
        }

        return data;
    }

    @Override
    public Page<Object[]> findAllSpecialEquipmentsForWithdrawal(String query, Integer inventoryLocationId, Integer inventoryCategoryId, Pageable pageable) {

        if (Checker.isStringNullOrEmpty(query)) {
            return specialEquipmentWithdrawalDetailRepo.findAllForStockWithdrawal(inventoryLocationId, inventoryCategoryId, pageable);
        } else {
            return specialEquipmentWithdrawalDetailRepo.findAllForStockWithdrawalByQuery("%" + query.trim() + "%", inventoryLocationId, inventoryCategoryId, pageable);

        }
    }

    @Override
    public Page<Object[]> findAllTurnOnOrderForWithdrawalPaged(String startDate, String endDate, Pageable pageable) {
        Page<Object[]> turnOnOrdersForWithdrawal;
        turnOnOrdersForWithdrawal = turnOnOrderWithdrawalRepo.findAllForWithdrawal(startDate, endDate, pageable);
        return turnOnOrdersForWithdrawal;
    }

    @Override
    public Page<StockWithdrawal> findAllForSpecialEquipmentAssignment(Pageable pageable) {
        return stockWithdrawalRepo.findAllForSpecialEquipmentAssignment(InventoryCategory.SPECIAL_EQUIPMENT.getId(), pageable);
    }

    @Override
    public Page<StockWithdrawal> findAllByQueryForSpecialEquipmentAssignment(String query, Pageable pageable) {
        query = "%" + query + "%";
        return stockWithdrawalRepo.findAllForSpecialEquipmentAssignmentByQuery(query.trim(), InventoryCategory.SPECIAL_EQUIPMENT.getId(), pageable);
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

        StockWithdrawal po = stockWithdrawalRepo.findFirstByOrderByIdAsc();
        if (po != null) {
            return documentDtoer.getDocumentStatuses(po.getWorkflow().getId());
        }

        return null;
    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        StockWithdrawal stockWithdrawalRepoOne = stockWithdrawalRepo.findById(postData.getDocumentId()).orElse(null);

        if (stockWithdrawalRepoOne != null) {
            // for logging
            List<StockWithdrawalDetail> details = stockWithdrawalDetailRepo.findByStockWithdrawalId(stockWithdrawalRepoOne.getId());
            ArrayList<StockWithdrawalDetailDto> detailsDto = new ArrayList<>();
            for (StockWithdrawalDetail d : details) {
                detailsDto.add(d.toDto());
            }
            stockWithdrawalRepoOne.setDetails(detailsDto);

            Map oldMap = this.forLogMapMain(stockWithdrawalRepoOne);
            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(stockWithdrawalRepoOne, stockWithdrawalRepoOne.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            stockWithdrawalRepoOne.setDocumentStatus(afterActionDocumentStatus);
            stockWithdrawalRepoOne.setUpdatedAt(null);
            stockWithdrawalRepoOne = stockWithdrawalRepo.save(stockWithdrawalRepoOne);

            // for logging
            Map newMap = this.forLogMapMain(stockWithdrawalRepoOne);
            newMap.put("remarks", postData.getRemarks());

            if (stockWithdrawalRepoOne != null) {
                documentProcessingFacade.processAction(stockWithdrawalRepoOne.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(stockWithdrawalRepoOne.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, newMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }

        }
        return response;
    }

    @Override
    @Transactional
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        StockWithdrawal withdrawal = (StockWithdrawal) v;
        return this.processCreate(withdrawal, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {

        StockWithdrawal withdrawal = (StockWithdrawal) v;

        PostResponse response = new PostResponse();
        response.setSuccess(false);

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        WithdrawalValidator validator = new WithdrawalValidator();
        validator.setService(this);
        validator.validate(withdrawal, bindingResult);

        if (bindingResult.hasErrors()) {

            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();

        } else {

            User createdBy = authenticationFacade.getLoggedIn();
            StockWithdrawal existingWithdrawal = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(withdrawal.getVoucherDate()));

            User approvedBy = userRepo.findOneByAccountNo(withdrawal.getApprovingOfficer().getAccountNo());
            Employee loggedInEmployee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());

            Boolean insertMode = withdrawal.getId() == null;
            if (insertMode) { // insert mode
                String typeCode = "";
                StockWithdrawalType type = StockWithdrawalType.typeFromInt(withdrawal.getType());
                switch (type) {
                    case MRS:
                        typeCode = StockWithdrawalType.MRS.getCode();
                        break;
                    case OSSPRS:
                        typeCode = StockWithdrawalType.OSSPRS.getCode();
                        break;
                    case OFTERS:
                        typeCode = StockWithdrawalType.OFTERS.getCode();
                        break;
                }
//                String offAcro = withdrawal.getOffice().getAcronym();
                Object latestCanvassCode = stockWithdrawalRepo.findLatestCodeByYear(voucherYear, withdrawal.getType());
                withdrawal.setCode(generatorFacade.voucherCodeNoOffice(typeCode, (latestCanvassCode == null ? "" : String.valueOf(latestCanvassCode)), withdrawal.getVoucherDate(), GlobalConstant.COUNTER_PAD_4));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                withdrawal.setDocumentStatus(documentStatus);

                Workflow wf = new Workflow();
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.WITHDRAWAL.getId());

                withdrawal.setYear(voucherYear);
                withdrawal.setTransaction(generatorFacade.transaction());
                withdrawal.setWorkflow(wf);
                withdrawal.setCreatedBy(createdBy);
                existingWithdrawal = withdrawal;
            } else {
                existingWithdrawal = stockWithdrawalRepo.findById(withdrawal.getId()).orElse(null);
            }
            // use for document logging
            Map oldMap = this.forLogMapMain(existingWithdrawal);

            existingWithdrawal.setCode(withdrawal.getCode());
            existingWithdrawal.setDepartment(loggedInEmployee.getDepartment());
            existingWithdrawal.setInventoryLocation(withdrawal.getInventoryLocation());
            existingWithdrawal.setInventoryCategory(withdrawal.getInventoryCategory());
            existingWithdrawal.setPurpose(withdrawal.getPurpose());
            existingWithdrawal.setVoucherDate(withdrawal.getVoucherDate());
            existingWithdrawal.setDescription(withdrawal.getDescription());
            existingWithdrawal.setYear(voucherYear);
            existingWithdrawal.setType(withdrawal.getType());
            existingWithdrawal.setApprovingOfficer(approvedBy);
            existingWithdrawal.setOffice(loggedInEmployee.getOffice());
            existingWithdrawal.setPurchaseRequest(withdrawal.getPurchaseRequest());
            existingWithdrawal.setEmployees(withdrawal.getEmployees());
            existingWithdrawal.setUpdatedAt(new Date());
            existingWithdrawal.setCostEstimate(withdrawal.getCostEstimate());

            this.model = stockWithdrawalRepo.save(existingWithdrawal);

            if (this.model != null) {

                // start: update default signatories
                signatoryFacade.sw(this.model);
                // end: update default signatories

                if (!insertMode) {
                    stockWithdrawalDetailRepo.deleteByStockWithdrawalId(existingWithdrawal.getId());
                    stockWithdrawalSpecialEquipmentDetailRepo.deleteAllByStockWithdrawalId(existingWithdrawal.getId());
                }

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldMap = null;
                }

                ArrayList<StockWithdrawalDetailDto> details = withdrawal.getDetails();
                for (StockWithdrawalDetailDto withdrawalDetailLine : details) {

                    StockWithdrawalDetail detail = new StockWithdrawalDetail();

                    StockWithdrawal sw1 = new StockWithdrawal();
                    sw1.setId(this.model.getId());
                    detail.setStockWithdrawal(sw1);

                    //TODO: Set values for details
                    Item item = new Item();
                    item.setId(withdrawalDetailLine.getItemId());
                    detail.setItem(item);

                    UnitMeasure unit = new UnitMeasure();
                    unit.setId(withdrawalDetailLine.getUnitId());
                    detail.setUnit(unit);

                    detail.setQuantity(withdrawalDetailLine.getQuantity());
                    detail.setQuantityReleased(withdrawalDetailLine.getQuantityReleased());
                    detail.setIsSpecialEquipment(withdrawalDetailLine.isSpecialEquipment());

                    if (!withdrawalDetailLine.getQuantity().equals(BigDecimal.ZERO)) {
                        StockWithdrawalDetail newDetail = stockWithdrawalDetailRepo.save(detail);

                        if (this.model.getPurchaseRequest() != null && newDetail != null) {
                            PurchaseRequestDetail purchaseRequestDetail = PurchaseRequestDetailRepo.findByPurchaseRequestIdAndItemId(this.model.getPurchaseRequest().getId(), withdrawalDetailLine.getItemId());
                            if (purchaseRequestDetail != null) {
                                if (insertMode) {
                                    purchaseRequestDetail.setWithdrawQuantity(purchaseRequestDetail.getWithdrawQuantity().add(newDetail.getQuantity()));
                                } else {
                                    purchaseRequestDetail.setWithdrawQuantity(purchaseRequestDetail.getWithdrawQuantity().subtract(withdrawalDetailLine.getInsertedQuantity()).add(newDetail.getQuantity()));
                                }
                                PurchaseRequestDetailRepo.save(purchaseRequestDetail);
                            }
                        }
                    }

                    if (withdrawalDetailLine.isSpecialEquipment()) {

                        List<SpecialEquipmentWithdrawalDetail> withdrawalSpecialEquipmentDetails = specialEquipmentWithdrawalDetailRepo.findAllByItemAndInventoryLocationAndInventoryCategory(withdrawalDetailLine.getItemId(), withdrawal.getInventoryLocation().getId(), withdrawal.getInventoryCategory().getId());
                        for (SpecialEquipmentWithdrawalDetail sewDetail : withdrawalSpecialEquipmentDetails) {
                            StockWithdrawalSpecialEquipmentDetail withdrawalSpecialEquipmentDetail = new StockWithdrawalSpecialEquipmentDetail();
                            withdrawalSpecialEquipmentDetail.setStockWithdrawal(sw1);
                            withdrawalSpecialEquipmentDetail.setSpecialEquipmentWithdrawalDetail(sewDetail);
                            stockWithdrawalSpecialEquipmentDetailRepo.save(withdrawalSpecialEquipmentDetail);
                        }

                    }

                }

                if(Checker.collectionIsNotEmpty(this.model.getEmployees())){

                    stockWithdrawalEmployeeRepo.deleteAllByStockWithdrawalId(this.model.getId());

                    for (EmployeeDto employee : withdrawal.getEmployees()){

                        StockWithdrawalEmployee stockWithdrawalEmployee = new StockWithdrawalEmployee();

                        stockWithdrawalEmployee.setStockWithdrawal(this.model);

                        Employee existingEmployee = employeeRepo.findOneByAccountNumber(employee.getAccountNo());
                        stockWithdrawalEmployee.setEmployee(existingEmployee);

                        stockWithdrawalEmployeeRepo.save(stockWithdrawalEmployee);

                    }

                }

                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());
                response.setSuccessMessage("Stock Withdrawal successfully saved!");
                response.setSuccess(true);
            }
        }

        return response;
    }

    @Override
    public void logNewValue(Integer logId) {

        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);
        if (documentLog != null) {
            StockWithdrawal doc = stockWithdrawalRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (doc != null) {

                List<StockWithdrawalDetail> details = stockWithdrawalDetailRepo.findByStockWithdrawalId(doc.getId());
                ArrayList<StockWithdrawalDetailDto> detailsDto = new ArrayList<>();
                for (StockWithdrawalDetail d : details) {
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
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.SW);
    }

    @Override
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {

        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        StockWithdrawal withdrawal = stockWithdrawalRepo.findById(vid).orElse(null);

        if (withdrawal != null) {

            List<StockWithdrawalDetail> details = stockWithdrawalDetailRepo.findByStockWithdrawalId(withdrawal.getId());
            BigDecimal totalQty = BigDecimal.ZERO;
            for (StockWithdrawalDetail d : details) {
                totalQty = totalQty.add(d.getQuantity());
            }

            String title = "";
            StockWithdrawalType type = StockWithdrawalType.typeFromInt(withdrawal.getType());
            switch (type) {
                case MRS:
                    title = StockWithdrawalType.MRS.getDescription();
                    break;
                case OSSPRS:
                    title = StockWithdrawalType.OSSPRS.getDescription();
                    break;
                case OFTERS:
                    title = StockWithdrawalType.OFTERS.getDescription();
                    break;
            }

            Department department = departmentRepo.findById(withdrawal.getDepartment().getId()).orElse(null);

            params.put("VOUCHER_NO", withdrawal.getCode());
            params.put("PURPOSE", withdrawal.getDescription());
            params.put("V_DATE", withdrawal.getVoucherDate());
            params.put("DEPARTMENT", department.getName());
            params.put("TOTAL_QTY", totalQty);
            params.put("TITLE", title.toUpperCase());
            params.put("INV_LOCATION", withdrawal.getInventoryLocation().getDescription());
//            params.put("WO_NUMBER", withdrawal.getWorkOrder() != null ? withdrawal.getWorkOrder().getCode() : "");
//            params.put("WO_CLASS", withdrawal.getWorkOrder() != null ? (withdrawal.getWorkOrder().getClassification() == 1 ? "New Construction" : withdrawal.getWorkOrder().getClassification() == 2 ? "System Improvement" : "Replacement for Retirement") : "");

            if(withdrawal.getWorkOrder() != null){
                params.put("LINKED_NUMBER", withdrawal.getWorkOrder().getCode());
            }

            if(withdrawal.getCostEstimate() != null){
                params.put("LINKED_NUMBER", withdrawal.getCostEstimate().getCode());
            }

            params = signatureFacade.getDocumentSignature(params, DocumentType.SW, withdrawal);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer vid) {

        List<Map> rrDetails = this.getDetails(vid);
        return new JRBeanCollectionDataSource(rrDetails);
    }

    private Map forLogMapMain(StockWithdrawal sw) {
        return documentLoggerFacade.makeLog(sw);
    }

}
