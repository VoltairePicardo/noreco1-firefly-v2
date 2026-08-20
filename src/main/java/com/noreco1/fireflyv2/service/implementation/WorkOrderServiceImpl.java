package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AssetDepreciationScheduleFacade;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.common.facade.LedgerFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.common.helpers.ServiceUtil;
import com.noreco1.fireflyv2.common.helpers.StringFormatter;
import org.springframework.data.domain.*;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.AssetVoucherLinkType;
import com.noreco1.fireflyv2.model.DocumentType;
import com.noreco1.fireflyv2.model.SLEntityClassification;
import com.noreco1.fireflyv2.model.WorkOrderDetail;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.model.enums.DocumentStatus;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.WorkOrderService;
import com.noreco1.fireflyv2.validator.AssetValidator;
import com.noreco1.fireflyv2.validator.WorkOrderValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service(value = "workOrderServiceImpl")
public class WorkOrderServiceImpl implements WorkOrderService {

    @Autowired
    WorkOrderRepo workOrderRepo;

    @Autowired
    WorkOrderDetailRepo workOrderDetailRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    AssetRepo assetRepo;

    @Autowired
    LedgerFacade ledgerFacade;

    @Autowired
    TemporaryBatchRepo tempBatchRepo;

    @Autowired
    TemporarySubLedgerRepo temporarySubLedgerRepo;

    @Autowired
    TemporaryGeneralLedgerRepo temporaryGeneralLedgerRepo;

    @Autowired
    SegmentAccountRepo segmentAccountRepo;

    @Autowired
    AllocationFactorRepo allocationFactorRepo;

    @Autowired
    SubLedgerRepo subLedgerRepo;

    @Autowired
    MaterialIssueRegisterRepo mirRepo;

    @Autowired
    MrctAssetRepo mrctAssetRepo;

    @Autowired
    private AssetDepreciationScheduleFacade depreciationScheduleFacade;

    @Autowired
    StockReleaseRepo stockReleaseRepo;

    @Autowired
    MaterialCreditTicketRepo materialCreditTicketRepo;

    @Autowired
    private AssetDetailRepo assetDetailRepo;

    @Autowired
    private AssetDepreciationScheduleDetailRepo assetDepreciationScheduleDetailRepo;

    @Autowired
    private AssetDepreciationScheduleRepo assetDepreciationScheduleRepo;

    @Autowired
    private AssetVoucherRepo assetVoucherRepo;

    @Autowired
    private ProjectRepo projectRepo;

    @Transactional
    @Override
    public PostResponse processUpdate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        WorkOrder workOrder = (WorkOrder) entity;
        PostResponse response = new PostResponse();

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        WorkOrderValidator validator = new WorkOrderValidator();
        validator.setWorkOrderRepo(workOrderRepo);
        validator.validate(workOrder, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
            response.setSuccess(false);
        } else {
            WorkOrder existingWorkOrder = workOrderRepo.findById(workOrder.getId()).orElse(null);
            if (existingWorkOrder != null) {

                if (existingWorkOrder.getIsClosed()) {
                    response.setFailureMessage("Update failed. Work order is already closed.");
                } else {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(workOrder.getDate());

                    workOrder.setYear(cal.get(Calendar.YEAR));
                    workOrder.setMonth(cal.get(Calendar.MONTH));

                    SLEntityClassification slEntityClassification = new SLEntityClassification();
                    slEntityClassification.setId(com.noreco1.fireflyv2.model.enums.SLEntityClassification.WORK_ORDER.getId());
                    workOrder.setSlEntityClassification(slEntityClassification);

                    Project project = projectRepo.findById(workOrder.getProject().getId()).orElse(null);
                    if(project != null) {
                        project.setTown(workOrder.getTown());
                        projectRepo.save(project);
                    }

                    existingWorkOrder = workOrderRepo.save(workOrder);
                    if (existingWorkOrder != null) {
                        response.setSuccess(true);
                        response.setModelId(existingWorkOrder.getId());
                        response.setSuccessMessage("Work order successfully updated!");
                    } else {
                        response.setFailureMessage("Failed to update work order");
                    }
                }
            } else {
                response.setFailureMessage("Work order is not available");
            }
        }
        return response;
    }

    @Transactional
    @Override
    public PostResponse processCreate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        WorkOrder workOrder = (WorkOrder) entity;

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        WorkOrderValidator validator = new WorkOrderValidator();
        validator.setWorkOrderRepo(workOrderRepo);
        validator.validate(workOrder, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
            response.setSuccess(false);
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(workOrder.getDate());

            workOrder.setYear(cal.get(Calendar.YEAR));
            workOrder.setMonth(cal.get(Calendar.MONTH));
            workOrder.setAccountNumber(generatorFacade.entityAccountNumber());
            workOrder.setCreatedBy(authenticationFacade.getLoggedIn());

            Project project = projectRepo.findById(workOrder.getProject().getId()).orElse(null);
            String townName = "";
            if(project != null) {
                project.setTown(workOrder.getTown());
                projectRepo.save(project);
                townName = workOrder.getTown().getName().replaceAll(" ", "_");
            }

            Object latestCode = workOrderRepo.findLatestVvCodeByYear(cal.get(Calendar.YEAR));
            String code = generatorFacade.voucherCodeWithTown("WO", StringFormatter.getValueOrBlank(latestCode), workOrder.getDate(), townName, GlobalConstant.COUNTER_PAD_2);

            workOrder.setCode(code);

            SLEntityClassification slEntityClassification = new SLEntityClassification();
            slEntityClassification.setId(com.noreco1.fireflyv2.model.enums.SLEntityClassification.WORK_ORDER.getId());
            workOrder.setSlEntityClassification(slEntityClassification);

            workOrder= workOrderRepo.save(workOrder);
            if (workOrder != null) {
                response.setSuccess(true);
                response.setModelId(workOrder.getId());
                response.setSuccessMessage("Work order successfully created!");
            }
        }
        return response;
    }

    @Transactional(readOnly = true)
    @Override
    public List<WorkOrder> findByStatus(Integer status) {
        if(status == null || status == -1) {
            return workOrderRepo.findAll();
        } else {
            return workOrderRepo.findByIsClosed(status == 1);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<WorkOrder> findByStatus(Integer status, String y, String m) {
        Integer year = -1;
        Integer month = -1;
        try {
            year = Integer.valueOf(y);
        } catch (Exception e) { }
        try {
            month = Integer.valueOf(m);
        } catch (Exception e) {}

        if(status == null || status == -1) {
            if (year != -1 && month != -1) {
                return workOrderRepo.findByYearAndMonth(year, month);
            } else if (year == -1 && month != -1) {
                return workOrderRepo.findByMonth(month);
            } else if (month == -1 && year != -1) {
                return workOrderRepo.findByYear(year);
            } else {
                return workOrderRepo.findAll();
            }
        } else {
            boolean isClosed = status == 1;
            if (year != -1 && month != -1) {
                return workOrderRepo.findByIsClosedAndYearAndMonth(isClosed, year, month);
            } else if (year == -1 && month != -1) {
                return workOrderRepo.findByIsClosedAndMonth(isClosed, month);
            } else if (month == -1 && year != -1) {
                return workOrderRepo.findByIsClosedAndYear(isClosed, year);
            } else {
                return workOrderRepo.findByIsClosed(isClosed);
            }
        }
    }

    @Transactional(readOnly = true)
    @Override
    public WorkOrder findById(Integer id) {
        WorkOrder workOrder = workOrderRepo.findById(id).orElse(null);
        if(workOrder != null && workOrder.getProject() != null) {
            workOrder.setTown(workOrder.getProject().getTown());
        }
        return workOrder;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Map> findVoucherForPosting(Integer workOrderAccountNo) {
        List<Map> data = new ArrayList<>();
        List<Object[]> vouchersForPosting = workOrderRepo.findVouchersForPosting(workOrderAccountNo, DocumentStatus.APPROVED.getId());

        if (!Checker.collectionIsEmpty(vouchersForPosting)){
            for(Object[] row:vouchersForPosting){
                Map m = new HashMap();
                m.put("code", row[0]);
                m.put("description", row[1]);
                m.put("transId", row[2]);

                BigDecimal amount = BigDecimal.ZERO;

                try {
                    BigDecimal totalDr = new BigDecimal(String.valueOf(row[3]));
                    BigDecimal totalCr = new BigDecimal(String.valueOf(row[4]));

                    amount=  totalDr.compareTo(totalCr) >= 0 ?  totalDr:totalCr;
                }finally {}

                m.put("amount", amount);

                data.add(m);
            }
        }
        return data;

    }

    @Transactional(readOnly = true)
    @Override
    public List<Map> getPostedVouchers(Integer id) {
        List<Map> data = new ArrayList<>();
        List<Object[]> rows = workOrderRepo.findPostedVouchers(id);

        if (!Checker.collectionIsEmpty(rows)){
            for(Object[] row:rows){
                Map m = new HashMap();
                m.put("id", row[0]);
                m.put("code", row[1]);
                m.put("date", row[2]);
                m.put("amount", row[3]);
                m.put("description", row[4]);

                data.add(m);
            }
        }

        return data;
    }

    @Transactional(readOnly = true)
    @Override
    public WorkOrderDetail getWorkOrderDetailsByWoId(Integer workOrderId) {
        WorkOrderDetail workOrderDetail = new WorkOrderDetail();

        WorkOrder workOrder = workOrderRepo.findById(workOrderId).orElse(null);

        if (workOrder != null) {
            workOrderDetail.setWorkOrder(workOrder);

            List<WorkOrderDetail> orderDetails = workOrderDetailRepo.findByWorkOrderId(workOrderId);
            if (!Checker.collectionIsEmpty(orderDetails)) {
                for(WorkOrderDetail detail:orderDetails) {
                    BigDecimal overhead = workOrderDetail.getOverhead() == null ? BigDecimal.ZERO :  workOrderDetail.getOverhead();
                    BigDecimal labor = workOrderDetail.getLabor() == null ? BigDecimal.ZERO :  workOrderDetail.getLabor();
                    BigDecimal materials = workOrderDetail.getMaterials() == null ? BigDecimal.ZERO :  workOrderDetail.getMaterials();
                    BigDecimal tax = workOrderDetail.getTax() == null ? BigDecimal.ZERO :  workOrderDetail.getTax();
                    BigDecimal houseConnection = workOrderDetail.getHouseConnection() == null ? BigDecimal.ZERO :  workOrderDetail.getHouseConnection();

                    BigDecimal overheadNew = detail.getOverhead() == null ? BigDecimal.ZERO :  detail.getOverhead();
                    BigDecimal laborNew = detail.getLabor() == null ? BigDecimal.ZERO :  detail.getLabor();
                    BigDecimal materialsNew = detail.getMaterials() == null ? BigDecimal.ZERO :  detail.getMaterials();
                    BigDecimal taxNew = detail.getTax() == null ? BigDecimal.ZERO :  detail.getTax();
                    BigDecimal houseConnectionNew = detail.getHouseConnection() == null ? BigDecimal.ZERO :  detail.getHouseConnection();

                    workOrderDetail.setId(detail.getId());
                    workOrderDetail.setOverhead(overhead.add(overheadNew));
                    workOrderDetail.setLabor(labor.add(laborNew));
                    workOrderDetail.setMaterials(materials.add(materialsNew));
                    workOrderDetail.setTax(tax.add(taxNew));
                    workOrderDetail.setHouseConnection(houseConnection.add(houseConnectionNew));
                }
            }

        }
        return workOrderDetail;
    }

    @Transactional
    @Override
    public PostResponse close(Asset asset, Integer id, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        AssetValidator validator = new AssetValidator();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);
        validator.validate(asset, bindingResult);

        try {
            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
            } else {
                WorkOrder existingWorkOrder = workOrderRepo.findByIdAndIsClosed(id, false);
                if (existingWorkOrder != null) {

                    existingWorkOrder.setIsClosed(true);
                    existingWorkOrder.setClosedBy(authenticationFacade.getLoggedIn());
                    existingWorkOrder.setClosedDatetime(new Date());

                    if (workOrderRepo.save(existingWorkOrder) != null) {

                        // create asset
                        Integer year = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(asset.getAcquisitionDate()));
                        Object latestCode = assetRepo.findLatestCodeByYear(year);
                        String code = generatorFacade.voucherCodeNoOffice("A", (latestCode == null ? "" : String.valueOf(latestCode)), asset.getAcquisitionDate(), GlobalConstant.COUNTER_PAD_4);

                        asset.setRefNo(code);
                        asset.setCode(code);
                        asset.setYear(year);
                        asset.setLocation(existingWorkOrder.getLocation());
                        asset.setAccountNo(generatorFacade.entityAccountNumber());
                        asset.setAcquisitionDate(new Date());
                        asset.setCreatedBy(authenticationFacade.getLoggedIn());
                        asset.setDefaultAnnualDepreciationRate();
                        asset.setDefaultMonthlyDepreciationRate();
                        asset.setWorkOrder(existingWorkOrder);
                        asset.setStatus("ACTIVE");

                        SLEntityClassification assetClass = new SLEntityClassification();
                        assetClass.setId(com.noreco1.fireflyv2.model.enums.SLEntityClassification.ASSET.getId());

                        asset.setSlEntityClassification(assetClass);

                        Asset newAsset = assetRepo.save(asset);

                        if (newAsset != null) {

                            // start: asset details

                            // reset schedule details
                            assetDepreciationScheduleDetailRepo.deleteByDepreciationScheduleAssetIdAndDepreciationScheduleDeducted(asset.getId(), false);
                            // reset schedule
                            assetDepreciationScheduleRepo.deleteByAssetIdAndDeducted(asset.getId(), false);

                            // Asset Details
                            // clear Asset Details
                            if (asset.getId() != null) assetDetailRepo.deleteByAssetId(asset.getId());

                            List<AssetDetail> assetDetails = null;
                            if (!asset.getAssetDetails().isEmpty()) {
                                assetDetails = new ArrayList<>();
                                for(Map row:asset.getAssetDetails())   {

                                    AssetDetail assetDetail = new AssetDetail();
                                    assetDetail.setAsset(newAsset);

                                    assetDetail.setValue(new BigDecimal(row.get("value").toString()));
                                    assetDetail.setMonthlyDepreciation(new BigDecimal(row.get("monthlyDepreciation").toString()));

                                    if (StringFormatter.getValueOrBlank(row.get("depreciatedValue").toString()).length() == 0){
                                        assetDetail.setDepreciatedValue(BigDecimal.ZERO);
                                    } else {
                                        assetDetail.setDepreciatedValue(new BigDecimal(row.get("depreciatedValue").toString()));
                                    }
                                    assetDetail.setRemainingValue(new BigDecimal(row.get("remainingValue").toString()));

                                    Map assetAccountMap = (Map) row.get("assetAccount");
                                    Map accumDepAccountMap = (Map) row.get("accumDepAccount");
                                    Map expenseAccountMap = (Map) row.get("expenseAccount");

                                    Account assetAccount = new Account();
                                    assetAccount.setId(Integer.valueOf(assetAccountMap.get("id").toString()));

                                    Account accumDepAccount = new Account();
                                    accumDepAccount.setId(Integer.valueOf(accumDepAccountMap.get("id").toString()));

                                    Account expenseAccount = new Account();
                                    expenseAccount.setId(Integer.valueOf(expenseAccountMap.get("id").toString()));

                                    assetDetail.setAssetAccount(assetAccount);
                                    assetDetail.setExpenseAccount(expenseAccount);
                                    assetDetail.setAccumDepAccount(accumDepAccount);

                                    AssetDetail newAssetDetail = assetDetailRepo.save(assetDetail);
                                    assetDetails.add(newAssetDetail);
                                }
                            }

                            // end: asset details

                            Transaction transaction = generatorFacade.transaction();

                            DocumentType docType = new DocumentType();
                            docType.setId(com.noreco1.fireflyv2.model.enums.DocumentType.WP.getId());

                            TemporaryBatch temporaryBatch = new TemporaryBatch();
                            temporaryBatch.setDate(newAsset.getAcquisitionDate());
                            temporaryBatch.setVoucherCreated(false);
                            temporaryBatch.setTransaction(transaction);
                            temporaryBatch.setDocumentType(docType);
                            temporaryBatch.setRemarks(asset.getDescription());

                            TemporaryBatch newTempBatch = tempBatchRepo.save(temporaryBatch);

                            if ( newTempBatch != null) {

                                // start: debit
                                if (Checker.collectionIsNotEmpty(assetDetails)) {
                                    Map<Integer, BigDecimal> assetAccountsDetailMap = new HashMap();
                                    java.sql.Date acquisitionDate = new java.sql.Date(newAsset.getAcquisitionDate().getTime());
                                    for(Map row:asset.getAssetDetails())   {
                                        Object valueObj = row.get("value");
                                        BigDecimal value = BigDecimal.valueOf(Double.valueOf(valueObj.toString()));

                                        Map assetAccountMap = (Map) row.get("assetAccount");
                                        Integer assetAccountId = (Integer) assetAccountMap.get("id");

                                        assetAccountsDetailMap = this.setRemainingValue(assetAccountsDetailMap, assetAccountId, value);

//                                        List<Object[]> percentagesDebit = allocationFactorRepo.findByAccountId(assetDetail.getAssetAccount().getId());
//
//                                        for (Object[] obj : percentagesDebit) {
//                                            Integer segmentId = (Integer) obj[1];
//                                            BigDecimal percentage = (BigDecimal) obj[2];
//
//                                            BigDecimal glShare = (percentage.divide(new BigDecimal(100))).multiply(amountDr).setScale(2, BigDecimal.ROUND_HALF_UP);
//
//                                            SegmentAccount segmentAccount = new SegmentAccount();
//                                            segmentAccount.setId(segmentId);
//
//                                            TemporaryGeneralLedger generalLedger = new TemporaryGeneralLedger();
//                                            generalLedger.setTemporaryBatch(newTempBatch);
//                                            generalLedger.setTransaction(transaction);
//                                            generalLedger.setDebit(glShare);
//                                            generalLedger.setCredit(BigDecimal.ZERO);
//                                            generalLedger.setSegmentAccount(segmentAccount);
//
//                                            TemporaryGeneralLedger temporaryGeneralLedger = temporaryGeneralLedgerRepo.save(generalLedger);
//
//                                            if (assetDetail.getAssetAccount().hasSL() == 1) {
//                                                // subledger
//                                                SlEntity slEntity = new SlEntity();
//                                                slEntity.setAccountNo(newAsset.getAccountNo());
//
//                                                TemporarySubLedger subLedger = new TemporarySubLedger();
//                                                subLedger.setTemporaryGeneralLedger(temporaryGeneralLedger);
//                                                subLedger.setTransaction(transaction);
//                                                subLedger.setSegmentAccount(segmentAccount);
//                                                subLedger.setSlEntity(slEntity);
//                                                subLedger.setTemporaryBatch(newTempBatch);
//
//                                                BigDecimal slAmount = temporaryGeneralLedger.getDebit();
//
//                                                // prevent insertion of SL entries having no amount
//                                                if (slAmount == null || slAmount.compareTo(BigDecimal.ZERO) == 0) {
//                                                    continue;
//                                                }
//
//                                                subLedger.setDebit(slAmount);
//                                                subLedger.setCredit(BigDecimal.ZERO);
//
//                                                temporarySubLedgerRepo.save(subLedger);
//                                            }
//                                        }
                                    }

                                    ledgerFacade.saveTempLedgerEntries(assetAccountsDetailMap, acquisitionDate, temporaryBatch, newAsset.getAccountNo(), true);
                                }
                                // end: debit

                                // start: credit
                                // get work-order-detail
                                List<WorkOrderDetail> orderDetails = workOrderDetailRepo.findByWorkOrderId(existingWorkOrder.getId());
                                // loop orderDetails to get accounting entries
                                for(WorkOrderDetail workOrderDetail:orderDetails) {
                                    // get accounting entries (subledger table) by work-order-detail.trans-id & work-order.accountNo
                                    List<Object[]> objects = subLedgerRepo.findByTransactionIdAndAccountNo(workOrderDetail.getTransaction().getId(), existingWorkOrder.getAccountNumber());

                                    for(Object[] object:objects) {
                                        BigDecimal amountCr = new BigDecimal(String.valueOf(object[2]));
                                        Integer segmentAccountId = (Integer) object[4];

                                        SegmentAccount segmentAccount = new SegmentAccount();
                                        segmentAccount.setId(segmentAccountId);

                                        TemporaryGeneralLedger generalLedger = new TemporaryGeneralLedger();
                                        TemporaryGeneralLedger exTempGl = temporaryGeneralLedgerRepo.findOneByTemporaryBatchIdAndSegmentAccountIdAndCreditIsNotNullAndCreditGreaterThan(newTempBatch.getId(), segmentAccountId, BigDecimal.ZERO);
                                        if (exTempGl != null)  {
                                            generalLedger = exTempGl;
                                        } else {
                                            generalLedger.setTemporaryBatch(newTempBatch);
                                            generalLedger.setTransaction(transaction);
                                            generalLedger.setSegmentAccount(segmentAccount);
                                        }
                                        generalLedger.setDebit(BigDecimal.ZERO);
                                        generalLedger.setCredit((generalLedger.getCredit() != null) ? generalLedger.getCredit().add(amountCr) : amountCr);

                                        TemporaryGeneralLedger temporaryGeneralLedger = temporaryGeneralLedgerRepo.save(generalLedger);

                                        SegmentAccount segmentAccountQ = segmentAccountRepo.findById(segmentAccountId).orElse(null);
                                        if (segmentAccountQ != null && segmentAccountQ.getAccount().getHasSL() == 1) {

                                            // subledger
                                            SlEntity slEntity = new SlEntity();
                                            slEntity.setAccountNo(existingWorkOrder.getAccountNumber());

                                            TemporarySubLedger subLedger = new TemporarySubLedger();
                                            TemporarySubLedger exTempSl = temporarySubLedgerRepo.findOneByTemporaryBatchIdAndSegmentAccountIdAndCreditIsNotNullAndCreditGreaterThan(newTempBatch.getId(), segmentAccountId, BigDecimal.ZERO);
                                            if (exTempSl != null)  {
                                                subLedger = exTempSl;
                                            } else {
                                                subLedger.setTemporaryGeneralLedger(temporaryGeneralLedger);
                                                subLedger.setTransaction(transaction);
                                                subLedger.setSegmentAccount(segmentAccount);
                                                subLedger.setTemporaryBatch(newTempBatch);
                                            }
                                            subLedger.setTemporaryGeneralLedger(temporaryGeneralLedger);
                                            subLedger.setTransaction(transaction);
                                            subLedger.setSegmentAccount(segmentAccount);
                                            subLedger.setTemporaryBatch(newTempBatch);

                                            subLedger.setSlEntity(slEntity);
                                            BigDecimal slAmount = amountCr;

                                            // prevent insertion of SL entries having no amount
                                            if (slAmount == null || slAmount.compareTo(BigDecimal.ZERO) == 0) {
                                                continue;
                                            }

                                            subLedger.setDebit(BigDecimal.ZERO);
                                            subLedger.setCredit((subLedger.getCredit() != null) ? subLedger.getCredit().add(slAmount) : slAmount);

                                            temporarySubLedgerRepo.save(subLedger);
                                        }
                                    }

                                    // insert: for MIR
                                    Integer voucherTransId = workOrderDetail.getTransaction().getId();
                                    MaterialIssueRegister mir = mirRepo.findOneByTransactionIdAndInventoryDocType(voucherTransId, InventoryDocType.MCT.name());
                                    if (mir != null) {
                                        StockRelease mrct = stockReleaseRepo.findOneByTransactionId(mir.getInvDocTransactionId());
                                        if (mrct != null) {

                                            MrctAsset mrctAsset = new MrctAsset();
                                            mrctAsset.setMrct(mrct);
                                            mrctAsset.setAsset(newAsset);

                                            mrctAssetRepo.save(mrctAsset);
                                        }
                                    }
                                }
                                // end: credit

                                // Closed out project status
                                Project project = existingWorkOrder.getProject();
                                com.noreco1.fireflyv2.model.DocumentStatus documentStatus = new com.noreco1.fireflyv2.model.DocumentStatus();
                                documentStatus.setId(DocumentStatus.CLOSED_OUT.getId());
                                project.setDocumentStatus(documentStatus);

                                projectRepo.save(project);

                                // depreciation schedule
                                depreciationScheduleFacade.generateSchedule(newAsset);

                                //create row on assetvoucher table
                                AssetVoucher assetVoucher = new AssetVoucher();
                                assetVoucher.setAsset(newAsset);
                                assetVoucher.setCreatedAt(new Date());

                                AssetVoucherLinkType assetVoucherLinkType = new AssetVoucherLinkType();
                                assetVoucherLinkType.setId(com.noreco1.fireflyv2.model.enums.AssetVoucherLinkType.ACQUISITION.getId());
                                assetVoucher.setAssetVoucherLinkType(assetVoucherLinkType);

                                assetVoucher.setTransaction(newTempBatch.getTransaction());

                                DocumentType documentType = new DocumentType();
                                documentType.setId(com.noreco1.fireflyv2.model.enums.DocumentType.JV.getId());
                                assetVoucher.setDocumentType(documentType);
                                assetVoucher.setCreatedBy(authenticationFacade.getLoggedIn());

                                assetVoucherRepo.save(assetVoucher);

                                response.setSuccess(true);
                                response.setSuccessMessage("Work order successfully closed out");
                            }

                        } else {
                            response.setFailureMessage("Creating asset failed");
                        }
                    } else {
                        response.setFailureMessage("Closing work order failed");
                    }
                } else {
                    response.setFailureMessage("Work order is not available or already been closed");
                }
            }
        }catch (Exception ex) {
            Logger.getLogger(AssetServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }

    @Transactional
    @Override
    public PostResponse createDetail(WorkOrderDetail workOrderDetail, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        try {
            workOrderDetail.setCreatedBy(authenticationFacade.getLoggedIn());
            workOrderDetail = workOrderDetailRepo.save(workOrderDetail);
            if (workOrderDetail != null) {
                response.setModelId(workOrderDetail.getId());
                response.setSuccessMessage("Work order detail successfully saved");
            } else {
                response.setFailureMessage("Failed to save work order detail");
            }
        }catch (Exception ex) {
            Logger.getLogger(WorkOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }

    @Transactional
    @Override
    public PostResponse updateDetail(WorkOrderDetail workOrderDetail, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        try {
            WorkOrderDetail existingWoDetail = workOrderDetailRepo.findById(workOrderDetail.getId()).orElse(null);
            if (existingWoDetail != null) {
                workOrderDetail.setCreatedBy(existingWoDetail.getCreatedBy());
                workOrderDetail = workOrderDetailRepo.save(workOrderDetail);
                if (workOrderDetail != null) {
                    response.setModelId(workOrderDetail.getId());
                    response.setSuccessMessage("Work order detail successfully updated");
                } else {
                    response.setFailureMessage("Failed to update work order detail");
                }
            } else {
                response.setFailureMessage("Work order detail is not available");
            }
        }catch (Exception ex) {
            Logger.getLogger(WorkOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }

    @Transactional(readOnly = true)
    @Override
    public List<com.noreco1.fireflyv2.controller.response.reports.WorkOrderDetail> getWorkOrderReportDetails(String asOf) {

        Date asOfDate = new java.sql.Date(new Date().getTime());
        try {
            long l = Long.parseLong(asOf);
            asOfDate = new java.sql.Date(l);
        } catch (Exception e) { }

        List<com.noreco1.fireflyv2.controller.response.reports.WorkOrderDetail> reportDetails = new ArrayList<>();
        List<Object[]> queryResult = workOrderRepo.findWorkOrderAsOfTheSpecifiedDate(asOfDate);

        for(Object[] obj : queryResult){

            com.noreco1.fireflyv2.controller.response.reports.WorkOrderDetail detail = new com.noreco1.fireflyv2.controller.response.reports.WorkOrderDetail();

            String code = (String)obj[0];
            String description = (String)obj[1];
            Date date = (Date)obj[2];
            BigDecimal materials = (BigDecimal)obj[3];
            BigDecimal labor = (BigDecimal)obj[4];
            BigDecimal overhead = (BigDecimal)obj[5];
            BigDecimal total = (BigDecimal)obj[6];
            BigDecimal houseConnection = (BigDecimal)obj[7];
            Date targetDate = (Date)obj[8];

            Boolean isNegative = total != null? total.compareTo(BigDecimal.ZERO) == -1 : false;

            detail.setWorkOrderNo(code);
            detail.setProjectDescription(description);
            detail.setDateStarted(date);
            detail.setMaterials(materials != null? materials : BigDecimal.ZERO);
            detail.setLabor(labor != null? labor : BigDecimal.ZERO);
            detail.setOverhead(overhead != null? overhead : BigDecimal.ZERO);
            detail.setTargetCompletion(targetDate);
            detail.setHouseConnections(houseConnection != null? houseConnection : BigDecimal.ZERO);

            if (isNegative) {
                detail.setTotalNegative(total != null? total : BigDecimal.ZERO);
            } else {
                detail.setTotalPositive(total != null? total : BigDecimal.ZERO);
            }

            reportDetails.add(detail);
        }

        return reportDetails;
    }

    @Transactional(readOnly = true)
    @Override
    public List<WorkOrder> findAll() {
        return workOrderRepo.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public List<Map> getProjectCostEstimateDetail(Integer workOrderId, Integer invLocId, Integer invCatId) {
        List<Map> data = new ArrayList<>();
        List<Object[]> rows = workOrderRepo.findProjectCostEstimateDetailById(workOrderId, invLocId, invCatId);

        if (!Checker.collectionIsEmpty(rows)){
            for(Object[] row:rows){
                Map m = new HashMap();
                m.put("quantityReleased", 0);
                m.put("inventoryBalance", row[0]);
                m.put("itemStockId", row[1]);
                m.put("itemId", row[2]);
                m.put("itemCode", row[3]);
                m.put("unitCode", row[4]);
                m.put("unitId", row[5]);
                m.put("unitCost", row[6]);
                m.put("itemDescription", row[7]);
                m.put("inventoryLocationId", row[8]);
                m.put("quantity", row[9]);

                data.add(m);
            }
        }

        return data;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Map> getProjectCostEstimateDetail(Integer workOrderId) {
        List<Map> data = new ArrayList<>();
        List<Object[]> rows = workOrderRepo.findProjectCostEstimateDetailById(workOrderId);

        if (!Checker.collectionIsEmpty(rows)){
            for(Object[] row:rows){
                Map m = new HashMap();
                m.put("quantityReleased", 0);
                m.put("inventoryBalance", row[0]);
                m.put("itemStockId", row[1]);
                m.put("itemId", row[2]);
                m.put("itemCode", row[3]);
                m.put("unitCode", row[4]);
                m.put("unitId", row[5]);
                m.put("unitCost", row[6]);
                m.put("itemDescription", row[7]);
                m.put("inventoryLocationId", row[8]);
                m.put("quantity", row[9]);
                m.put("inventoryCategoryId", row[10]);

                data.add(m);
            }
        }

        return data;
    }

    // -------------------------------------------------------------------------
    // REST API implementations
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    @Override
    public Page<WorkOrder> list(Integer statusId, String year, String month, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        List<WorkOrder> all = workOrderRepo.findAll(Sort.by("id").descending());

        if (statusId != null) {
            boolean closed = statusId == 1;
            all = all.stream().filter(w -> Boolean.TRUE.equals(w.getIsClosed()) == closed).toList();
        }
        if (year != null && !year.isBlank()) {
            try {
                int y = Integer.parseInt(year);
                all = all.stream().filter(w -> w.getYear() != null && w.getYear() == y).toList();
            } catch (NumberFormatException ignored) {}
        }
        if (month != null && !month.isBlank()) {
            try {
                int m = Integer.parseInt(month);
                all = all.stream().filter(w -> w.getMonth() != null && w.getMonth() == m).toList();
            } catch (NumberFormatException ignored) {}
        }
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase();
            all = all.stream().filter(w ->
                (w.getCode()        != null && w.getCode().toLowerCase().contains(q)) ||
                (w.getDescription() != null && w.getDescription().toLowerCase().contains(q))
            ).toList();
        }

        int total = all.size();
        int start = (int) pageable.getOffset();
        int end   = Math.min(start + pageable.getPageSize(), total);
        List<WorkOrder> content = start > total ? Collections.emptyList() : all.subList(start, end);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public PostResponse createFromPayload(Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        try {
            WorkOrder wo = buildWorkOrderFromPayload(payload, null);
            WorkOrder saved = workOrderRepo.save(wo);
            updateProjectTown(saved.getProject(), saved.getTown());
            response.setSuccessMessage("Work Order successfully created.");
            response.setModelId(saved.getId());
        } catch (Exception e) {
            response.setFailureMessage("Failed to create Work Order: " + e.getMessage());
        }
        return response;
    }

    @Override
    public PostResponse updateFromPayload(Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        try {
            Object idObj = payload.get("id");
            if (idObj == null) { response.setFailureMessage("ID is required for update."); return response; }
            Integer id = ((Number) idObj).intValue();
            WorkOrder existing = workOrderRepo.findById(id).orElse(null);
            if (existing == null) { response.setFailureMessage("Work Order not found."); return response; }

            Object dateObj = payload.get("date");
            if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
                try {
                    java.util.Date d = java.sql.Date.valueOf(dateStr);
                    existing.setDate(d);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    existing.setYear(cal.get(Calendar.YEAR));
                    existing.setMonth(cal.get(Calendar.MONTH) + 1);
                } catch (Exception ignored) {}
            }

            if (payload.get("description") != null) existing.setDescription((String) payload.get("description"));
            if (payload.get("location")    != null) existing.setLocation((String) payload.get("location"));

            Object pcFrom = payload.get("periodCoveredFrom");
            if (pcFrom instanceof String s && !s.isBlank()) {
                try { existing.setPeriodCoveredFrom(java.sql.Date.valueOf(s)); } catch (Exception ignored) {}
            }
            Object pcTo = payload.get("periodCoveredTo");
            if (pcTo instanceof String s && !s.isBlank()) {
                try { existing.setPeriodCoveredTo(java.sql.Date.valueOf(s)); } catch (Exception ignored) {}
            }

            String typeStr = (String) payload.get("type");
            if (typeStr != null) existing.setType(mapWorkOrderType(typeStr));

            Object projectObj = payload.get("project");
            if (projectObj instanceof Map<?, ?> pm && pm.get("id") != null) {
                Project p = new Project();
                p.setId(((Number) pm.get("id")).intValue());
                existing.setProject(p);
            }

            Object townObj = payload.get("town");
            if (townObj instanceof Map<?, ?> tm && tm.get("id") != null) {
                Town t = new Town();
                t.setId(((Number) tm.get("id")).intValue());
                existing.setTown(t);
            }

            existing.setUpdatedAt(new java.util.Date());
            WorkOrder saved = workOrderRepo.save(existing);
            updateProjectTown(saved.getProject(), saved.getTown());
            response.setSuccessMessage("Work Order successfully updated.");
            response.setModelId(saved.getId());
        } catch (Exception e) {
            response.setFailureMessage("Failed to update Work Order: " + e.getMessage());
        }
        return response;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Map<String, Object>> getPostedVouchersMap(Integer id) {
        List<Object[]> rows = workOrderRepo.findPostedVouchers(id);
        List<Map<String, Object>> result = new ArrayList<>();
        if (rows != null) {
            for (Object[] row : rows) {
                Map<String, Object> m = new HashMap<>();
                m.put("id",          row[0]);
                m.put("code",        row[1]);
                m.put("date",        row[2]);
                m.put("amount",      row[3]);
                m.put("description", row[4]);
                result.add(m);
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    @Override
    public WorkOrderDetail getWorkOrderDetailSummary(Integer id) {
        WorkOrderDetail summary = new WorkOrderDetail();
        List<WorkOrderDetail> details = workOrderDetailRepo.findByWorkOrderId(id);
        if (details == null || details.isEmpty()) return summary;

        BigDecimal labor = BigDecimal.ZERO, overhead = BigDecimal.ZERO,
                   materials = BigDecimal.ZERO, tax = BigDecimal.ZERO,
                   houseConnection = BigDecimal.ZERO;

        for (WorkOrderDetail d : details) {
            labor           = labor.add(d.getLabor()           != null ? d.getLabor()           : BigDecimal.ZERO);
            overhead        = overhead.add(d.getOverhead()      != null ? d.getOverhead()        : BigDecimal.ZERO);
            materials       = materials.add(d.getMaterials()    != null ? d.getMaterials()       : BigDecimal.ZERO);
            tax             = tax.add(d.getTax()               != null ? d.getTax()             : BigDecimal.ZERO);
            houseConnection = houseConnection.add(d.getHouseConnection() != null ? d.getHouseConnection() : BigDecimal.ZERO);
            summary.setId(d.getId());
        }
        summary.setLabor(labor);
        summary.setOverhead(overhead);
        summary.setMaterials(materials);
        summary.setTax(tax);
        summary.setHouseConnection(houseConnection);
        return summary;
    }

    @Override
    public PostResponse postWorkOrder(Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        try {
            Object woIdObj = payload.get("workOrderId");
            Object txIdObj = payload.get("voucherId");
            if (woIdObj == null || txIdObj == null) {
                response.setFailureMessage("Work Order and Voucher are required.");
                return response;
            }

            java.util.Date now = new java.util.Date();
            WorkOrderDetail detail = new WorkOrderDetail();

            WorkOrder wo = new WorkOrder();
            wo.setId(((Number) woIdObj).intValue());
            detail.setWorkOrder(wo);

            Transaction tx = new Transaction();
            tx.setId(((Number) txIdObj).intValue());
            detail.setTransaction(tx);

            detail.setMaterials(parseBigDecimalValue(payload.get("materials")));
            detail.setLabor(parseBigDecimalValue(payload.get("labor")));
            detail.setOverhead(parseBigDecimalValue(payload.get("overhead")));
            detail.setHouseConnection(parseBigDecimalValue(payload.get("houseConnection")));
            detail.setTax(parseBigDecimalValue(payload.get("inputTax")));
            detail.setCreatedBy(authenticationFacade.getLoggedIn());
            detail.setCreatedAt(now);
            detail.setUpdatedAt(now);

            WorkOrderDetail saved = workOrderDetailRepo.save(detail);
            response.setSuccessMessage("Work Order posting successfully saved.");
            response.setModelId(saved.getId());
        } catch (Exception e) {
            response.setFailureMessage("Failed to post Work Order: " + e.getMessage());
        }
        return response;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Map<String, Object>> getLogs(Integer id) {
        List<Map<String, Object>> result = new ArrayList<>();
        WorkOrder wo = workOrderRepo.findById(id).orElse(null);
        if (wo == null) return result;

        if (wo.getCreatedBy() != null || wo.getCreatedAt() != null) {
            Map<String, Object> created = new HashMap<>();
            created.put("id",        1);
            created.put("action",    "Created");
            created.put("createdAt", wo.getCreatedAt());
            created.put("createdBy", wo.getCreatedBy());
            created.put("remarks",   "Work order created");
            result.add(created);
        }

        if (Boolean.TRUE.equals(wo.getIsClosed()) && wo.getClosedDatetime() != null) {
            Map<String, Object> closed = new HashMap<>();
            closed.put("id",        2);
            closed.put("action",    "Closed Out");
            closed.put("createdAt", wo.getClosedDatetime());
            closed.put("createdBy", wo.getClosedBy());
            closed.put("remarks",   "Work order closed out");
            result.add(closed);
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private WorkOrder buildWorkOrderFromPayload(Map<String, Object> payload, Integer id) {
        WorkOrder wo = new WorkOrder();
        if (id != null) wo.setId(id);

        java.util.Date now = new java.util.Date();

        Object dateObj = payload.get("date");
        java.util.Date woDate = now;
        if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
            try { woDate = java.sql.Date.valueOf(dateStr); } catch (Exception ignored) {}
        }
        wo.setDate(woDate);
        wo.setTargetDate(woDate);

        Calendar cal = Calendar.getInstance();
        cal.setTime(woDate);
        wo.setYear(cal.get(Calendar.YEAR));
        wo.setMonth(cal.get(Calendar.MONTH) + 1);

        if (payload.get("description") != null) wo.setDescription((String) payload.get("description"));
        if (payload.get("location")    != null) wo.setLocation((String) payload.get("location"));

        Object pcFrom = payload.get("periodCoveredFrom");
        if (pcFrom instanceof String s && !s.isBlank()) {
            try { wo.setPeriodCoveredFrom(java.sql.Date.valueOf(s)); } catch (Exception ignored) {}
        }
        Object pcTo = payload.get("periodCoveredTo");
        if (pcTo instanceof String s && !s.isBlank()) {
            try { wo.setPeriodCoveredTo(java.sql.Date.valueOf(s)); } catch (Exception ignored) {}
        }

        String typeStr = (String) payload.get("type");
        wo.setType(mapWorkOrderType(typeStr));

        Object projectObj = payload.get("project");
        if (projectObj instanceof Map<?, ?> pm && pm.get("id") != null) {
            Project p = new Project();
            p.setId(((Number) pm.get("id")).intValue());
            wo.setProject(p);
        }

        Object townObj = payload.get("town");
        if (townObj instanceof Map<?, ?> tm && tm.get("id") != null) {
            Town t = new Town();
            t.setId(((Number) tm.get("id")).intValue());
            wo.setTown(t);
        }

        Object latestCode = workOrderRepo.findLatestVvCodeByYear(cal.get(Calendar.YEAR));
        String code = generatorFacade.voucherCodeNoOffice(
                "WO", latestCode == null ? "" : String.valueOf(latestCode),
                woDate, GlobalConstant.COUNTER_PAD_4);
        wo.setCode(code);

        wo.setAccountNumber(generatorFacade.entityAccountNumber());
        wo.setCreatedAt(now);
        wo.setUpdatedAt(now);
        wo.setCreatedBy(authenticationFacade.getLoggedIn());
        wo.setIsClosed(false);

        SLEntityClassification slClass = new SLEntityClassification();
        slClass.setId(com.noreco1.fireflyv2.model.enums.SLEntityClassification.WORK_ORDER.getId());
        wo.setSlEntityClassification(slClass);

        return wo;
    }

    private Integer mapWorkOrderType(String typeStr) {
        if (typeStr == null) return null;
        return switch (typeStr.toUpperCase()) {
            case "LABOR"     -> 1;
            case "MATERIALS" -> 2;
            case "BOTH"      -> 3;
            default          -> null;
        };
    }

    private BigDecimal parseBigDecimalValue(Object val) {
        if (val == null) return BigDecimal.ZERO;
        try { return new BigDecimal(val.toString()); }
        catch (Exception e) { return BigDecimal.ZERO; }
    }

    private void updateProjectTown(Project project, Town town) {
        if (project == null || project.getId() == null || town == null) return;
        projectRepo.findById(project.getId()).ifPresent(p -> {
            p.setTown(town);
            projectRepo.save(p);
        });
    }

    private Map setRemainingValue(Map accountDetailMap, Integer accountId, BigDecimal value) {
        Object remainingValueExpObj = accountDetailMap.get(accountId);
        if(remainingValueExpObj != null) {
            BigDecimal exValue  = (BigDecimal) remainingValueExpObj;
            BigDecimal newValue = value.add(exValue);

            accountDetailMap.put(accountId, newValue);
        } else {
            accountDetailMap.put(accountId, value);
        }

        return accountDetailMap;
    }
}
