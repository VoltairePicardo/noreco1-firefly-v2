package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.BillOfMaterialDetailDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.PostRoleResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.service.BillOfMaterialService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.BillOfMaterialValidator;
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
import java.text.DecimalFormat;
import java.util.*;

@Service(value = "billOfMaterialServiceImpl")
public class BillOfMaterialServiceImpl implements BillOfMaterialService, PrintableVoucher {

    private BillOfMaterial model;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    BillOfMaterialRepo billOfMaterialRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    BillOfMaterialDetailRepo billOfMaterialDetailRepo;

    @Autowired
    BillOfMaterialMiscellaneousChargeRepo billOfMaterialMiscellaneousChargeRepo;

    @Autowired
    BillOfMaterialAssemblyUnitRepo billOfMaterialAssemblyUnitRepo;

    @Autowired
    BillOfMaterialAssemblyUnitItemRepo billOfMaterialAssemblyUnitItemRepo;

    @Autowired
    AssemblyUnitDetailRepo assemblyUnitDetailRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    DocumentDtoer documentDtoer;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    ItemRepo itemRepo;

    @Autowired
    ItemStockRepo itemStockRepo;

    @Autowired
    SignatureFacade signatureFacade;

    @Autowired
    EmployeeRepo employeeRepo;

    @Override
    public BillOfMaterial findById(Integer id) {
        BillOfMaterial ret = billOfMaterialRepo.findById(id).orElse(null);

        ret.setDetails(this.getDetails(ret));

        ArrayList<BillOfMaterialAssemblyUnit> assemblyUnitDetails = billOfMaterialAssemblyUnitRepo.findByBillOfMaterialId(ret.getId());
        ret.setBillOfMaterialAssemblyUnits(assemblyUnitDetails);

        ArrayList<BillOfMaterialMiscellaneousCharge> billOfMaterialMiscellaneousCharges = billOfMaterialMiscellaneousChargeRepo.findByBillOfMaterialId(ret.getId());
        ret.setMiscellaneousCharges(billOfMaterialMiscellaneousCharges);

        return ret;
    }

    @Override
    public List<BillOfMaterial> findAll() {
        return billOfMaterialRepo.findAll();
    }

    @Override
    public Page<BillOfMaterial> findAll(Pageable pageable) {
        return billOfMaterialRepo.findAll(pageable);
    }

    @Override
    public Page<BillOfMaterial> findByQuery(String query, Pageable pageable) {
        return billOfMaterialRepo.findByCode(query, pageable);
    }

    @Override
    public List<DocumentStatus> getDocumentsStatuses() {
        return documentDtoer.getDocumentStatuses(com.noreco1.fireflyv2.model.enums.Workflow.BOM.getId());
    }

    @Override
    public List<BillOfMaterial> getListForSummaryReport(String from, String to, HttpServletRequest request) {
        List<BillOfMaterial> list = new ArrayList<>();
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            String status = request.getParameter("status");

            if(!Checker.isStringNullAndEmpty(status)) {
                Integer statusInt = Integer.parseInt(status);
                list = billOfMaterialRepo.findByVoucherDateBetweenAndDocumentStatusId(fromDate, toDate, statusInt);
            } else {
                list = billOfMaterialRepo.findByVoucherDateBetween(fromDate, toDate);
            }

            if(!Checker.collectionIsEmpty(list)){
                for(BillOfMaterial l : list){
                    l.setDetails(this.getDetails(l));

                    ArrayList<BillOfMaterialAssemblyUnit> assemblyUnitDetails = billOfMaterialAssemblyUnitRepo.findByBillOfMaterialId(l.getId());
                    l.setBillOfMaterialAssemblyUnits(assemblyUnitDetails);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public List<BillOfMaterialDetailDto> findDetailByAssemblyUnitId(Integer assemblyUnitId, Integer invLocId) {
        List<BillOfMaterialDetailDto> data = new ArrayList<>();
        List<AssemblyUnitDetail> details = assemblyUnitDetailRepo.findByAssemblyUnitId(assemblyUnitId);

        if(!details.isEmpty()) {
            for (AssemblyUnitDetail detail : details) {
                BillOfMaterialDetailDto dto = new BillOfMaterialDetailDto();
                ItemStock itemStock = itemStockRepo.findByItemIdAndInventoryLocationId(detail.getItem().getId(), invLocId);

                dto.setItemId(detail.getItem().getId());
                dto.setAssemblyUnitId(detail.getAssemblyUnit().getId());
                dto.setQuantity(detail.getQuantity());
                dto.setItemCode(detail.getItem().getCode());
                dto.setItemDescription(detail.getItem().getDescription());
                dto.setUnitCode(detail.getItem().getUnit().getCode());
                if(itemStock != null) {
                    dto.setItemStockId(itemStock.getId());
                    dto.setInventoryCost(itemStock.getUnitCost());
                    dto.setInventoryQty(itemStock.getQuantity());
                }

                data.add(dto);
            }
        }

        return data;
    }

    @Override
    public List<BillOfMaterialDetailDto> findDetailByBillOfMaterialTransId(Integer transId) {

        List<BillOfMaterialDetailDto> billOfMaterialDetailDtos = new ArrayList<>();

        try {

            BillOfMaterial billOfMaterial = billOfMaterialRepo.findOneByTransactionId(transId);

            if (Checker.isValidId(billOfMaterial.getId())){

                List<BillOfMaterialDetail> details = billOfMaterialDetailRepo.findByBillOfMaterialId(billOfMaterial.getId());

                if (Checker.collectionIsNotEmpty(details)){

                    for (BillOfMaterialDetail billOfMaterialDetail : details){

                        BillOfMaterialDetailDto billOfMaterialDetailDto = new BillOfMaterialDetailDto();

                        billOfMaterialDetailDto.setId(billOfMaterialDetail.getId());
                        billOfMaterialDetailDto.setItemDescription(billOfMaterialDetail.getItem().getDescription());
                        billOfMaterialDetailDto.setQuantity(billOfMaterialDetail.getQuantity());
                        billOfMaterialDetailDto.setUnitCost(billOfMaterialDetail.getUnitCost());
                        billOfMaterialDetailDto.setTotalCost(billOfMaterialDetail.getTotalCost());
                        billOfMaterialDetailDto.setInventoryCost(billOfMaterialDetail.getInventoryCost());
                        billOfMaterialDetailDto.setMarkUp(billOfMaterialDetail.getMarkUp());

                        billOfMaterialDetailDtos.add(billOfMaterialDetailDto);

                    }

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return billOfMaterialDetailDtos;

    }

    @Override
    public PostResponse updateType(BillOfMaterial ce) {
        PostResponse response = new PostRoleResponse();
        BillOfMaterial billOfMaterial = billOfMaterialRepo.findById(ce.getId()).orElse(null);

        if(billOfMaterial != null) {

            // use for document logging
            Map oldProjectMap =  documentLoggerFacade.makeLog(billOfMaterial);

            billOfMaterial.setType(ce.getType());

            billOfMaterialRepo.save(billOfMaterial);

            Map newProjectMap =documentLoggerFacade.makeLog(billOfMaterial);

            documentLoggerFacade.log(billOfMaterial.getTransaction(), authenticationFacade.getLoggedIn(), oldProjectMap, newProjectMap);

            response.setSuccessMessage("Bill of Material Type successfully updated.");

        } else  {
            response.setFailureMessage("Bill of Material is not available.");
        }

        return response;
    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        BillOfMaterial billOfMaterial = billOfMaterialRepo.findById(postData.getDocumentId()).orElse(null);

        if (billOfMaterial != null &&
                billOfMaterial.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId() &&
                billOfMaterial.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId()
                ) {
            billOfMaterial.setDetails(this.getDetails(billOfMaterial));

            ArrayList<BillOfMaterialAssemblyUnit> assemblyUnitDetails = billOfMaterialAssemblyUnitRepo.findByBillOfMaterialId(billOfMaterial.getId());
            billOfMaterial.setBillOfMaterialAssemblyUnits(assemblyUnitDetails);

            // for logging
            Map oldRrMap = this.forLogMapMain(billOfMaterial);

            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(billOfMaterial, billOfMaterial.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            billOfMaterial.setDocumentStatus(afterActionDocumentStatus);
            billOfMaterial.setUpdatedAt(new Date());
            billOfMaterial = billOfMaterialRepo.save(billOfMaterial);

            if (billOfMaterial != null) {

                // for logging
                billOfMaterial.setDetails(this.getDetails(billOfMaterial));

                assemblyUnitDetails = billOfMaterialAssemblyUnitRepo.findByBillOfMaterialId(billOfMaterial.getId());
                billOfMaterial.setBillOfMaterialAssemblyUnits(assemblyUnitDetails);

                Map newRrMap = this.forLogMapMain(billOfMaterial);
                newRrMap.put("remarks", postData.getRemarks());

                documentProcessingFacade.processAction(billOfMaterial.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(billOfMaterial.getTransaction(), authenticationFacade.getLoggedIn(), oldRrMap, newRrMap);

                response.setSuccessMessage("Bill of Material successfully processed");
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

        BillOfMaterial billOfMaterial = (BillOfMaterial) v;

        PostResponse response = new PostResponse();
        response.setSuccess(false);

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        BillOfMaterialValidator validator = new BillOfMaterialValidator();
        validator.setService(this);
        validator.validate(billOfMaterial, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            BillOfMaterial existingSA = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(billOfMaterial.getVoucherDate()));

            User checkedBy = userRepo.findOneByAccountNo(billOfMaterial.getChecker().getAccountNo());
            User recommendedBy = userRepo.findOneByAccountNo(billOfMaterial.getRecommendedBy().getAccountNo());
            User approvedBy = userRepo.findOneByAccountNo(billOfMaterial.getApprovingOfficer().getAccountNo());

            Boolean insertMode = billOfMaterial.getId() == null;
            if (insertMode) { // insert mode
                Object latestCode = billOfMaterialRepo.findLatestCodeByYear(voucherYear);
                billOfMaterial.setCode(generatorFacade.voucherCodeNoOffice("BOM", (latestCode == null ? "" : String.valueOf(latestCode)), billOfMaterial.getVoucherDate(), GlobalConstant.COUNTER_PAD_4));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                billOfMaterial.setDocumentStatus(documentStatus);

                Workflow wf = new Workflow();

                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.BOM.getId());

                billOfMaterial.setYear(voucherYear);
                billOfMaterial.setTransaction(generatorFacade.transaction());
                billOfMaterial.setWorkflow(wf);
                billOfMaterial.setCreatedBy(createdBy);
                existingSA = billOfMaterial;
            } else {
                existingSA = billOfMaterialRepo.findById(billOfMaterial.getId()).orElse(null);
            }
            // use for document logging
            Map oldMap = this.forLogMapMain(existingSA);

            existingSA.setVoucherDate(billOfMaterial.getVoucherDate());
            existingSA.setYear(voucherYear);
            existingSA.setProject(billOfMaterial.getProject());
            existingSA.setInventoryLocation(billOfMaterial.getInventoryLocation());
            existingSA.setTotalMaterialCost(billOfMaterial.getTotalMaterialCost());
            existingSA.setTotalMiscellaneousCharge(billOfMaterial.getTotalMiscellaneousCharge());
            existingSA.setTotalMeteringCost(billOfMaterial.getTotalMeteringCost());
            existingSA.setLaborCost(billOfMaterial.getLaborCost());
            existingSA.setTotalAssemblyLaborCost(billOfMaterial.getTotalAssemblyLaborCost());
            existingSA.setFreightHandling(billOfMaterial.getFreightHandling());
            existingSA.setContingency(billOfMaterial.getContingency());
            existingSA.setChecker(checkedBy);
            existingSA.setRecommendedBy(recommendedBy);
            existingSA.setApprovingOfficer(approvedBy);
            existingSA.setOffice(billOfMaterial.getOffice());
            existingSA.setNotes(billOfMaterial.getNotes());
            existingSA.setType(billOfMaterial.getType());

            if(billOfMaterial.getLaborCost() == null || billOfMaterial.getLaborCost().equals(BigDecimal.ZERO)) {
                existingSA.setLaborCostPercentage(BigDecimal.ZERO);
            } else {
                existingSA.setLaborCostPercentage(billOfMaterial.getLaborCostPercentage());
            }

            if(billOfMaterial.getFreightHandling() == null || billOfMaterial.getFreightHandling().equals(BigDecimal.ZERO)) {
                existingSA.setFreightHandlingPercentage(BigDecimal.ZERO);
            } else {
                existingSA.setFreightHandlingPercentage(billOfMaterial.getFreightHandlingPercentage());
            }

            if(billOfMaterial.getContingency() == null || billOfMaterial.getContingency().equals(BigDecimal.ZERO)) {
                existingSA.setContingencyPercentage(BigDecimal.ZERO);
            } else {
                existingSA.setContingencyPercentage(billOfMaterial.getContingencyPercentage());
            }

            existingSA.setUpdatedAt(new Date());
            this.model = billOfMaterialRepo.save(existingSA);

            if (this.model != null) {

                // start: update default signatories
                signatoryFacade.bom(this.model);
                // end: update default signatories

                if (!insertMode) {
                    billOfMaterialDetailRepo.deleteByBillOfMaterialId(model.getId());
                    billOfMaterialMiscellaneousChargeRepo.deleteByBillOfMaterialId(model.getId());
                    billOfMaterialAssemblyUnitItemRepo.deleteByBillOfMaterialAssemblyUnitBillOfMaterialId(model.getId());
                    billOfMaterialAssemblyUnitRepo.deleteByBillOfMaterialId(model.getId());
                }

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldMap = null;
                }

                ArrayList<BillOfMaterialDetailDto> details = billOfMaterial.getDetails();
                for (BillOfMaterialDetailDto detailDto : details) {
                    BillOfMaterialDetail billOfMaterialDetail = new BillOfMaterialDetail();
                    Item item = itemRepo.findById(detailDto.getItemId()).orElse(null);

                    //TODO: Set values for details
                    billOfMaterialDetail.setBillOfMaterial(this.model);
                    billOfMaterialDetail.setItem(item);
                    billOfMaterialDetail.setQuantity(detailDto.getQuantity());
                    billOfMaterialDetail.setUnitCost(detailDto.getUnitCost());
                    billOfMaterialDetail.setTotalCost(detailDto.getTotalCost());
                    billOfMaterialDetail.setInventoryCost(detailDto.getInventoryCost());
                    billOfMaterialDetail.setMarkUp(detailDto.getMarkUp());
                    billOfMaterialDetail.setCategory(detailDto.getCategory());

                    billOfMaterialDetailRepo.save(billOfMaterialDetail);
                }

                ArrayList<BillOfMaterialMiscellaneousCharge> miscellaneousCharges = billOfMaterial.getMiscellaneousCharges();
                for (BillOfMaterialMiscellaneousCharge miscellaneousCharge : miscellaneousCharges) {
                    BillOfMaterialMiscellaneousCharge billOfMaterialMiscellaneousCharge = new BillOfMaterialMiscellaneousCharge();

                    //TODO: Set values for misc charge
                    billOfMaterialMiscellaneousCharge.setBillOfMaterial(this.model);
                    billOfMaterialMiscellaneousCharge.setMiscellaneousCharge(miscellaneousCharge.getMiscellaneousCharge());
                    billOfMaterialMiscellaneousCharge.setQuantity(miscellaneousCharge.getQuantity());
                    billOfMaterialMiscellaneousCharge.setUnitCost(miscellaneousCharge.getUnitCost());
                    billOfMaterialMiscellaneousCharge.setTotalCost(miscellaneousCharge.getTotalCost());
                    billOfMaterialMiscellaneousCharge.setRemarks(miscellaneousCharge.getRemarks());

                    billOfMaterialMiscellaneousChargeRepo.save(billOfMaterialMiscellaneousCharge);
                }

                ArrayList<BillOfMaterialAssemblyUnit> assemblyUnits = billOfMaterial.getBillOfMaterialAssemblyUnits();
                for (BillOfMaterialAssemblyUnit assemblyUnit : assemblyUnits) {
                    BillOfMaterialAssemblyUnit billOfMaterialAssemblyUnit = new BillOfMaterialAssemblyUnit();

                    //TODO: Set values for details
                    billOfMaterialAssemblyUnit.setBillOfMaterial(this.model);
                    billOfMaterialAssemblyUnit.setAssemblyUnit(assemblyUnit.getAssemblyUnit());
                    billOfMaterialAssemblyUnit.setQuantity(assemblyUnit.getQuantity());
                    billOfMaterialAssemblyUnit.setUnitCost(assemblyUnit.getUnitCost());
                    billOfMaterialAssemblyUnit.setTotalCost(assemblyUnit.getTotalCost());

                    billOfMaterialAssemblyUnit = billOfMaterialAssemblyUnitRepo.save(billOfMaterialAssemblyUnit);
                    billOfMaterialAssemblyUnitRepo.flush();

                    ArrayList<BillOfMaterialDetailDto> ceDetails = billOfMaterial.getDetails();
                    for (BillOfMaterialDetailDto itemLine : ceDetails) {

                        List<Map> assemblyUnitIds = itemLine.getAssemblyUnitIds();
                        for (Map assemblyUnitMap: assemblyUnitIds) {

                            Object assemblyUnitId = assemblyUnitMap.get("assemblyUnitId");

                            if(assemblyUnitId.equals(billOfMaterialAssemblyUnit.getAssemblyUnit().getId())) {

                                BillOfMaterialAssemblyUnitItem billOfMaterialAssemblyUnitItem = new BillOfMaterialAssemblyUnitItem();

                                Item item  = new Item();
                                item.setId(itemLine.getItemId());
                                item.setCode(itemLine.getItemCode());
                                item.setDescription(itemLine.getItemDescription());

                                billOfMaterialAssemblyUnitItem.setBillOfMaterialAssemblyUnit(billOfMaterialAssemblyUnit);
                                billOfMaterialAssemblyUnitItem.setItem(item);

                                billOfMaterialAssemblyUnitItemRepo.save(billOfMaterialAssemblyUnitItem);
                            }
                        }
                    }
                }

                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());
                response.setSuccessMessage("Bill Of Material successfully saved!");
                response.setSuccess(true);
            }
        }

        return response;
    }

    @Override
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        BillOfMaterial billOfMaterial = billOfMaterialRepo.findById(vid).orElse(null);

        if (billOfMaterial != null) {

            List<Map> details = this.getDetailsReport(vid);
            BigDecimal totalQty = BigDecimal.ZERO;
            for (Map d : details) {
                totalQty = totalQty.add(new BigDecimal(d.get("quantity")+""));
            }

            DecimalFormat decimalFormat = new DecimalFormat("###.##");

            params.put("VOUCHER_NO", billOfMaterial.getCode());
            params.put("V_DATE", billOfMaterial.getVoucherDate());
            params.put("PROJECT_NAME", billOfMaterial.getProject().getName());
            params.put("PROJECT_LOCATION", billOfMaterial.getProject().getLocation());
            params.put("PROJECT_CODE", billOfMaterial.getProject().getCode());
            params.put("PROJECT_PURPOSE", billOfMaterial.getProject().getPurpose());
            params.put("NOTES", billOfMaterial.getNotes());
            params.put("TOTAL_QTY", totalQty);
            params.put("TOTAL_LABOR_COST", billOfMaterial.getLaborCost());
            params.put("TOTAL_LABOR_COST_PERCENTAGE", decimalFormat.format(billOfMaterial.getLaborCostPercentage())+"%");
            params.put("TOTAL_MATERIAL_COST", billOfMaterial.getTotalMaterialCost());
            params.put("TOTAL_METERING_COST", billOfMaterial.getTotalMeteringCost());
            params.put("TOTAL_MISCELLANEOUS_CHARGE", billOfMaterial.getTotalMiscellaneousCharge());
            params.put("TOTAL_ASSEMBLY_LABOR_COST", billOfMaterial.getTotalAssemblyLaborCost());
            params.put("FREIGHT_HANDLING", billOfMaterial.getFreightHandling());
            params.put("FREIGHT_HANDLING_PERCENTAGE", decimalFormat.format(billOfMaterial.getFreightHandlingPercentage())+"%");

            params.put("CONTINGENCY", billOfMaterial.getContingency());
            params.put("CONTINGENCY_PERCENTAGE", decimalFormat.format(billOfMaterial.getContingencyPercentage())+"%");

            params.put("SUBREPORT_DIR", GlobalConstant.JASPER_BASE_PATH + "/vouchers/");
            params.put("RECAP_DS", new JRBeanCollectionDataSource(billOfMaterial.getTotalAssemblyLaborCost().compareTo(BigDecimal.ZERO) == 0 ? new ArrayList<>() : this.makeBillOfMaterialAssemblyUnitMap(billOfMaterial.getId())));

            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.BOM, billOfMaterial);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer vid) {
        List<Map> details = this.getDetailsReport(vid);
        return new JRBeanCollectionDataSource(details);
    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);
        if (documentLog != null) {
            Integer transId = documentLog.getTransaction().getId();
            BillOfMaterial billOfMaterial = billOfMaterialRepo.findOneByTransactionId(transId);

            if (billOfMaterial != null) {
                billOfMaterial.setDetails(this.getDetails(billOfMaterial));

                ArrayList<BillOfMaterialAssemblyUnit> assemblyUnitDetails = billOfMaterialAssemblyUnitRepo.findByBillOfMaterialId(billOfMaterial.getId());
                billOfMaterial.setBillOfMaterialAssemblyUnits(assemblyUnitDetails);

                ArrayList<BillOfMaterialMiscellaneousCharge> billOfMaterialMiscellaneousCharges = billOfMaterialMiscellaneousChargeRepo.findByBillOfMaterialId(billOfMaterial.getId());
                billOfMaterial.setMiscellaneousCharges(billOfMaterialMiscellaneousCharges);

                Map map = forLogMapMain(billOfMaterial);
                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.BOM);
    }

    @Override
    public List<Map> findByDateRangeAndStatusId(String from, String to, Integer docStatusId) {
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

            List<BillOfMaterial> docs = billOfMaterialRepo.findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(loggedIn.getId(), fromDate, toDate, docStatusId);
            return this.makeMapList(docs);
        } catch (Exception ex) {
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

            User loggedIn = authenticationFacade.getLoggedIn();

            List<BillOfMaterial> docs = billOfMaterialRepo.findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(loggedIn.getId(), fromDate, toDate, Arrays.asList(ids));
            return this.makeMapList(docs);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public List<Map> getDetailsReport(int id) {
        List<Map> data = new ArrayList<>();

        BillOfMaterial billOfMaterial = billOfMaterialRepo.findById(id).orElse(null);
        if (billOfMaterial != null) {
            List<BillOfMaterialDetail> details = billOfMaterialDetailRepo.findByBillOfMaterialIdOrderByCategoryAscItemDescriptionAsc(billOfMaterial.getId());

            if (!details.isEmpty()) {
                int counter = 1;
                for (BillOfMaterialDetail detail : details) {
                    Map detailMap = new HashMap();

                    detailMap.put("id", 0);
                    detailMap.put("itemId", detail.getItem().getId());
                    detailMap.put("code", detail.getItem().getCode());
                    detailMap.put("description", detail.getItem().getDescription());
                    detailMap.put("quantity", detail.getQuantity());
                    detailMap.put("unitCode", detail.getItem().getUnit().getCode());
                    detailMap.put("unitId", detail.getItem().getUnit().getId());
                    detailMap.put("unitCost", detail.getUnitCost());
                    detailMap.put("totalCost", detail.getTotalCost());
                    detailMap.put("inventoryCost", detail.getInventoryCost());
                    detailMap.put("category", detail.getCategory() == 1 ? "A. Accessories" : "B. Metering");

                    data.add(detailMap);
                }
            }
            List<BillOfMaterialMiscellaneousCharge> billOfMaterialMiscellaneousCharges = billOfMaterialMiscellaneousChargeRepo.findByBillOfMaterialIdOrderByMiscellaneousChargeDescriptionAsc(billOfMaterial.getId());

            if (!billOfMaterialMiscellaneousCharges.isEmpty()) {
                int counter = 1;
                for (BillOfMaterialMiscellaneousCharge detail : billOfMaterialMiscellaneousCharges) {
                    Map detailMap = new HashMap();

                    detailMap.put("id", 0);
                    detailMap.put("itemId", "");
                    detailMap.put("code", "");
                    detailMap.put("description", detail.getMiscellaneousCharge().getDescription());
                    detailMap.put("quantity", detail.getQuantity());
                    detailMap.put("unitCode", "");
                    detailMap.put("unitId", "");
                    detailMap.put("unitCost", detail.getUnitCost());
                    detailMap.put("totalCost", detail.getTotalCost());
                    detailMap.put("category", "C. Miscellaneous Charges");

                    data.add(detailMap);
                }
            }
        }

        return data;
    }

    public List<Map> makeBillOfMaterialAssemblyUnitMap(int id) {
        List<Map> data = new ArrayList<>();

        ArrayList<BillOfMaterialAssemblyUnit> assemblyUnitDetails = billOfMaterialAssemblyUnitRepo.findByBillOfMaterialId(id);
        if (!Checker.collectionIsEmpty(assemblyUnitDetails)) {
                int counter = 1;
                for (BillOfMaterialAssemblyUnit detail : assemblyUnitDetails) {
                    Map detailMap = new HashMap();

                    detailMap.put("id", counter++);
                    detailMap.put("assemblyTypeId", detail.getAssemblyUnit().getAssemblyType().getId());
                    detailMap.put("assemblyType", detail.getAssemblyUnit().getAssemblyType().getDescription());
                    detailMap.put("code", detail.getAssemblyUnit().getCode());
                    detailMap.put("quantity", detail.getQuantity());
                    detailMap.put("unitCost", detail.getUnitCost());
                    detailMap.put("totalCost", detail.getTotalCost());

                    data.add(detailMap);
                }
        }

        return data;
    }

    public ArrayList<BillOfMaterialDetailDto> getDetails(BillOfMaterial billOfMaterial) {
        ArrayList<BillOfMaterialDetailDto> detailsDto = new ArrayList<>();
        ArrayList<BillOfMaterialDetail> details = billOfMaterialDetailRepo.findByBillOfMaterialId(billOfMaterial.getId());

        if(!details.isEmpty()) {
            for (BillOfMaterialDetail detail : details) {
                BillOfMaterialDetailDto dto = new BillOfMaterialDetailDto();
                ItemStock itemStock = itemStockRepo.findByItemIdAndInventoryLocationId(detail.getItem().getId(), billOfMaterial.getInventoryLocation().getId());

                List<BillOfMaterialAssemblyUnitItem> assemblyUnitItems = billOfMaterialAssemblyUnitItemRepo.findByBillOfMaterialAssemblyUnitBillOfMaterialIdAndItemId(billOfMaterial.getId(), detail.getItem().getId());

                if(Checker.collectionIsNotEmpty(assemblyUnitItems)) {
                    for (BillOfMaterialAssemblyUnitItem assemblyUnitItem : assemblyUnitItems) {
                        dto.setAssemblyUnitId(assemblyUnitItem.getBillOfMaterialAssemblyUnit().getAssemblyUnit().getId());
                        dto.setAssemblyCode(assemblyUnitItem.getBillOfMaterialAssemblyUnit().getAssemblyUnit().getCode());

                        AssemblyUnitDetail assemblyUnitDetail = assemblyUnitDetailRepo.findByAssemblyUnitIdAndItemId(assemblyUnitItem.getBillOfMaterialAssemblyUnit().getAssemblyUnit().getId(), itemStock.getItem().getId());

                        Map assemblyUnitIdsMap = new HashMap();
                        assemblyUnitIdsMap.put("assemblyUnitId", assemblyUnitItem.getBillOfMaterialAssemblyUnit().getAssemblyUnit().getId());
                        assemblyUnitIdsMap.put("multiplier", assemblyUnitDetail.getQuantity());

                        dto.getAssemblyUnitIds().add(assemblyUnitIdsMap);
                    }
                }

                dto.setItemId(detail.getItem().getId());
                dto.setInventoryCost(detail.getInventoryCost());
                dto.setQuantity(detail.getQuantity());
                dto.setInventoryQty(itemStock.getQuantity());
                dto.setItemCode(detail.getItem().getCode());
                dto.setItemDescription(detail.getItem().getDescription());
                dto.setUnitCode(detail.getItem().getUnit().getCode());
                dto.setMarkUp(detail.getMarkUp());
                dto.setUnitCost(detail.getUnitCost());
                dto.setTotalCost(detail.getTotalCost());
                dto.setUnitQty(detail.getQuantity());
                dto.setCategory(detail.getCategory());

                detailsDto.add(dto);
            }
        }
        return detailsDto;
    }

    private Map forLogMapMain(BillOfMaterial billOfMaterial) {
        return documentLoggerFacade.makeLog(billOfMaterial);
    }

    private List<Map> makeMapList(List<BillOfMaterial> billOfMaterials) {

        List<Map> mapList = new ArrayList<>();

        if (!Checker.collectionIsEmpty(billOfMaterials)) {
            for (BillOfMaterial c : billOfMaterials) {
                mapList.add(composeMap(c));
            }
        }
        return mapList;
    }

    private Map composeMap(BillOfMaterial r) {
        Map map = new HashMap();

        map.put("id", r.getId());
        map.put("code", r.getCode());
        map.put("voucherDate", r.getVoucherDate());
        map.put("projectCode", r.getProject().getCode());
        map.put("projectName", r.getProject().getName());
        map.put("createdBy", r.getCreatedBy());
        map.put("documentStatus", r.getDocumentStatus());

        return map;
    }

}
