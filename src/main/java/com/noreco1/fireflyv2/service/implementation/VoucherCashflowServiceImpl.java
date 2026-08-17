package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.CashFlowAccountEntryFacade;
import com.noreco1.fireflyv2.common.facade.SignatoryFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.model.enums.DocumentStatus;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.GeneralLedgerLineDto2;
import com.noreco1.fireflyv2.controller.response.VoucherCashflowItemDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.VoucherCashflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class VoucherCashflowServiceImpl implements VoucherCashflowService {

    @Autowired
    CheckVoucherRepo checkVoucherRepo;

    @Autowired
    AccountsPayableVoucherRepo apvRepo;

    @Autowired
    JournalVoucherRepo jvRepo;

    @Autowired
    CashReceiptsRepo cashReceiptsRepo;

    @Autowired
    CashflowItemRepo cashflowItemRepo;

    @Autowired
    VoucherCashflowDetailRepo voucherCashflowDetailRepo;

    @Autowired
    MonthlyCycleRepo monthlyCycleRepo;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    GeneralLedgerRepo generalLedgerRepo;

    @Autowired
    DateRangeRepo dateRangeRepo;

    @Autowired
    AllocationFactorRepo allocationFactorRepo;

    @Autowired
    FactorPercentageDistroRepo factorPercentageDistroRepo;

    @Autowired
    CashFlowAccountEntryFacade cashFlowAccountEntryFacade;

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

    @Override
    public PostResponse setVoucherCashflow(String voucherCodePrefix, VoucherCashflowItemDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        response.setFailureMessage("Something went wrong");

        try {

            Voucher voucher = null;
            if (voucherCodePrefix.equals(DocumentType.CV.getCode())) {
                CheckVoucher checkVoucher = checkVoucherRepo.findById(postData.getVoucherId()).orElse(null);
                voucher = checkVoucher;

            } else if (voucherCodePrefix.equals(DocumentType.APV.getCode())) {
                AccountsPayableVoucher apv = apvRepo.findById(postData.getVoucherId()).orElse(null);
                voucher = apv;
            } else if (voucherCodePrefix.equals(DocumentType.JV.getCode())) {
                JournalVoucher jv = jvRepo.findById(postData.getVoucherId()).orElse(null);
                voucher = jv;
            } else if (voucherCodePrefix.equals(DocumentType.CRV.getCode())) {
                CashReceipts jv = cashReceiptsRepo.findById(postData.getVoucherId()).orElse(null);
                voucher = jv;
            }

            if (voucher != null) {

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(voucher.getVoucherDate());

                MonthlyCycle cycle = monthlyCycleRepo.findByYearAndMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1);

                if (cycle != null && cycle.getStatus().equals(MonthlyCycleStatus.CLOSE)) {
                    response.getFields().add("cycle");
                    response.getMessages().add("Month is already closed. Updating of cashflow is not allowed.");
                    response.setSuccess(false);
                    return response;
                }

                if (!Checker.collectionIsEmpty(postData.getCashflowData())) {

                    int dataCnt = postData.getCashflowData().size();
                    int counter = 0;
                    for(Map data:postData.getCashflowData()) {

                        Map ledgerMap = (Map) data.get("ledger");
                        Integer accountId = (Integer) ledgerMap.get("accountId");

                        if(!Checker.isValidId(accountId)) {
                            counter++;
                            continue;
                        }

                        // clear existing cash flow entries by account id
                        voucherCashflowDetailRepo.deleteByTransactionIdAndGeneralLedgerSegmentAccountAccountId(voucher.getTransaction().getId(), accountId);

                        java.sql.Date sqlDate = new java.sql.Date(voucher.getVoucherDate().getTime());

                        List<DateRange> dateRanges = dateRangeRepo.findByEndGreaterThanEqualAndStartLessThanEqual(sqlDate, sqlDate);
                        if(Checker.collectionIsNotEmpty(dateRanges)) {

                            for (DateRange range : dateRanges) {
                                // get percentage distribution

                                List<GeneralLedger> generalLedgerList = this.generalLedgerRepo.findByTransactionIdAndSegmentAccountAccountId(voucher.getTransaction().getId(), accountId);

                                for(GeneralLedger generalLedger: generalLedgerList) {
                                    AllocationFactor allocationFactor = allocationFactorRepo.findOneByAccountId(generalLedger.getSegmentAccount().getAccount().getId());

                                    GeneralLedgerLineDto2 ledgerLine = new GeneralLedgerLineDto2();
                                    ledgerLine.setCashFlowAccounts((List<Map>) data.get("entries"));

                                    if(allocationFactor.getFactor().getCode().equals(com.noreco1.fireflyv2.model.enums.Factor.MANUAL.toString())) {

                                        cashFlowAccountEntryFacade.setCashFlowAccountEntries(generalLedger, ledgerLine, voucher.getVoucherDate(), false, null);

                                    } else {

                                        Set<FactorPercentageDistro> percentages = factorPercentageDistroRepo.findByFactorIdAndValidityDateId(allocationFactor.getFactor().getId(), range.getId());

                                        if (Checker.collectionIsNotEmpty(percentages)) {

                                            int percCounter = 0;
                                            int lastIdx = percentages.size();

                                            for (FactorPercentageDistro obj : percentages) {

                                                percCounter++;

                                                BigDecimal percentage = obj.getPercentage();
                                                cashFlowAccountEntryFacade.setCashFlowAccountEntries(generalLedger, ledgerLine, voucher.getVoucherDate(), percCounter == lastIdx, percentage);
                                            }
                                        }
                                    }

                                    break; // process only one date range
                                }
                            }
                        }
                        counter++;
                    }

                    if (counter == dataCnt) {
                        response.setSuccess(true);
                    }
                } else {

                    List<VoucherCashflowDetail> cashflowDetailList = voucherCashflowDetailRepo.findByTransactionId(voucher.getTransaction().getId());
                    if (!cashflowDetailList.isEmpty()) {
                        // clear existing
                        voucherCashflowDetailRepo.deleteByTransactionId(voucher.getTransaction().getId());
                        response.setSuccessMessage("Cash flow setting successfully saved");
                    } else {
                        response.setFailureMessage("Nothing to save");
                    }
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.CASHFLOW_STATEMENT);
    }
}
