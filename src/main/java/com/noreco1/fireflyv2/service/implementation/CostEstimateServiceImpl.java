package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.CostEstimateDetailDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.PostRoleResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.service.CostEstimateService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.CostEstimateValidator;
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
import java.text.DecimalFormat;
import java.util.*;

@Service(value = "costEstimateServiceImpl")
public class CostEstimateServiceImpl implements CostEstimateService, PrintableVoucher {

    private CostEstimate model;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    CostEstimateRepo costEstimateRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    CostEstimateDetailRepo costEstimateDetailRepo;

    @Autowired
    CostEstimateMiscellaneousChargeRepo costEstimateMiscellaneousChargeRepo;

    @Autowired
    CostEstimateAssemblyUnitRepo costEstimateAssemblyUnitRepo;

    @Autowired
    CostEstimateAssemblyUnitItemRepo costEstimateAssemblyUnitItemRepo;

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

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public CostEstimate findById(Integer id) {
        CostEstimate ret = costEstimateRepo.findById(id).orElse(null);

        ret.setDetails(this.getDetails(ret));

        ArrayList<CostEstimateAssemblyUnit> assemblyUnitDetails = costEstimateAssemblyUnitRepo.findByCostEstimateId(ret.getId());
        ret.setCostEstimateAssemblyUnits(assemblyUnitDetails);

        ArrayList<CostEstimateMiscellaneousCharge> costEstimateMiscellaneousCharges = costEstimateMiscellaneousChargeRepo.findByCostEstimateId(ret.getId());
        ret.setMiscellaneousCharges(costEstimateMiscellaneousCharges);

        return ret;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<CostEstimate> findAll() {
        return costEstimateRepo.findAll();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<CostEstimate> findAll(Pageable pageable) {
        return costEstimateRepo.findAll(pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<CostEstimate> findByQuery(String query, Pageable pageable) {
        return costEstimateRepo.findByCode(query, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<CostEstimate> findAllForPurchaseRequest(String query, Pageable pageable) {
        if (Checker.isStringNullAndEmpty(query)) {
            return costEstimateRepo.findAllForPurchaseRequest(pageable);
        } else {
            return costEstimateRepo.findAllForPurchaseRequestByQuery("%"+query+"%", pageable);
        }
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<DocumentStatus> getDocumentsStatuses() {
        return documentDtoer.getDocumentStatuses(com.noreco1.fireflyv2.model.enums.Workflow.CE.getId());
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<CostEstimate> getListForSummaryReport(String from, String to, HttpServletRequest request) {
        List<CostEstimate> list = new ArrayList<>();
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
                list = costEstimateRepo.findByVoucherDateBetweenAndDocumentStatusId(fromDate, toDate, statusInt);
            } else {
                list = costEstimateRepo.findByVoucherDateBetween(fromDate, toDate);
            }

            if(!Checker.collectionIsEmpty(list)){
                for(CostEstimate l : list){
                    l.setDetails(this.getDetails(l));

                    ArrayList<CostEstimateAssemblyUnit> assemblyUnitDetails = costEstimateAssemblyUnitRepo.findByCostEstimateId(l.getId());
                    l.setCostEstimateAssemblyUnits(assemblyUnitDetails);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<CostEstimateDetailDto> findDetailByAssemblyUnitId(Integer assemblyUnitId, Integer invLocId) {
        List<CostEstimateDetailDto> data = new ArrayList<>();
        List<AssemblyUnitDetail> details = assemblyUnitDetailRepo.findByAssemblyUnitId(assemblyUnitId);

        if(!details.isEmpty()) {
            for (AssemblyUnitDetail detail : details) {
                CostEstimateDetailDto dto = new CostEstimateDetailDto();
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

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<CostEstimateDetailDto> findDetailByCostEstimateTransId(Integer transId) {

        List<CostEstimateDetailDto> costEstimateDetailDtos = new ArrayList<>();

        try {

            CostEstimate costEstimate = costEstimateRepo.findOneByTransactionId(transId);

            if (Checker.isValidId(costEstimate.getId())){

                List<CostEstimateDetail> details = costEstimateDetailRepo.findByCostEstimateId(costEstimate.getId());

                if (Checker.collectionIsNotEmpty(details)){

                    for (CostEstimateDetail costEstimateDetail : details){

                        CostEstimateDetailDto costEstimateDetailDto = new CostEstimateDetailDto();

                        costEstimateDetailDto.setId(costEstimateDetail.getId());
                        costEstimateDetailDto.setItemId(costEstimateDetail.getItem().getId());
                        costEstimateDetailDto.setItemDescription(costEstimateDetail.getItem().getDescription());
                        costEstimateDetailDto.setUnitId(costEstimateDetail.getItem().getUnit() != null ? costEstimateDetail.getItem().getUnit().getId() : null);
                        costEstimateDetailDto.setUnitCode(costEstimateDetail.getItem().getUnit() != null ? costEstimateDetail.getItem().getUnit().getCode() : null);
                        costEstimateDetailDto.setQuantity(costEstimateDetail.getQuantity());
                        costEstimateDetailDto.setUnitCost(costEstimateDetail.getUnitCost());
                        costEstimateDetailDto.setTotalCost(costEstimateDetail.getTotalCost());
                        costEstimateDetailDto.setInventoryCost(costEstimateDetail.getInventoryCost());
                        costEstimateDetailDto.setMarkUp(costEstimateDetail.getMarkUp());

                        costEstimateDetailDtos.add(costEstimateDetailDto);

                    }

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return costEstimateDetailDtos;

    }

    @Override
    public PostResponse updateType(CostEstimate ce) {
        PostResponse response = new PostRoleResponse();
        CostEstimate costEstimate = costEstimateRepo.findById(ce.getId()).orElse(null);

        if(costEstimate != null) {

            // use for document logging
            Map oldProjectMap =  documentLoggerFacade.makeLog(costEstimate);

            costEstimate.setType(ce.getType());

            costEstimateRepo.save(costEstimate);

            Map newProjectMap =documentLoggerFacade.makeLog(costEstimate);

            documentLoggerFacade.log(costEstimate.getTransaction(), authenticationFacade.getLoggedIn(), oldProjectMap, newProjectMap);

            response.setSuccessMessage("Cost Estimate Type successfully updated.");

        } else  {
            response.setFailureMessage("Cost Estimate is not available.");
        }

        return response;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<CostEstimateAssemblyUnitItem> findItemsByCostEstimate(Integer id) {
        return costEstimateAssemblyUnitItemRepo.findAllByCostEstimateAssemblyUnitCostEstimateId(id);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<CostEstimate> getCostEstimateForStockWithdrawal(String query, Integer invLocId, Pageable pageable) {
        if(Checker.isStringNullOrEmpty(query)){
            return costEstimateRepo.findAllForCostEstimate(pageable);
        } else {
            return costEstimateRepo.findAllForCostEstimateByQuery(query, pageable);
        }
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<CostEstimateDetailDto> findAllDetailByCostEstimateTransId(Integer transId) {

        List<CostEstimateDetailDto> costEstimateDetailDtos = new ArrayList<>();

        try {

            CostEstimate costEstimate = costEstimateRepo.findOneByTransactionId(transId);

            if (Checker.isValidId(costEstimate.getId())){

                List<CostEstimateDetail> details = costEstimateDetailRepo.findByCostEstimateId(costEstimate.getId());
                for (CostEstimateDetail costEstimateDetail : details){

                    CostEstimateDetailDto costEstimateDetailDto = new CostEstimateDetailDto();

                    costEstimateDetailDto.setItemId(costEstimateDetail.getItem().getId());
                    costEstimateDetailDto.setItemDescription(costEstimateDetail.getItem().getDescription());
                    costEstimateDetailDto.setItemCode(costEstimateDetail.getItem().getCode());
                    costEstimateDetailDto.setUnitId(costEstimateDetail.getItem().getUnit() != null ? costEstimateDetail.getItem().getUnit().getId() : null);
                    costEstimateDetailDto.setUnitCode(costEstimateDetail.getItem().getUnit() != null ? costEstimateDetail.getItem().getUnit().getCode() : null);
                    costEstimateDetailDto.setQuantity(costEstimateDetail.getQuantity());
                    costEstimateDetailDto.setUnitCost(costEstimateDetail.getUnitCost());
                    costEstimateDetailDto.setTotalCost(costEstimateDetail.getTotalCost());
                    costEstimateDetailDto.setInventoryCost(costEstimateDetail.getInventoryCost());
                    costEstimateDetailDto.setMarkUp(costEstimateDetail.getMarkUp());

                    costEstimateDetailDtos.add(costEstimateDetailDto);

                }

                List<CostEstimateAssemblyUnitItem> assemblyUnitItems = this.costEstimateAssemblyUnitItemRepo.findAllByCostEstimateAssemblyUnitCostEstimateId(costEstimate.getId());
                for (CostEstimateAssemblyUnitItem assemblyUnitItem : assemblyUnitItems){

                    CostEstimateDetailDto costEstimateDetailDto = new CostEstimateDetailDto();

                    costEstimateDetailDto.setItemId(assemblyUnitItem.getItem().getId());
                    costEstimateDetailDto.setItemDescription(assemblyUnitItem.getItem().getDescription());
                    costEstimateDetailDto.setItemCode(assemblyUnitItem.getItem().getCode());
                    costEstimateDetailDto.setUnitId(assemblyUnitItem.getItem().getUnit() != null ? assemblyUnitItem.getItem().getUnit().getId() : null);
                    costEstimateDetailDto.setUnitCode(assemblyUnitItem.getItem().getUnit() != null ? assemblyUnitItem.getItem().getUnit().getCode() : null);
                    costEstimateDetailDto.setQuantity(assemblyUnitItem.getCostEstimateAssemblyUnit().getQuantity());
                    costEstimateDetailDto.setUnitCost(assemblyUnitItem.getCostEstimateAssemblyUnit().getUnitCost());
                    costEstimateDetailDto.setTotalCost(assemblyUnitItem.getCostEstimateAssemblyUnit().getTotalCost());

                    costEstimateDetailDtos.add(costEstimateDetailDto);

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return costEstimateDetailDtos;

    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        CostEstimate costEstimate = costEstimateRepo.findById(postData.getDocumentId()).orElse(null);

        if (costEstimate != null &&
                costEstimate.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId() &&
                costEstimate.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId()
                ) {
            costEstimate.setDetails(this.getDetails(costEstimate));

            ArrayList<CostEstimateAssemblyUnit> assemblyUnitDetails = costEstimateAssemblyUnitRepo.findByCostEstimateId(costEstimate.getId());
            costEstimate.setCostEstimateAssemblyUnits(assemblyUnitDetails);

            // for logging
            Map oldRrMap = this.forLogMapMain(costEstimate);

            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(costEstimate, costEstimate.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            costEstimate.setDocumentStatus(afterActionDocumentStatus);
            costEstimate.setUpdatedAt(new Date());
            costEstimate = costEstimateRepo.save(costEstimate);

            if (costEstimate != null) {

                // for logging
                costEstimate.setDetails(this.getDetails(costEstimate));

                assemblyUnitDetails = costEstimateAssemblyUnitRepo.findByCostEstimateId(costEstimate.getId());
                costEstimate.setCostEstimateAssemblyUnits(assemblyUnitDetails);

                Map newRrMap = this.forLogMapMain(costEstimate);
                newRrMap.put("remarks", postData.getRemarks());

                documentProcessingFacade.processAction(costEstimate.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(costEstimate.getTransaction(), authenticationFacade.getLoggedIn(), oldRrMap, newRrMap);

                response.setSuccessMessage("Cost Estimate successfully processed");
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

        CostEstimate costEstimate = (CostEstimate) v;

        PostResponse response = new PostResponse();
        response.setSuccess(false);

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        CostEstimateValidator validator = new CostEstimateValidator();
        validator.setService(this);
        validator.validate(costEstimate, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            CostEstimate existingSA = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(costEstimate.getVoucherDate()));

            User checkedBy = userRepo.findOneByAccountNo(costEstimate.getChecker().getAccountNo());
            User recommendedBy = userRepo.findOneByAccountNo(costEstimate.getRecommendedBy().getAccountNo());
            User approvedBy = userRepo.findOneByAccountNo(costEstimate.getApprovingOfficer().getAccountNo());

            Boolean insertMode = costEstimate.getId() == null;
            if (insertMode) { // insert mode
                Object latestCode = costEstimateRepo.findLatestCodeByYear(voucherYear);
                costEstimate.setCode(generatorFacade.voucherCodeNoOffice("CE", (latestCode == null ? "" : String.valueOf(latestCode)), costEstimate.getVoucherDate(), GlobalConstant.COUNTER_PAD_4));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                costEstimate.setDocumentStatus(documentStatus);

                Workflow wf = new Workflow();

                if(!Checker.isStringNullOrEmpty(costEstimate.getProject().getConsumerAccountNumber()) || !Checker.isStringNullOrEmpty(costEstimate.getProject().getConsumerName())){
                    wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.CEC.getId());
                } else {
                    wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.CE.getId());
                }

                costEstimate.setYear(voucherYear);
                costEstimate.setTransaction(generatorFacade.transaction());
                costEstimate.setWorkflow(wf);
                costEstimate.setCreatedBy(createdBy);
                existingSA = costEstimate;
            } else {
                existingSA = costEstimateRepo.findById(costEstimate.getId()).orElse(null);
            }
            // use for document logging
            Map oldMap = this.forLogMapMain(existingSA);

            existingSA.setVoucherDate(costEstimate.getVoucherDate());
            existingSA.setYear(voucherYear);
            existingSA.setProject(costEstimate.getProject());
            existingSA.setInventoryLocation(costEstimate.getInventoryLocation());
            existingSA.setTotalMaterialCost(costEstimate.getTotalMaterialCost());
            existingSA.setTotalMiscellaneousCharge(costEstimate.getTotalMiscellaneousCharge());
            existingSA.setTotalMeteringCost(costEstimate.getTotalMeteringCost());
            existingSA.setLaborCost(costEstimate.getLaborCost());
            existingSA.setTotalAssemblyLaborCost(costEstimate.getTotalAssemblyLaborCost());
            existingSA.setFreightHandling(costEstimate.getFreightHandling());
            existingSA.setContingency(costEstimate.getContingency());
            existingSA.setChecker(checkedBy);
            existingSA.setRecommendedBy(recommendedBy);
            existingSA.setApprovingOfficer(approvedBy);
            existingSA.setOffice(costEstimate.getOffice());
            existingSA.setNotes(costEstimate.getNotes());
            existingSA.setType(costEstimate.getType());

            if(costEstimate.getLaborCost() == null || costEstimate.getLaborCost().equals(BigDecimal.ZERO)) {
                existingSA.setLaborCostPercentage(BigDecimal.ZERO);
            } else {
                existingSA.setLaborCostPercentage(costEstimate.getLaborCostPercentage());
            }

            if(costEstimate.getFreightHandling() == null || costEstimate.getFreightHandling().equals(BigDecimal.ZERO)) {
                existingSA.setFreightHandlingPercentage(BigDecimal.ZERO);
            } else {
                existingSA.setFreightHandlingPercentage(costEstimate.getFreightHandlingPercentage());
            }

            if(costEstimate.getContingency() == null || costEstimate.getContingency().equals(BigDecimal.ZERO)) {
                existingSA.setContingencyPercentage(BigDecimal.ZERO);
            } else {
                existingSA.setContingencyPercentage(costEstimate.getContingencyPercentage());
            }

            existingSA.setUpdatedAt(new Date());
            this.model = costEstimateRepo.save(existingSA);

            if (this.model != null) {

                // start: update default signatories
                signatoryFacade.ce(this.model);
                // end: update default signatories

                if (!insertMode) {
                    costEstimateDetailRepo.deleteByCostEstimateId(model.getId());
                    costEstimateMiscellaneousChargeRepo.deleteByCostEstimateId(model.getId());
                    costEstimateAssemblyUnitItemRepo.deleteByCostEstimateAssemblyUnitCostEstimateId(model.getId());
                    costEstimateAssemblyUnitRepo.deleteByCostEstimateId(model.getId());
                }

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldMap = null;
                }

                ArrayList<CostEstimateDetailDto> details = costEstimate.getDetails();
                for (CostEstimateDetailDto detailDto : details) {
                    CostEstimateDetail costEstimateDetail = new CostEstimateDetail();
                    Item item = itemRepo.findById(detailDto.getItemId()).orElse(null);

                    //TODO: Set values for details
                    costEstimateDetail.setCostEstimate(this.model);
                    costEstimateDetail.setItem(item);
                    costEstimateDetail.setQuantity(detailDto.getQuantity());
                    costEstimateDetail.setUnitCost(detailDto.getUnitCost());
                    costEstimateDetail.setTotalCost(detailDto.getTotalCost());
                    costEstimateDetail.setInventoryCost(detailDto.getInventoryCost());
                    costEstimateDetail.setMarkUp(detailDto.getMarkUp());
                    costEstimateDetail.setCategory(detailDto.getCategory());

                    costEstimateDetailRepo.save(costEstimateDetail);
                }

                ArrayList<CostEstimateMiscellaneousCharge> miscellaneousCharges = costEstimate.getMiscellaneousCharges();
                for (CostEstimateMiscellaneousCharge miscellaneousCharge : miscellaneousCharges) {
                    CostEstimateMiscellaneousCharge costEstimateMiscellaneousCharge = new CostEstimateMiscellaneousCharge();

                    //TODO: Set values for misc charge
                    costEstimateMiscellaneousCharge.setCostEstimate(this.model);
                    costEstimateMiscellaneousCharge.setMiscellaneousCharge(miscellaneousCharge.getMiscellaneousCharge());
                    costEstimateMiscellaneousCharge.setQuantity(miscellaneousCharge.getQuantity());
                    costEstimateMiscellaneousCharge.setUnitCost(miscellaneousCharge.getUnitCost());
                    costEstimateMiscellaneousCharge.setTotalCost(miscellaneousCharge.getTotalCost());
                    costEstimateMiscellaneousCharge.setRemarks(miscellaneousCharge.getRemarks());

                    costEstimateMiscellaneousChargeRepo.save(costEstimateMiscellaneousCharge);
                }

                ArrayList<CostEstimateAssemblyUnit> assemblyUnits = costEstimate.getCostEstimateAssemblyUnits();
                for (CostEstimateAssemblyUnit assemblyUnit : assemblyUnits) {
                    CostEstimateAssemblyUnit costEstimateAssemblyUnit = new CostEstimateAssemblyUnit();

                    //TODO: Set values for details
                    costEstimateAssemblyUnit.setCostEstimate(this.model);
                    costEstimateAssemblyUnit.setAssemblyUnit(assemblyUnit.getAssemblyUnit());
                    costEstimateAssemblyUnit.setQuantity(assemblyUnit.getQuantity());
                    costEstimateAssemblyUnit.setUnitCost(assemblyUnit.getUnitCost());
                    costEstimateAssemblyUnit.setTotalCost(assemblyUnit.getTotalCost());

                    costEstimateAssemblyUnit = costEstimateAssemblyUnitRepo.save(costEstimateAssemblyUnit);
                    costEstimateAssemblyUnitRepo.flush();

                    ArrayList<CostEstimateDetailDto> ceDetails = costEstimate.getDetails();
                    for (CostEstimateDetailDto itemLine : ceDetails) {

                        List<Map> assemblyUnitIds = itemLine.getAssemblyUnitIds();
                        for (Map assemblyUnitMap: assemblyUnitIds) {

                            Object assemblyUnitId = assemblyUnitMap.get("assemblyUnitId");

                            if(assemblyUnitId.equals(costEstimateAssemblyUnit.getAssemblyUnit().getId())) {

                                CostEstimateAssemblyUnitItem costEstimateAssemblyUnitItem = new CostEstimateAssemblyUnitItem();

                                Item item  = new Item();
                                item.setId(itemLine.getItemId());
                                item.setCode(itemLine.getItemCode());
                                item.setDescription(itemLine.getItemDescription());

                                costEstimateAssemblyUnitItem.setCostEstimateAssemblyUnit(costEstimateAssemblyUnit);
                                costEstimateAssemblyUnitItem.setItem(item);

                                costEstimateAssemblyUnitItemRepo.save(costEstimateAssemblyUnitItem);
                            }
                        }
                    }
                }

                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());
                response.setSuccessMessage("Cost Estimate successfully saved!");
                response.setSuccess(true);
            }
        }

        return response;
    }

    @Override
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        CostEstimate costEstimate = costEstimateRepo.findById(vid).orElse(null);

        if (costEstimate != null) {

            List<Map> details = this.getDetailsReport(vid);
            BigDecimal totalQty = BigDecimal.ZERO;
            for (Map d : details) {
                totalQty = totalQty.add(new BigDecimal(d.get("quantity")+""));
            }

            DecimalFormat decimalFormat = new DecimalFormat("###.##");

            params.put("VOUCHER_NO", costEstimate.getCode());
            params.put("V_DATE", costEstimate.getVoucherDate());
            params.put("PROJECT_NAME", costEstimate.getProject().getName());
            params.put("PROJECT_LOCATION", costEstimate.getProject().getLocation());
            params.put("PROJECT_CODE", costEstimate.getProject().getCode());
            params.put("PROJECT_PURPOSE", costEstimate.getProject().getPurpose());
            params.put("NOTES", costEstimate.getNotes());
            params.put("TOTAL_QTY", totalQty);
            params.put("TOTAL_LABOR_COST", costEstimate.getLaborCost());
            params.put("TOTAL_LABOR_COST_PERCENTAGE", decimalFormat.format(costEstimate.getLaborCostPercentage())+"%");
            params.put("TOTAL_MATERIAL_COST", costEstimate.getTotalMaterialCost());
            params.put("TOTAL_METERING_COST", costEstimate.getTotalMeteringCost());
            params.put("TOTAL_MISCELLANEOUS_CHARGE", costEstimate.getTotalMiscellaneousCharge());
            params.put("TOTAL_ASSEMBLY_LABOR_COST", costEstimate.getTotalAssemblyLaborCost());
            params.put("FREIGHT_HANDLING", costEstimate.getFreightHandling());
            params.put("FREIGHT_HANDLING_PERCENTAGE", decimalFormat.format(costEstimate.getFreightHandlingPercentage())+"%");

            params.put("CONTINGENCY", costEstimate.getContingency());
            params.put("CONTINGENCY_PERCENTAGE", decimalFormat.format(costEstimate.getContingencyPercentage())+"%");

            params.put("SUBREPORT_DIR", GlobalConstant.JASPER_BASE_PATH + "/vouchers/");
            params.put("RECAP_DS", new JRBeanCollectionDataSource(costEstimate.getTotalAssemblyLaborCost().compareTo(BigDecimal.ZERO) == 0 ? new ArrayList<>() : this.makeCostEstimateAssemblyUnitMap(costEstimate.getId())));

            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.CE, costEstimate);
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
            CostEstimate costEstimate = costEstimateRepo.findOneByTransactionId(transId);

            if (costEstimate != null) {
                costEstimate.setDetails(this.getDetails(costEstimate));

                ArrayList<CostEstimateAssemblyUnit> assemblyUnitDetails = costEstimateAssemblyUnitRepo.findByCostEstimateId(costEstimate.getId());
                costEstimate.setCostEstimateAssemblyUnits(assemblyUnitDetails);

                ArrayList<CostEstimateMiscellaneousCharge> costEstimateMiscellaneousCharges = costEstimateMiscellaneousChargeRepo.findByCostEstimateId(costEstimate.getId());
                costEstimate.setMiscellaneousCharges(costEstimateMiscellaneousCharges);

                Map map = forLogMapMain(costEstimate);
                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.CE);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
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

            List<CostEstimate> docs = costEstimateRepo.findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(loggedIn.getId(), fromDate, toDate, docStatusId);
            return this.makeMapList(docs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
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

            List<CostEstimate> docs = costEstimateRepo.findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(loggedIn.getId(), fromDate, toDate, Arrays.asList(ids));
            return this.makeMapList(docs);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public List<Map> getDetailsReport(int id) {
        List<Map> data = new ArrayList<>();

        CostEstimate costEstimate = costEstimateRepo.findById(id).orElse(null);
        if (costEstimate != null) {
            List<CostEstimateDetail> details = costEstimateDetailRepo.findByCostEstimateIdOrderByCategoryAscItemDescriptionAsc(costEstimate.getId());

            if (!details.isEmpty()) {
                int counter = 1;
                for (CostEstimateDetail detail : details) {
                    Map detailMap = new HashMap();

                    detailMap.put("id", 0);
                    detailMap.put("itemId", detail.getItem().getId());
                    detailMap.put("code", detail.getItem().getCode());
                    detailMap.put("description", detail.getItem().getDescription());
                    detailMap.put("quantity", detail.getQuantity());
                    detailMap.put("unitCode", detail.getItem().getUnit() != null ? detail.getItem().getUnit().getCode() : "");
                    detailMap.put("unitId", detail.getItem().getUnit() != null ? detail.getItem().getUnit().getId() : "");
                    detailMap.put("unitCost", detail.getUnitCost());
                    detailMap.put("totalCost", detail.getTotalCost());
                    detailMap.put("inventoryCost", detail.getInventoryCost());
                    detailMap.put("category", detail.getCategory() == 1 ? "A. Accessories" : "B. Metering");

                    data.add(detailMap);
                }
            }
            List<CostEstimateMiscellaneousCharge> costEstimateMiscellaneousCharges = costEstimateMiscellaneousChargeRepo.findByCostEstimateIdOrderByMiscellaneousChargeDescriptionAsc(costEstimate.getId());

            if (!costEstimateMiscellaneousCharges.isEmpty()) {
                int counter = 1;
                for (CostEstimateMiscellaneousCharge detail : costEstimateMiscellaneousCharges) {
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

    public List<Map> makeCostEstimateAssemblyUnitMap(int id) {
        List<Map> data = new ArrayList<>();

        ArrayList<CostEstimateAssemblyUnit> assemblyUnitDetails = costEstimateAssemblyUnitRepo.findByCostEstimateId(id);
        if (!Checker.collectionIsEmpty(assemblyUnitDetails)) {
                int counter = 1;
                for (CostEstimateAssemblyUnit detail : assemblyUnitDetails) {
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

    public ArrayList<CostEstimateDetailDto> getDetails(CostEstimate costEstimate) {
        ArrayList<CostEstimateDetailDto> detailsDto = new ArrayList<>();
        ArrayList<CostEstimateDetail> details = costEstimateDetailRepo.findByCostEstimateId(costEstimate.getId());

        if(!details.isEmpty()) {
            for (CostEstimateDetail detail : details) {
                CostEstimateDetailDto dto = new CostEstimateDetailDto();
                ItemStock itemStock = itemStockRepo.findByItemIdAndInventoryLocationId(detail.getItem().getId(), costEstimate.getInventoryLocation().getId());

                List<CostEstimateAssemblyUnitItem> assemblyUnitItems = costEstimateAssemblyUnitItemRepo.findByCostEstimateAssemblyUnitCostEstimateIdAndItemId(costEstimate.getId(), detail.getItem().getId());

                if(Checker.collectionIsNotEmpty(assemblyUnitItems)) {
                    for (CostEstimateAssemblyUnitItem assemblyUnitItem : assemblyUnitItems) {
                        dto.setAssemblyUnitId(assemblyUnitItem.getCostEstimateAssemblyUnit().getAssemblyUnit().getId());
                        dto.setAssemblyCode(assemblyUnitItem.getCostEstimateAssemblyUnit().getAssemblyUnit().getCode());

                        if (itemStock != null) {
                            AssemblyUnitDetail assemblyUnitDetail = assemblyUnitDetailRepo.findByAssemblyUnitIdAndItemId(assemblyUnitItem.getCostEstimateAssemblyUnit().getAssemblyUnit().getId(), itemStock.getItem().getId());
                            if (assemblyUnitDetail != null) {
                                Map assemblyUnitIdsMap = new HashMap();
                                assemblyUnitIdsMap.put("assemblyUnitId", assemblyUnitItem.getCostEstimateAssemblyUnit().getAssemblyUnit().getId());
                                assemblyUnitIdsMap.put("multiplier", assemblyUnitDetail.getQuantity());
                                dto.getAssemblyUnitIds().add(assemblyUnitIdsMap);
                            }
                        }
                    }
                }

                dto.setItemId(detail.getItem().getId());
                dto.setInventoryCost(detail.getInventoryCost());
                dto.setQuantity(detail.getQuantity());
                dto.setInventoryQty(itemStock != null ? itemStock.getQuantity() : BigDecimal.ZERO);
                dto.setItemCode(detail.getItem().getCode());
                dto.setItemDescription(detail.getItem().getDescription());
                dto.setUnitCode(detail.getItem().getUnit() != null ? detail.getItem().getUnit().getCode() : "");
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

    private Map forLogMapMain(CostEstimate costEstimate) {
        return documentLoggerFacade.makeLog(costEstimate);
    }

    private List<Map> makeMapList(List<CostEstimate> costEstimates) {

        List<Map> mapList = new ArrayList<>();

        if (!Checker.collectionIsEmpty(costEstimates)) {
            for (CostEstimate c : costEstimates) {
                mapList.add(composeMap(c));
            }
        }
        return mapList;
    }

    private Map composeMap(CostEstimate r) {
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
