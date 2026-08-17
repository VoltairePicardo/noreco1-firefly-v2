package com.noreco1.fireflyv2.common.facade;

import com.noreco1.fireflyv2.model.CashflowItem;
import com.noreco1.fireflyv2.model.GeneralLedger;
import com.noreco1.fireflyv2.model.VoucherCashflowDetail;
import com.noreco1.fireflyv2.repo.VoucherCashflowDetailRepo;
import com.noreco1.fireflyv2.controller.response.GeneralLedgerLineDto2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;


@Component
public class CashFlowAccountEntryFacadeImpl implements CashFlowAccountEntryFacade {

    @Autowired
    VoucherCashflowDetailRepo voucherCashflowDetailRepo;

    public void setCashFlowAccountEntries(GeneralLedger newGl, GeneralLedgerLineDto2 ledgerLine,
                                          Date voucherDate, boolean lastGLEntry, BigDecimal percentage) {

        List<Map> cashFlowAccounts = ledgerLine.getCashFlowAccounts();
        if (!cashFlowAccounts.isEmpty()) {

            for (Map cashFlowAccountListLine : cashFlowAccounts) {

                Map account = (Map) cashFlowAccountListLine.get("account");

                CashflowItem cashFlowAccount = new CashflowItem();
                cashFlowAccount.setId((Integer) account.get("id"));

                VoucherCashflowDetail cfAccountDetail = new VoucherCashflowDetail();
                cfAccountDetail.setTransaction(newGl.getTransaction());
                cfAccountDetail.setGeneralLedger(newGl);
                cfAccountDetail.setCashflowItem(cashFlowAccount);
                cfAccountDetail.setVoucherDate(voucherDate);

                BigDecimal cashFlowAccountAmount = new BigDecimal(cashFlowAccountListLine.get("amount") + "");

                // prevent insertion of Cash Flow Account entries having no amount
                if (cashFlowAccountAmount == null || cashFlowAccountAmount.compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }

                BigDecimal cfShare = cashFlowAccountAmount;

                if(percentage != null) {
                    cfShare = (percentage.multiply(cashFlowAccountAmount)).setScale(2, BigDecimal.ROUND_HALF_UP);

                    if(lastGLEntry) { // fix overage

                        List<Object[]> sumByTransId = this.voucherCashflowDetailRepo.sumByTransIdAndCashFlowItemId(newGl.getTransaction().getId(), cashFlowAccount.getId(), newGl.getSegmentAccount().getAccount().getId());

                        if(!sumByTransId.isEmpty()) {

                            Object row1 = sumByTransId.get(0);
                            if (row1 != null) {
                                BigDecimal totalAmount = (BigDecimal) row1; // total already saved
                                BigDecimal tempTotalAmount = cfShare.add(totalAmount); // total already saved + last amount

                                if (cashFlowAccountAmount.compareTo(tempTotalAmount) != 0) {
                                    BigDecimal diff = cashFlowAccountAmount.subtract(tempTotalAmount);
                                    cfShare = cfShare.add(diff);    // diff could be negative
                                }
                            }
                        }
                    }
                }

                cfAccountDetail.setAmount(cfShare);

                voucherCashflowDetailRepo.save(cfAccountDetail);
            }

        }

    }

    @Override
    public void setCashFlowAccountEntries(GeneralLedger newGl, GeneralLedgerLineDto2 ledgerLine, Date voucherDate) {
        List<Map> cashFlowAccounts = ledgerLine.getCashFlowAccounts();
        if (!cashFlowAccounts.isEmpty()) {
            for (Map cashFlowAccountListLine : cashFlowAccounts) {

                Map account = (Map) cashFlowAccountListLine.get("account");

                CashflowItem cashFlowAccount = new CashflowItem();
                cashFlowAccount.setId((Integer) account.get("id"));

                VoucherCashflowDetail cfAccountDetail = new VoucherCashflowDetail();
                cfAccountDetail.setTransaction(newGl.getTransaction());
                cfAccountDetail.setGeneralLedger(newGl);
                cfAccountDetail.setCashflowItem(cashFlowAccount);
                cfAccountDetail.setVoucherDate(voucherDate);

                BigDecimal cashFlowAccountAmount = new BigDecimal(cashFlowAccountListLine.get("amount") + "");

                // prevent insertion of Cash Flow Account entries having no amount
                if (cashFlowAccountAmount == null || cashFlowAccountAmount.compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }

                cfAccountDetail.setAmount(cashFlowAccountAmount);

                voucherCashflowDetailRepo.save(cfAccountDetail);
            }

        }

    }

}
