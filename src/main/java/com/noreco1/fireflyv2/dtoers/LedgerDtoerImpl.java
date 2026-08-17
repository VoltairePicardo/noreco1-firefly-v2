package com.noreco1.fireflyv2.dtoers;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.SettingFacade;
import com.noreco1.fireflyv2.common.helpers.BooleanFormatter;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.StringFormatter;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.GeneralLedgerLineDto;
import com.noreco1.fireflyv2.controller.response.GeneralLedgerLineDto2;
import com.noreco1.fireflyv2.controller.response.SubLedgerDto;
import com.noreco1.fireflyv2.controller.response.reports.CommonLedgerDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by TSI Admin on 5/3/2015.
 */

@Component
public class LedgerDtoerImpl implements LedgerDtoer {

    @Autowired
    GeneralLedgerRepo generalLedgerRepo;

    @Autowired
    SubLedgerRepo subLedgerRepo;

    @Autowired
    CheckVoucherChequeRepo chequeRepo;

    @Autowired
    CheckVoucherIncomePaymentRepo incomePaymentRepo;

    @Autowired
    SettingFacade settingFacade;

    @Autowired
    TemporaryGeneralLedgerRepo temporaryGeneralLedgerRepo;

    @Autowired
    TemporarySubLedgerRepo temporarySubLedgerRepo;

    @Autowired
    TemporaryBatchRepo temporaryBatchRepo;

    @Autowired
    TaxCodeRepo taxCodeRepo;

    @Autowired
    VoucherCashflowDetailRepo voucherCashflowDetailRepo;

    @Autowired
    StockTransactionDetailRepo stockTransactionDetailRepo;

    @Autowired
    AllocationFactorRepo allocationFactorRepo;

    @Autowired
    FactorPercentageDistroRepo factorPercentageDistroRepo;

    @Autowired
    SegmentAccountRepo segmentAccountRepo;

    @Autowired
    CashAdvanceRepo cashAdvanceRepo;

    @Autowired
    AccountRepo accountRepo;

    @Override
    public List<SubLedgerDto> getSLEntriesDtoByGl(Integer glId) {
        List<SubLedger> sls = subLedgerRepo.findByGeneralLedgerId(glId);
        return composeSubLedgerDto(sls);
    }

    @Override
    public List<SubLedgerDto> getSLEntriesDtoByTrans(Integer transId) {
        List<SubLedger> sls = subLedgerRepo.findByTransactionId(transId);
        return composeSubLedgerDto(sls);
    }

    @Override
    public List<GeneralLedgerLineDto> getGLEntriesDtoByTrans(Integer transId) {
        List<GeneralLedger> generalLedgerLines = generalLedgerRepo.findByTransactionId(transId);

        List<GeneralLedgerLineDto> ledgerLineDtos = new ArrayList<>();

        if (generalLedgerLines != null) {
            for (GeneralLedger line : generalLedgerLines) {

                String description  = line.getSegmentAccount() == null ? "":line.getSegmentAccount().getAccount().getTitle();
                String code  = line.getSegmentAccount() == null ? "":line.getSegmentAccount().getAccountCode();
                Integer segmentId = line.getSegmentAccount() == null ? 0:line.getSegmentAccount().getId();
                Integer acctId = line.getSegmentAccount() == null ? 0:line.getSegmentAccount().getAccount().getId();

                GeneralLedgerLineDto lineDto = new GeneralLedgerLineDto();
                lineDto.setCredit(line.getCredit());
                lineDto.setDebit(line.getDebit());
                lineDto.setDescription(description);
                lineDto.setId(line.getId());
                lineDto.setSegmentAccountCode(code);
                lineDto.setSegmentAccountId(segmentId);

                List<SubLedger> subLedgers = subLedgerRepo.findByGeneralLedgerId(line.getId());
                lineDto.setHasSL((subLedgers != null && subLedgers.size() > 0));

//                CheckVoucherCheque cheque = chequeRepo.findOneByAccountIdAndTransactionId(acctId, transId);
//                if (cheque != null) {
//                    lineDto.setCheckNumber(cheque.getCheckNumber());
//                }

                ledgerLineDtos.add(lineDto);
            }
        }

        return ledgerLineDtos;
    }

    @Override
    public List<GeneralLedgerLineDto2> getGLAccountEntriesDtoByTrans(Integer transId, boolean withoutTax) {

        List<Object[]> generalLedgerLines;

        if(withoutTax) {
            generalLedgerLines = generalLedgerRepo.findByTransactionIdWithoutTaxGroupByAccount(transId);
        } else {
            generalLedgerLines = generalLedgerRepo.findByTransactionIdGroupByAccount(transId);
        }

        List<GeneralLedgerLineDto2> ledgerLineDtos = new ArrayList<>();

        if (generalLedgerLines != null) {
            for (Object[] line : generalLedgerLines) {
                GeneralLedgerLineDto2 lineDto = new GeneralLedgerLineDto2();

                BigDecimal debit = new BigDecimal(line[0].toString());
                BigDecimal credit = new BigDecimal(line[1].toString());
                Integer accountId = (Integer)line[2];
                String code = (String)line[3];
                String title = (String)line[4];
                Integer glId = (Integer)line[5];

                lineDto.setDebit(debit);
                lineDto.setCredit(credit);
                lineDto.setDescription(title);
                lineDto.setAccountId(accountId);
                lineDto.setCode(code);
                lineDto.setId(glId);

                // format debit & credit
                lineDto.setDebitStr(new DecimalFormat("#,##0.00").format(debit));
                lineDto.setCreditStr(new DecimalFormat("#,##0.00").format(credit));

                lineDto.setSearchText(StringFormatter.accountSearchText(code, title));

                List<Object[]> subLedgerLines = subLedgerRepo.findByTransactionIdAndAccountId(transId, accountId);
                lineDto.setHasSL((subLedgerLines != null && subLedgerLines.size() > 0));

                // withholding tax: atc
//                Integer wtaxAccountId = 0;
//                try {
//                    Map wtaxAccount = settingFacade.getByCode(SettingCode.WTAX_ACCOUNT.toString());
//                    wtaxAccountId = Integer.parseInt(wtaxAccount.get("id").toString());
//                } finally {}
//
//                if (wtaxAccountId.equals(accountId)) {

                CheckVoucherIncomePayment incomePayment = incomePaymentRepo.findOneByTransactionId(transId);
                if (incomePayment != null) {
                    Map account = new HashMap();
                    account.put("id", accountId);
                    account.put("title", title);
                    account.put("code", code);
                    account.put("amount", incomePayment.getAmount());

                    Map wTaxEntry = new HashMap();
                    wTaxEntry.put("account", account);
                    wTaxEntry.put("amount", incomePayment.getAmount());
                    wTaxEntry.put("percentage", incomePayment.getPercentage());
                    wTaxEntry.put("atc", incomePayment.getTaxCode());
                    wTaxEntry.put("baseAmount", incomePayment.getBaseAmount());

                    lineDto.setwTaxEntry(wTaxEntry);
                }

                // cashflow
                List<VoucherCashflowDetail> list = voucherCashflowDetailRepo.findByGeneralLedgerId(glId);
                if(!list.isEmpty()) {

                    List<Map> cfAccountsMap = new ArrayList<>();

                    for (VoucherCashflowDetail vcf: list) {

                        CashflowItem cashflowItem = vcf.getCashflowItem();
                        cashflowItem.setParentCashflowItem(null); // lessen data
                        cashflowItem.setCashflowItemType(null); // lessen data

                        Map row = new HashMap();
                        row.put("amount", vcf.getAmount());
                        row.put("account", cashflowItem);

                        cfAccountsMap.add(row);
                    }

                    lineDto.setCashFlowAccounts(cfAccountsMap);
                }

                // manual distribution
                List<Map> distribution = new ArrayList<>();

                AllocationFactor allocationFactor = allocationFactorRepo.findOneByAccountId(accountId);
                if(allocationFactor != null && allocationFactor.getFactor().getCode().equals(com.noreco1.fireflyv2.model.enums.Factor.MANUAL.toString())) {

                    Set<FactorPercentageDistro> distros = factorPercentageDistroRepo.findByFactorId(allocationFactor.getFactor().getId());
                    if(!distros.isEmpty()) {

                        List<GeneralLedger> generalLedgers = generalLedgerRepo.findByTransactionId(transId);

                        for(FactorPercentageDistro percentageDistro:distros) {


                            for(GeneralLedger generalLedger:generalLedgers) {

                                SegmentAccount segmentAccount = segmentAccountRepo.findOneByAccountIdAndBusinessSegmentId(accountId, percentageDistro.getBusinessSegment().getId());

                                if(segmentAccount != null){
                                    if(generalLedger.getSegmentAccount().getId() == segmentAccount.getId()) {

                                        BigDecimal amount;

                                        if (generalLedger.getCredit().compareTo(BigDecimal.ZERO) == 0)  {
                                            amount = generalLedger.getDebit();
                                        } else {
                                            amount = generalLedger.getCredit();
                                        }

                                        Map mapDisto = new HashMap();
                                        mapDisto.put("amount", amount);
                                        mapDisto.put("segmentAccountId", segmentAccount.getId());
                                        mapDisto.put("segmentId", segmentAccount.getBusinessSegment().getId());
                                        mapDisto.put("percentage", percentageDistro.getPercentage());

                                        distribution.add(mapDisto);

                                        break;
                                    }
                                }

                            }
                        }
                    }

                    lineDto.setDistribution(distribution);
                }

                List<Object[]> slEntries = subLedgerRepo.findByTransactionIdAndAccountId(transId, accountId);
                if (!Checker.collectionIsEmpty(slEntries)) {

                    List<SubLedgerDto> subLedgerDtos = new ArrayList<>();

                    for (Object[] l:slEntries) {
                        SubLedgerDto dto = new SubLedgerDto();

                        dto.setId((Integer) l[0]);
                        dto.setAccountNo((Integer) l[1]);
                        dto.setName((String) l[2]);
                        dto.setAccountId((Integer) l[3]);
                        dto.setAmount((BigDecimal) l[4]);
                        dto.setDebit((BigDecimal) l[5]);
                        dto.setCredit((BigDecimal) l[6]);

                        subLedgerDtos.add(dto);
                    }

                    lineDto.setSlentries(subLedgerDtos);

                }

                ledgerLineDtos.add(lineDto);
            }
        }

        return ledgerLineDtos;
    }

    @Override
    public List<SubLedgerDto> getSLEntriesDtoByTransAndAccount(Integer transId, Integer accountId, Boolean isDebit) {
        List<SubLedgerDto> subLedgerDtos = new ArrayList<>();

        List<Object[]> list = subLedgerRepo.findByTransactionIdAndAccountId(transId, accountId);
        if (!Checker.collectionIsEmpty(list)) {
            for (Object[] l:list) {
                SubLedgerDto dto = new SubLedgerDto();

                dto.setId((Integer) l[0]);
                dto.setAccountNo((Integer) l[1]);
                dto.setName((String) l[2]);
                dto.setAccountId((Integer) l[3]);
                dto.setAmount((BigDecimal) l[4]);
                dto.setDebit((BigDecimal) l[5]);
                dto.setCredit((BigDecimal) l[6]);

                subLedgerDtos.add(dto);
            }
        }
        return subLedgerDtos;
    }

    private List<SubLedgerDto> composeSubLedgerDto( List<SubLedger> ledgerLines) {
        List<SubLedgerDto> subLedgerDtos = new ArrayList<>();

        for (SubLedger subLedger : ledgerLines) {
            SubLedgerDto dto = new SubLedgerDto();
            dto.setSegmentAccountId(subLedger.getSegmentAccount().getId());
            dto.setName(subLedger.getSlEntity().getName());
            dto.setAccountNo(subLedger.getSlEntity().getAccountNo());
            dto.setAmount(subLedger.getDebit().add(subLedger.getCredit()));
            dto.setDebit(subLedger.getDebit());
            dto.setCredit(subLedger.getCredit());
            dto.setSegmentAccountCode(subLedger.getSegmentAccount().getAccountCode());

            GeneralLedger generalLedger = subLedger.getGeneralLedger();
            dto.setGeneralLedgerId(generalLedger != null ? generalLedger.getId() : 0);
            dto.setId(subLedger.getId());

            subLedgerDtos.add(dto);
        }

        return subLedgerDtos;
    }

    @Override
    public List<Map> getGLEntriesDtoByTransAndAccount(Integer transId, Integer accountId) {
        List<Map> result = new ArrayList<>();
        List<Object[]> rows = generalLedgerRepo.findByTransactionIdAndAccount(transId, accountId);

        if (!Checker.collectionIsEmpty(rows)) {
            for (Object[] l:rows) {
                Map record = new HashMap();

                record.put("ledgerId",  l[0]);
                record.put("segmentAccountId",  l[2]);
                record.put("debit",  l[3]);
                record.put("credit",  l[4]);
                record.put("segmentDescription",  l[5]);

                result.add(record);
            }
        }
        return result;
    }

    @Override
    public List<CommonLedgerDetail> getVoucherLedgerLines(Integer transId) {
        List<CommonLedgerDetail> details = new ArrayList<>();

        BigDecimal totalGLDebit = BigDecimal.ZERO;
        BigDecimal totalGLCredit= BigDecimal.ZERO;
        BigDecimal totalSLDebit = BigDecimal.ZERO;
        BigDecimal totalSLCredit= BigDecimal.ZERO;

        List<GeneralLedgerLineDto2> ledgerLineDto2s = this.getGLAccountEntriesDtoByTrans(transId, true);

        if (!Checker.collectionIsEmpty(ledgerLineDto2s)) {
            for(GeneralLedgerLineDto2 gl : ledgerLineDto2s) {
                CommonLedgerDetail d = new CommonLedgerDetail();

                d.setAccountCode(gl.getCode());
                d.setAccountTitle(gl.getDescription());
                d.setGlCreditAmount(gl.getCredit());
                d.setGlDebitAmount(gl.getDebit());

                details.add(d);

                totalGLDebit = totalGLDebit.add(gl.getDebit());
                totalGLCredit = totalGLCredit.add(gl.getCredit());

                boolean isDebit = gl.getCredit() == null || gl.getCredit().compareTo(BigDecimal.ZERO) == 0;

                List<SubLedgerDto> subLedgerLineDtos = this.getSLEntriesDtoByTransAndAccount(transId, gl.getAccountId(), isDebit);
                if (!Checker.collectionIsEmpty(subLedgerLineDtos)) {

                    for(SubLedgerDto sl : subLedgerLineDtos) {
                        CommonLedgerDetail sld = new CommonLedgerDetail();

                        sld.setAccountCode("&nbsp;&nbsp;&nbsp;" + String.valueOf(sl.getAccountNo()));
                        sld.setAccountTitle("&nbsp;&nbsp;&nbsp;" + sl.getName());

                        BigDecimal drAmount = sl.getAmount();
                        BigDecimal crAmount = sl.getAmount();
                        if (gl.getDebit() == null || gl.getDebit().compareTo(BigDecimal.ZERO) == 0) {
                            drAmount = BigDecimal.ZERO;
                        } else {
                            crAmount = BigDecimal.ZERO;
                        }

                        sld.setSlCreditAmount(crAmount);
                        sld.setSlDebitAmount(drAmount);

                        details.add(sld);

                        totalSLDebit = totalSLDebit.add(drAmount);
                        totalSLCredit = totalSLCredit.add(crAmount);
                    }
                }

            }
        }

        CommonLedgerDetail totalD = new CommonLedgerDetail();
        details.add(totalD);    // spacing

        totalD = new CommonLedgerDetail();
        totalD.setAccountTitle("<b>TOTAL</b>");
        totalD.setGlCreditAmount(totalGLCredit);
        totalD.setGlDebitAmount(totalGLDebit);
        totalD.setSlCreditAmount(totalSLCredit);
        totalD.setSlDebitAmount(totalSLDebit);

        details.add(totalD);

        return details;
    }

    @Override
    public List<Map> getAllTempBatches() {
        List<Map> result = new ArrayList<>();
        List<Object[]> rows = temporaryBatchRepo.findAllWithAmount();

        if (!Checker.collectionIsEmpty(rows)) {
            for (Object[] l:rows) {
                Map record = new HashMap();

                record.put("tempBatchId",  l[0]);
                record.put("tempBatchDate",  l[1]);
                record.put("remarks",  l[2]);
                record.put("docTypeId",  l[3]);
                record.put("docTypeDesc",  l[4]);
                record.put("tempGLId",  l[5]);
                record.put("amount",  l[6]);
                record.put("transId",  l[7]);

                result.add(record);
            }
        }
        return result;
    }

    @Override
    public List<GeneralLedgerLineDto2> getGLAccountEntriesDtoTempBatchId(Integer tempBatchId) {
        List<Object[]> generalLedgerLines = temporaryGeneralLedgerRepo.findByBatchId(tempBatchId);

        List<GeneralLedgerLineDto2> ledgerLineDtos = new ArrayList<>();

        if (generalLedgerLines != null) {
            ledgerLineDtos = this.toGLEntries(generalLedgerLines);
        }

        return ledgerLineDtos;
    }

    @Override
    public List<SubLedgerDto> getSLAccountEntriesDtoTempBatchId(Integer tempBatchId, Integer accountId) {
        List<SubLedgerDto> subLedgerDtos = new ArrayList<>();

        List<Object[]> list = temporarySubLedgerRepo.findByTempBatchIdAndAccountId(tempBatchId, accountId);
        if (!Checker.collectionIsEmpty(list)) {
            for (Object[] l:list) {
                SubLedgerDto dto = new SubLedgerDto();

                dto.setId((Integer) l[0]);
                dto.setAccountNo((Integer) l[1]);
                dto.setName((String) l[2]);
                dto.setAccountId((Integer) l[3]);
                dto.setAmount((BigDecimal) l[4]);

                subLedgerDtos.add(dto);
            }
        }
        return subLedgerDtos;
    }

    @Override
    public SubLedger getSLEntryByAccountNoForPrep(Integer accountNo) {
        SubLedger sl = subLedgerRepo.findByAccountNoSumDebitCredit(accountNo);
        return sl;
    }

    @Override
    public List<Map> getCashflowAccountsByGlId(Integer glId) {
        List<Map> cfAccountsMap = new ArrayList<>();

        List<VoucherCashflowDetail> list = voucherCashflowDetailRepo.findByGeneralLedgerId(glId);
        if(!list.isEmpty()) {
            for (VoucherCashflowDetail vcf: list) {

                CashflowItem cashflowItem = vcf.getCashflowItem();
                cashflowItem.setParentCashflowItem(null); // lessen data

                Map row = new HashMap();
                row.put("amount", vcf.getAmount());
                row.put("account", cashflowItem);

                cfAccountsMap.add(row);
            }
        }

        return cfAccountsMap;
    }

    @Override
    public List<GeneralLedgerLineDto2> getGLAccountEntriesDtoMIRTransId(Integer transId) {
        List<Object[]> generalLedgerLines = stockTransactionDetailRepo.findGLAccountEntriesByTransactionId(transId);

        if (generalLedgerLines != null) {
            return this.toGLEntries(generalLedgerLines);
        }

        return null;
    }

    @Override
    public List<GeneralLedgerLineDto2> getGLAccountEntriesDtoMIRTransIdAndMST(Integer transId) {
        List<Object[]> generalLedgerLines = stockTransactionDetailRepo.findGLAccountEntriesByTransactionIdMST(transId);

        if (generalLedgerLines != null) {
            return this.toGLEntries(generalLedgerLines);
        }

        return null;
    }

    @Override
    public List<GeneralLedgerLineDto2> setGLAccountEntriesDtoForCashAdvance(Integer caId) {

        CashAdvance cashAdvance = cashAdvanceRepo.findById(caId).orElse(null);

        //Get CA for CV Default Accounts
        Map defaultAccountForCaMap = settingFacade.getByCode(GlobalConstant.CA_FOR_CV_DEFAULT_ACCOUNTS);
        int debitAccountId = (int) defaultAccountForCaMap.get("debitAccountId");
        int creditAccountId = (int) defaultAccountForCaMap.get("creditAccountId");

        List<GeneralLedgerLineDto2> ledgerLineDtos = new ArrayList<>();

        if(cashAdvance != null){

            BigDecimal amount = cashAdvance.getAmount();

            //Start Debit
            GeneralLedgerLineDto2 debitDto = new GeneralLedgerLineDto2();
            Account debitAccount = accountRepo.findById(debitAccountId).orElse(null);
            debitDto.setDebit(amount);
            debitDto.setCredit(BigDecimal.ZERO);
            debitDto.setDescription(debitAccount.getTitle());
            debitDto.setAccountId(debitAccount.getId());
            debitDto.setCode(debitAccount.getCode());
            debitDto.setHasSL(BooleanFormatter.convertIntToBoolean(debitAccount.getHasSL()));
            // format debit & credit
            debitDto.setDebitStr(new DecimalFormat("#,##0.00").format(amount));
            debitDto.setCreditStr(new DecimalFormat("#,##0.00").format(BigDecimal.ZERO));
            debitDto.setSearchText(StringFormatter.accountSearchText(debitAccount.getCode(), debitAccount.getTitle()));
            ledgerLineDtos.add(debitDto);

            //Start Credit
            GeneralLedgerLineDto2 creditDto = new GeneralLedgerLineDto2();
            Account creditAccount = accountRepo.findById(creditAccountId).orElse(null);
            creditDto.setDebit(BigDecimal.ZERO);
            creditDto.setCredit(amount);
            creditDto.setDescription(creditAccount.getTitle());
            creditDto.setAccountId(creditAccount.getId());
            creditDto.setCode(creditAccount.getCode());
            creditDto.setHasSL(BooleanFormatter.convertIntToBoolean(creditAccount.getHasSL()));
            // format debit & credit
            creditDto.setDebitStr(new DecimalFormat("#,##0.00").format(BigDecimal.ZERO));
            creditDto.setCreditStr(new DecimalFormat("#,##0.00").format(amount));
            creditDto.setSearchText(StringFormatter.accountSearchText(creditAccount.getCode(), creditAccount.getTitle()));
            ledgerLineDtos.add(creditDto);
        }

        return ledgerLineDtos;
    }

    @Override
    public List<SubLedgerDto> setSLEntryDtoForCashAdvance(Integer caId, Integer accountId) {
        List<SubLedgerDto> subLedgerDtos = new ArrayList<>();

        CashAdvance cashAdvance = cashAdvanceRepo.findById(caId).orElse(null);
        Map defaultAccountForCaMap = settingFacade.getByCode(GlobalConstant.CA_FOR_CV_DEFAULT_ACCOUNTS);
        int debitAccountId = (int) defaultAccountForCaMap.get("debitAccountId");

        if (cashAdvance != null) {
            if(accountId == debitAccountId){

                Account account = accountRepo.findById(debitAccountId).orElse(null);
                if(account != null && BooleanFormatter.convertIntToBoolean(account.getHasSL())){
                    SubLedgerDto dto = new SubLedgerDto();

                    dto.setGeneralLedgerLineId(accountId);
                    dto.setAccountNo(cashAdvance.getEmployee().getAccountNumber());
                    dto.setName(cashAdvance.getEmployee().getName());
                    dto.setAccountId(accountId);
                    dto.setAmount(cashAdvance.getAmount());
                    dto.setDebit(cashAdvance.getAmount());

                    subLedgerDtos.add(dto);
                }

            }
        }
        return subLedgerDtos;
    }

    @Override
    public List<GeneralLedgerLineDto2> getAccountSettingEntriesForJV(Integer transId) {
        List<Object[]> generalLedgerLines = stockTransactionDetailRepo.findGLAccountSettingEntriesByTransactionIdForJV(transId);

        if (generalLedgerLines != null) {
            return this.toGLEntries(generalLedgerLines);
        }

        return null;
    }

    private List<GeneralLedgerLineDto2> toGLEntries(List<Object[]> generalLedgerLines) {
        List<GeneralLedgerLineDto2> ledgerLineDtos = new ArrayList<>();
        for (Object[] line : generalLedgerLines) {
            GeneralLedgerLineDto2 lineDto = new GeneralLedgerLineDto2();

            BigDecimal debit = new BigDecimal(line[0].toString());
            BigDecimal credit = new BigDecimal(line[1].toString());
            Integer accountId = (Integer)line[2];
            String code = (String)line[3];
            String title = (String)line[4];
            Boolean hasSl = (Boolean)line[5];

            if(debit.compareTo(credit) == 0){//debit is equal to credit

                debit = BigDecimal.ZERO;
                credit = BigDecimal.ZERO;

            } else if(debit.compareTo(credit) == 1){//debit is greater than credit

                debit = debit.subtract(credit);
                credit = BigDecimal.ZERO;

            }else{ //debit is lesser than credit

                credit = credit.subtract(debit);
                debit = BigDecimal.ZERO;

            }

            lineDto.setDebit(debit);
            lineDto.setCredit(credit);
            lineDto.setDescription(title);
            lineDto.setAccountId(accountId);
            lineDto.setCode(code);
            lineDto.setHasSL(hasSl);

            // format debit & credit
            lineDto.setDebitStr(new DecimalFormat("#,##0.00").format(debit));
            lineDto.setCreditStr(new DecimalFormat("#,##0.00").format(credit));

            lineDto.setSearchText(StringFormatter.accountSearchText(code, title));

            ledgerLineDtos.add(lineDto);
        }
        return ledgerLineDtos;
    }
}
