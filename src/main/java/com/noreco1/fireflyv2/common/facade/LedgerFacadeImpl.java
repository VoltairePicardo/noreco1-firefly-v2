package com.noreco1.fireflyv2.common.facade;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.ServiceUtil;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.GeneralLedgerLineDto2;
import com.noreco1.fireflyv2.controller.response.SubLedgerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.Date;

/**
 * Created by TSI Admin on 5/1/2015.
 */

@Component
public class LedgerFacadeImpl implements LedgerFacade {

    @Autowired
    GeneralLedgerRepo generalLedgerRepo;

    @Autowired
    AllocationFactorRepo allocationFactorRepo;

    @Autowired
    SubLedgerRepo subLedgerRepo;

    @Autowired
    CheckVoucherIncomePaymentRepo incomePaymentRepo;

    @Autowired
    VoucherCashflowDetailRepo voucherCashflowDetailRepo;

    @Autowired
    CashFlowAccountEntryFacade cashFlowAccountEntryFacade;

    @Autowired
    FactorPercentageDistroRepo factorPercentageDistroRepo;

    @Autowired
    SegmentAccountRepo segmentAccountRepo;

    @Autowired
    DateRangeRepo dateRangeRepo;

    @Autowired
    AccountRepo accountRepo;

    @Autowired
    TemporaryGeneralLedgerRepo temporaryGeneralLedgerRepo;

    @Autowired
    TemporarySubLedgerRepo temporarySubLedgerRepo;

    @Override
    public Long postGeneralLedger(Transaction transaction, List<GeneralLedgerLineDto2> generalLedgerLines, List<SubLedgerDto> subLedgerLines, Date voucherDate) {
        Long count = 0L;

        if (transaction != null && transaction.getId() > 0) {
            voucherCashflowDetailRepo.deleteByTransactionId(transaction.getId());
            generalLedgerRepo.deleteByTransactionId(transaction.getId());
            subLedgerRepo.deleteByTransactionId(transaction.getId());
            incomePaymentRepo.deleteByTransactionId(transaction.getId());
        }

        for(GeneralLedgerLineDto2 ledgerLine: generalLedgerLines) {

            BigDecimal debit = ledgerLine.getDebit();
            BigDecimal credit = ledgerLine.getCredit();

            boolean isDebit = debit.compareTo(credit) > 0;

//            BigDecimal glAmount = ledgerLine.getDebit();
//            BigDecimal glAmount = debit.subtract(credit);
            BigDecimal glAmountDebit = debit;
            BigDecimal glAmountCredit = credit;
            /*boolean isDebit = true;
            if (glAmount == null || glAmount.compareTo(BigDecimal.ZERO) == 0) {
                isDebit = false;
                glAmount = ledgerLine.getCredit();
            }*/

            // prevent insertion of GL entries having no debit or credit amount
            if ((glAmountDebit == null || glAmountDebit.compareTo(BigDecimal.ZERO) == 0) && (glAmountCredit == null || glAmountCredit.compareTo(BigDecimal.ZERO) == 0)) {
                count ++;
                continue;
            }

            List<Map> distribution = ledgerLine.getDistribution();

            if(distribution.isEmpty()) {

                java.sql.Date sqlDate = new java.sql.Date(voucherDate.getTime());

                List<DateRange> dateRanges = dateRangeRepo.findByEndGreaterThanEqualAndStartLessThanEqual(sqlDate, sqlDate);
                if(Checker.collectionIsNotEmpty(dateRanges)) {

                    for (DateRange range : dateRanges) {

                        // get percentage distribution
                        AllocationFactor allocationFactor = allocationFactorRepo.findOneByAccountId(ledgerLine.getAccountId());

                        if(allocationFactor != null){

                            Set<FactorPercentageDistro> percentages = factorPercentageDistroRepo.findByFactorIdAndValidityDateId(allocationFactor.getFactor().getId(), range.getId());

                            if(Checker.collectionIsNotEmpty(percentages)) {

                                int counter = 0;
                                int lastIdx = percentages.size();
                                BigDecimal glTotalDebit = BigDecimal.ZERO;
                                BigDecimal glTotalCredit = BigDecimal.ZERO;

                                for(FactorPercentageDistro obj:percentages) {
                                    counter++;  // used to check if end of loop

                                    System.out.println("TESTERS: "+ledgerLine.getAccountId() +" - "+ obj.getBusinessSegment().getId());
                                    SegmentAccount segmentAccount = segmentAccountRepo.findOneByAccountIdAndBusinessSegmentId(ledgerLine.getAccountId(), obj.getBusinessSegment().getId());
                                    BigDecimal percentage = obj.getPercentage();

                                    BigDecimal glShareDebit = (percentage.multiply(glAmountDebit)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                    BigDecimal glShareCredit = (percentage.multiply(glAmountCredit)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                    if ((glShareDebit == null || glShareDebit.compareTo(BigDecimal.ZERO) == 0) && (glShareCredit == null || glShareCredit.compareTo(BigDecimal.ZERO) == 0)) {
                                        count ++;
                                        continue;
                                    }

                                    GeneralLedger generalLedger = new GeneralLedger();
                                    generalLedger.setTransaction(transaction);

                                    glTotalDebit = glTotalDebit.add(glShareDebit);
                                    if (glTotalDebit.compareTo(glAmountDebit) > 0) {
                                        // glTotal: 101, amount: 100
                                        // 101 - 100 = 1
                                        // 101 - 1 = 100
                                        BigDecimal i01 = glTotalDebit.subtract(glAmountDebit);
                                        glShareDebit = glShareDebit.subtract(i01);
                                    } else if (counter == lastIdx) {
                                        if (glTotalDebit.compareTo(glAmountDebit) < 0) {
                                            // glTotal: 100, amount: 101
                                            // 101 - 100 = 1
                                            // 100 + 1 = 101
                                            BigDecimal i01 = glAmountDebit.subtract(glTotalDebit);
                                            glShareDebit = glShareDebit.add(i01);
                                        }
                                    }


                                    glTotalCredit = glTotalCredit.add(glShareCredit);
                                    if (glTotalCredit.compareTo(glAmountCredit) > 0) {
                                        // glTotal: 101, amount: 100
                                        // 101 - 100 = 1
                                        // 101 - 1 = 100
                                        BigDecimal i01 = glTotalCredit.subtract(glAmountCredit);
                                        glShareCredit = glShareCredit.subtract(i01);
                                    } else if (counter == lastIdx) {
                                        if (glTotalCredit.compareTo(glAmountCredit) < 0) {
                                            // glTotal: 100, amount: 101
                                            // 101 - 100 = 1
                                            // 100 + 1 = 101
                                            BigDecimal i01 = glAmountCredit.subtract(glTotalCredit);
                                            glShareCredit = glShareCredit.add(i01);
                                        }
                                    }

                            /*    if (isDebit) {
                                    generalLedger.setDebit(glShare);
                                } else {
                                    generalLedger.setCredit(glShare);
                                }*/

                                    generalLedger.setDebit(glShareDebit);
                                    generalLedger.setCredit(glShareCredit);

                                    generalLedger.setSegmentAccount(segmentAccount);

                                    GeneralLedger newGl = generalLedgerRepo.save(generalLedger);

                                    count += newGl == null ? 0 : 1;


                                    cashFlowAccountEntryFacade.setCashFlowAccountEntries(newGl, ledgerLine, voucherDate);

                                    if (Checker.collectionIsEmpty(subLedgerLines)) continue;


                                    int counterSl = 0;
                                    int lastIdxSl = subLedgerLines.size() -1 ;
                                    BigDecimal slTotalDebit = BigDecimal.ZERO;
                                    BigDecimal slTotalCredit = BigDecimal.ZERO;

                                    for(SubLedgerDto subLedgerLine: subLedgerLines) {

                                        if (subLedgerLine.getAccountId().equals(ledgerLine.getAccountId())) {
                                            SubLedger subLedger = new SubLedger();
                                            subLedger.setTransaction(transaction);
                                            subLedger.setSegmentAccount(segmentAccount);
                                            subLedger.setTransaction(newGl.getTransaction());
                                            subLedger.setGeneralLedger(newGl);

                                            SlEntity slEntity = new SlEntity();
                                            slEntity.setAccountNo(subLedgerLine.getAccountNo());
                                            subLedger.setSlEntity(slEntity);

                                            BigDecimal slAmountDebit = subLedgerLine.getDebit();
                                            BigDecimal slAmountCredit = subLedgerLine.getCredit();

                                            // prevent insertion of SL entries having no amount
                                        /*if (slAmount == null || slAmount.compareTo(BigDecimal.ZERO) == 0) {
                                            continue;
                                        }*/

                                            boolean noDebitAmount = slAmountDebit == null || slAmountDebit.compareTo(BigDecimal.ZERO) == 0;
                                            boolean noCreditAmount = slAmountCredit == null || slAmountCredit.compareTo(BigDecimal.ZERO) == 0;

                                            if (noDebitAmount && noCreditAmount) {
                                                continue;
                                            }

                                            BigDecimal slShareDebit = (percentage.multiply(slAmountDebit)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                            BigDecimal slShareCredit = (percentage.multiply(slAmountCredit)).setScale(2, BigDecimal.ROUND_HALF_UP);

                                            slTotalDebit = slTotalDebit.add(slShareDebit);

                                            if (slTotalDebit.compareTo(glShareDebit) > 0) {
                                                // glTotal: 101, amount: 100
                                                // 101 - 100 = 1
                                                // 101 - 1 = 100
                                                BigDecimal i01 = slTotalDebit.subtract(glShareDebit);
                                                slShareDebit = slShareDebit.subtract(i01);
                                            } else if (counterSl == lastIdxSl) {
                                                if (slTotalDebit.compareTo(glShareDebit) < 0) {
                                                    // glTotal: 100, amount: 101
                                                    // 101 - 100 = 1
                                                    // 100 + 1 = 101
                                                    BigDecimal i01 = glShareDebit.subtract(slTotalDebit);
                                                    slShareDebit = slShareDebit.add(i01);
                                                }
                                            }

                                            slTotalCredit = slTotalCredit.add(slShareCredit);
                                            if (slTotalCredit.compareTo(glShareCredit) > 0) {
                                                // glTotal: 101, amount: 100
                                                // 101 - 100 = 1
                                                // 101 - 1 = 100
                                                BigDecimal i01 = slTotalDebit.subtract(glShareCredit);
                                                slShareCredit = slShareCredit.subtract(i01);
                                            } else if (counterSl == lastIdxSl) {
                                                if (slTotalCredit.compareTo(glShareCredit) < 0) {
                                                    // glTotal: 100, amount: 101
                                                    // 101 - 100 = 1
                                                    // 100 + 1 = 101
                                                    BigDecimal i01 = glShareCredit.subtract(slTotalCredit);
                                                    slShareCredit = slShareCredit.add(i01);
                                                }
                                            }

                                        /*if (isDebit)
                                            subLedger.setDebit(slShare);
                                        else
                                            subLedger.setCredit(slShare);*/

                                            subLedger.setDebit(slShareDebit);
                                            subLedger.setCredit(slShareCredit);

                                            subLedgerRepo.save(subLedger);

                                            counterSl++;
                                        }
                                    }
                                }

                                break;
                            }

                        }
                    }
                }
            } else {    // manually allocated

                BigDecimal slTotalDebit = BigDecimal.ZERO;
                BigDecimal slTotalCredit = BigDecimal.ZERO;

                for(Map obj:distribution) {
                    BigDecimal glShare = new BigDecimal(obj.get("amount") != null ? obj.get("amount").toString().replace(",", ""):"0");
//                    BigDecimal glShare = new BigDecimal(obj.get("amount") != null ? obj.get("amount").toString():"0");
                    Integer segmentAccountId = (Integer)obj.get("segmentAccountId");

                    // prevent insertion of GL entries having no debit or credit amount
                    if (glShare == null || glShare.compareTo(BigDecimal.ZERO) == 0) {
                        count ++;
                        continue;
                    }

                    GeneralLedger generalLedger = new GeneralLedger();
                    generalLedger.setTransaction(transaction);
                    if (isDebit) {
                        generalLedger.setDebit(glShare);
                    } else {
                        generalLedger.setCredit(glShare);
                    }

                    SegmentAccount segmentAccount = new SegmentAccount();
                    segmentAccount.setId(segmentAccountId);
                    generalLedger.setSegmentAccount(segmentAccount);

                    GeneralLedger newGl = generalLedgerRepo.save(generalLedger);

                    count += newGl == null ? 0 : 1;

                    cashFlowAccountEntryFacade.setCashFlowAccountEntries(newGl, ledgerLine, voucherDate);

                    if (subLedgerLines.isEmpty()) continue;
                    for(SubLedgerDto subLedgerLine: subLedgerLines) {

                        if (subLedgerLine.getAccountId().equals(ledgerLine.getAccountId())) {
                            SubLedger subLedger = new SubLedger();
                            subLedger.setSegmentAccount(segmentAccount);
                            subLedger.setTransaction(newGl.getTransaction());
                            subLedger.setGeneralLedger(newGl);

                            SlEntity slEntity = new SlEntity();
                            slEntity.setAccountNo(subLedgerLine.getAccountNo());
                            subLedger.setSlEntity(slEntity);

                            BigDecimal slAmount = subLedgerLine.getAmount();
                            BigDecimal slAmountDebit = subLedgerLine.getDebit();
                            BigDecimal slAmountCredit = subLedgerLine.getCredit();

                            // prevent insertion of SL entries having no amount
                           /* if (slAmount == null || slAmount.compareTo(BigDecimal.ZERO) == 0) {
                                continue;
                            }*/

                            boolean noDebitAmount = slAmountDebit == null || slAmountDebit.compareTo(BigDecimal.ZERO) == 0;
                            boolean noCreditAmount = slAmountCredit == null || slAmountCredit.compareTo(BigDecimal.ZERO) == 0;

                            if (noDebitAmount && noCreditAmount) {
                                continue;
                            }

                            // conversion
                            BigDecimal percentDebit = BigDecimal.ZERO;
                            BigDecimal percentCredit = BigDecimal.ZERO;
                            BigDecimal slShareDebit = BigDecimal.ZERO;
                            BigDecimal slShareCredit = BigDecimal.ZERO;


                            if(glAmountDebit.compareTo(BigDecimal.ZERO) > 0){
                                percentDebit = (glShare.multiply(new BigDecimal(100))).divide(glAmountDebit, 2, BigDecimal.ROUND_HALF_UP);
                                slShareDebit = ((percentDebit.divide(new BigDecimal(100))).multiply(slAmountDebit)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            } else {
                                percentCredit = (glShare.multiply(new BigDecimal(100))).divide(glAmountCredit, 2, BigDecimal.ROUND_HALF_UP);
                                slShareCredit = ((percentCredit.divide(new BigDecimal(100))).multiply(slAmountCredit)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            }

                            slTotalDebit = slTotalDebit.add(slShareDebit);
                            slTotalCredit = slTotalCredit.add(slShareCredit);
                            if (slTotalDebit.compareTo(glAmountDebit) > 0) {
                                // slTotal: 101, glAmount: 100
                                // 101 - 100 = 1
                                // 101 - 1 = 100
                                BigDecimal i01 = slTotalDebit.subtract(glAmountDebit);
                                slShareDebit = slShareDebit.subtract(i01);
                            }

                            if (slTotalCredit.compareTo(glAmountCredit) > 0) {
                                // slTotal: 101, glAmount: 100
                                // 101 - 100 = 1
                                // 101 - 1 = 100
                                BigDecimal i01 = slTotalCredit.subtract(glAmountCredit);
                                slShareCredit = slShareCredit.subtract(i01);
                            }

                            /*if (isDebit)
                                subLedger.setDebit(slShare);
                            else
                                subLedger.setCredit(slShare);*/

                            subLedger.setDebit(slShareDebit);
                            subLedger.setCredit(slShareCredit);

                            subLedgerRepo.save(subLedger);
                        }
                    }
                }

            }

            if (ledgerLine.getwTaxEntry() != null) {

                CheckVoucherIncomePayment incomePayment = incomePaymentRepo.findOneByTransactionId(transaction.getId());
                if (incomePayment == null) incomePayment = new CheckVoucherIncomePayment();
                LinkedHashMap atc = (LinkedHashMap)ledgerLine.getwTaxEntry().get("atc");

                TaxCode taxCode = new TaxCode();
                taxCode.setId(Integer.parseInt(atc.get("id").toString()));

                incomePayment.setTransaction(transaction);
                incomePayment.setAmount(new BigDecimal(ledgerLine.getwTaxEntry().get("amount").toString()));
                incomePayment.setBaseAmount(new BigDecimal(ledgerLine.getwTaxEntry().get("baseAmount").toString()));
                incomePayment.setPercentage(new BigDecimal(ledgerLine.getwTaxEntry().get("percentage").toString()));
                incomePayment.setTaxCode(taxCode);

                incomePaymentRepo.save(incomePayment);
            }
        }
        return count;
    }

    /*@Override
    public Long postGeneralLedger(Transaction transaction, List<GeneralLedgerLineDto2> generalLedgerLines, List<SubLedgerDto> subLedgerLines, Date voucherDate) {

        Long count = 0L;

        if (transaction != null && transaction.getId() > 0) {
            voucherCashflowDetailRepo.deleteByTransactionId(transaction.getId());
            generalLedgerRepo.deleteByTransactionId(transaction.getId());
            subLedgerRepo.deleteByTransactionId(transaction.getId());
            incomePaymentRepo.deleteByTransactionId(transaction.getId());
        }

        for(GeneralLedgerLineDto2 ledgerLine: generalLedgerLines) {

            Integer generalLedgerLineIndex = generalLedgerLines.indexOf(ledgerLine);    // determine correct ledger in case lines have same account used

            boolean isDebit = true;
            BigDecimal glAmount = ledgerLine.getDebit();
            if (glAmount == null || glAmount.compareTo(BigDecimal.ZERO) == 0) {
                isDebit = false;
                glAmount = ledgerLine.getCredit();
            }

            // prevent insertion of GL entries having no debit or credit amount
            if (glAmount == null || glAmount.compareTo(BigDecimal.ZERO) == 0) {
                count ++;
                continue;
            }

            List<Map> distribution = ledgerLine.getDistribution();

            if(distribution.isEmpty()) {

                java.sql.Date sqlDate = new java.sql.Date(voucherDate.getTime());

                List<DateRange> dateRanges = dateRangeRepo.findByEndGreaterThanEqualAndStartLessThanEqual(sqlDate, sqlDate);
                if(Checker.collectionIsNotEmpty(dateRanges)) {

                    for (DateRange range : dateRanges) {

                        // get percentage distribution
                        AllocationFactor allocationFactor = allocationFactorRepo.findOneByAccountId(ledgerLine.getAccountId());
                        Set<FactorPercentageDistro> percentages = factorPercentageDistroRepo.findByFactorIdAndValidityDateId(allocationFactor.getFactor().getId(), range.getId());

                        if(Checker.collectionIsNotEmpty(percentages)) {

                            int counter = 0;
                            int lastIdx = percentages.size();
                            BigDecimal glTotal = BigDecimal.ZERO;

                            for(FactorPercentageDistro obj:percentages) {
                                counter++;  // used to check if end of loop

                                SegmentAccount segmentAccount = segmentAccountRepo.findOneByAccountIdAndBusinessSegmentId(ledgerLine.getAccountId(), obj.getBusinessSegment().getId());
                                BigDecimal percentage = obj.getPercentage();

                                BigDecimal glShare = (percentage.multiply(glAmount)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                if (glShare == null || glShare.compareTo(BigDecimal.ZERO) == 0) {
                                    count ++;
                                    continue;
                                }

                                GeneralLedger generalLedger = new GeneralLedger();
                                generalLedger.setTransaction(transaction);

                                glTotal = glTotal.add(glShare);
                                if (glTotal.compareTo(glAmount) > 0) {
                                    // glTotal: 101, amount: 100
                                    // 101 - 100 = 1
                                    // 101 - 1 = 100
                                    BigDecimal i01 = glTotal.subtract(glAmount);
                                    glShare = glShare.subtract(i01);
                                } else if (counter == lastIdx) {
                                    if (glTotal.compareTo(glAmount) < 0) {
                                        // glTotal: 100, amount: 101
                                        // 101 - 100 = 1
                                        // 100 + 1 = 101
                                        BigDecimal i01 = glAmount.subtract(glTotal);
                                        glShare = glShare.add(i01);
                                    }
                                }

                                if (isDebit) {
                                    generalLedger.setDebit(glShare);
                                } else {
                                    generalLedger.setCredit(glShare);
                                }

                                generalLedger.setSegmentAccount(segmentAccount);

                                GeneralLedger newGl = generalLedgerRepo.save(generalLedger);

                                count += newGl == null ? 0 : 1;

                                cashFlowAccountEntryFacade.setCashFlowAccountEntries(newGl, ledgerLine, voucherDate, counter == lastIdx, percentage);

                                if (Checker.collectionIsEmpty(subLedgerLines)) continue;

                                int counterSl = 0;
                                int lastIdxSl = subLedgerLines.size() -1 ;
                                BigDecimal slTotal = BigDecimal.ZERO;

                                for(SubLedgerDto subLedgerLine: subLedgerLines) {

                                    if (subLedgerLine.getAccountId().equals(ledgerLine.getAccountId()) &&
                                            subLedgerLine.getGeneralLedgerLineIndex().equals(generalLedgerLineIndex)) {

                                        BigDecimal slAmount = subLedgerLine.getAmount();

                                        // prevent insertion of SL entries having no amount
                                        if (slAmount == null || slAmount.compareTo(BigDecimal.ZERO) == 0) {
                                            continue;
                                        }

                                        SubLedger subLedger = new SubLedger();
                                        subLedger.setTransaction(transaction);
                                        subLedger.setSegmentAccount(segmentAccount);
                                        subLedger.setTransaction(newGl.getTransaction());
                                        subLedger.setGeneralLedger(newGl);

                                        SlEntity slEntity = new SlEntity();
                                        slEntity.setAccountNo(subLedgerLine.getAccountNo());
                                        subLedger.setSlEntity(slEntity);

                                        BigDecimal slShare = (percentage.multiply(slAmount)).setScale(2, BigDecimal.ROUND_HALF_UP);

                                        slTotal = slTotal.add(slShare);
                                        if (slTotal.compareTo(glShare) > 0) {
                                            // glTotal: 101, amount: 100
                                            // 101 - 100 = 1
                                            // 101 - 1 = 100
                                            BigDecimal i01 = slTotal.subtract(glShare);
                                            slShare = slShare.subtract(i01);
                                        } else if (counterSl == lastIdxSl) {
                                            if (slTotal.compareTo(glShare) < 0) {
                                                // glTotal: 100, amount: 101
                                                // 101 - 100 = 1
                                                // 100 + 1 = 101
                                                BigDecimal i01 = glShare.subtract(slTotal);
                                                slShare = slShare.add(i01);
                                            }
                                        }

                                        if (isDebit)
                                            subLedger.setDebit(slShare);
                                        else
                                            subLedger.setCredit(slShare);

                                        subLedgerRepo.save(subLedger);

                                        counterSl++;
                                    }
                                }
                            }

                            break; // process only one date range
                        }
                    }
                }
            } else {    // manually allocated

                BigDecimal slTotal = BigDecimal.ZERO;

                for(Map obj:distribution) {
                    BigDecimal glShare = new BigDecimal(obj.get("amount") != null ? obj.get("amount").toString():"0");
                    Integer segmentAccountId = (Integer)obj.get("segmentAccountId");

                    // prevent insertion of GL entries having no debit or credit amount
                    if (glShare == null || glShare.compareTo(BigDecimal.ZERO) == 0) {
                        count ++;
                        continue;
                    }

                    GeneralLedger generalLedger = new GeneralLedger();
                    generalLedger.setTransaction(transaction);
                    if (isDebit) {
                        generalLedger.setDebit(glShare);
                    } else {
                        generalLedger.setCredit(glShare);
                    }

                    SegmentAccount segmentAccount = new SegmentAccount();
                    segmentAccount.setId(segmentAccountId);
                    generalLedger.setSegmentAccount(segmentAccount);

                    GeneralLedger newGl = generalLedgerRepo.save(generalLedger);

                    count += newGl == null ? 0 : 1;

                    cashFlowAccountEntryFacade.setCashFlowAccountEntries(newGl, ledgerLine, voucherDate, false, null);

                    if (subLedgerLines.isEmpty()) continue;
                    for(SubLedgerDto subLedgerLine: subLedgerLines) {

                        if (subLedgerLine.getAccountId().equals(ledgerLine.getAccountId())) {
                            SubLedger subLedger = new SubLedger();
                            subLedger.setSegmentAccount(segmentAccount);
                            subLedger.setTransaction(newGl.getTransaction());
                            subLedger.setGeneralLedger(newGl);

                            SlEntity slEntity = new SlEntity();
                            slEntity.setAccountNo(subLedgerLine.getAccountNo());
                            subLedger.setSlEntity(slEntity);

                            BigDecimal slAmount = subLedgerLine.getAmount();

                            // prevent insertion of SL entries having no amount
                            if (slAmount == null || slAmount.compareTo(BigDecimal.ZERO) == 0) {
                                continue;
                            }

                            // conversion
                            BigDecimal percent = (glShare.multiply(new BigDecimal(100))).divide(glAmount, 2, BigDecimal.ROUND_HALF_UP);
                            BigDecimal slShare = ((percent.divide(new BigDecimal(100))).multiply(slAmount)).setScale(2, BigDecimal.ROUND_HALF_UP);

                            slTotal = slTotal.add(slShare);
                            if (slTotal.compareTo(glAmount) > 0) {
                                // slTotal: 101, glAmount: 100
                                // 101 - 100 = 1
                                // 101 - 1 = 100
                                BigDecimal i01 = slTotal.subtract(glAmount);
                                slShare = slShare.subtract(i01);
                            }

                            if (isDebit)
                                subLedger.setDebit(slShare);
                            else
                                subLedger.setCredit(slShare);

                            subLedgerRepo.save(subLedger);
                        }
                    }
                }

            }


            if (ledgerLine.getwTaxEntry() != null) {

                LinkedHashMap atc = (LinkedHashMap)ledgerLine.getwTaxEntry().get("atc");
                Integer taxCodeId = Integer.parseInt(atc.get("id").toString());

                CheckVoucherIncomePayment incomePayment = incomePaymentRepo.findOneByTransactionIdAndTaxCodeIdAndAccountId(transaction.getId(), taxCodeId, ledgerLine.getAccountId());
                if (incomePayment == null) incomePayment = new CheckVoucherIncomePayment();

                TaxCode taxCode = new TaxCode();
                taxCode.setId(taxCodeId);

                incomePayment.setTransaction(transaction);
                incomePayment.setAmount(new BigDecimal(ledgerLine.getwTaxEntry().get("amount").toString()));
                incomePayment.setBaseAmount(new BigDecimal(ledgerLine.getwTaxEntry().get("baseAmount").toString()));
                incomePayment.setPercentage(new BigDecimal(ledgerLine.getwTaxEntry().get("percentage").toString()));
                incomePayment.setTaxCode(taxCode);
                incomePayment.setAccount(new Account(ledgerLine.getAccountId()));

                incomePaymentRepo.save(incomePayment);
            }
        }
        return count;
    }*/

    @Override
    public void saveTempLedgerEntries(Map<Integer, BigDecimal> accountsDetailMap, java.sql.Date date, TemporaryBatch temporaryBatch, Integer accountNo, boolean isDebit) {
        Iterator hmIterator = accountsDetailMap.entrySet().iterator();

        while (hmIterator.hasNext()) {

            Map.Entry mapElement = (Map.Entry)hmIterator.next();

            BigDecimal glAmount = (BigDecimal) mapElement.getValue();
            Integer accountId = (Integer) mapElement.getKey();

            Account account = accountRepo.findById(accountId).orElse(null);

            List<DateRange> dateRanges = dateRangeRepo.findByEndGreaterThanEqualAndStartLessThanEqual(date, date);
            if(Checker.collectionIsNotEmpty(dateRanges)) {

                for (DateRange range : dateRanges) {

                    // get percentage distribution
                    AllocationFactor allocationFactor = allocationFactorRepo.findOneByAccountId(accountId);
                    Set<FactorPercentageDistro> percentages = factorPercentageDistroRepo.findByFactorIdAndValidityDateId(allocationFactor.getFactor().getId(), range.getId());

                    if (Checker.collectionIsNotEmpty(percentages)) {

                        int counter = 0;
                        int lastIdx = percentages.size();
                        BigDecimal glTotal = BigDecimal.ZERO;

                        for(FactorPercentageDistro obj:percentages) {
                            counter++;  // used to check if end of loop

                            SegmentAccount segmentAccount = segmentAccountRepo.findOneByAccountIdAndBusinessSegmentId(accountId, obj.getBusinessSegment().getId());
                            BigDecimal percentage = obj.getPercentage();

                            BigDecimal glShare = (percentage.multiply(glAmount)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            if (glShare == null || glShare.compareTo(BigDecimal.ZERO) == 0) {
                                continue;
                            }

                            TemporaryGeneralLedger temporaryGeneralLedger = new TemporaryGeneralLedger();
                            temporaryGeneralLedger.setTransaction(temporaryBatch.getTransaction());
                            temporaryGeneralLedger.setTemporaryBatch(temporaryBatch);

                            glTotal = glTotal.add(glShare);
                            glShare = ServiceUtil.getLastGlShare(glShare, glTotal, glAmount, counter, lastIdx);

                            if(isDebit) {
                                temporaryGeneralLedger.setDebit(glShare);
                                temporaryGeneralLedger.setCredit(BigDecimal.ZERO);
                            } else {
                                temporaryGeneralLedger.setCredit(glShare);
                                temporaryGeneralLedger.setDebit(BigDecimal.ZERO);
                            }

                            temporaryGeneralLedger.setSegmentAccount(segmentAccount);
                            temporaryGeneralLedger.setAccount(account);

                            temporaryGeneralLedgerRepo.save(temporaryGeneralLedger);


                            if (account.getHasSL() == 1) {
                                // sub ledger

                                TemporarySubLedger temporarySubLedger = new TemporarySubLedger();
                                temporarySubLedger.setSegmentAccount(segmentAccount);

                                if(isDebit) {
                                    temporarySubLedger.setDebit(glShare);
                                    temporarySubLedger.setCredit(BigDecimal.ZERO);
                                } else {
                                    temporarySubLedger.setCredit(glShare);
                                    temporarySubLedger.setDebit(BigDecimal.ZERO);
                                }

                                temporarySubLedger.setTemporaryBatch(temporaryBatch);
                                temporarySubLedger.setTransaction(temporaryBatch.getTransaction());
                                temporarySubLedger.setBalance(glShare);
                                temporarySubLedger.setTemporaryGeneralLedger(temporaryGeneralLedger);

                                SlEntity slEntity = new SlEntity();
                                slEntity.setAccountNo(accountNo);

                                temporarySubLedger.setSlEntity(slEntity);

                                temporarySubLedgerRepo.save(temporarySubLedger);
                            }

                        }

                        break; // process only one date range
                    }
                }

            }
        }
    }

    @Override
    public Map computeGLTotals(List<GeneralLedgerLineDto2> generalLedgerLines, Date voucherDate) {

        BigDecimal grandDebitTotal = BigDecimal.ZERO;
        BigDecimal grandCreditTotal = BigDecimal.ZERO;

        outer:
        for(GeneralLedgerLineDto2 ledgerLine: generalLedgerLines) {

            if(!Checker.isValidId(ledgerLine.getAccountId())) {
                break;
            }

            BigDecimal glAmount = ledgerLine.getDebit();    // get debit amount of a ledger entry line

            boolean isDebit = Checker.isAmountGreaterThanZero(glAmount);    // check if has value
            if (!isDebit) {  // if debit column has no value, means entry is in credit
                glAmount = ledgerLine.getCredit();
            }

            // dont compute if has no debit or credit amount
            if (!Checker.isAmountGreaterThanZero(glAmount)) {
                continue;
            }

            List<Map> distribution = ledgerLine.getDistribution();

            if(distribution.isEmpty()) {    // ledger entry is not manually setup when creating voucher

                java.sql.Date sqlDate = new java.sql.Date(voucherDate.getTime());

                // make sure setting is within validity date
                List<DateRange> dateRanges = dateRangeRepo.findByEndGreaterThanEqualAndStartLessThanEqual(sqlDate, sqlDate);
                if(Checker.collectionIsNotEmpty(dateRanges)) {

                    inner:
                    for (DateRange range : dateRanges) {

                        // get percentage distribution of Allocation Factor assigned the account of the ledger line
                        AllocationFactor allocationFactor = allocationFactorRepo.findOneByAccountId(ledgerLine.getAccountId());
                        Set<FactorPercentageDistro> percentages = factorPercentageDistroRepo.findByFactorIdAndValidityDateId(allocationFactor.getFactor().getId(), range.getId());

                        if(Checker.collectionIsNotEmpty(percentages)) {

                            int counter = 0;
                            int lastIdx = percentages.size();

                            BigDecimal glTotal = BigDecimal.ZERO;

                            boolean hasPercentageAboveZero = false; // guard for manual allocations
                            for(FactorPercentageDistro obj:percentages) {
                                counter++;  // used to check if end of loop

                                BigDecimal percentage = obj.getPercentage();

                                // overall GL amount is will be computed/distributed according to percentage share
                                BigDecimal glShare = (percentage.multiply(glAmount)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                if (!Checker.isAmountGreaterThanZero(glShare)) {
                                    continue;
                                } else {
                                    hasPercentageAboveZero = true;
                                }

                                boolean endLedgerCompute = false;

                                glTotal = glTotal.add(glShare); // save total shares computed

                                // check if total computed by percentage is higher than actual GL line amount
                                if (glTotal.compareTo(glAmount) > 0) {
                                    // glTotal: 101, glAmount: 100, glShare: 50
                                    // 101 - 100 = 1
                                    // 50 - 1 = 49
                                    BigDecimal i01 = glTotal.subtract(glAmount);
                                    glShare = glShare.subtract(i01);

                                    endLedgerCompute = true;

                                } else if (counter == lastIdx) {    // last line
                                    if (glTotal.compareTo(glAmount) < 0) { // but total computed is short
                                        // glTotal: 100, amount: 101, glShare: 48
                                        // 101 - 100 = 1
                                        // 48 + 1 = 49
                                        BigDecimal i01 = glAmount.subtract(glTotal);
                                        glShare = glShare.add(i01);
                                    }

                                    endLedgerCompute = true;
                                }

                                if(isDebit) {
                                    grandDebitTotal = grandDebitTotal.add(glShare);
                                } else {
                                    grandCreditTotal = grandCreditTotal.add(glShare);
                                }

                                if(endLedgerCompute) {
                                    break inner;
                                }
                            }

                            if(!hasPercentageAboveZero) {
                                if(isDebit) {
                                    grandDebitTotal = grandDebitTotal.add(glAmount);
                                } else {
                                    grandCreditTotal = grandCreditTotal.add(glAmount);
                                }
                            }
                            break; // process only one date range
                        }

                    }
                }
            } else {    // manually allocated

                for(Map obj:distribution) {
                    BigDecimal glShare = new BigDecimal(obj.get("amount") != null ? obj.get("amount").toString():"0");

                    if (!Checker.isAmountGreaterThanZero(glShare)) {
                        continue;
                    }

                    if(isDebit) {
                        grandDebitTotal = grandDebitTotal.add(glShare);
                    } else {
                        grandCreditTotal = grandCreditTotal.add(glShare);
                    }

                }

            }
        }

        Map totalsMap = new HashMap();

        totalsMap.put("grandDebitTotal", grandDebitTotal);
        totalsMap.put("grandCreditTotal", grandCreditTotal);

        return totalsMap;
    }

    public boolean totalsAreBalanced(List<GeneralLedgerLineDto2> generalLedgerLines, Date voucherDate) {

        BigDecimal grandDebitTotal = BigDecimal.ZERO;
        BigDecimal grandCreditTotal = BigDecimal.ZERO;

        Map map = this.computeGLTotals(generalLedgerLines, voucherDate);

        Object grandDebitTotalObj = map.get("grandDebitTotal");

        if(grandDebitTotalObj != null) {
            grandDebitTotal = (BigDecimal) grandDebitTotalObj;
        }

        Object grandCreditTotalObj = map.get("grandCreditTotal");
        if(grandCreditTotalObj != null) {
            grandCreditTotal = (BigDecimal) grandCreditTotalObj;
        }

        return grandCreditTotal.compareTo(grandDebitTotal) == 0;
    }

    /*public Long postGeneralLedger(Transaction transaction, List<GeneralLedgerLineDto2> generalLedgerLines, List<SubLedgerDto> subLedgerLines, Date voucherDate) {
        Long count = 0L;

        if (transaction != null && transaction.getId() > 0) {
            voucherCashflowDetailRepo.deleteByTransactionId(transaction.getId());
            generalLedgerRepo.deleteByTransactionId(transaction.getId());
            subLedgerRepo.deleteByTransactionId(transaction.getId());
            incomePaymentRepo.deleteByTransactionId(transaction.getId());
        }

        for(GeneralLedgerLineDto2 ledgerLine: generalLedgerLines) {
            boolean isDebit = true;
            BigDecimal glAmount = ledgerLine.getDebit();
            if (glAmount == null || glAmount.compareTo(BigDecimal.ZERO) == 0) {
                isDebit = false;
                glAmount = ledgerLine.getCredit();
            }

            // prevent insertion of GL entries having no debit or credit amount
            if (glAmount == null || glAmount.compareTo(BigDecimal.ZERO) == 0) {
                count ++;
                continue;
            }

            List<Map> distribution = ledgerLine.getDistribution();
            List<Object[]> percentages = allocationFactorRepo.findByAccountId(ledgerLine.getAccountId());

            if (Checker.collectionIsEmpty(distribution)) { // automatic sl distribution

                int counter = 0;
                int lastIdx = percentages.size();
                BigDecimal glTotal = BigDecimal.ZERO;

                for(Object[] obj:percentages) {
                    counter++;  // used to check if end of loop

                    Integer segmentId = (Integer)obj[1];
                    BigDecimal percentage = (BigDecimal)obj[2];

                    BigDecimal glShare  = (percentage.divide(new BigDecimal(100))).multiply(glAmount).setScale(2, BigDecimal.ROUND_HALF_UP);

                    GeneralLedger generalLedger = new GeneralLedger();
                    generalLedger.setTransaction(transaction);

                    glTotal = glTotal.add(glShare);
                    if (glTotal.compareTo(glAmount) > 0) {
                        // glTotal: 101, amount: 100
                        // 101 - 100 = 1
                        // 101 - 1 = 100
                        BigDecimal i01 = glTotal.subtract(glAmount);
                        glShare = glShare.subtract(i01);
                    } else if (counter == lastIdx) {
                        if (glTotal.compareTo(glAmount) < 0) {
                            // glTotal: 100, amount: 101
                            // 101 - 100 = 1
                            // 100 + 1 = 101
                            BigDecimal i01 = glAmount.subtract(glTotal);
                            glShare = glShare.add(i01);
                        }
                    }

                    if (isDebit) {
                        generalLedger.setDebit(glShare);
                    } else {
                        generalLedger.setCredit(glShare);
                    }

                    SegmentAccount segmentAccount = new SegmentAccount();
                    segmentAccount.setId(segmentId);
                    generalLedger.setSegmentAccount(segmentAccount);

                    GeneralLedger newGl = generalLedgerRepo.save(generalLedger);

                    count += newGl == null ? 0 : 1;


                    cashFlowAccountEntryFacade.setCashFlowAccountEntries(newGl, ledgerLine, voucherDate);

                    if (Checker.collectionIsEmpty(subLedgerLines)) continue;


                    for(SubLedgerDto subLedgerLine: subLedgerLines) {

                        if (subLedgerLine.getAccountId().equals(ledgerLine.getAccountId())) {
                            SubLedger subLedger = new SubLedger();
                            subLedger.setTransaction(transaction);
                            subLedger.setSegmentAccount(segmentAccount);
                            subLedger.setTransaction(newGl.getTransaction());
                            subLedger.setGeneralLedger(newGl);

                            SlEntity slEntity = new SlEntity();
                            slEntity.setAccountNo(subLedgerLine.getAccountNo());
                            subLedger.setSlEntity(slEntity);

                            BigDecimal slAmount = subLedgerLine.getAmount();

                            // prevent insertion of SL entries having no amount
                            if (slAmount == null || slAmount.compareTo(BigDecimal.ZERO) == 0) {
                                continue;
                            }

                            BigDecimal slShare = ((percentage.divide(new BigDecimal(100))).multiply(slAmount)).setScale(2, BigDecimal.ROUND_HALF_UP);

                            if (isDebit)
                                subLedger.setDebit(slShare);
                            else
                                subLedger.setCredit(slShare);

                            subLedgerRepo.save(subLedger);
                        }
                    }
                }
            } else {
                BigDecimal slTotal = BigDecimal.ZERO;

                for(Map obj:distribution) {
                    BigDecimal glShare = new BigDecimal(obj.get("amount").toString());
                    Integer segmentAccountId = (Integer)obj.get("segmentAccountId");

                    // prevent insertion of GL entries having no debit or credit amount
                    if (glShare == null || glShare.compareTo(BigDecimal.ZERO) == 0) {
                        count ++;
                        continue;
                    }

                    GeneralLedger generalLedger = new GeneralLedger();
                    generalLedger.setTransaction(transaction);
                    if (isDebit) {
                        generalLedger.setDebit(glShare);
                    } else {
                        generalLedger.setCredit(glShare);
                    }

                    SegmentAccount segmentAccount = new SegmentAccount();
                    segmentAccount.setId(segmentAccountId);
                    generalLedger.setSegmentAccount(segmentAccount);

                    GeneralLedger newGl = generalLedgerRepo.save(generalLedger);

                    count += newGl == null ? 0 : 1;

                    cashFlowAccountEntryFacade.setCashFlowAccountEntries(newGl, ledgerLine, voucherDate);

                    if (Checker.collectionIsEmpty(subLedgerLines)) continue;
                    for(SubLedgerDto subLedgerLine: subLedgerLines) {

                        if (subLedgerLine.getAccountId().equals(ledgerLine.getAccountId())) {
                            SubLedger subLedger = new SubLedger();
                            subLedger.setSegmentAccount(segmentAccount);
                            subLedger.setTransaction(newGl.getTransaction());
                            subLedger.setGeneralLedger(newGl);

                            SlEntity slEntity = new SlEntity();
                            slEntity.setAccountNo(subLedgerLine.getAccountNo());
                            subLedger.setSlEntity(slEntity);

                            BigDecimal slAmount = subLedgerLine.getAmount();

                            // prevent insertion of SL entries having no amount
                            if (slAmount == null || slAmount.compareTo(BigDecimal.ZERO) == 0) {
                                continue;
                            }

                            // conversion
                            BigDecimal percent = (glShare.multiply(new BigDecimal(100))).divide(glAmount, 2, BigDecimal.ROUND_HALF_UP);
                            BigDecimal slShare = ((percent.divide(new BigDecimal(100))).multiply(slAmount)).setScale(2, BigDecimal.ROUND_HALF_UP);

                            slTotal = slTotal.add(slShare);
                            if (slTotal.compareTo(glAmount) > 0) {
                                // slTotal: 101, glAmount: 100
                                // 101 - 100 = 1
                                // 101 - 1 = 100
                                BigDecimal i01 = slTotal.subtract(glAmount);
                                slShare = slShare.subtract(i01);
                            }

                            if (isDebit)
                                subLedger.setDebit(slShare);
                            else
                                subLedger.setCredit(slShare);

                            subLedgerRepo.save(subLedger);
                        }
                    }
                }
            }

            if (ledgerLine.getwTaxEntry() != null) {

                CheckVoucherIncomePayment incomePayment = incomePaymentRepo.findOneByTransactionId(transaction.getId());
                if (incomePayment == null) incomePayment = new CheckVoucherIncomePayment();
                LinkedHashMap atc = (LinkedHashMap)ledgerLine.getwTaxEntry().get("atc");

                TaxCode taxCode = new TaxCode();
                taxCode.setId(Integer.parseInt(atc.get("id").toString()));

                incomePayment.setTransaction(transaction);
                incomePayment.setAmount(new BigDecimal(ledgerLine.getwTaxEntry().get("amount").toString()));
                incomePayment.setBaseAmount(new BigDecimal(ledgerLine.getwTaxEntry().get("baseAmount").toString()));
                incomePayment.setPercentage(new BigDecimal(ledgerLine.getwTaxEntry().get("percentage").toString()));
                incomePayment.setTaxCode(taxCode);

                incomePaymentRepo.save(incomePayment);
            }
        }
        return count;
    }*/
}
