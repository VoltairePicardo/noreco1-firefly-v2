package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.Debug;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.common.helpers.ServiceUtil;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.enums.AssetStatus;
import com.noreco1.fireflyv2.model.form.YearMonth;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.service.AssetDepreciationService;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.text.DateFormatSymbols;
import java.util.*;

@Service(value = "assetDepreciationServiceImpl")
public class AssetDepreciationServiceImpl implements AssetDepreciationService {

    @Autowired
    AssetDepreciationScheduleRepo assetDepreciationScheduleRepo;

    @Autowired
    AssetDepreciationRepo assetDepreciationRepo;

    @Autowired
    AssetDepreciationDetailRepo assetDepreciationDetailRepo;

    @Autowired
    AuthenticationFacade authenticationFacade;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    TemporaryBatchRepo temporaryBatchRepo;

    @Autowired
    TemporaryGeneralLedgerRepo temporaryGeneralLedgerRepo;

    @Autowired
    TemporarySubLedgerRepo temporarySubLedgerRepo;

    @Autowired
    AllocationFactorRepo allocationFactorRepo;

    @Autowired
    AssetRepo assetRepo;

    @Autowired
    AssetDepreciationScheduleDetailRepo assetDepreciationScheduleDetailRepo;

    @Autowired
    AssetDetailRepo assetDetailRepo;

    @Autowired
    DateRangeRepo dateRangeRepo;

    @Autowired
    FactorPercentageDistroRepo factorPercentageDistroRepo;

    @Autowired
    SegmentAccountRepo segmentAccountRepo;

    @Override
    public PostResponse processUpdate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        return null;
    }

    @Override
    public PostResponse processCreate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        return null;
    }

    @Transactional
    @Override
    public PostResponse process(YearMonth yearMonth, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();
        try {
            if (yearMonth == null) {
                response.setFailureMessage("No year and month selected");
                return response;
            }

            if (yearMonth.getYear() == null) {
                response.setFailureMessage("No year selected");
                return response;
            }

            if (yearMonth.getMonth() == null) {
                response.setFailureMessage("No month selected");
                return response;
            }

            Integer month = yearMonth.getMonth() + 1; // java & js are zero-based  but mysql data is not

            List<AssetDepreciationScheduleDetail> scheduleDetails = assetDepreciationScheduleDetailRepo
                    .findByDepreciationScheduleYearAndDepreciationScheduleMonthAndDepreciationScheduleDeductedAndAssetDetailAssetStatus(yearMonth.getYear(), month, false, AssetStatus.ACTIVE.name());

            if (!scheduleDetails.isEmpty()) {

                User user = authenticationFacade.getLoggedIn();
                Transaction transaction = generatorFacade.transaction();

                AssetDepreciation assetDepreciation = new AssetDepreciation();
                assetDepreciation.setMonth(month);
                assetDepreciation.setYear(yearMonth.getYear());
                assetDepreciation.setCreatedBy(user);
                assetDepreciation.setTransaction(transaction);

                assetDepreciation = assetDepreciationRepo.save(assetDepreciation);

                TemporaryBatch temporaryBatch = new TemporaryBatch();
                temporaryBatch.setTransaction(transaction);
                DocumentType documentType = new DocumentType();
                documentType.setId(com.noreco1.fireflyv2.model.enums.DocumentType.DEPRECIATION.getId());
                temporaryBatch.setDocumentType(documentType);
                temporaryBatch.setDate(new Date());
                temporaryBatch.setVoucherCreated(false);
                temporaryBatch.setRemarks("Asset depreciation: " + new DateFormatSymbols().getMonths()[yearMonth.getMonth()] + " " + yearMonth.getYear());

                temporaryBatch = temporaryBatchRepo.save(temporaryBatch);

                Map<Integer, Map<Object, Object>> creditEntries =  new HashedMap();
                Map<Integer, Map<Object, Object>> debitEntries =  new HashedMap();

                for(AssetDepreciationScheduleDetail schedule:scheduleDetails) {

                    AssetDepreciationDetail detail = new AssetDepreciationDetail();
                    detail.setDepreciationAmount(schedule.getDepreciationAmount());
                    detail.setAssetDepreciation(assetDepreciation);
                    detail.setAssetDepreciationScheduleDetail(schedule);

                    AssetDetail assetDetail = schedule.getAssetDetail();

                    BigDecimal depreciatedValue = assetDetail.getDepreciatedValue();
                    detail.setDepreciatedValue(depreciatedValue.add(schedule.getDepreciationAmount()));

                    BigDecimal remainingValue = assetDetail.getRemainingValue();
                    detail.setRemainingValue(remainingValue.subtract(schedule.getDepreciationAmount()));

                    assetDepreciationDetailRepo.save(detail);

                    debitEntries = this.createLedgerEntriesMap(debitEntries, assetDetail.getExpenseAccount(), schedule);
                    creditEntries = this.createLedgerEntriesMap(creditEntries, assetDetail.getAccumDepAccount(), schedule);

                    // Update AssetDepreciationSchedule by setting deducted = true
                    AssetDepreciationSchedule depreciationSchedule = schedule.getDepreciationSchedule();
                    depreciationSchedule.setDeducted(true);
                    assetDepreciationScheduleRepo.save(depreciationSchedule);

                    // update asset
                    Asset asset = schedule.getDepreciationSchedule().getAsset();

                    asset.setTotalDepreciatedValue(asset.getTotalDepreciatedValue().add(schedule.getDepreciationAmount()));
                    asset.setTotalRemainingValue(asset.getTotalRemainingValue().subtract(schedule.getDepreciationAmount()));

                    if(asset.getTotalRemainingValue().compareTo(BigDecimal.ZERO) <= 0){
                        asset.setTotalRemainingValue(BigDecimal.ONE);
                    }

                    assetRepo.save(asset);

                    // update asset detail
                    assetDetail.setDepreciatedValue(detail.getDepreciatedValue());
                    assetDetail.setRemainingValue(detail.getRemainingValue());
                    assetDetailRepo.save(assetDetail);

                }

                this.saveTempGL(debitEntries, transaction, temporaryBatch, assetDepreciation, true);
                this.saveTempGL(creditEntries, transaction, temporaryBatch, assetDepreciation, false);

                response.setSuccessMessage("Asset depreciation successfully processed");
            } else {
                response.setFailureMessage("No asset depreciation available");
            }

        }catch (Exception e) {
            e.printStackTrace();
            response.setFailureMessage("Something went wrong!");
            throw new RuntimeException(e.getMessage());
        }
        return response;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<AssetDepreciation> findAllByYearAndMonth(Integer year, Integer month) {
        try {
            return assetDepreciationRepo.findAllByYearAndMonthOrderByYearDescMonthDesc(year, month);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public AssetDepreciation findById(Integer id) {
        return assetDepreciationRepo.findById(id).orElse(null);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Map> findAllDetailsById(Integer assetDepreciationId) {
        List<Map> data = new ArrayList<>();

        List<AssetDepreciationDetail> details = assetDepreciationDetailRepo.findByAssetDepreciationId(assetDepreciationId);
        if (!details.isEmpty()) {

            Integer prevAccountNo = 0;
            for(AssetDepreciationDetail detail:details) {

                Map map = new HashMap();

                AssetDepreciationScheduleDetail assetDepreciationScheduleDetail = detail.getAssetDepreciationScheduleDetail();
                Asset asset = assetDepreciationScheduleDetail.getDepreciationSchedule().getAsset();

                if (!prevAccountNo.equals(asset.getAccountNo())) {
                    map.put("acctNo", asset.getAccountNo());
                    map.put("refNo", asset.getRefNo());
                    map.put("description", asset.getDescription());
                }

                map.put("code", assetDepreciationScheduleDetail.getAssetAccount().getCode());
                map.put("account", assetDepreciationScheduleDetail.getAssetAccount().getTitle());
                map.put("depreciationAmount", detail.getDepreciationAmount());

                data.add(map);

                prevAccountNo = asset.getAccountNo();
            }
        }
        return data;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Map getTotals(Integer assetDepreciationId) {
        Map map = new HashMap();

        List<AssetDepreciationDetail> details = assetDepreciationDetailRepo.findByAssetDepreciationId(assetDepreciationId);

        BigDecimal total = BigDecimal.ZERO;

        map.put("total", total);
        map.put("count", details.size());

        if (!details.isEmpty()) {
            for(AssetDepreciationDetail detail:details) {
                total = total.add(detail.getDepreciationAmount());
            }
            map.put("total", total);
        }
        return map;
    }

    private void saveTempGL(Map ledgerEntriesMap, Transaction transaction, TemporaryBatch temporaryBatch, AssetDepreciation assetDepreciation, Boolean isDebit) {
        Iterator it = ledgerEntriesMap.entrySet().iterator();

//        BigDecimal glGrandTotal = BigDecimal.ZERO;

        while (it.hasNext()) {

            Map.Entry pair = (Map.Entry) it.next();

            Object value = pair.getValue();
            Map accountAndAmountMap = (Map) value;

            List<AssetDepreciationScheduleDetail> scheduleDetails = (List<AssetDepreciationScheduleDetail>)accountAndAmountMap.get("scheduleDetails");
            Account account = (Account)accountAndAmountMap.get("account");
            BigDecimal amount = (BigDecimal)accountAndAmountMap.get("amount");

            java.sql.Date date = new java.sql.Date(temporaryBatch.getDate().getTime());

            List<DateRange> dateRanges = dateRangeRepo.findByEndGreaterThanEqualAndStartLessThanEqual(date, date);
            if(Checker.collectionIsNotEmpty(dateRanges)) {

                for (DateRange range : dateRanges) {

                    // get percentage distribution
                    AllocationFactor allocationFactor = allocationFactorRepo.findOneByAccountId(account.getId());
                    Set<FactorPercentageDistro> percentages = factorPercentageDistroRepo.findByFactorIdAndValidityDateId(allocationFactor.getFactor().getId(), range.getId());

                    if (Checker.collectionIsNotEmpty(percentages)) {

                        int counter = 0;
                        int lastIdx = percentages.size();
                        BigDecimal glTotal = BigDecimal.ZERO;

                        for(FactorPercentageDistro obj:percentages) {
                            counter++;  // used to check if end of loop

                            SegmentAccount segmentAccount = segmentAccountRepo.findOneByAccountIdAndBusinessSegmentId(account.getId(), obj.getBusinessSegment().getId());
                            BigDecimal percentage = obj.getPercentage();

                            BigDecimal glShare = (percentage.multiply(amount)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            if (glShare == null || glShare.compareTo(BigDecimal.ZERO) == 0) {
                                continue;
                            }

                            TemporaryGeneralLedger temporaryGeneralLedger = new TemporaryGeneralLedger();
                            temporaryGeneralLedger.setTransaction(temporaryBatch.getTransaction());
                            temporaryGeneralLedger.setTemporaryBatch(temporaryBatch);

                            glTotal = glTotal.add(glShare);
                            glShare = ServiceUtil.getLastGlShare(glShare, glTotal, amount, counter, lastIdx);

                            if(isDebit) {
                                temporaryGeneralLedger.setDebit(glShare);
                                temporaryGeneralLedger.setCredit(BigDecimal.ZERO);
                            } else {
                                temporaryGeneralLedger.setDebit(BigDecimal.ZERO);
                                temporaryGeneralLedger.setCredit(glShare);
                            }

                            temporaryGeneralLedger.setSegmentAccount(segmentAccount);
                            temporaryGeneralLedger.setAccount(account);

                            temporaryGeneralLedgerRepo.save(temporaryGeneralLedger);

                            if (account.getHasSL() == 1) {
                                // sub ledger
                                for(AssetDepreciationScheduleDetail scheduleDetail:scheduleDetails) {

                                    TemporarySubLedger temporarySubLedger = new TemporarySubLedger();
                                    temporarySubLedger.setSegmentAccount(segmentAccount);
                                    temporarySubLedger.setTemporaryBatch(temporaryBatch);
                                    temporarySubLedger.setTransaction(temporaryBatch.getTransaction());
                                    temporarySubLedger.setBalance(glShare);
                                    temporarySubLedger.setTemporaryGeneralLedger(temporaryGeneralLedger);

                                    BigDecimal slShare = scheduleDetail.getDepreciationAmount();
                                    if (isDebit) {
                                        temporarySubLedger.setDebit(slShare);
                                    } else {
                                        temporarySubLedger.setCredit(slShare);
                                    }

                                    SlEntity slEntity = new SlEntity();
                                    slEntity.setAccountNo(scheduleDetail.getAssetDetail().getAsset().getAccountNo());

                                    temporarySubLedger.setSlEntity(slEntity);

                                    temporarySubLedgerRepo.save(temporarySubLedger);
                                }
                            }

                        }
                    } else {
                        String message = "Check Account Allocation Percentage Distribution : " + account.getTitle();
                        throw new RuntimeException(message);
                    }
                }
            } else {
                String message = "Check Allocation Date Range : " + account.getTitle();
                throw new RuntimeException(message);
            }

            /*List<Object[]> allocationFactors = allocationFactorRepo.findAllByAccountId(account.getId());

            if (!allocationFactors.isEmpty()) {

                int counter = 0;
                int lastIdx = allocationFactors.size();
                BigDecimal glTotal = BigDecimal.ZERO;

                for(Object[] obj:allocationFactors) {
                    counter++;

                    Integer segmentId = (Integer)obj[1];
                    BigDecimal percentage = (BigDecimal)obj[2];

//                    BigDecimal glShare  = (percentage.divide(new BigDecimal(100))).multiply(amount).setScale(2, BigDecimal.ROUND_HALF_UP);;
                    BigDecimal glShare = (percentage.multiply(amount)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    SegmentAccount segmentAccount = new SegmentAccount();
                    segmentAccount.setId(segmentId);

                    TemporaryGeneralLedger temporaryGL = new TemporaryGeneralLedger();
                    temporaryGL.setTransaction(transaction);
                    temporaryGL.setTemporaryBatch(temporaryBatch);
                    temporaryGL.setSegmentAccount(segmentAccount);

                    glTotal = glTotal.add(glShare);
                    glGrandTotal = glGrandTotal.add(glShare);

                    if (glTotal.compareTo(amount) > 0) {
                        // glTotal: 101, amount: 100
                        // 101 - 100 = 1
                        // 101 - 1 = 100
                        BigDecimal i01 = glTotal.subtract(amount);
                        glShare = glShare.subtract(i01);
                    } else if (counter == lastIdx) {
                        if (glTotal.compareTo(amount) < 0) {
                            // glTotal: 100, amount: 101
                            // 101 - 100 = 1
                            // 100 + 1 = 101
                            BigDecimal i01 = amount.subtract(glTotal);
                            glShare = glShare.add(i01);
                        }
                    }

                    if(isDebit) {
                        temporaryGL.setDebit(glShare);
                        temporaryGL.setCredit(new BigDecimal(0));
                    } else {
                        temporaryGL.setDebit(new BigDecimal(0));
                        temporaryGL.setCredit(glShare);
                    }

                    TemporaryGeneralLedger ledger = temporaryGeneralLedgerRepo.save(temporaryGL);

                    if (account.hasSL() == 1) { // save temp SL

                        // only accum dep
                        for(AssetDepreciationScheduleDetail scheduleDetail:scheduleDetails) {

                            TemporarySubLedger temporarySubLedger = new TemporarySubLedger();

                            temporarySubLedger.setTemporaryBatch(temporaryBatch);
                            temporarySubLedger.setTemporaryGeneralLedger(ledger);
                            temporarySubLedger.setSegmentAccount(segmentAccount);
                            temporarySubLedger.setTransaction(transaction);

                            SlEntity slEntity = new SlEntity();
                            slEntity.setAccountNo(scheduleDetail.getAssetDetail().getAsset().getAccountNo());
                            temporarySubLedger.setSlEntity(slEntity);

                            BigDecimal slShare = scheduleDetail.getDepreciationAmount();

                            if (isDebit) {
                                temporarySubLedger.setDebit(slShare);
                            } else {
                                temporarySubLedger.setCredit(slShare);
                            }

                            temporarySubLedgerRepo.save(temporarySubLedger);
                        }
                    }
                }
            } else {
                String message = "AssetDepreciationImpl: no allocation factors for : " + account.getTitle();
                Debug.print(message);
                throw new RuntimeException(message);
            }*/
        }
    }

    private Map createLedgerEntriesMap(Map<Integer, Map<Object, Object>> ledgerEntriesMap, Account account, AssetDepreciationScheduleDetail scheduleDetail) {

        Map<Object, Object> entriesMap = ledgerEntriesMap.get(account.getId());

        if (entriesMap != null) {
            Object amountObj = entriesMap.get("amount");

            BigDecimal exAmount = amountObj == null ? BigDecimal.ZERO : (BigDecimal) amountObj;

            entriesMap.put("amount", exAmount.add(scheduleDetail.getDepreciationAmount()));
        } else {
            entriesMap = new HashedMap();
            entriesMap.put("account" , account);
            entriesMap.put("amount" , scheduleDetail.getDepreciationAmount());
        }

        Object scheduleDetailsObj = entriesMap.get("scheduleDetails");

        List<AssetDepreciationScheduleDetail> scheduleDetails = new ArrayList<>();

        if (scheduleDetailsObj != null) {
           scheduleDetails = (List<AssetDepreciationScheduleDetail>) scheduleDetailsObj;
        }

        scheduleDetails.add(scheduleDetail);

        entriesMap.put("scheduleDetails", scheduleDetails);

        ledgerEntriesMap.put(account.getId(), entriesMap);

        return ledgerEntriesMap;
    }
}
