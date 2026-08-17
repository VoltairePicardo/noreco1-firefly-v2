package com.noreco1.fireflyv2.dtoers;

import com.noreco1.fireflyv2.common.facade.SettingFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.common.helpers.StringFormatter;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.enums.DisplayStatus;
import com.noreco1.fireflyv2.model.enums.DocumentStatus;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.model.enums.InventoryDocType;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.reports.*;
import com.noreco1.fireflyv2.controller.response.reports.QuotationDetail;
import com.noreco1.fireflyv2.service.MaterialIssueRegisterService;
import com.noreco1.fireflyv2.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by TSI Admin on 5/19/2015.
 */

@Component
public class AccountingReportDtoerImpl implements AccountingReportDtoer {

    @Autowired
    AccountRepo accountRepo;

    @Autowired
    JournalVoucherRepo journalVoucherRepo;

    @Autowired
    CheckVoucherRepo checkVoucherRepo;

    @Autowired
    GeneralLedgerRepo generalLedgerRepo;

    @Autowired
    AccountsPayableVoucherRepo accountsPayableVoucherRepo;

    @Autowired
    SalesVoucherRepo salesVoucherRepo;

    @Autowired
    CashReceiptsRepo cashReceiptsRepo;

    @Autowired
    MaterialIssueRegisterRepo mirRepo;

    @Autowired
    MaterialIssueRegisterService mirService;

    @Autowired
    PurchaseRequestRepo rvRepo;

    @Autowired
    PurchaseOrderRepo poRepo;

    @Autowired
    JobOrderRepo joRepo;

    @Autowired
    JoAcceptanceRepo joaRepo;

    @Autowired
    CanvassRepo canvassRepo;

    @Autowired
    PaymentRequestRepo paymentRequestRepo;

    @Autowired
    PrepaymentRepo prepaymentRepo;

    @Autowired
    WorkOrderService workOrderService;

    @Autowired
    CashflowItemRepo cashflowItemRepo;

    @Autowired
    CashflowItemTypeRepo cashflowItemTypeRepo;

    @Autowired
    VoucherCashflowDetailRepo voucherCashflowDetailRepo;

    @Autowired
    BudgetRepo budgetRepo;

    @Autowired
    AdjustmentJournalRepo adjustmentJournalRepo;

    @Autowired
    SettingFacade settingFacade;

    @Autowired
    CanvassDetailRepo canvassDetailRepo;

    @Autowired
    PurchaseRequestDetailRepo PurchaseRequestDetailRepo;

    @Autowired
    MaterialCreditTicketRepo materialCreditTicketRepo;

    @Autowired
    ItemTransactionDetailRepo itemTransactionDetailRepo;

    @Autowired
    MaterialSalvageTicketRepo materialSalvageTicketRepo;

    @Autowired
    StockWithdrawalRepo stockWithdrawalRepo;

    @Autowired
    StockWithdrawalDetailRepo stockWithdrawalDetailRepo;

    @Autowired
    StockReleaseRepo stockReleaseRepo;

    @Autowired
    StockAdjustmentRepo stockAdjustmentRepo;

    @Autowired
    StockTransferRepo stockTransferRepo;

    @Autowired
    StockReceiveRepo stockReceiveRepo;

    @Autowired
    StockTransactionDetailRepo stockTransactionDetailRepo;

    @Autowired
    CheckVoucherChequeRepo checkVoucherChequeRepo;

    @Autowired
    BankAccountRepo bankAccountRepo;

    // registers
    @Override
    public List<CommonRegisterDetail> getForJVRegister(String from, String to, Integer statusId) {
        List<Object[]> journalList = null;

        if(statusId == null || statusId == 0){
            journalList = journalVoucherRepo.findForRegisterByDateRange(from, to);
        }else{
            journalList = journalVoucherRepo.findForRegisterByDateRange(from, to, statusId);
        }

        return this.makeCommonRegisterDetail2(journalList, false, DocumentType.JV);
    }

    public List<RegisterRecapDetail> getForJVRegisterRecap(String from, String to, Integer statusId) {
        List<Object[]> rows = null;

        if(statusId != null && statusId != 0){
            rows = journalVoucherRepo.findForRegisterRecapByDateRangeAndOfficeId(from, to, statusId);
        }else{
            rows = journalVoucherRepo.findForRegisterRecapByDateRangeAndOfficeId(from, to, DocumentStatus.APPROVED.getId());
        }
        return this.makeRegisterRecapDetail(rows);
    }

    @Override
    public List<CommonRegisterDetail> getForCVRegister(String from, String to, Integer statusId) {
        List<Object[]> journalList = null;

        if(statusId == null || statusId == 0){
            journalList = checkVoucherRepo.findForRegisterByDateRange(from, to);
        }else{
            journalList = checkVoucherRepo.findForRegisterByDateRange(from, to, statusId);
        }

        return this.makeCommonRegisterDetail2(journalList, false, DocumentType.CV);
    }

    @Override
    public List<RegisterRecapDetail> getForCVRegisterRecap(String from, String to, Integer statusId) {
        List<Object[]> rows = null;

        if(statusId == null || statusId == 0){
            rows = checkVoucherRepo.findForRegisterRecapByDateRange(from, to);
        }else{
            rows = checkVoucherRepo.findForRegisterRecapByDateRange(from, to, statusId);
        }

        return this.makeRegisterRecapDetail(rows);
    }

    @Override
    public List<CommonRegisterDetail> getForAPVRegister(String from, String to, Integer statusId) {
        List<Object[]> journalList = null;

        if(statusId == null || statusId == 0){
            journalList = accountsPayableVoucherRepo.findForRegisterByDateRange(from, to);
        }else{
            journalList = accountsPayableVoucherRepo.findForRegisterByDateRange(from, to, statusId);
        }
        return this.makeCommonRegisterDetail2(journalList, false, DocumentType.APV);
    }

    @Override
    public List<RegisterRecapDetail> getForAPVRegisterRecap(String from, String to, Integer statusId) {
        List<Object[]> rows = null;

        if(statusId == null || statusId == 0){
            rows = accountsPayableVoucherRepo.findForRegisterRecapByDateRange(from, to);
        }else{
            rows = accountsPayableVoucherRepo.findForRegisterRecapByDateRange(from, to, statusId);
        }

        return this.makeRegisterRecapDetail(rows);
    }

    @Override
    public List<CommonRegisterDetail> getForSalesRegister(String from, String to, Integer statusId) {
        List<Object[]> rows = null;

        if(statusId == null || statusId == 0){
            rows = salesVoucherRepo.findForRegisterByDateRange(from, to);
        }else{
            rows = salesVoucherRepo.findForRegisterByDateRange(from, to, statusId);
        }

        return this.makeCommonRegisterDetail2(rows, false, DocumentType.SV);
    }

    @Override
    public List<RegisterRecapDetail> getForSalesRegisterRecap(String from, String to, Integer statusId) {
        List<Object[]> rows = null;

        if(statusId == null || statusId == 0){
            rows = salesVoucherRepo.findForRegisterRecapByDateRange(from, to);
        }else{
            rows = salesVoucherRepo.findForRegisterRecapByDateRange(from, to, statusId);

        }

        return this.makeRegisterRecapDetail(rows);
    }

    @Override
    public List<CommonRegisterDetail> getForCashRegister(String from, String to, Integer statusId) {
        List<Object[]> rows = null;

        if(statusId == null || statusId == 0){
            rows = cashReceiptsRepo.findForRegisterByDateRange(from, to);
        }else{
            rows = cashReceiptsRepo.findForRegisterByDateRange(from, to, statusId);
        }
        return this.makeCommonRegisterDetail2(rows, false, DocumentType.CRV);
    }

    @Override
    public List<RegisterRecapDetail> getForCashRegisterRecap(String from, String to, Integer statusId) {
        List<Object[]> rows = null;

        if(statusId == null || statusId == 0){
            rows = cashReceiptsRepo.findForRegisterRecapByDateRange(from, to);
        }else{
            rows = cashReceiptsRepo.findForRegisterRecapByDateRange(from, to, statusId);
        }
        return this.makeRegisterRecapDetail(rows);
    }

    @Override
    public List<Map> getForCashReceiptsRegisterRecapNew(String from, String to, Integer statusId) {
        List<Object[]> rows;

        if(statusId == null || statusId == 0){
            rows = cashReceiptsRepo.findForCrvRecapGL(from, to);
        }else{
            rows = cashReceiptsRepo.findForCrvRecapGL(from, to, statusId);
        }

        return this.makeRegisterRecapForCrv(rows);
    }

    @Override
    public List<CommonRegisterDetail> getForMaterialIssueRegister(String from, String to) {
        List<Object[]> rows = mirRepo.findForRegisterByDateRange(from, to, DocumentStatus.APPROVED.getId());
        return this.makeCommonRegisterDetail2(rows, true, DocumentType.MR);
    }

    @Override
    public List<CommonRegisterDetail> getForMaterialIssueRegister(String from, String to, Integer statusId, String invDocumentType) {
        List<Object[]> rows = mirRepo.findForRegisterByDateRange(from, to, DocumentStatus.APPROVED.getId(), invDocumentType);
        return this.makeCommonRegisterDetail2(rows, true, DocumentType.MR);
    }

    @Override
    public List<RegisterRecapDetail> getForMaterialIssueRegisterRecap(String from, String to, Integer statusId) {
        List<Object[]> rows = null;

        if(statusId == null || statusId == 0){
            rows = mirRepo.findForRegisterRecapByDateRange(from, to, statusId);
        }else{
            rows = mirRepo.findForRegisterRecapByDateRange(from, to, statusId);
        }

        return this.makeRegisterRecapDetail(rows);
    }

    @Override
    public List<RegisterRecapDetail> getForMaterialIssueRegisterRecap(String from, String to, String invDocumentType, Integer statusId) {
        List<Object[]> rows = null;

        if(statusId == null || statusId == 0){
            rows = mirRepo.findForRegisterRecapByDateRangeAndInvDocType(from, to, invDocumentType);
        }else{
            rows = mirRepo.findForRegisterRecapByDateRangeAndInvDocType(from, to, statusId, invDocumentType);        }
        return this.makeRegisterRecapDetail(rows);
    }

    @Override
    public List<CommonRegisterDetail> getForAJRegister(String from, String to, Integer statusId) {
        List<Object[]> journalList = null;

        if(statusId == null || statusId == 0){
            journalList = adjustmentJournalRepo.findForRegisterByDateRange(from, to);
        }else{
            journalList = adjustmentJournalRepo.findForRegisterByDateRange(from, to, statusId);
        }
        return this.makeCommonRegisterDetail2(journalList, false, DocumentType.AJ);
    }

    @Override
    public List<RegisterRecapDetail> getForAJRegisterRecap(String from, String to, Integer statusId) {
        List<Object[]> rows = null;

        if(statusId == null || statusId == 0){
            rows = adjustmentJournalRepo.findForRegisterRecapByDateRange(from, to);
        }else{
            rows = adjustmentJournalRepo.findForRegisterRecapByDateRange(from, to, statusId);
        }
        return this.makeRegisterRecapDetail(rows);
    }

    //Summary

    @Override
    public List<CommonSummaryDetail> getForRVSummary(String from, String to, Integer statusId) {
        List<Object[]> rows;
        if (statusId == DisplayStatus.ALL.getId()) {
            rows = rvRepo.findForSummaryByDateRange(from, to);
        } else if (statusId == DisplayStatus.PENDING.getId()) {
            rows = rvRepo.findForSummaryByDateRangeAndStatusPending(from, to, DocumentStatus.APPROVED.getId(),
                    DocumentStatus.FOR_CANVASSING.getId(), DocumentStatus.CANVASSED.getId());
        } else if (statusId == DocumentStatus.APPROVED.getId()) {
            rows = rvRepo.findForSummaryByDateRangeAndStatusApproved(from, to, DocumentStatus.APPROVED.getId(),
                    DocumentStatus.FOR_CANVASSING.getId(), DocumentStatus.CANVASSED.getId());
        } else {
            rows = rvRepo.findForSummaryByDateRangeAndStatus(from, to, statusId);
        }
        return this.makeCommonSummaryDetail(rows, DocumentType.RV.getId());
    }

    @Override
    public List<CommonSummaryDetail> getForPOSummary(String from, String to, Integer statusId) {
        List<Object[]> rows;
        if (statusId == DisplayStatus.ALL.getId()) {
            rows = poRepo.findForSummaryByDateRange(from, to);
        } else if (statusId == DisplayStatus.PENDING.getId()) {
            rows = poRepo.findForSummaryByDateRangeAndStatusPending(from, to, DocumentStatus.APPROVED.getId());
        } else {
            rows = poRepo.findForSummaryByDateRangeAndStatus(from, to, statusId);
        }
        return this.makeCommonSummaryDetail(rows, DocumentType.PO.getId());
    }

    @Override
    public List<CommonSummaryDetail> getForJOSummary(String from, String to, Integer statusId) {
        List<Object[]> rows;
        if (statusId == DisplayStatus.ALL.getId()) {
            rows = joRepo.findForSummaryByDateRange(from, to);
        } else if (statusId == DisplayStatus.PENDING.getId()) {
            rows = joRepo.findForSummaryByDateRangeAndStatusPending(from, to, DocumentStatus.APPROVED.getId());
        } else {
            rows = joRepo.findForSummaryByDateRangeAndStatus(from, to, statusId);
        }
        return this.makeCommonSummaryDetail(rows, DocumentType.JO.getId());
    }

    @Override
    public List<CommonSummaryDetail> getForJOASummary(String from, String to, Integer statusId) {
        List<Object[]> rows;
        if (statusId == DisplayStatus.ALL.getId()) {
            rows = joaRepo.findForSummaryByDateRange(from, to);
        } else if (statusId == DisplayStatus.PENDING.getId()) {
            rows = joaRepo.findForSummaryByDateRangeAndStatusPending(from, to, DocumentStatus.APPROVED.getId());
        } else {
            rows = joaRepo.findForSummaryByDateRangeAndStatus(from, to, statusId);
        }
        return this.makeCommonSummaryDetail(rows, DocumentType.JOA.getId());
    }

    @Override
    public List<CommonSummaryDetail> getForCanvassSummary(String from, String to, Integer statusId) {
        List<Object[]> rows;
        if (statusId == DisplayStatus.ALL.getId()) {
            rows = canvassRepo.findForSummaryByDateRange(from, to);
        } else if (statusId == DisplayStatus.PENDING.getId()) {
            rows = canvassRepo.findForSummaryByDateRangeAndStatusPending(from, to, DocumentStatus.APPROVED.getId());
        } else {
            rows = canvassRepo.findForSummaryByDateRangeAndStatus(from, to, statusId);
        }
        return this.makeCommonSummaryDetail(rows, DocumentType.CF.getId());
    }

    @Override
    public List<CommonSummaryDetail> getForPRSummary(String from, String to, Integer statusId) {
        List<Object[]> rows;
        if (statusId == DisplayStatus.ALL.getId()) {
            rows = paymentRequestRepo.findForSummaryByDateRange(from, to);
        } else if (statusId == DisplayStatus.PENDING.getId()) {
            rows = paymentRequestRepo.findForSummaryByDateRangeAndStatusPending(from, to, DocumentStatus.APPROVED.getId());
        } else {
            rows = paymentRequestRepo.findForSummaryByDateRangeAndStatus(from, to, statusId);
        }
        return this.makeCommonSummaryDetail(rows, DocumentType.PR.getId());
    }

    @Override
    public List<CommonSummaryDetail> getForPESummary(String month, String year) {
        List<Object[]> rows;
        rows = prepaymentRepo.findByMonthAndYearObject(month, year);
        return this.makeSummaryDetailForPrepayment(rows);
    }

    @Override
    public List<com.noreco1.fireflyv2.controller.response.reports.WorkOrderDetail> getWorkOrderReportDetails(String asOf) {
        return workOrderService.getWorkOrderReportDetails(asOf);
    }

    @Override
    public List<CashFlowStatementDetail> getForCashFlowStatement(String from, String to) {

        final String INDENTION = "  ";
        List<CashFlowStatementDetail> rows = new ArrayList<>();
        try {
            int year = DateHelper.toYear(to);

            String firstMDateSQL = DateHelper.fistDateOfMonthSQLFormat(to);
            String lastMDateSQL = DateHelper.lastDateOfMonthSQLFormat(to);

            // cash flow items type: e.g. INTERNAL CASH GENERATION
            List<CashflowItemType> cashflowItemTypes = cashflowItemTypeRepo.findAll();
            if (Checker.collectionIsNotEmpty(cashflowItemTypes)) {
                BigDecimal balanceToDate = BigDecimal.ZERO;
                BigDecimal balanceActualThisMonth = BigDecimal.ZERO;
                BigDecimal balanceBudget = BigDecimal.ZERO;

                for (CashflowItemType itemType : cashflowItemTypes) {
                    CashFlowStatementDetail csd1 = new CashFlowStatementDetail();
                    csd1.setAccount(itemType.getName());

                    rows.add(csd1);

                    // Get cash flow items without parents: e.g. COLLECTION FROM CUSTOMERS A/R
                    List<Object[]> parentCashflowItems = voucherCashflowDetailRepo.findByCashflowItemTypeIdAndDateRangeAndHasNoParent(itemType.getId(), from, to);
                    if (Checker.collectionIsNotEmpty(parentCashflowItems)) {
                        BigDecimal totalToDate = BigDecimal.ZERO;
                        BigDecimal totalActualThisMonth = BigDecimal.ZERO;
                        BigDecimal totalBudget = BigDecimal.ZERO;

                        for (Object[] cashflowItem : parentCashflowItems) {
                            Integer id = Integer.valueOf(cashflowItem[0].toString());
                            String account0 = String.valueOf(cashflowItem[1]);
                            BigDecimal amount0 = (BigDecimal) cashflowItem[2];

                            Object[] amountThisMonthObj0 = voucherCashflowDetailRepo.findByCashflowItemIdAndDateRange(id, firstMDateSQL, lastMDateSQL);
                            BigDecimal amountThisMonth0 = (BigDecimal) amountThisMonthObj0[0];
                            List<Object[]> budgetObj0 = budgetRepo.findByCashflowItemIdAndDateRange(id, year);
                            BigDecimal budget0 = Checker.collectionIsNotEmpty(budgetObj0) ? (BigDecimal) budgetObj0.get(0)[0] : BigDecimal.ZERO;

                            CashFlowStatementDetail csd2 = new CashFlowStatementDetail();
                            csd2.setAccount(INDENTION + INDENTION + account0);
                            csd2.setToDate(amount0);
                            csd2.setActualThisMonth(amountThisMonth0);
                            csd2.setBudget(budget0);
                            csd2.setBudgetBalance(budget0.subtract(csd2.getToDate()));

                            rows.add(csd2);

                            totalToDate = totalToDate.add(amount0);
                            totalActualThisMonth = totalActualThisMonth.add(amountThisMonth0);
                            totalBudget = totalBudget.add(budget0);

                            // Get children, first level for now: e.g. From Projected Sales
                            List<Object[]> voucherCashflowDetails = voucherCashflowDetailRepo.findByCashFlowItemParentIdAndDateRange(id, from, to);
                            if (Checker.collectionIsNotEmpty(voucherCashflowDetails)) {
                                BigDecimal subTotalToDate = BigDecimal.ZERO;
                                BigDecimal subTotalActualThisMonth = BigDecimal.ZERO;
                                BigDecimal subTotalBudget = BigDecimal.ZERO;

                                for (Object[] voucherCashflowDetail : voucherCashflowDetails) {
                                    Integer secondParentId = Integer.valueOf(voucherCashflowDetail[2].toString());
                                    String account = String.valueOf(voucherCashflowDetail[0]);
                                    BigDecimal amount = (BigDecimal) voucherCashflowDetail[1];

                                    Object[] amountThisMonthObj1 = voucherCashflowDetailRepo.findByCashflowItemIdAndDateRange(id, firstMDateSQL, lastMDateSQL);
                                    BigDecimal amountThisMonth1 = (BigDecimal) amountThisMonthObj1[0];
                                    List<Object[]> budgetObj1 = budgetRepo.findByCashflowItemIdAndDateRange(secondParentId, year);
                                    BigDecimal budget1 = Checker.collectionIsNotEmpty(budgetObj1) ? (BigDecimal) budgetObj1.get(0)[0] : BigDecimal.ZERO;

                                    CashFlowStatementDetail csd3 = new CashFlowStatementDetail();
                                    csd3.setAccount(INDENTION + INDENTION + INDENTION + account);
                                    csd3.setToDate(amount);
                                    csd3.setActualThisMonth(amountThisMonth1);
                                    csd3.setBudget(budget1);
                                    csd3.setBudgetBalance(budget1.subtract(csd3.getToDate()));

                                    rows.add(csd3);

                                    subTotalToDate = subTotalToDate.add(amount);
                                    subTotalActualThisMonth = subTotalActualThisMonth.add(amountThisMonth1);
                                    subTotalBudget = subTotalBudget.add(budget1);

                                    //get Children 2nd level
                                    Integer parId = Integer.parseInt(voucherCashflowDetail[2]+"");
                                    List<Object[]> voucherCashflowDetails2 = voucherCashflowDetailRepo.findByCashFlowItemParentIdAndDateRange(parId, from, to);
                                    if (Checker.collectionIsNotEmpty(voucherCashflowDetails2)) {
                                        for (Object[] voucherCashflowDetail2 : voucherCashflowDetails2) {
                                            Integer thirdParentId = Integer.valueOf(voucherCashflowDetail2[2].toString());
                                            account = String.valueOf(voucherCashflowDetail2[0]);
                                            amount = (BigDecimal) voucherCashflowDetail2[1];

                                            amountThisMonthObj1 = voucherCashflowDetailRepo.findByCashflowItemIdAndDateRange(id, firstMDateSQL, lastMDateSQL);
                                            amountThisMonth1 = (BigDecimal) amountThisMonthObj1[0];
                                            budgetObj1 = budgetRepo.findByCashflowItemIdAndDateRange(thirdParentId, year);
                                            budget1 = Checker.collectionIsNotEmpty(budgetObj1) ? (BigDecimal) budgetObj1.get(0)[0] : BigDecimal.ZERO;

                                            CashFlowStatementDetail csd4 = new CashFlowStatementDetail();
                                            csd4.setAccount(INDENTION + INDENTION + INDENTION + INDENTION + account);
                                            csd4.setToDate(amount);
                                            csd4.setActualThisMonth(amountThisMonth1);
                                            csd4.setBudget(budget1);
                                            csd4.setBudgetBalance(budget1.subtract(csd4.getToDate()));

                                            rows.add(csd4);

                                            subTotalToDate = subTotalToDate.add(amount);
                                            subTotalActualThisMonth = subTotalActualThisMonth.add(amountThisMonth1);
                                            subTotalBudget = subTotalBudget.add(budget1);

                                        }

                                    }

                                }
                                // Total children cash flow items: e.g. From Projected Sales
                                CashFlowStatementDetail csdSubTotal = new CashFlowStatementDetail();
                                csdSubTotal.setAccount(INDENTION + INDENTION + "SUB TOTAL");
                                csdSubTotal.setToDate(subTotalToDate);
                                csdSubTotal.setActualThisMonth(subTotalActualThisMonth);
                                csdSubTotal.setBudget(subTotalBudget);
                                csdSubTotal.setBudgetBalance(subTotalBudget.subtract(csdSubTotal.getToDate()));

                                rows.add(csdSubTotal);
                                rows.add(this.cashFLowStatementBlankRow());

                                totalToDate = totalToDate.add(subTotalToDate);
                                totalActualThisMonth = totalActualThisMonth.add(subTotalActualThisMonth);
                                totalBudget = totalBudget.add(subTotalBudget);
                            }
                        }

                        // Total cash flow items without parents: e.g. COLLECTION FROM CUSTOMERS A/R
                        CashFlowStatementDetail csdTotal = new CashFlowStatementDetail();
                        csdTotal.setAccount("TOTAL");
                        csdTotal.setToDate(totalToDate);
                        csdTotal.setActualThisMonth(totalActualThisMonth);
                        csdTotal.setBudget(totalBudget);
                        csdTotal.setBudgetBalance(totalBudget.subtract(csdTotal.getToDate()));

                        rows.add(this.cashFLowStatementBlankRow());
                        rows.add(csdTotal);
                        rows.add(this.cashFLowStatementBlankRow());

                        balanceToDate = balanceToDate.add(totalToDate);
                        balanceActualThisMonth = balanceActualThisMonth.add(totalActualThisMonth);
                        balanceBudget = balanceBudget.add(totalBudget);
                    }
                }

                CashFlowStatementDetail beginningBalance = new CashFlowStatementDetail();
                beginningBalance.setAccount("Cash Balances, Beginning");
                rows.add(beginningBalance);

                CashFlowStatementDetail balance = new CashFlowStatementDetail();
                balance.setAccount("CASH BALANCE, END");
                balance.setToDate(balanceToDate);
                balance.setActualThisMonth(balanceActualThisMonth);
                balance.setBudget(balanceBudget);
                balance.setBudgetBalance(balanceBudget.subtract(balance.getToDate()));

                rows.add(this.cashFLowStatementBlankRow());
                rows.add(balance);
                rows.add(this.cashFLowStatementBlankRow());
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return rows;
    }

    @Override
    public List<CashFlowStatementDetail> getForCashFlowStatementBSUP(String from, String to) {

        final String INDENTION = "  ";
        List<CashFlowStatementDetail> rows = new ArrayList<>();
        try {
            int year = DateHelper.toYear(to);

            String firstMDateSQL = DateHelper.fistDateOfMonthSQLFormat(to);
            String lastMDateSQL = DateHelper.lastDateOfMonthSQLFormat(to);


            //convert string startdate to sqlDate to Calendar to get year and create jan1
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
            Date begCutOffDateSql = sdf.parse(from);
            Calendar c = Calendar.getInstance();
            c.setTime(begCutOffDateSql);
            String jan1 = c.get(Calendar.YEAR) + "-01-01";

            // cash flow items type: e.g. INTERNAL CASH GENERATION
            List<CashflowItemType> cashflowItemTypes = cashflowItemTypeRepo.findAll();
            if (Checker.collectionIsNotEmpty(cashflowItemTypes)) {
                BigDecimal balanceToDate = BigDecimal.ZERO;
                BigDecimal balanceActualThisMonth = BigDecimal.ZERO;
                BigDecimal balanceBudget = BigDecimal.ZERO;

                for (CashflowItemType itemType : cashflowItemTypes) {
                    CashFlowStatementDetail csd1 = new CashFlowStatementDetail();
                    csd1.setAccount(itemType.getName());

                    rows.add(csd1);

                    // Get cash flow items without parents: e.g. COLLECTION FROM CUSTOMERS A/R
                    List<Object[]> parentCashflowItems = voucherCashflowDetailRepo.findByCashflowItemTypeIdAndDateRangeAndHasNoParent(itemType.getId(), from, to);
                    if (Checker.collectionIsNotEmpty(parentCashflowItems)) {
                        BigDecimal totalToDate = BigDecimal.ZERO;
                        BigDecimal totalActualThisMonth = BigDecimal.ZERO;
                        BigDecimal totalBudget = BigDecimal.ZERO;

                        for (Object[] cashflowItem : parentCashflowItems) {
                            Integer id = Integer.valueOf(cashflowItem[0].toString());
                            String account0 = String.valueOf(cashflowItem[3]) +". "+ String.valueOf(cashflowItem[1]);
                            BigDecimal amount0 = (BigDecimal) cashflowItem[2];

                            Object[] amountThisMonthObj0 = voucherCashflowDetailRepo.findByCashflowItemIdAndDateRange(id, firstMDateSQL, lastMDateSQL);
                            BigDecimal amountThisMonth0 = (BigDecimal) amountThisMonthObj0[0];
                            List<Object[]> budgetObj0 = budgetRepo.findByCashflowItemIdAndDateRange(id, year);
                            BigDecimal budget0 = Checker.collectionIsNotEmpty(budgetObj0) ? (BigDecimal) budgetObj0.get(0)[0] : BigDecimal.ZERO;

                            CashFlowStatementDetail csd2 = new CashFlowStatementDetail();
                            csd2.setAccount(INDENTION + INDENTION + account0);
                            csd2.setToDate(amount0);
                            csd2.setActualThisMonth(amountThisMonth0);
                            csd2.setBudget(budget0);
                            csd2.setBudgetBalance(budget0.subtract(csd2.getToDate()));

                            rows.add(csd2);

                            totalToDate = totalToDate.add(amount0);
                            totalActualThisMonth = totalActualThisMonth.add(amountThisMonth0);
                            totalBudget = totalBudget.add(budget0);

                            // Get children, first level for now: e.g. From Projected Sales
                            List<Object[]> voucherCashflowDetails = voucherCashflowDetailRepo.findByCashFlowItemParentIdToDate(id, from);
                            if (Checker.collectionIsNotEmpty(voucherCashflowDetails)) {
                                BigDecimal subTotalToDate = BigDecimal.ZERO;
                                BigDecimal subTotalActualThisMonth = BigDecimal.ZERO;
                                BigDecimal subTotalBudget = BigDecimal.ZERO;

                                for (Object[] voucherCashflowDetail : voucherCashflowDetails) {
                                    Integer secondParentId = Integer.valueOf(voucherCashflowDetail[2].toString());
                                    String account = String.valueOf(voucherCashflowDetail[3]) +". "+ String.valueOf(voucherCashflowDetail[0]);
                                    BigDecimal amount = (BigDecimal) voucherCashflowDetail[1];

                                    Object[] amountThisMonthObj1 = voucherCashflowDetailRepo.findByCashflowItemIdAndDateRange(secondParentId, firstMDateSQL, lastMDateSQL);
                                    BigDecimal amountThisMonth1 = (BigDecimal) amountThisMonthObj1[0];
                                    List<Object[]> budgetObj1 = budgetRepo.findByCashflowItemIdAndDateRange(secondParentId, year);
                                    BigDecimal budget1 = Checker.collectionIsNotEmpty(budgetObj1) ? (BigDecimal) budgetObj1.get(0)[0] : BigDecimal.ZERO;

                                    CashFlowStatementDetail csd3 = new CashFlowStatementDetail();
                                    csd3.setAccount(INDENTION + INDENTION + INDENTION + account);
                                    csd3.setToDate(amount);
                                    csd3.setActualThisMonth(amountThisMonth1);
                                    csd3.setBudget(budget1);
                                    csd3.setBudgetBalance(budget1.subtract(csd3.getToDate()));

                                    rows.add(csd3);

                                    subTotalToDate = subTotalToDate.add(amount);
                                    subTotalActualThisMonth = subTotalActualThisMonth.add(amountThisMonth1);
                                    subTotalBudget = subTotalBudget.add(budget1);

                                    //get Children 2nd level
                                    Integer parId = Integer.parseInt(voucherCashflowDetail[2]+"");
                                    List<Object[]> voucherCashflowDetails2 = voucherCashflowDetailRepo.findByCashFlowItemParentIdAndDateRange(parId, from, to);
                                    if (Checker.collectionIsNotEmpty(voucherCashflowDetails2)) {

                                        BigDecimal totalAmountForBudgetChildAccounts = BigDecimal.ZERO;
                                        BigDecimal totalAmountForToDateChildAccounts = BigDecimal.ZERO;
                                        BigDecimal totalAmountForActualThisMonthChildAccounts = BigDecimal.ZERO;

                                        for (Object[] voucherCashflowDetail2 : voucherCashflowDetails2) {
                                            Integer thirdParentId = Integer.valueOf(voucherCashflowDetail2[2].toString());
                                            account = String.valueOf(voucherCashflowDetail2[3]) +". "+ String.valueOf(voucherCashflowDetail2[0]);
                                            amount = (BigDecimal) voucherCashflowDetail2[1];

                                            amountThisMonthObj1 = voucherCashflowDetailRepo.findByCashflowItemIdAndDateRange(id, firstMDateSQL, lastMDateSQL);
                                            amountThisMonth1 = (BigDecimal) amountThisMonthObj1[0];
                                            budgetObj1 = budgetRepo.findByCashflowItemIdAndDateRange(thirdParentId, year);
                                            budget1 = Checker.collectionIsNotEmpty(budgetObj1) ? (BigDecimal) budgetObj1.get(0)[0] : BigDecimal.ZERO;

                                            CashFlowStatementDetail csd4 = new CashFlowStatementDetail();
                                            csd4.setAccount(INDENTION + INDENTION + INDENTION + INDENTION + account);
                                            csd4.setToDate(amount);
                                            csd4.setActualThisMonth(amountThisMonth1);
                                            csd4.setBudget(budget1);
                                            csd4.setBudgetBalance(budget1.subtract(csd4.getToDate()));

                                            rows.add(csd4);

                                            subTotalToDate = subTotalToDate.add(amount);
                                            subTotalActualThisMonth = subTotalActualThisMonth.add(amountThisMonth1);
                                            subTotalBudget = subTotalBudget.add(budget1);

                                            // total amount for child accounts only
                                            totalAmountForBudgetChildAccounts = totalAmountForBudgetChildAccounts.add(budget1);
                                            totalAmountForToDateChildAccounts = totalAmountForToDateChildAccounts.add(amount);
                                            totalAmountForActualThisMonthChildAccounts = totalAmountForActualThisMonthChildAccounts.add(amountThisMonth1);

                                        }

                                        //get total amount for child accounts and display it on parent account
                                        csd3.setBudget(totalAmountForBudgetChildAccounts);
                                        csd3.setToDate(totalAmountForToDateChildAccounts);
                                        csd3.setActualThisMonth(totalAmountForActualThisMonthChildAccounts);
                                        csd3.setBudgetBalance(totalAmountForBudgetChildAccounts);

                                    }
                                }

                                //get total amount for child accounts and display it on parent account
                                csd2.setBudget(subTotalBudget);
                                csd2.setToDate(subTotalToDate);
                                csd2.setActualThisMonth(subTotalActualThisMonth);
                                csd2.setBudgetBalance(subTotalBudget);

                                totalToDate = totalToDate.add(subTotalToDate);
                                totalActualThisMonth = totalActualThisMonth.add(subTotalActualThisMonth);
                                totalBudget = totalBudget.add(subTotalBudget);

                            }
                        }

                        // Total cash flow items without parents: e.g. COLLECTION FROM CUSTOMERS A/R
                        CashFlowStatementDetail csdTotal = new CashFlowStatementDetail();
                        csdTotal.setAccount("TOTAL");
                        csdTotal.setToDate(totalToDate);
                        csdTotal.setActualThisMonth(totalActualThisMonth);
                        csdTotal.setBudget(totalBudget);
                        csdTotal.setBudgetBalance(totalBudget.subtract(csdTotal.getToDate()));

                        rows.add(csdTotal);
                        rows.add(this.cashFLowStatementBlankRow());

                        balanceToDate = balanceToDate.add(totalToDate);
                        balanceActualThisMonth = balanceActualThisMonth.add(totalActualThisMonth);
                        balanceBudget = balanceBudget.add(totalBudget);
                    }
                }

                CashFlowStatementDetail beginningBalance = new CashFlowStatementDetail();
                beginningBalance.setAccount("Cash Balances, Beginning");

                //map cash and cash equivalents account on settings table to supply param acctId
                // and add results to achieve all cashBalances
                Map account = settingFacade.getByCode("CASH_ACCOUNT");
                BigDecimal cashBalanceToDate = voucherCashflowDetailRepo.findCashBalances(Integer.parseInt(account.get("id")+""), DocumentStatus.APPROVED.getId(), jan1, to);
                BigDecimal cashBalanceThisMonth = voucherCashflowDetailRepo.findCashBalances(Integer.parseInt(account.get("id")+""), DocumentStatus.APPROVED.getId(), from, to);
                account = settingFacade.getByCode("CASH_EQUIVALENTS_ACCOUNT");
                cashBalanceToDate = cashBalanceToDate.add(voucherCashflowDetailRepo.findCashBalances(Integer.parseInt(account.get("id")+""), DocumentStatus.APPROVED.getId(), jan1, to));
                cashBalanceThisMonth = cashBalanceThisMonth.add(voucherCashflowDetailRepo.findCashBalances(Integer.parseInt(account.get("id")+""), DocumentStatus.APPROVED.getId(), from, to));

                beginningBalance.setToDate(cashBalanceToDate);
                beginningBalance.setActualThisMonth(cashBalanceThisMonth);
                rows.add(beginningBalance);

                CashFlowStatementDetail balance = new CashFlowStatementDetail();
                balance.setAccount("CASH BALANCE, END");
                balance.setToDate(balanceToDate);
                balance.setActualThisMonth(balanceActualThisMonth);
                balance.setBudget(balanceBudget);
                balance.setBudgetBalance(balanceBudget.subtract(balance.getToDate()));

                rows.add(this.cashFLowStatementBlankRow());
                rows.add(balance);
                rows.add(this.cashFLowStatementBlankRow());
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return rows;
    }

    @Override
    public List<QuotationDetail> getForQuotationSummary(Integer rivId) {

        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        List<QuotationDetail> data = new ArrayList<>();

        List<CanvassDetail> canvassDetails = canvassDetailRepo.findByPurchaseRequestDetailPurchaseRequestId(rivId);

        Map detailsMap = new HashMap<>();
        Map suppliersIndexMap = new HashMap<>();

        if (!canvassDetails.isEmpty()) {

            QuotationDetail quotationDetailSuppliers = new QuotationDetail();
            int supplierCounter = 0;
            for (int idx=0; idx<canvassDetails.size(); idx++) {

                CanvassDetail canvassDetail = canvassDetails.get(idx);

                Supplier supplier = canvassDetail.getSupplier();
                if(supplier == null) continue; // Canvass havent accomplished by supplier yet

                // check if unique
                Object sIdx = suppliersIndexMap.get(canvassDetail.getSupplier().getAccountNumber());
                if(sIdx == null) {

                    if(supplierCounter == 0) {
                        quotationDetailSuppliers.setSupplier1(supplier.getName());
                    }
                    else if(supplierCounter == 1) {
                        quotationDetailSuppliers.setSupplier2(supplier.getName());
                    }
                    else if(supplierCounter == 2) {
                        quotationDetailSuppliers.setSupplier3(supplier.getName());
                    }

                    suppliersIndexMap.put(supplier.getAccountNumber(), supplierCounter);
                    supplierCounter++;
                }

                if(supplierCounter == 3) break; // up 3 suppliers only
            }

            if(supplierCounter > 0) data.add(quotationDetailSuppliers);  // suppliers only


            // items
            for (CanvassDetail line : canvassDetails) {

                Object o = detailsMap.get(line.getPurchaseRequestDetail().getId());
                if(o != null) {
                    CanvassDetail detailFromMap  = (CanvassDetail) o;

                    if(detailFromMap.getPriceSupplier2() == null || detailFromMap.getPriceSupplier2().compareTo(BigDecimal.ZERO) == 0) {

                        detailFromMap.setPriceSupplier2(line.getUnitPrice());

                    } else if(detailFromMap.getPriceSupplier3() == null || detailFromMap.getPriceSupplier3().compareTo(BigDecimal.ZERO) == 0) {
                        detailFromMap.setPriceSupplier3(line.getUnitPrice());
                    }

                    detailsMap.put(line.getPurchaseRequestDetail().getId(), detailFromMap); // put back to the map

                } else {
                    line.setPriceSupplier1(line.getUnitPrice());
                    detailsMap.put(line.getPurchaseRequestDetail().getId(), line);
                }
            }

            int counter = 1;
            Iterator it = detailsMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                CanvassDetail canvassDetail = (CanvassDetail) pair.getValue();

                QuotationDetail quotationDetailPrices = new QuotationDetail();

                quotationDetailPrices.setId(counter++);
                quotationDetailPrices.setDescription(canvassDetail.getPurchaseRequestDetail().getItem() != null ? canvassDetail.getPurchaseRequestDetail().getItem().getDescription():canvassDetail.getPurchaseRequestDetail().getJoDescription());
                quotationDetailPrices.setQuantity(canvassDetail.getPurchaseRequestDetail().getQuantity());

                if(canvassDetail.getPriceSupplier1() != null) {
                    quotationDetailPrices.setSupplier1(decimalFormat.format(canvassDetail.getPriceSupplier1()));
                }
                if(canvassDetail.getPriceSupplier2() != null) {
                    quotationDetailPrices.setSupplier2(decimalFormat.format(canvassDetail.getPriceSupplier2()));
                }

                if(canvassDetail.getPriceSupplier3() != null) {
                    quotationDetailPrices.setSupplier3(decimalFormat.format(canvassDetail.getPriceSupplier3()));
                }

                data.add(quotationDetailPrices);
                it.remove(); // avoids a ConcurrentModificationException
            }
        }

        return data;
    }

    @Override
    public List<QuotationDetail> getForQuotation(Integer rivId) {

        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        List<QuotationDetail> data = new ArrayList<>();

        List<CanvassDetail> canvassDetails = canvassDetailRepo.findByPurchaseRequestDetailPurchaseRequestId(rivId);

        Map detailsMap = new HashMap<>();
        Map suppliersIndexMap = new HashMap<>();

        if (!canvassDetails.isEmpty()) {

            QuotationDetail quotationDetailSuppliers = new QuotationDetail();
            int supplierCounter = 0;
            for (int idx=0; idx<canvassDetails.size(); idx++) {

                CanvassDetail canvassDetail = canvassDetails.get(idx);

                Supplier supplier = canvassDetail.getSupplier();
                if(supplier == null) continue; // Canvass havent accomplished by supplier yet

                // check if unique
                Object sIdx = suppliersIndexMap.get(canvassDetail.getSupplier().getAccountNumber());
                if(sIdx == null) {

                    if(supplierCounter == 0) {
                        quotationDetailSuppliers.setSupplier1(supplier.getName());
                    }
                    else if(supplierCounter == 1) {
                        quotationDetailSuppliers.setSupplier2(supplier.getName());
                    }
                    else if(supplierCounter == 2) {
                        quotationDetailSuppliers.setSupplier3(supplier.getName());
                    }

                    suppliersIndexMap.put(supplier.getAccountNumber(), supplierCounter);
                    supplierCounter++;
                }

                if(supplierCounter == 3) break; // up 3 suppliers only
            }

            if(supplierCounter > 0) data.add(quotationDetailSuppliers);  // suppliers only


            // items
            for (CanvassDetail line : canvassDetails) {

                Object o = detailsMap.get(line.getPurchaseRequestDetail().getId());
                if(o != null) {
                    CanvassDetail detailFromMap  = (CanvassDetail) o;

                    if(detailFromMap.getPriceSupplier2() == null || detailFromMap.getPriceSupplier2().compareTo(BigDecimal.ZERO) == 0) {

                        detailFromMap.setPriceSupplier2(line.getUnitPrice());

                    } else if(detailFromMap.getPriceSupplier3() == null || detailFromMap.getPriceSupplier3().compareTo(BigDecimal.ZERO) == 0) {
                        detailFromMap.setPriceSupplier3(line.getUnitPrice());
                    }

                    detailsMap.put(line.getPurchaseRequestDetail().getId(), detailFromMap); // put back to the map

                } else {
                    line.setPriceSupplier1(line.getUnitPrice());
                    detailsMap.put(line.getPurchaseRequestDetail().getId(), line);
                }
            }

            int counter = 1;
            Iterator it = detailsMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                CanvassDetail canvassDetail = (CanvassDetail) pair.getValue();

                QuotationDetail quotationDetailPrices = new QuotationDetail();

                quotationDetailPrices.setId(counter++);
                quotationDetailPrices.setPurchaseRequestDetailId(canvassDetail.getPurchaseRequestDetail().getId());
                quotationDetailPrices.setDescription(canvassDetail.getPurchaseRequestDetail().getItem() != null ? canvassDetail.getPurchaseRequestDetail().getItem().getDescription():canvassDetail.getPurchaseRequestDetail().getJoDescription());
                quotationDetailPrices.setQuantity(canvassDetail.getPurchaseRequestDetail().getQuantity());
                quotationDetailPrices.setAvailable(true);

                if(canvassDetail.getPriceSupplier1() != null) {
                    quotationDetailPrices.setSupplier1(decimalFormat.format(canvassDetail.getPriceSupplier1()));
                }
                if(canvassDetail.getPriceSupplier2() != null) {
                    quotationDetailPrices.setSupplier2(decimalFormat.format(canvassDetail.getPriceSupplier2()));
                }

                if(canvassDetail.getPriceSupplier3() != null) {
                    quotationDetailPrices.setSupplier3(decimalFormat.format(canvassDetail.getPriceSupplier3()));
                }

                data.add(quotationDetailPrices);
                it.remove(); // avoids a ConcurrentModificationException
            }
        }

        return data;
    }

    @Override
    public List<Map> getForMCRTSummary(String from, String to, Integer inventoryLocationId, Integer documentStatusId) {
        List<Map> data = new ArrayList<>();
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            List<MaterialCreditTicket> list;

            if(inventoryLocationId > 0 && documentStatusId > 0) {
                list = materialCreditTicketRepo.findByVoucherDateBetweenAndStockReleaseInventoryLocationIdAndDocumentStatusId(fromDate, toDate, inventoryLocationId, documentStatusId);
            } else if(inventoryLocationId > 0) {
                list = materialCreditTicketRepo.findByVoucherDateBetweenAndStockReleaseInventoryLocationId(fromDate, toDate, inventoryLocationId);
            } else if(documentStatusId > 0) {
                list = materialCreditTicketRepo.findByVoucherDateBetweenAndDocumentStatusId(fromDate, toDate, documentStatusId);
            } else {
                list = materialCreditTicketRepo.findByVoucherDateBetween(fromDate, toDate);
            }

            if(!list.isEmpty()) {
                for (MaterialCreditTicket mcrt: list) {

                    Map map = new HashMap();

                    map.put("voucherDate", mcrt.getVoucherDate());
                    map.put("code", mcrt.getCode());
                    map.put("description", mcrt.getRemarks());
                    map.put("status", mcrt.getDocumentStatus().getStatus());
                    map.put("invLocId", mcrt.getStockRelease().getInventoryLocation().getId());
                    map.put("invLocDesc", mcrt.getStockRelease().getInventoryLocation().getDescription());
                    map.put("itemId", null);
                    map.put("itemCode", null);
                    map.put("quantity", BigDecimal.ZERO);
                    map.put("unitCost", BigDecimal.ZERO);
                    map.put("totalCost", BigDecimal.ZERO);

                    ArrayList<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(mcrt.getTransaction().getId());
                    if(!details.isEmpty()) {
                        for (ItemTransactionDetail detail : details) {

                            Item item = detail.getItem();

                            map.put("itemId", item.getId());
                            map.put("itemCode", item.getCode());
                            map.put("quantity", detail.getQuantity());
                            map.put("unit", detail.getItem().getUnit().getCode());
                            map.put("unitCost", detail.getUnitCost());
                            map.put("totalCost", detail.getTotalCost());

                            data.add(map);

                            map = new HashMap();
                        }
                    } else {
                        data.add(map);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public List<Map> getForMSTSummary(String from, String to, Integer inventoryLocationId, Integer documentStatusId) {
        List<Map> data = new ArrayList<>();
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            List<MaterialSalvageTicket> list;

            if(inventoryLocationId > 0 && documentStatusId > 0) {
                list = materialSalvageTicketRepo.findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusId(fromDate, toDate, inventoryLocationId, documentStatusId);
            } else if(inventoryLocationId > 0) {
                list = materialSalvageTicketRepo.findByVoucherDateBetweenAndInventoryLocationId(fromDate, toDate, inventoryLocationId);
            } else if(documentStatusId > 0) {
                list = materialSalvageTicketRepo.findByVoucherDateBetweenAndDocumentStatusId(fromDate, toDate, documentStatusId);
            } else {
                list = materialSalvageTicketRepo.findByVoucherDateBetween(fromDate, toDate);
            }

            if(!list.isEmpty()) {
                for (MaterialSalvageTicket mst: list) {

                    Map map = new HashMap();

                    map.put("voucherDate", mst.getVoucherDate());
                    map.put("code", mst.getCode());
                    map.put("description", mst.getPurpose());
                    map.put("status", mst.getDocumentStatus().getStatus());
                    map.put("invLocId", mst.getInventoryLocation().getId());
                    map.put("invLocDesc", mst.getInventoryLocation().getDescription());
                    map.put("itemId", null);
                    map.put("itemCode", null);
                    map.put("quantity", BigDecimal.ZERO);
                    map.put("unitCost", BigDecimal.ZERO);
                    map.put("totalCost", BigDecimal.ZERO);

                    ArrayList<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(mst.getTransaction().getId());
                    if(!details.isEmpty()) {
                        for (ItemTransactionDetail detail : details) {

                            Item item = detail.getItem();

                            map.put("itemId", item.getId());
                            map.put("itemCode", item.getCode());
                            map.put("quantity", detail.getQuantity());
                            map.put("unitCost", detail.getUnitCost());
                            map.put("unit", detail.getItem().getUnit().getCode());
                            map.put("totalCost", detail.getTotalCost());
                            map.put("invLocId", mst.getInventoryLocation().getId());
                            map.put("invLocDesc", mst.getInventoryLocation().getDescription());

                            data.add(map);

                            map = new HashMap();
                        }
                    } else {
                        data.add(map);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public List<Map> getForWithdrawalsSummary(String from, String to, Integer documentTypeId, Integer inventoryLocationId, Integer documentStatusId) {
        List<Map> data = new ArrayList<>();
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");
            if (fromDate == null) {
                fromDate = new Date(0);
            }
            if (toDate == null) {
                toDate = new Date();
            }
            List<StockWithdrawal> list;
            if(documentTypeId > 0 && inventoryLocationId > 0 && documentStatusId > 0) {
                list = stockWithdrawalRepo.findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusIdAndInventoryCategoryTypeOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, inventoryLocationId, documentStatusId, documentTypeId);
            } else if(documentTypeId > 0 && inventoryLocationId > 0) {
                list = stockWithdrawalRepo.findByVoucherDateBetweenAndInventoryLocationIdAndInventoryCategoryTypeOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, inventoryLocationId, documentTypeId);
            } else if(documentTypeId > 0 &&  documentStatusId > 0) {
                list = stockWithdrawalRepo.findByVoucherDateBetweenAndDocumentStatusIdAndInventoryCategoryTypeOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, documentStatusId, documentTypeId);
            } else if(inventoryLocationId > 0 && documentStatusId > 0) {
                list = stockWithdrawalRepo.findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusIdOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, inventoryLocationId, documentStatusId);
            } else if(inventoryLocationId > 0) {
                list = stockWithdrawalRepo.findByVoucherDateBetweenAndInventoryLocationIdOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, inventoryLocationId);
            } else if(documentStatusId > 0) {
                list = stockWithdrawalRepo.findByVoucherDateBetweenAndDocumentStatusIdOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, documentStatusId);
            }  else if(documentTypeId > 0 ) {
                list = stockWithdrawalRepo.findByVoucherDateBetweenAndInventoryCategoryTypeOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, documentTypeId);
            } else {
                list = stockWithdrawalRepo.findByVoucherDateBetweenOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate);
            }
            if(!list.isEmpty()) {
                for (StockWithdrawal sw: list) {
                    Map map = new HashMap();
                    map.put("voucherDate", sw.getVoucherDate());
                    map.put("code", sw.getCode());
                    map.put("description", sw.getDescription());
                    map.put("status", sw.getDocumentStatus().getStatus());
                    map.put("itemId", null);
                    map.put("itemCode", null);
                    map.put("quantity", BigDecimal.ZERO);
                    map.put("quantityReleased", BigDecimal.ZERO);
                    map.put("unit", null);
                    ArrayList<StockWithdrawalDetail> details = stockWithdrawalDetailRepo.findByStockWithdrawalId(sw.getId());
                    if(!details.isEmpty()) {
                        for (StockWithdrawalDetail detail : details) {
                            Item item = detail.getItem();
                            map.put("itemId", item.getId());
                            map.put("itemCode", item.getCode());
                            map.put("quantity", detail.getQuantity());
                            map.put("quantityReleased", detail.getQuantityReleased());
                            map.put("unit", item.getUnit().getCode());
                            data.add(map);
                            map = new HashMap();
                        }
                    } else {
                        data.add(map);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public List<Map> getForStockReleaseSummary(String from, String to, Integer documentTypeId, Integer inventoryLocationId, Integer documentStatusId) {
        List<Map> data = new ArrayList<>();
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");
            if (fromDate == null) {
                fromDate = new Date(0);
            }
            if (toDate == null) {
                toDate = new Date();
            }
            List<StockRelease> list;
            if(documentTypeId > 0 && inventoryLocationId > 0 && documentStatusId > 0) {
                list = stockReleaseRepo.findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusIdAndTypeOrderByTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, inventoryLocationId, documentStatusId, documentTypeId);
            } else if(documentTypeId > 0 && inventoryLocationId > 0) {
                list = stockReleaseRepo.findByVoucherDateBetweenAndInventoryLocationIdAndTypeOrderByTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, inventoryLocationId, documentTypeId);
            } else if(documentTypeId > 0 &&  documentStatusId > 0) {
                list = stockReleaseRepo.findByVoucherDateBetweenAndDocumentStatusIdAndTypeOrderByTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, documentStatusId, documentTypeId);
            } else if(inventoryLocationId > 0 && documentStatusId > 0) {
                list = stockReleaseRepo.findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusIdOrderByTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, inventoryLocationId, documentStatusId);
            } else if(inventoryLocationId > 0) {
                list = stockReleaseRepo.findByVoucherDateBetweenAndInventoryLocationIdOrderByTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, inventoryLocationId);
            } else if(documentStatusId > 0) {
                list = stockReleaseRepo.findByVoucherDateBetweenAndDocumentStatusIdOrderByTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, documentStatusId);
            }  else if(documentTypeId > 0 ) {
                list = stockReleaseRepo.findByVoucherDateBetweenAndTypeOrderByTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate, documentTypeId);
            } else {
                list = stockReleaseRepo.findByVoucherDateBetweenOrderByTypeAscInventoryLocationIdAscCodeAsc(fromDate, toDate);
            }
            if(!list.isEmpty()) {
                for (StockRelease stockRelease: list) {
                    Map map = new HashMap();
                    map.put("voucherDate", stockRelease.getVoucherDate());
                    map.put("code", stockRelease.getCode());
                    map.put("description", stockRelease.getDescription());
                    map.put("status", stockRelease.getDocumentStatus().getStatus());
                    map.put("itemId", null);
                    map.put("itemCode", null);
                    map.put("quantity", BigDecimal.ZERO);
                    map.put("quantityReleased", BigDecimal.ZERO);
                    map.put("unit", null);
                    ArrayList<StockWithdrawalDetail> details = stockWithdrawalDetailRepo.findByStockWithdrawalTransactionId(stockRelease.getDocumentTransaction().getId());
                    if(!details.isEmpty()) {
                        for (StockWithdrawalDetail detail : details) {
                            Item item = detail.getItem();
                            map.put("itemId", item.getId());
                            map.put("itemCode", item.getCode());
                            map.put("quantity", detail.getQuantity());
                            map.put("quantityReleased", detail.getQuantityReleased());
                            map.put("unit", item.getUnit().getCode());
                            data.add(map);
                            map = new HashMap();
                        }
                    } else {
                        ArrayList<ItemTransactionDetail> transDetails = itemTransactionDetailRepo.findByTransactionId(stockRelease.getDocumentTransaction().getId());
                        if(!transDetails.isEmpty()) {
                            for (ItemTransactionDetail detail : transDetails) {
                                Item item = detail.getItem();
                                map.put("itemId", item.getId());
                                map.put("itemCode", item.getCode());
                                map.put("quantity", detail.getQuantity());
                                map.put("quantityReleased", detail.getQuantityReleased());
                                map.put("unit", item.getUnit().getCode());
                                data.add(map);
                                map = new HashMap();
                            }
                        } else {
                            data.add(map);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public List<Map> getForStockAdjustmentSummary(String from, String to, Integer inventoryLocationId, Integer documentStatusId) {
        List<Map> data = new ArrayList<>();
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            List<StockAdjustment> list;

            if(inventoryLocationId > 0 && documentStatusId > 0) {
                list = stockAdjustmentRepo.findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusId(fromDate, toDate, inventoryLocationId, documentStatusId);
            } else if(inventoryLocationId > 0) {
                list = stockAdjustmentRepo.findByVoucherDateBetweenAndInventoryLocationId(fromDate, toDate, inventoryLocationId);
            } else if(documentStatusId > 0) {
                list = stockAdjustmentRepo.findByVoucherDateBetweenAndDocumentStatusId(fromDate, toDate, documentStatusId);
            } else {
                list = stockAdjustmentRepo.findByVoucherDateBetween(fromDate, toDate);
            }

            if(!list.isEmpty()) {
                for (StockAdjustment stockAdjustment: list) {

                    Map map = new HashMap();

                    map.put("voucherDate", stockAdjustment.getVoucherDate());
                    map.put("code", stockAdjustment.getCode());
                    map.put("description", stockAdjustment.getRemarks());
                    map.put("status", stockAdjustment.getDocumentStatus().getStatus());
                    map.put("invLocId", stockAdjustment.getInventoryLocation().getId());
                    map.put("invLocDesc", stockAdjustment.getInventoryLocation().getDescription());
                    map.put("itemId", null);
                    map.put("itemCode", null);
                    map.put("quantity", BigDecimal.ZERO);
                    map.put("unitCost", BigDecimal.ZERO);
                    map.put("totalCost", BigDecimal.ZERO);

                    ArrayList<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(stockAdjustment.getTransaction().getId());
                    if(!details.isEmpty()) {
                        for (ItemTransactionDetail detail : details) {

                            Item item = detail.getItem();

                            map.put("itemId", item.getId());
                            map.put("itemCode", item.getCode());
                            map.put("quantity", detail.getAdjustment());
                            map.put("unitCost", detail.getUnitCost());
                            map.put("unit", detail.getItem().getUnit().getCode());
                            map.put("totalCost", detail.getTotalCost());
                            map.put("invLocId", stockAdjustment.getInventoryLocation().getId());
                            map.put("invLocDesc", stockAdjustment.getInventoryLocation().getDescription());

                            data.add(map);

                            map = new HashMap();
                        }
                    } else {
                        data.add(map);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public List<Map> getForStockTransferSummary(String from, String to, Integer inventoryLocationId, Integer documentStatusId) {
        List<Map> data = new ArrayList<>();
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            List<StockTransfer> list;

            if(inventoryLocationId > 0 && documentStatusId > 0) {
                list = stockTransferRepo.findByVoucherDateBetweenAndFromInventoryLocationIdAndDocumentStatusIdOrderByFromInventoryLocationIdAsc(fromDate, toDate, inventoryLocationId, documentStatusId);
            } else if(inventoryLocationId > 0) {
                list = stockTransferRepo.findByVoucherDateBetweenAndFromInventoryLocationIdOrderByFromInventoryLocationIdAsc(fromDate, toDate, inventoryLocationId);
            } else if(documentStatusId > 0) {
                list = stockTransferRepo.findByVoucherDateBetweenAndDocumentStatusIdOrderByFromInventoryLocationIdAsc(fromDate, toDate, documentStatusId);
            } else {
                list = stockTransferRepo.findByVoucherDateBetween(fromDate, toDate);
            }

            if(!list.isEmpty()) {
                for (StockTransfer stockTransfer: list) {

                    Map map = new HashMap();

                    map.put("voucherDate", stockTransfer.getVoucherDate());
                    map.put("code", stockTransfer.getCode());
                    map.put("description", stockTransfer.getRemarks());
                    map.put("status", stockTransfer.getDocumentStatus().getStatus());
                    map.put("invLocId", stockTransfer.getFromInventoryLocation().getId());
                    map.put("invLocDesc", stockTransfer.getFromInventoryLocation().getDescription());
                    map.put("itemId", null);
                    map.put("itemCode", null);
                    map.put("quantity", BigDecimal.ZERO);
                    map.put("unitCost", BigDecimal.ZERO);
                    map.put("totalCost", BigDecimal.ZERO);

                    ArrayList<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(stockTransfer.getTransaction().getId());
                    if(!details.isEmpty()) {
                        for (ItemTransactionDetail detail : details) {

                            Item item = detail.getItem();

                            map.put("itemId", item.getId());
                            map.put("itemCode", item.getCode());
                            map.put("quantity", detail.getQuantity());
                            map.put("unitCost", detail.getUnitCost());
                            map.put("unit", detail.getItem().getUnit().getCode());
                            map.put("totalCost", detail.getTotalCost());
                            map.put("invLocId", stockTransfer.getFromInventoryLocation().getId());
                            map.put("invLocDesc", stockTransfer.getFromInventoryLocation().getDescription());

                            data.add(map);

                            map = new HashMap();
                        }
                    } else {
                        data.add(map);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public List<Map> getForStockReceiveSummary(String from, String to, Integer inventoryLocationId, Integer documentStatusId) {
        List<Map> data = new ArrayList<>();
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            List<StockReceive> list;

            if(inventoryLocationId > 0 && documentStatusId > 0) {
                list = stockReceiveRepo.findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusId(fromDate, toDate, inventoryLocationId, documentStatusId);
            } else if(inventoryLocationId > 0) {
                list = stockReceiveRepo.findByVoucherDateBetweenAndInventoryLocationId(fromDate, toDate, inventoryLocationId);
            } else if(documentStatusId > 0) {
                list = stockReceiveRepo.findByVoucherDateBetweenAndDocumentStatusId(fromDate, toDate, documentStatusId);
            } else {
                list = stockReceiveRepo.findByVoucherDateBetween(fromDate, toDate);
            }

            if(!list.isEmpty()) {
                for (StockReceive stockReceive: list) {

                    Map map = new HashMap();

                    map.put("voucherDate", stockReceive.getVoucherDate());
                    map.put("code", stockReceive.getCode());
                    map.put("description", stockReceive.getDescription());
                    map.put("status", stockReceive.getDocumentStatus().getStatus());
                    map.put("invLocId", stockReceive.getInventoryLocation().getId());
                    map.put("invLocDesc", stockReceive.getInventoryLocation().getDescription());
                    map.put("itemId", null);
                    map.put("itemCode", null);
                    map.put("quantity", BigDecimal.ZERO);
                    map.put("unitCost", BigDecimal.ZERO);
                    map.put("totalCost", BigDecimal.ZERO);

                    ArrayList<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(stockReceive.getTransaction().getId());
                    if(!details.isEmpty()) {
                        for (StockTransactionDetail detail : details) {

                            Item item = detail.getItemStock().getItem();

                            map.put("itemId", item.getId());
                            map.put("itemCode", item.getCode());
                            map.put("quantity", detail.getQuantity());
                            map.put("unitCost", detail.getUnitCost());
                            map.put("unit", item.getUnit().getCode());
                            map.put("totalCost", detail.getTotalCost());
                            map.put("invLocId", stockReceive.getInventoryLocation().getId());
                            map.put("invLocDesc", stockReceive.getInventoryLocation().getDescription());

                            data.add(map);

                            map = new HashMap();
                        }
                    } else {
                        data.add(map);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    // financial statements


    // private methods here
    private CashFlowStatementDetail cashFLowStatementBlankRow() {
        CashFlowStatementDetail detail = new CashFlowStatementDetail();
        detail.setAccount("");
        return detail;
    }

    private List<CommonRegisterDetail> makeCommonRegisterDetail(List<Object[]> rows, Boolean isMIR) {
        List<CommonRegisterDetail> data = new ArrayList<>();

        for (Object[] row : rows) {
            Integer jvId = Integer.parseInt(row[0].toString());
            String jvNumber = String.valueOf(row[1]);
            String explanation = String.valueOf(row[2]);
            Date voucherDate = (Date) row[3];
            Integer transId = Integer.parseInt(row[4].toString());
            String payee = String.valueOf(row[5]);
            InventoryDocType docType = null;
            String docId = null;

            List<Object[]> transactions;

            if (isMIR) {
                docType = InventoryDocType.valueOf(String.valueOf(row[5]));
                docId = (String.valueOf(row[6]) == "0") ? "" : String.valueOf(row[6]);

                transactions = generalLedgerRepo.findForRegisterByTransIdAndInvDocType(transId, String.valueOf(row[5]));
            } else {

                transactions = generalLedgerRepo.findForRegisterByTransId(transId);
            }

            int counter = 1;
            if (!Checker.collectionIsEmpty(transactions)) {
                for (Object[] detail : transactions) {
                    String segmentAccountCode = String.valueOf(detail[0]);
                    BigDecimal debit = (BigDecimal) detail[1];
                    BigDecimal credit = (BigDecimal) detail[2];
                    String title = String.valueOf(detail[3]);

                    CommonRegisterDetail jvDetail = new CommonRegisterDetail();
                    if (counter == 1) {
                        jvDetail.setReference(jvNumber);
                        jvDetail.setExplanation(explanation);
                        jvDetail.setVoucherDate(voucherDate);
                        jvDetail.setPayee(payee);
                    } else {
                        jvDetail.setReference(null);
                        jvDetail.setExplanation(explanation);
                        jvDetail.setVoucherDate(null);
                        jvDetail.setPayee(null);
                    }

                    jvDetail.setTitle(title);
                    jvDetail.setCode(segmentAccountCode);
                    jvDetail.setDebit(debit);
                    jvDetail.setCredit(credit);
                    jvDetail.setvDate(voucherDate);

                    // For MIR only.
                    if (isMIR && counter == 1) {
                        String docNumber = null;

                        switch (docType) {
                            case MCT:
                                docNumber = "MCT";
                                break;
                            case SA:
                                docNumber = "SA";
                                break;
                            case MCRT:
                                docNumber = "MCRT";
                                break;
                            case MST:
                                docNumber = "MST";
                                break;
                            case STR:
                                docNumber = "STR";
                                break;
                        }

                        jvDetail.setDocNumber((docId.isEmpty()) ? "" : docNumber + "-" + docId);
                    }

                    data.add(jvDetail);

                    counter++;
                }
            }
        }

        return data;
    }

    private List<CommonRegisterDetail> makeCommonRegisterDetail2(List<Object[]> rows, Boolean isMIR, DocumentType documentType) {
        List<CommonRegisterDetail> data = new ArrayList<>();

        try {

            for (Object[] row : rows) {
                Integer cvId = Integer.parseInt(row[0].toString());
                String reference = String.valueOf(row[1]);
                String explanation = String.valueOf(row[2]);
                Date voucherDate = (Date) row[3];
                Integer transId = Integer.parseInt(row[4].toString());
                String payee = row.length > 5 ? String.valueOf(row[5]) : null;
                String docNumber = null;

                List<Object[]> transactions;

                if (isMIR) {
                    docNumber = (String.valueOf(row[6]).equals("0")) || row[6] == null ? "" : String.valueOf(row[6]);

                    if(row[5] == null){ // for no inventory document selected
                        transactions = generalLedgerRepo.findForRegisterByTransId(transId);
                    }else{
                        transactions = generalLedgerRepo.findForRegisterByTransIdAndInvDocType(transId, String.valueOf(row[5]));
                    }

                } else {

                    transactions = generalLedgerRepo.findForRegisterByTransId(transId);
                }

                CommonRegisterDetail jvDetail = new CommonRegisterDetail();
                jvDetail.setReference(reference);
                jvDetail.setVoucherDate(voucherDate);
                jvDetail.setPayee(payee);

                if (!Checker.collectionIsEmpty(transactions)) {
                    String segmentAccountCode = "";
                    String debt = "";
                    String credt = "";
                    String title = "";
                    BigDecimal debits = BigDecimal.ZERO;
                    BigDecimal credits = BigDecimal.ZERO;

                    int transCounter = 0;
                    for (Object[] detail : transactions) {

                        DecimalFormat formatter = new DecimalFormat("#,##0.00");
                        BigDecimal debit = (BigDecimal) detail[1];
                        BigDecimal credit = (BigDecimal) detail[2];
                        String tit = String.valueOf(detail[3]);

                        segmentAccountCode += String.valueOf(detail[0]) + StringFormatter.blankOrNewLine(transCounter, transactions.size());
                        debt += debit != null && debit.compareTo(BigDecimal.ZERO) != 0 ? formatter.format(debit) + StringFormatter.blankOrNewLine(transCounter, transactions.size()) : StringFormatter.blankOrNewLine(transCounter, transactions.size());
                        credt += credit != null && credit.compareTo(BigDecimal.ZERO) != 0 ? formatter.format(credit) + StringFormatter.blankOrNewLine(transCounter, transactions.size()) : StringFormatter.blankOrNewLine(transCounter, transactions.size());
                        title += tit.length() > 35 ? tit.substring(0, 35) + "..." : tit;    // shorten title
                        title += "\n";
                        debits = debits.add(debit);
                        credits = credits.add(credit);

                        transCounter++;
                    }
                    jvDetail.setTitle(title);
                    jvDetail.setCode(segmentAccountCode);
                    jvDetail.setsDebit(debt);
                    jvDetail.setsCredit(credt);
                    jvDetail.setDebit(debits);
                    jvDetail.setCredit(credits);
                    jvDetail.setvDate(voucherDate);// For MIR only.

                    String bankChecks = "";
                    if(documentType.getId() == DocumentType.CV.getId()){

                        CheckVoucher checkVoucher = checkVoucherRepo.findOneByTransactionId(transId);

                        if(checkVoucher != null){

                            List<CheckVoucherCheque> cheques = checkVoucherChequeRepo.findByTransactionId(transId);
                            if(!cheques.isEmpty()){

                                for(CheckVoucherCheque cheque : cheques){

                                    BankAccount bankAccount = bankAccountRepo.findFirstByBankIdAndAccountId(checkVoucher.getBank().getId(), cheque.getAccount().getId());
                                    String bankDetail = "";
                                    if(bankAccount != null){
//                                        bankDetail = bankAccount.getBank().getName()+" ("+ bankAccount.getAccountNumber() +") ";
                                        bankDetail = bankAccount.getBank().getName()+" ("+ bankAccount.getAccountNumber() +") ";
                                    }

                                    if(bankChecks.length() > 0){

                                        bankChecks += "\n"+bankDetail + cheque.getCheckNumber();


                                    } else {

                                        bankChecks = bankDetail + cheque.getCheckNumber();

                                    }
                                }
                            }

                        }

                    }

                    if(explanation.length() > 0){
                        explanation += "\n"+bankChecks;
                    } else {
                        explanation += bankChecks;
                    }

                    jvDetail.setExplanation(explanation);

                    if (isMIR) {
                        jvDetail.setDocNumber(docNumber);
                    }
                }
                if(isMIR) {
                    jvDetail.setvDate(voucherDate);// For MIR with no transactions only.
                }
                data.add(jvDetail);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    private List<RegisterRecapDetail> makeRegisterRecapDetail(List<Object[]> rows) {
        List<RegisterRecapDetail> details = new ArrayList<>();

        if (!Checker.collectionIsEmpty(rows)) {
            String prevGLAccountCode = "";

            for (Object[] row : rows) {
                BigDecimal glDebit = (BigDecimal) row[0];
                BigDecimal glCredit = (BigDecimal) row[1];
                BigDecimal slDebit = (BigDecimal) row[3];
                BigDecimal slCredit = (BigDecimal) row[4];
                String accountNo = String.valueOf(row[5]);
                String glAccountCode = (String) row[6];
                String glAccountTitle = (String) row[7];
                String slEntityName = (String) row[8];
                boolean isSubs =  ((int)row[10] == 1);
                if (isSubs && prevGLAccountCode.equals(glAccountCode)) {
                    glAccountCode = "";
                    glAccountTitle = "";
                    glDebit = BigDecimal.ZERO;
                    glCredit = BigDecimal.ZERO;
                }

                RegisterRecapDetail detail = new RegisterRecapDetail();
                detail.setSlAccountTitle(slEntityName);
                detail.setSlAccountCode(accountNo == "null" ? "" : accountNo);
                detail.setSlDebit(slDebit);
                detail.setSlCredit(slCredit);

                detail.setGlAccountTitle(glAccountTitle);
                detail.setGlAccountCode(glAccountCode);
                detail.setGlDebit(glDebit);
                detail.setGlCredit(glCredit);

                details.add(detail);
                prevGLAccountCode = glAccountCode;

//                if(!glAccountCode.equals("")) prevGLAccountCode = glAccountCode;
            }
        }

        return details;
    }

    private List<CommonSummaryDetail> makeCommonSummaryDetail(List<Object[]> rows, Integer docTypeId) {
        List<CommonSummaryDetail> data = new ArrayList<>();

        for (Object[] row : rows) {
            CommonSummaryDetail detail = new CommonSummaryDetail();
            String code;
            String particulars;
            Date voucherDate;
            String supplier;
            Integer term;
            BigDecimal amount;
            BigDecimal adjustment;
            BigDecimal netAmount;
            Integer noOfItems;
            BigDecimal balance;
            Integer noOfMonths;
            String requestedBy;
            String status;

            if (docTypeId == DocumentType.PO.getId()) {
                code = String.valueOf(row[1]);
                voucherDate = (Date) row[2];
                supplier = String.valueOf(row[3]);
                term = Integer.parseInt(row[4].toString());
                amount = new BigDecimal(row[5].toString());
                noOfItems = Integer.parseInt(row[6].toString());
                status = String.valueOf(row[7]);
                detail.setReference(code);
                detail.setVoucherDate(voucherDate);
                detail.setSupplier(supplier);
                detail.setTerm(term);
                detail.setAmount(amount);
                detail.setNoOfItems(noOfItems);
                detail.setStatus(status);
            } else if (docTypeId == DocumentType.JO.getId()) {
                code = String.valueOf(row[1]);
                particulars = String.valueOf(row[2]);
                voucherDate = (Date) row[3];
                supplier = String.valueOf(row[4]);
                term = Integer.parseInt(row[5].toString());
                amount = new BigDecimal(row[6].toString());
                noOfItems = Integer.parseInt(row[7].toString());
                status = String.valueOf(row[8]);
                detail.setReference(code);
                detail.setVoucherDate(voucherDate);
                detail.setParticulars(particulars);
                detail.setSupplier(supplier);
                detail.setTerm(term);
                detail.setAmount(amount);
                detail.setNoOfItems(noOfItems);
                detail.setStatus(status);
            } else if (docTypeId == DocumentType.JOA.getId()) {
                code = String.valueOf(row[1]);
                voucherDate = (Date) row[2];
                supplier = String.valueOf(row[3]);
                amount = new BigDecimal(row[4].toString());
                adjustment = new BigDecimal(row[5].toString());
                netAmount = new BigDecimal(row[6].toString());
                status = String.valueOf(row[7]);
                detail.setReference(code);
                detail.setVoucherDate(voucherDate);
                detail.setSupplier(supplier);
                detail.setAmount(amount);
                detail.setAdjustment(adjustment);
                detail.setNetAmount(netAmount);
                detail.setStatus(status);
            } else if (docTypeId == DocumentType.RV.getId()) {
                code = String.valueOf(row[1]);
                particulars = String.valueOf(row[2]);
                voucherDate = (Date) row[3];
                Date requiredDate = (Date) row[4];
                noOfItems = Integer.parseInt(row[5].toString());
                requestedBy = String.valueOf(row[6]);
                status = String.valueOf(row[7]);
                detail.setReference(code);
                detail.setVoucherDate(voucherDate);
                detail.setParticulars(particulars);
                detail.setRequiredDate(requiredDate);
                detail.setNoOfItems(noOfItems);
                detail.setRequestedBy(requestedBy);
                detail.setStatus(status);
            } else if (docTypeId == DocumentType.CF.getId()) {
                code = String.valueOf(row[1]);
                particulars = String.valueOf(row[2]);
                voucherDate = (Date) row[3];
                noOfItems = Integer.parseInt(row[4].toString());
                requestedBy = String.valueOf(row[5]);
                status = String.valueOf(row[6]);
                detail.setReference(code);
                detail.setParticulars(particulars);
                detail.setVoucherDate(voucherDate);
                detail.setNoOfItems(noOfItems);
                detail.setRequestedBy(requestedBy);
                detail.setStatus(status);
            } else if (docTypeId == DocumentType.PR.getId()) {
                code = String.valueOf(row[1]);
                voucherDate = (Date) row[2];
                supplier = String.valueOf(row[3]);
                amount = new BigDecimal(row[4].toString());
                adjustment = new BigDecimal(row[5].toString());
                netAmount = new BigDecimal(row[6].toString());
                status = String.valueOf(row[7]);
                detail.setReference(code);
                detail.setVoucherDate(voucherDate);
                detail.setSupplier(supplier);
                detail.setAmount(amount);
                detail.setAdjustment(adjustment);
                detail.setNetAmount(netAmount);
                detail.setStatus(status);
            }
            data.add(detail);
        }

        return data;
    }

    private List<CommonSummaryDetail> makeSummaryDetailForPrepayment(List<Object[]> rows) {
        List<CommonSummaryDetail> data = new ArrayList<>();

        for (Object[] row : rows) {
            CommonSummaryDetail detail = new CommonSummaryDetail();
            String code = String.valueOf(row[0]);
            String particulars = String.valueOf(row[1]);
            BigDecimal amount = new BigDecimal(row[3].toString());
            BigDecimal balance = new BigDecimal(row[4].toString());
            Integer noOfMonths = Integer.parseInt(row[2].toString());

            detail.setReference(code);
            detail.setParticulars(particulars);
            detail.setNoOfMonths(noOfMonths);
            detail.setAmount(amount);
            detail.setBalance(balance);

            data.add(detail);
        }

        return data;
    }

    private List<Map> makeRegisterRecapForCrv(List<Object[]> glRows) {
        List<RegisterRecapDetail> details = new ArrayList<>();

        List<Map> maps = new ArrayList<>();

        try {

            for (Object[] glRow : glRows) {

                Map glMap = new HashMap();

                String accountCode = (String) glRow[0];
                String accountTitle = (String) glRow[1];
                BigDecimal glTotalDebit = (BigDecimal) glRow[2];
                BigDecimal glTotalCredit = (BigDecimal) glRow[3];
                Integer glId = (Integer) glRow[4];

                glMap.put("accountCode", accountCode);
                glMap.put("accountTitle", accountTitle);
                glMap.put("glTotalDebit", glTotalDebit);
                glMap.put("glTotalCredit", glTotalCredit);

                boolean isDrGreaterThanCr = (glTotalDebit.subtract(glTotalCredit)).compareTo(BigDecimal.ZERO) > 0;

                if(isDrGreaterThanCr) {
                    glMap.put("netTotalDebit", glTotalDebit.subtract(glTotalCredit));
                } else {
                    glMap.put("netTotalDebit", BigDecimal.ZERO);
                }

                if(isDrGreaterThanCr) {
                    glMap.put("netTotalCredit", BigDecimal.ZERO);
                } else {
                    glMap.put("netTotalCredit", glTotalCredit.subtract(glTotalDebit));
                }

                List<Object[]> subLedgers = cashReceiptsRepo.findForCrvRecapSL(glId);

                BigDecimal totalSLDebit = BigDecimal.ZERO;
                BigDecimal totalSLCredit = BigDecimal.ZERO;

                if(Checker.collectionIsNotEmpty(subLedgers)){

                    List<Map> slMaps = new ArrayList<>();

                    for(Object[] subLedger : subLedgers){

                        Map slMap = new HashMap();

                        Integer accountNo = (Integer) subLedger[2];
                        String accountName = (String) subLedger[3];
                        BigDecimal slDebit = (BigDecimal) subLedger[4];
                        BigDecimal slCredit = (BigDecimal) subLedger[5];

                        slMap.put("accountNo", accountNo);
                        slMap.put("accountName", accountName);
                        slMap.put("slDebit", slDebit);
                        slMap.put("slCredit", slCredit);

//                        String slAccountCode = (String) subLedger[6];
//                        slMap.put("accountCode", slAccountCode);

                        totalSLDebit = totalSLDebit.add(slDebit);
                        totalSLCredit = totalSLCredit.add(slCredit);

                        slMaps.add(slMap);

                    }

                    glMap.put("slMaps", slMaps);

                }

                glMap.put("totalSLDebit", totalSLDebit);
                glMap.put("totalSLCredit", totalSLCredit);

                maps.add(glMap);

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return maps;

    }
}