package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.Debug;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.AllocationFactor;
import com.noreco1.fireflyv2.model.MonthlyCycle;
import com.noreco1.fireflyv2.model.enums.MonthlyCycleStatus;
import com.noreco1.fireflyv2.repo.AllocationFactorRepo;
import com.noreco1.fireflyv2.repo.MonthlyCycleRepo;
import com.noreco1.fireflyv2.controller.response.GeneralLedgerLineDto2;
import com.noreco1.fireflyv2.controller.response.SubLedgerDto;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class CommonValidator {

    public static Errors validateBackdating(Errors errors,  MonthlyCycleRepo monthlyCycleRepo, Date documentDate) {

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(documentDate);

            MonthlyCycle cycle = monthlyCycleRepo.findByYearAndMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1);

            if (cycle != null && cycle.getStatus().equals(MonthlyCycleStatus.CLOSE)) {
                errors.rejectValue("voucherDate", "monthly.cycle.closed");
            }
        }catch (Exception e) {
            Debug.print(e.getLocalizedMessage());
            throw new RuntimeException(e.getMessage() == null ? e.getMessage() : e.getCause().getMessage());
        }
        return errors;
    }
    public static Errors validateJournal(Errors errors, List<GeneralLedgerLineDto2> ledgerLineDtos, List<SubLedgerDto> subLedgerLineDtos, AllocationFactorRepo allocationFactorRepo) {

        Map glAccountAmountMap = new HashMap<>();
        Map slAccountAmountMap = new HashMap<>();

        if(Checker.collectionIsEmpty(ledgerLineDtos)) {
            errors.rejectValue("generalLedgerLines", "journal.empty");
        } else {
            BigDecimal dr = BigDecimal.ZERO;
            BigDecimal cr = BigDecimal.ZERO;
            Boolean hasMissingGL = false;
            Boolean hasMissingSL = false;

            for(GeneralLedgerLineDto2 glLineDto : ledgerLineDtos) {
                if (glLineDto.getCredit() != null) {
                    cr = cr.add(glLineDto.getCredit().setScale(2, RoundingMode.HALF_UP));
                }
                if (glLineDto.getDebit() != null) {
                    dr = dr.add(glLineDto.getDebit().setScale(2, RoundingMode.HALF_UP));
                }

                if ((glLineDto.getAccountId() == null || glLineDto.getAccountId() == 0) && (dr.compareTo(BigDecimal.ZERO) > 0 || cr.compareTo(BigDecimal.ZERO) > 0)) {
                    hasMissingGL = true;
                    break;
                }

                if (glLineDto.getHasSL()) {
                    if(subLedgerLineDtos.isEmpty()) {
                        hasMissingSL = true;
                        break;
                    }
                   /* else {
                        // check in sl entries if GL account has SL entries
                        boolean accountInSLEntries = false;
                        for(SubLedgerDto slLineDto : subLedgerLineDtos) {
                            if( slLineDto.getAccountId().equals(glLineDto.getAccountId()) ) {
                                accountInSLEntries = true;
                                break;
                            }
                        }
                        if(! accountInSLEntries) {
                            hasMissingSL = true;
                            break;
                        }
                    }*/
                }

                BigDecimal prevAmount = (BigDecimal) glAccountAmountMap.get(glLineDto.getAccountId());
                BigDecimal newAmount = (cr.compareTo(BigDecimal.ZERO) == 0) ? dr : cr;
                glAccountAmountMap.put(glLineDto.getAccountId(), prevAmount != null ? newAmount.add(prevAmount) : newAmount);

                // cashflow
                if(!glLineDto.getCashFlowAccounts().isEmpty()) {

                    BigDecimal totalCfPerLine = BigDecimal.ZERO;

                    for(Map cf:glLineDto.getCashFlowAccounts()) {

                        BigDecimal amount = new BigDecimal(cf.get("amount").toString());
                        totalCfPerLine = totalCfPerLine.add(amount);
                    }

                    if(glLineDto.getDebit().compareTo(BigDecimal.ZERO) > 0) {
                        if(totalCfPerLine.compareTo(glLineDto.getDebit()) != 0) {
                            errors.rejectValue("generalLedgerLines", "journal.cashflow.amount.unmatched", new String[] {glLineDto.getSearchText()}, "");
                        }
                    } else {
                        if(glLineDto.getCredit().compareTo(BigDecimal.ZERO) > 0) {
                            if(totalCfPerLine.compareTo(glLineDto.getCredit()) != 0) {
                                errors.rejectValue("generalLedgerLines", "journal.cashflow.amount.unmatched", new String[] {glLineDto.getSearchText()}, "");
                            }
                        }
                    }
                }
                // validate MANUAL allocation, check for distribution
                if (allocationFactorRepo != null) {

                    AllocationFactor allocationFactor = allocationFactorRepo.findOneByAccountId(glLineDto.getAccountId());
                    if(allocationFactor != null && allocationFactor.getFactor().getCode().equals(com.noreco1.fireflyv2.model.enums.Factor.MANUAL.toString())) {

                        if(Checker.collectionIsEmpty(glLineDto.getDistribution())) {
                            errors.rejectValue("generalLedgerLines", "journal.no.segment.allocation", new String[] {glLineDto.getSearchText()}, "");
                        }
                    }
                }
//                // validate MANUAL allocation, check for distribution
//                if (allocationFactorRepo != null) {
//
//                    AllocationFactor allocationFactor = allocationFactorRepo.findOneByAccountId(glLineDto.getAccountId());
//                    if(allocationFactor != null && allocationFactor.getFactor().getCode().equals(com.noreco1.fireflyv2.model.enums.Factor.MANUAL.toString())) {
//
//                        if(Checker.collectionIsEmpty(glLineDto.getDistribution())) {
//                            errors.rejectValue("generalLedgerLines", "journal.no.segment.allocation");
//                        }
//                    }
//                }
//                // validate MANUAL allocation, check for distribution
//                if (allocationFactorRepo != null) {
//
//                    AllocationFactor allocationFactor = allocationFactorRepo.findOneByAccountId(glLineDto.getAccountId());
//                    if(allocationFactor != null && allocationFactor.getFactor().getCode().equals(com.noreco1.fireflyv2.model.enums.Factor.MANUAL.toString())) {
//
//                        if(Checker.collectionIsEmpty(glLineDto.getDistribution())) {
//                            errors.rejectValue("generalLedgerLines", "journal.no.segment.allocation");
//                        }
//                    }
//                }

            }

            if (hasMissingGL) {
                errors.rejectValue("generalLedgerLines", "journal.no.account.selected");
            } else if (hasMissingSL) {
                errors.rejectValue("generalLedgerLines", "journal.sl.missing.entity");
            } else if(dr.compareTo(cr) != 0) {
                errors.rejectValue("generalLedgerLines", "journal.imbalance");
            }
        }

        if(!Checker.collectionIsEmpty(subLedgerLineDtos)) {

            Boolean hasMissingSL = false;
            BigDecimal totalSlAmount = BigDecimal.ZERO;

            for(SubLedgerDto lineDto : subLedgerLineDtos) {
                BigDecimal perEntityAmount = BigDecimal.ZERO;

                if (lineDto.getAmount() != null) {
//                    perEntityAmount = lineDto.getAmount();
                    perEntityAmount = lineDto.getDebit().add(lineDto.getCredit());
                    totalSlAmount = totalSlAmount.add(perEntityAmount);
                }

                if (lineDto.getAccountId() == null || lineDto.getAccountId() == 0) {
                    hasMissingSL = true;
                } else {
                    BigDecimal prevAmount = (BigDecimal) slAccountAmountMap.get(lineDto.getSegmentAccountId());
                    slAccountAmountMap.put(lineDto.getSegmentAccountId(), prevAmount != null ? perEntityAmount.add(prevAmount) : perEntityAmount);
                }
            }

            if (totalSlAmount.compareTo(BigDecimal.ZERO) == 0) {
                errors.rejectValue("generalLedgerLines", "journal.sl.amount.zero");
            }

            if (hasMissingSL) {
                errors.rejectValue("generalLedgerLines", "journal.sl.missing.entity");
            }

            Iterator<Map.Entry<Integer, BigDecimal>> entries = glAccountAmountMap.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<Integer, BigDecimal> entry = entries.next();

                Integer glSegmentAccountId = entry.getKey();
                BigDecimal glAmount = entry.getValue();

                BigDecimal slAmount = (BigDecimal) slAccountAmountMap.get(glSegmentAccountId);

                if (slAmount != null && glAmount.compareTo(slAmount) != 0) {
                    errors.rejectValue("generalLedgerLines", "journal.sl.amount.unmatched");
                    break;
                }
            }

        }

        return errors;
    }
}
