package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.common.helpers.StringFormatter;
import com.noreco1.fireflyv2.common.facade.AssetDepreciationScheduleFacade;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.common.facade.SignatureFacade;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.controller.form.AssetVoucherLinkForm;
import com.noreco1.fireflyv2.controller.form.RetireAssetForm;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.AssetVoucherLinkType;
import com.noreco1.fireflyv2.model.SLEntityClassification;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.AssetService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.AssetRetirementValidator;
import com.noreco1.fireflyv2.validator.AssetValidator;
import com.noreco1.fireflyv2.validator.AssetVoucherLinkValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service(value="assetServiceImpl")
public class AssetServiceImpl implements AssetService {

    @Autowired
    AssetRepo assetRepo;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private AssetDepreciationScheduleFacade depreciationScheduleFacade;

    @Autowired
    private MrctAssetRepo mrctAssetRepo;

    @Autowired
    private AssetDepreciationDetailRepo assetDepreciationDetailRepo;

    @Autowired
    private AssetDetailRepo assetDetailRepo;

    @Autowired
    private AssetDepreciationScheduleDetailRepo assetDepreciationScheduleDetailRepo;

    @Autowired
    private AssetDepreciationScheduleRepo assetDepreciationScheduleRepo;

    @Autowired
    private StockTransactionDetailRepo stockTransactionDetailRepo;

    @Autowired
    private SubLedgerRepo subLedgerRepo;

    @Autowired
    private GeneralLedgerRepo generalLedgerRepo;

    @Autowired
    private AssetVoucherRepo assetVoucherRepo;

    @Autowired
    private AssetVoucherLinkTypeRepo assetVoucherLinkTypeRepo;

    @Autowired
    private JournalVoucherRepo journalVoucherRepo;

    @Autowired
    private MaterialIssueRegisterRepo materialIssueRegisterRepo;

    @Autowired
    private CheckVoucherRepo checkVoucherRepo;

    @Autowired
    private TemporaryBatchRepo temporaryBatchRepo;

    @Autowired
    AllocationFactorRepo allocationFactorRepo;

    @Autowired
    LedgerFacade ledgerFacade;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    SignatureFacade signatureFacade;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Asset findByRefNo(String refNo) {
       return assetRepo.findOneByRefNoOrCode(refNo, refNo);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public HashMap findById(Integer id) {
        HashMap map = new HashMap();
        Asset asset = assetRepo.findById(id).orElse(null);

        if (asset != null) {
            map = composeHashMap(asset);

            List<AssetVoucher> linkedVouchers = assetVoucherRepo.findByAssetId(asset.getId());
            List<Map> vouchers = new ArrayList<>();

            for(AssetVoucher voucher: linkedVouchers) {

                Map row = new HashMap();

                if(voucher.getDocumentType().getId().equals(DocumentType.JV.getId())) {
                    JournalVoucher journalVoucher = journalVoucherRepo.findOneByTransactionId(voucher.getTransaction().getId());

                    if(journalVoucher != null) {
                        row.put("code", journalVoucher.getCode());
                        row.put("date", journalVoucher.getVoucherDate());
                        row.put("particulars", journalVoucher.getExplanation());
                        row.put("amount", journalVoucher.getAmount());

                        vouchers.add(row);
                    }

                } else if(voucher.getDocumentType().getId().equals(DocumentType.MR.getId())) {
                    MaterialIssueRegister materialIssueRegister = materialIssueRegisterRepo.findOneByTransactionId(voucher.getTransaction().getId());

                    if(materialIssueRegister != null) {
                        row.put("code", materialIssueRegister.getCode());
                        row.put("date", materialIssueRegister.getVoucherDate());
                        row.put("particulars", materialIssueRegister.getParticulars());
                        row.put("amount", materialIssueRegister.getAmount());

                        vouchers.add(row);
                    }

                } else  if(voucher.getDocumentType().getId().equals(DocumentType.MR.getId())) {
                    CheckVoucher checkVoucher = checkVoucherRepo.findOneByTransactionId(voucher.getTransaction().getId());

                    if(checkVoucher != null) {
                        row.put("code", checkVoucher.getCode());
                        row.put("date", checkVoucher.getVoucherDate());
                        row.put("amount", checkVoucher.getAmount());
                        row.put("particulars", checkVoucher.getParticulars());

                        vouchers.add(row);
                    }
                }
            }

            map.put("vouchers", vouchers);
        }

        return map;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<HashMap> findAll() {
        List<HashMap> mapList = new ArrayList<>();
        List<Asset> assets = assetRepo.findAll();

        if (!Checker.collectionIsEmpty(assets)) {
            for (Asset a : assets) {
                mapList.add(composeHashMap(a));
            }
        }

        return mapList;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Map> getItems(Integer assetId) {
        List<Map> data = new ArrayList<>();
        try {
            List<MrctAsset> mrctAssets = mrctAssetRepo.findByAssetId(assetId);
            if (!mrctAssets.isEmpty()) {
                for (MrctAsset mrctAsset:mrctAssets) {
                    Asset asset = mrctAsset.getAsset();

                    List<StockTransactionDetail> mrctDetailList = stockTransactionDetailRepo.findByStockTransactionTransactionId(mrctAsset.getMrct().getTransaction().getId());
                    if (!mrctDetailList.isEmpty()) {
                        for(StockTransactionDetail stockTransactionDetail:mrctDetailList) {
                            Item item = stockTransactionDetail.getItemStock().getItem();
                            if (item != null) {

                                Map map = new HashMap();
                                map.put("itemCode", item.getCode());
                                map.put("description", item.getDescription());
                                map.put("mrctNumber", mrctAsset.getMrct().getCode());
                                map.put("unit", item.getUnit() == null ? "":item.getUnit().getCode());
                                map.put("quantity", stockTransactionDetail.getQuantity());
                                map.put("cost", stockTransactionDetail.getUnitCost());
                                map.put("total", stockTransactionDetail.getTotalCost());

                                BigDecimal depreciatedAmountPerUnit = BigDecimal.ZERO;
                                BigDecimal depreciatedAmountTotal = BigDecimal.ZERO;
                                BigDecimal remainingValuePerUnit = BigDecimal.ZERO;
                                BigDecimal remainingValueTotal = stockTransactionDetail.getTotalCost();

//                                Integer count = assetDepreciationDetailRepo.countAssetDepreciationDetailByAssetId(asset.getId());
//                                if (count != null && count > 0) {
//
//                                    Integer noOfMonthsDepreciated = count;
//                                    // (monthly depreciation rate / 100)
//                                    BigDecimal monthlyDepreciationRate = asset.getMonthlyDepreciationRate().divide(new BigDecimal(100));
//
//                                    // Depreciated amount per unit = Number of months depreciated x (monthly depreciation rate / 100) x Item cost
//                                    depreciatedAmountPerUnit = monthlyDepreciationRate.multiply(stockTransactionDetail.getUnitCost()).multiply(new BigDecimal(noOfMonthsDepreciated));
//                                    depreciatedAmountPerUnit = depreciatedAmountPerUnit.setScale(2, BigDecimal.ROUND_HALF_UP);
//
//                                    // Depreciated amount total = depreciated amount per unit  x quantity
//                                    depreciatedAmountTotal = depreciatedAmountPerUnit.multiply(stockTransactionDetail.getQuantity());
//
//                                    // Remaining value per unit = Item Cost - depreciated amount per unit
//                                    remainingValuePerUnit = stockTransactionDetail.getUnitCost().subtract(depreciatedAmountPerUnit);
//
//                                    // Remaining value total = Remaining value per unit x quantity
//                                    remainingValueTotal = remainingValuePerUnit.multiply(stockTransactionDetail.getQuantity());
//
//                                } else {
//                                    remainingValuePerUnit = stockTransactionDetail.getUnitCost();
//                                }
//
//                                map.put("depreciatedAmountPerUnit", depreciatedAmountPerUnit);
//                                map.put("depreciatedAmountTotal", depreciatedAmountTotal);
//                                map.put("remainingValuePerUnit", remainingValuePerUnit);
//                                map.put("remainingValueTotal", remainingValueTotal);

                                data.add(map);
                            }
                        }
                    }
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Map> getDetails(Integer assetId, Integer voucherTransNo, Integer transType) {
        List<Map> data = new ArrayList<>();
        List<Account> assetAccountsWithSL = new ArrayList<>();

        boolean queryVoucherData = Checker.isValidId(voucherTransNo);

        Asset asset = assetRepo.findById(assetId).orElse(null);

        List<AssetDetail> detailList = assetDetailRepo.findByAssetId(assetId);
        List<AssetDepreciationSchedule> processedDepreciation = assetDepreciationScheduleRepo.findByAssetIdAndDeductedOrderByYearAscMonthAsc(assetId, true);
        BigDecimal remainingLifeInMonths = BigDecimal.valueOf(asset.getDepreciationMonths()).subtract(BigDecimal.valueOf(processedDepreciation.size()));

        for(AssetDetail assetDetail: detailList) {

            Map row = new HashMap();

            Account assetA = new Account();
            assetA.setId(assetDetail.getAssetAccount().getId());
            assetA.setCode(assetDetail.getAssetAccount().getCode());
            assetA.setTitle(assetDetail.getAssetAccount().getTitle());

            Account expenseA = new Account();
            expenseA.setId(assetDetail.getExpenseAccount().getId());
            expenseA.setCode(assetDetail.getExpenseAccount().getCode());
            expenseA.setTitle(assetDetail.getExpenseAccount().getTitle());

            Account accumDepA = new Account();
            accumDepA.setId(assetDetail.getAccumDepAccount().getId());
            accumDepA.setCode(assetDetail.getAccumDepAccount().getCode());
            accumDepA.setTitle(assetDetail.getAccumDepAccount().getTitle());

            row.put("id", assetDetail.getId());
            row.put("assetAccount", assetA);
            row.put("expenseAccount", expenseA);
            row.put("accumDepAccount", accumDepA);
            row.put("value", assetDetail.getValue());
            row.put("valueOrig", assetDetail.getValue());

            BigDecimal debit = BigDecimal.ZERO;
            row.put("debit", debit);

            if(queryVoucherData && !Checker.isAssetAdjustment(transType)) {

                List<Object[]> assetAccountDebitEntry = subLedgerRepo.findAssetAccountDebitEntry(asset.getAccountNo(), voucherTransNo, asset.getId(), assetA.getId());
                if(Checker.collectionIsNotEmpty(assetAccountDebitEntry)) {
                    Object debitObj = assetAccountDebitEntry.get(0);
                    debit =(BigDecimal) debitObj;

                    row.put("debit", debit);
                    row.put("value", assetDetail.getValue().add(debit));

                    assetAccountsWithSL.add(assetA);
                }
            }

            BigDecimal depreciatedValue = assetDetail.getDepreciatedValue() == null? BigDecimal.ZERO :  assetDetail.getDepreciatedValue();

            row.put("depreciatedValue", depreciatedValue);
            row.put("depreciatedValueOrig", depreciatedValue);

            BigDecimal remainingValue = assetDetail.getValue().subtract(depreciatedValue);
            remainingValue = remainingValue.add(debit);
            row.put("remainingValue", remainingValue);

            BigDecimal monthlyDepreciation = assetDetail.getMonthlyDepreciation();
            if(queryVoucherData && !Checker.isAssetAdjustment(transType)) {
                monthlyDepreciation = remainingValue.divide(remainingLifeInMonths, 2, RoundingMode.HALF_UP);
            }
            row.put("monthlyDepreciation", monthlyDepreciation);

            // check if asset is processed for depreciation
            AssetDepreciationScheduleDetail scheduleDeducted = assetDepreciationScheduleDetailRepo.findFirstByAssetDetailIdAndDepreciationScheduleDeducted(assetDetail.getId(), true);
            if (scheduleDeducted != null) {
                row.put("dontRemove", true);
            }

            data.add(row);
        }

        if(queryVoucherData && !Checker.isAssetAdjustment(transType)) {

            if(Checker.collectionIsNotEmpty(assetAccountsWithSL)) {

                for(Account account: assetAccountsWithSL) {
                    List<Object[]> list = generalLedgerRepo.findSumDebitWhereAccountNoEqual(voucherTransNo, account.getId());

                    data = linkAssetVoucherNewRow(list, data, remainingLifeInMonths);
                }
            } else {

                List<Object[]> list = generalLedgerRepo.findSumDebitByAccountNo(voucherTransNo);

                data = linkAssetVoucherNewRow(list, data, remainingLifeInMonths);
            }
        }

        return data;
    }

    private List<Map> linkAssetVoucherNewRow(List<Object[]> list, List<Map> data, BigDecimal remainingLifeInMonths) {

        if(Checker.collectionIsNotEmpty(list)) {
            for(Object[] row: list) {

                Integer accountId = (Integer) row[0];
                String accountCode = (String) row[1];
                String accountTitle = (String) row[2];
                BigDecimal debit = (BigDecimal) row[3];

                Account newAccount = new Account();
                newAccount.setId(accountId);
                newAccount.setCode(accountCode);
                newAccount.setTitle(accountTitle);

                Map newRow = new HashMap();

                newRow.put("assetAccount", newAccount);    // account used in the voucher
                newRow.put("value", debit);
                newRow.put("valueOrig", debit);
                newRow.put("depreciatedValue", BigDecimal.ZERO);
                newRow.put("remainingValue", debit);

                BigDecimal monthlyDepreciation = BigDecimal.ZERO;
                if(remainingLifeInMonths.compareTo(BigDecimal.ZERO) > 0) monthlyDepreciation = debit.subtract(BigDecimal.ONE).divide(remainingLifeInMonths, 2, RoundingMode.HALF_UP);

                newRow.put("monthlyDepreciation", monthlyDepreciation);
                newRow.put("newRow", true);

                data.add(newRow);
            }
        }

        return data;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<AssetDepreciationSchedule> findAssetDepreciationScheduleByAssetId(Integer id) {
        List<AssetDepreciationSchedule> scheduleList = assetDepreciationScheduleRepo.findByAssetId(id);

        for(AssetDepreciationSchedule s: scheduleList) {
            s.setAsset(null);
        }
        return scheduleList;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<AssetVoucherLinkType> getLinkTypes() {
        return assetVoucherLinkTypeRepo.findByOrderByDescriptionAsc();
    }

    @Override
    @Transactional
    public PostResponse saveLink(AssetVoucherLinkForm form, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);
            AssetVoucherLinkValidator validator = new AssetVoucherLinkValidator();
            validator.validate(form, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
                response.setSuccess(false);
            } else {

                AssetVoucher assetVoucher = new AssetVoucher();
                assetVoucher.setAsset(form.getAsset());
                assetVoucher.setCreatedAt(new Date());
                assetVoucher.setAssetVoucherLinkType(form.getLinkType());
                assetVoucher.setTransaction(form.getVoucherTransaction());
                assetVoucher.setDocumentType(form.getDocumentType());
                assetVoucher.setCreatedBy(authenticationFacade.getLoggedIn());

                if(Checker.isAssetAdjustment(form.getLinkType().getId())) {
                    assetVoucher.setAdjustmentType(form.getAdjustmentType());
                }

                assetVoucherRepo.save(assetVoucher);

                Asset linkedAsset = assetRepo.findById(form.getAsset().getId()).orElse(null);

                if (linkedAsset != null) {

                    boolean updateAssetDetailsAndTheLike = Checker.isMajorRepair(form.getLinkType().getId()) ||
                            Checker.isAssetAdjustment(form.getLinkType().getId()) ||
                            Checker.isAssetRetirement(form.getLinkType().getId()) ||
                            Checker.isAssetAcquisition(form.getLinkType().getId());

                    if (updateAssetDetailsAndTheLike) {

                        if(Checker.isMajorRepair(form.getLinkType().getId()) || Checker.isAssetAcquisition(form.getLinkType().getId())
                                || Checker.isAssetAdjustment(form.getLinkType().getId())) {

                            linkedAsset.setTotalValue(form.getAsset().getTotalValue());
                            linkedAsset.setTotalMonthlyDepreciation(form.getAsset().getTotalMonthlyDepreciation());
                            linkedAsset.setTotalDepreciatedValue(form.getAsset().getTotalDepreciatedValue());
                            linkedAsset.setTotalRemainingValue(form.getAsset().getTotalRemainingValue());
                            linkedAsset.setDepreciationYears(form.getAsset().getDepreciationYears());
                            linkedAsset.setDepreciationMonths(form.getAsset().getDepreciationMonths());
                            linkedAsset.setEndYear(form.getAsset().getEndYear());
                            linkedAsset.setEndMonth(form.getAsset().getEndMonth());

                        }

                        assetRepo.save(linkedAsset);

                        // update asset details
                        for (Map row : form.getAssetDetails()) {

                            Object valueObj = row.get("value");
                            Object monthlyDepreciationObj = row.get("monthlyDepreciation");
                            Object depreciatedValueObj = row.get("depreciatedValue");
                            Object remainingValueObj = row.get("remainingValue");

                            AssetDetail assetDetail = null;
                            Object id = row.get("id");

                            if (id != null) {
                                Integer assetDetailId = (Integer) id;

                                assetDetail = assetDetailRepo.findById(assetDetailId).orElse(null);

                            } else {    // new row

                                Map assetAccountMap = (Map) row.get("assetAccount");
                                Map expenseAccountMap = (Map) row.get("expenseAccount");
                                Map accumDepAccountMap = (Map) row.get("accumDepAccount");

                                Account assetAccount = new Account();
                                assetAccount.setId((Integer) assetAccountMap.get("id"));

                                Account expenseAccount = new Account();
                                expenseAccount.setId((Integer) expenseAccountMap.get("id"));

                                Account accumpDepAccount = new Account();
                                accumpDepAccount.setId((Integer) accumDepAccountMap.get("id"));

                                assetDetail = new AssetDetail();
                                assetDetail.setAsset(linkedAsset);
                                assetDetail.setAssetAccount(assetAccount);
                                assetDetail.setExpenseAccount(expenseAccount);
                                assetDetail.setAccumDepAccount(accumpDepAccount);
                            }

                            if (assetDetail != null) {

                                if(Checker.isMajorRepair(form.getLinkType().getId()) || Checker.isAssetAcquisition(form.getLinkType().getId()) ||
                                        Checker.isAssetAdjustment(form.getLinkType().getId())) {

                                    assetDetail.setValue(new BigDecimal(valueObj + ""));
                                    assetDetail.setMonthlyDepreciation(new BigDecimal(monthlyDepreciationObj + ""));
                                    assetDetail.setDepreciatedValue(new BigDecimal(depreciatedValueObj + ""));
                                    assetDetail.setRemainingValue(new BigDecimal(remainingValueObj + ""));
                                }

                                assetDetailRepo.save(assetDetail);
                            }

                        }

                        //remove existing AssetDepreciationScheduleDetail
                        assetDepreciationScheduleDetailRepo.deleteByDepreciationScheduleAssetIdAndDepreciationScheduleDeducted(form.getAsset().getId(), false);

                        // update schedule
                        assetDepreciationScheduleRepo.deleteByAssetIdAndDeducted(linkedAsset.getId(), false);
                        depreciationScheduleFacade.generateSchedule(linkedAsset);
                    }

                }

                response.setSuccessMessage("Asset & Voucher have been linked.");
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    @Override
    public PostResponse retire(RetireAssetForm form, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        try {

            MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);
            AssetRetirementValidator validator = new AssetRetirementValidator();
            validator.validate(form, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
                response.setSuccess(false);
            } else {

                Asset asset = assetRepo.findById(form.getId()).orElse(null);
                if(asset != null) {

                    if(!asset.getStatus().equals(AssetStatus.RETIRED.name())) {

                        java.sql.Date acquisitionDate = new java.sql.Date(asset.getAcquisitionDate().getTime());

                        asset.setStatus(AssetStatus.RETIRED.name());
                        asset.setRetirementRemarks(form.getRetirementRemarks());

                        assetRepo.save(asset);

                        TemporaryBatch temporaryBatch = new TemporaryBatch();
                        temporaryBatch.setTransaction(generatorFacade.transaction());

                        com.noreco1.fireflyv2.model.DocumentType documentType = new com.noreco1.fireflyv2.model.DocumentType();
                        documentType.setId(com.noreco1.fireflyv2.model.enums.DocumentType.ASSET_RETIREMENT.getId());

                        temporaryBatch.setDocumentType(documentType);
                        temporaryBatch.setDate(new Date());
                        temporaryBatch.setRemarks(form.getRetirementRemarks());
                        temporaryBatch.setVoucherCreated(false);

                        temporaryBatchRepo.save(temporaryBatch);

                        Map<Integer, BigDecimal> assetAccountsDetailMap = new HashMap();
                        Map<Integer, BigDecimal> expenseAccountsDetailMap = new HashMap();
                        Map<Integer, BigDecimal> accumDepAccountsDetailMap = new HashMap();

                        // merge same accounts, sum their remaining values
                        for (Map row: form.getAssetDetails()) {

                            // start: Asset Account
                            Object valueObj = row.get("value");
                            BigDecimal value = BigDecimal.valueOf(Double.valueOf(valueObj.toString()));

                            Map assetAccountMap = (Map) row.get("assetAccount");
                            Integer assetAccountId = (Integer) assetAccountMap.get("id");

                            assetAccountsDetailMap = this.setRemainingValue(assetAccountsDetailMap, assetAccountId, value);
                            // end: Asset Account

                            // start: Expense
                            Object remainingValueObj = row.get("remainingValue");
                            BigDecimal remainingValue = BigDecimal.valueOf(Double.valueOf(remainingValueObj.toString()));

                            Map expenseAccountMap = (Map) row.get("expenseAccount");
                            Integer expenseAccountId = (Integer) expenseAccountMap.get("id");

                            expenseAccountsDetailMap = this.setRemainingValue(expenseAccountsDetailMap, expenseAccountId, remainingValue);
                            // end: Expense

                            // start: Accumulated Depreciation
                            Object depreciatedValueObj = row.get("depreciatedValue");
                            BigDecimal depreciatedValue = BigDecimal.valueOf(Double.valueOf(depreciatedValueObj.toString()));

                            Map accumDepAccountMap = (Map) row.get("accumDepAccount");
                            Integer accumDepAccountId = (Integer) accumDepAccountMap.get("id");

                            accumDepAccountsDetailMap = this.setRemainingValue(accumDepAccountsDetailMap, accumDepAccountId, depreciatedValue);
                            // end: Accumulated Depreciation
                        }

                        // save temp ledger entries
                        // Expense
                        ledgerFacade.saveTempLedgerEntries(expenseAccountsDetailMap, acquisitionDate, temporaryBatch, asset.getAccountNo(), true);
                        // end expense account

                        // Accumulated Depreciation
                        ledgerFacade.saveTempLedgerEntries(accumDepAccountsDetailMap, acquisitionDate, temporaryBatch, asset.getAccountNo(), true);
                        // end Accumulated Depreciation

                        // Asset Account
                        ledgerFacade.saveTempLedgerEntries(assetAccountsDetailMap, acquisitionDate, temporaryBatch, asset.getAccountNo(), false);
                        // end Asset Account

                        // link Asset & JV
                        AssetVoucher assetVoucher = new AssetVoucher();
                        assetVoucher.setAsset(asset);
                        assetVoucher.setCreatedAt(new Date());

                        AssetVoucherLinkType assetVoucherLinkType = new AssetVoucherLinkType();
                        assetVoucherLinkType.setId(com.noreco1.fireflyv2.model.enums.AssetVoucherLinkType.RETIREMENT.getId());

                        assetVoucher.setAssetVoucherLinkType(assetVoucherLinkType);
                        assetVoucher.setTransaction(temporaryBatch.getTransaction());

                        com.noreco1.fireflyv2.model.DocumentType linkDocumentType = new com.noreco1.fireflyv2.model.DocumentType();
                        linkDocumentType.setId(com.noreco1.fireflyv2.model.enums.DocumentType.JV.getId());

                        assetVoucher.setDocumentType(linkDocumentType);
                        assetVoucher.setCreatedBy(authenticationFacade.getLoggedIn());

                        assetVoucherRepo.save(assetVoucher);
                        // end: link Asset & JV

                        // update asset details values
                        asset.setTotalDepreciatedValue(asset.getTotalValue());
                        asset.setTotalRemainingValue(BigDecimal.ZERO);

                        assetRepo.save(asset);

                        List<AssetDetail> assetDetails = assetDetailRepo.findByAssetId(asset.getId());
                        if(Checker.collectionIsNotEmpty(assetDetails)) {
                            for(AssetDetail assetDetail: assetDetails) {

                                assetDetail.setDepreciatedValue(assetDetail.getValue());
                                assetDetail.setRemainingValue(BigDecimal.ZERO);

                                assetDetailRepo.save(assetDetail);
                            }
                        }
                        // end: update asset details values

                        response.setSuccessMessage("Asset has been retired.");

                    } else {
                        response.setFailureMessage("Asset retired already");
                    }

                } else {
                    response.setFailureMessage("Asset not found.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Asset> find(String query, Pageable pageable) {
        if(Checker.isStringNullOrEmpty(query) ){
            return assetRepo.findAll(pageable);
        }

        query = queryToWildcard(query);
        return assetRepo.findByCodeOrDescription(query, query, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Asset> find(String query, Pageable pageable, Integer assetTypeId) {
        if(Checker.isStringNullOrEmpty(query) ){
            return assetRepo.findByAssetTypeId(assetTypeId, pageable);
        }

        query = queryToWildcard(query);
        return assetRepo.findByAssetTypeAndCodeOrDescription(assetTypeId, query, query, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Asset> find(String query, Pageable pageable, Integer assetTypeId, boolean isFullyDepreciated) {
        if(Checker.isStringNullOrEmpty(query) ){
            if(isFullyDepreciated) {
                return assetRepo.findByAssetTypeIdAndTotalRemainingValueLessThanEqual(assetTypeId, BigDecimal.ZERO, pageable);
            } else {
                return assetRepo.findByAssetTypeIdAndTotalRemainingValueGreaterThan(assetTypeId, BigDecimal.ZERO, pageable);
            }
        }

        query = queryToWildcard(query);
        if(isFullyDepreciated) {
            return assetRepo.findByAssetTypeAndIsFullyDepreciatedAndCodeOrDescription(assetTypeId, query, query, pageable);
        } else {
            return assetRepo.findByAssetTypeAndIsNotFullyDepreciatedAndCodeOrDescription(assetTypeId, query, query, pageable);
        }

    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Asset> find(String query, Pageable pageable, Integer assetTypeId, String status) {
        if(Checker.isStringNullOrEmpty(query) ){
            return assetRepo.findByAssetTypeIdAndStatus(assetTypeId, status, pageable);
        }

        query = queryToWildcard(query);
        return assetRepo.findByAssetTypeAndStatusAndCodeOrDescription(assetTypeId, status, query, query, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Asset> find(String query, Pageable pageable, Integer assetTypeId, boolean isFullyDepreciated, String status) {
        if(Checker.isStringNullOrEmpty(query) ){
            if(isFullyDepreciated) {
                return assetRepo.findByAssetTypeIdAndStatusAndTotalRemainingValueLessThanEqual(assetTypeId, status, BigDecimal.ZERO, pageable);
            } else {
                return assetRepo.findByAssetTypeIdAndStatusAndTotalRemainingValueGreaterThan(assetTypeId, status, BigDecimal.ZERO, pageable);
            }
        }

        query = queryToWildcard(query);
        if(isFullyDepreciated) {
            return assetRepo.findByAssetTypeAndStatusAndIsFullyDepreciatedAndCodeOrDescription(assetTypeId, status, query, query, pageable);
        } else {
            return assetRepo.findByAssetTypeAndStatusAndIsNotFullyDepreciatedAndCodeOrDescription(assetTypeId, status, query, query, pageable);
        }
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Asset> find(String query, Pageable pageable, boolean isFullyDepreciated) {
        if(Checker.isStringNullOrEmpty(query) ) {
            if (isFullyDepreciated) {
                return assetRepo.findByTotalRemainingValueLessThanEqual(BigDecimal.ZERO, pageable);
            } else {
                return assetRepo.findByTotalRemainingValueGreaterThan(BigDecimal.ZERO, pageable);
            }
        }

        query = queryToWildcard(query);
        if(isFullyDepreciated) {
            return assetRepo.findByIsFullyDepreciatedAndCodeOrDescription(query, query, pageable);
        } else {
            return assetRepo.findByIsNotFullyDepreciatedAndCodeOrDescription(query, query, pageable);
        }
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Asset> find(String query, Pageable pageable, boolean isFullyDepreciated, String status) {
        if(Checker.isStringNullOrEmpty(query) ) {
            if (isFullyDepreciated) {
                return assetRepo.findByStatusAndTotalRemainingValueLessThanEqual(status, BigDecimal.ZERO, pageable);
            } else {
                return assetRepo.findByStatusAndTotalRemainingValueGreaterThan(status, BigDecimal.ZERO, pageable);
            }
        }

        query = queryToWildcard(query);
        if (isFullyDepreciated) {
            return assetRepo.findByStatusAndIsFullyDepreciatedAndCodeOrDescription(status, query, query, pageable);
        } else {
            return assetRepo.findByStatusAndIsNotFullyDepreciatedAndCodeOrDescription(status, query, query, pageable);
        }

    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Asset> find(String query, Pageable pageable, String status) {
        if(Checker.isStringNullOrEmpty(query) ) {
            return assetRepo.findByStatus(status, pageable);
        }

        query = queryToWildcard(query);
        return assetRepo.findByStatusAndCodeOrDescription(status, query, query, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Asset> findAllForMaintenanceOrder(Pageable pageable) {
        return assetRepo.findAllByStatus(AssetStatus.ACTIVE.name(), pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Asset> findAllForMaintenanceOrderByQuery(String query, Pageable pageable) {
        return assetRepo.findAllByCodeContainsOrDescriptionContainsAndStatus(query, query, AssetStatus.ACTIVE.name(), pageable);
    }

    @Override
    public PostResponse processUpdate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        return this.processCreate(entity, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(Object entity, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            Asset asset = (Asset) entity;
            MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);
            AssetValidator validator = new AssetValidator();

            validator.validate(asset, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();

                response = messageFormatter.getResponse();

                response.setSuccess(false);
            } else {
                User createdBy = authenticationFacade.getLoggedIn();

                if (asset.getId() != null) {

                    Asset exAsset = assetRepo.findById(asset.getId()).orElse(null);
                    if (exAsset == null) {
                        response.setFailureMessage("Asset is not available!");
                        return response;
                    } else {
                        asset.setCreatedBy(exAsset.getCreatedBy());
                        asset.setAccountNo(exAsset.getAccountNo());
                        asset.setWorkOrder(exAsset.getWorkOrder());
                        asset.setYear(exAsset.getYear());
                        asset.setCode(exAsset.getCode());
                        asset.setRefNo(exAsset.getRefNo());
                        asset.setStatus(exAsset.getStatus());
                    }
                } else {

                    Integer year = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(asset.getAcquisitionDate()));
                    Object latestCode = assetRepo.findLatestCodeByYear(year);
                    String code = generatorFacade.voucherCodeNoOffice("A", (latestCode == null ? "" : String.valueOf(latestCode)), asset.getAcquisitionDate(), GlobalConstant.COUNTER_PAD_4);

                    asset.setStatus(AssetStatus.ACTIVE.name());
                    asset.setRefNo(code);
                    asset.setCode(code);
                    asset.setYear(year);
                    asset.setCreatedBy(createdBy);
                    asset.setAccountNo(generatorFacade.entityAccountNumber());
                }

                // Set others fields.
                if (asset.getTotalDepreciatedValue() == null) {
                    asset.setTotalDepreciatedValue(BigDecimal.ZERO);
                }
                asset.setDefaultAnnualDepreciationRate();
                asset.setDefaultMonthlyDepreciationRate();

                if (!Checker.isValidId(asset.getId())){
                    asset.setWorkOrder(new WorkOrder(0));
                }

                SLEntityClassification slEntityClassification = new SLEntityClassification();
                slEntityClassification.setId(com.noreco1.fireflyv2.model.enums.SLEntityClassification.ASSET.getId());
                asset.setSlEntityClassification(slEntityClassification);
                asset.setCreatedAt(new Date());

                Asset a = assetRepo.save(asset);

                if (a != null) {

                    // asset details to be deleted
                    // asset details id
                    List<AssetDetail> assetDetailsTobeRemoved = new ArrayList<>();
                    Map<Integer, AssetDetail> assetDetailsMap = new HashMap();

                    List<AssetDepreciationScheduleDetail> depreciationScheduleNotDeducted = assetDepreciationScheduleDetailRepo.findByDepreciationScheduleAssetIdAndDepreciationScheduleDeducted(asset.getId(), false);
                    if (!depreciationScheduleNotDeducted.isEmpty()) {
                        for(AssetDepreciationScheduleDetail d: depreciationScheduleNotDeducted) {
                            // need to check unique asset details
                            AssetDetail detail = assetDetailsMap.get(d.getAssetDetail().getId());
                            if (detail == null) {
                                assetDetailsMap.put(d.getAssetDetail().getId(), d.getAssetDetail());
//                                assetDetailsTobeRemoved.add(d.getAssetDetail());
                            }

                        }
                    }

                    // reset schedule details
                    //assetDepreciationScheduleDetailRepo.deleteByDepreciationScheduleAssetIdAndDepreciationScheduleDeducted(asset.getId(), false);
                    if (!depreciationScheduleNotDeducted.isEmpty()) assetDepreciationScheduleDetailRepo.deleteInBatch(depreciationScheduleNotDeducted);

                    // reset schedule
                    assetDepreciationScheduleRepo.deleteByAssetIdAndDeducted(asset.getId(), false);

                    // Asset Details
                    // clear Asset Details: not yet processed

                    List<AssetDepreciationScheduleDetail> depreciationScheduleDeducted = assetDepreciationScheduleDetailRepo.findByDepreciationScheduleAssetIdAndDepreciationScheduleDeducted(asset.getId(), true);
                    if (!depreciationScheduleDeducted.isEmpty()) {
                        for(AssetDepreciationScheduleDetail d: depreciationScheduleDeducted) {

                            AssetDetail detail = assetDetailsMap.get(d.getAssetDetail().getId());
                            if (detail != null) {
                                assetDetailsTobeRemoved.remove(d.getAssetDetail());
                            }

                        }
                    }

                    if (asset.getId() != null) {
                        assetDetailRepo.deleteInBatch(assetDetailsTobeRemoved);
                    }

                    if (!asset.getAssetDetails().isEmpty()) {

                        for(Map row:asset.getAssetDetails())   {

                            // prevent dupes
                            Object dontRemove = row.get("dontRemove");
                            if (dontRemove != null && (Boolean)dontRemove) {
                                continue;
                            }

                            AssetDetail assetDetail = null;

                            Object id = row.get("id");
                            if(id != null) {
                                // get existing for updating
                                assetDetail = assetDetailRepo.findById((Integer)id).orElse(null);
                            }

                            if(assetDetail == null) {   // no existing assetdetail
                                assetDetail = new AssetDetail();
                            }

                            assetDetail.setAsset(a);

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

                            assetDetailRepo.save(assetDetail);

                        }
                    }

                    // depreciation schedule
                    depreciationScheduleFacade.generateSchedule(a);

                    response.setModelId(a.getId());
                    response.setSuccessMessage("Continuing Property Record successfully saved!");

                    response.setSuccess(true);
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    private HashMap composeHashMap(Asset asset) {
        HashMap<String, Object> hm = new HashMap<>();
        SimpleDateFormat dp = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat df = new SimpleDateFormat("MMMMM");
        HashMap<String, Object> createdBy;
        String startMonth;
        String endMonth;

        try {
            // Set created by user object.
            createdBy = new HashMap<>();

            createdBy.put("id", asset.getCreatedBy().getId());
            createdBy.put("accountNo", asset.getCreatedBy().getAccountNo());
            createdBy.put("fullName", asset.getCreatedBy().getFullName());

            // Set start and end months.
            startMonth = df.format(dp.parse("1989-" + asset.getStartMonth() + "-13"));
            endMonth = df.format(dp.parse("1989-" + asset.getEndMonth() + "-13"));

            hm.put("id", asset.getId());
            hm.put("accountNo", asset.getAccountNo());
            hm.put("refNo", asset.getRefNo());
            hm.put("description", asset.getDescription());
            hm.put("acquisitionDate", asset.getAcquisitionDate());
            hm.put("totalValue", asset.getTotalValue());
            hm.put("depreciationMonths", asset.getDepreciationMonths());
            hm.put("depreciationYears", asset.getDepreciationYears());
            hm.put("startYear", asset.getStartYear());
            hm.put("startMonth", startMonth);
            hm.put("endYear", asset.getEndYear());
            hm.put("endMonth", endMonth);
            hm.put("totalMonthlyDepreciation", asset.getTotalMonthlyDepreciation());
            hm.put("totalDepreciatedValue", asset.getTotalDepreciatedValue());
            hm.put("totalRemainingValue", asset.getTotalRemainingValue());
            hm.put("createdByUser", createdBy);
            hm.put("createdAt", asset.getCreatedAt());
            hm.put("monthlyDepreciationRate", asset.getMonthlyDepreciationRate());
            hm.put("annualDepreciationRate", asset.getAnnualDepreciationRate());
            hm.put("location", asset.getLocation());
            hm.put("assetType", asset.getAssetType());
            hm.put("status", asset.getStatus());
            hm.put("retirementRemarks", asset.getRetirementRemarks());

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return hm;
    }

    private String queryToWildcard(String query) {
        return '%' + query + '%';
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
