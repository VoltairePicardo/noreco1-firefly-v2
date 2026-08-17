package com.noreco1.fireflyv2.common.facade;

import com.noreco1.fireflyv2.model.*;

import java.util.Map;

/**
 * Created by TSI Admin on 10/8/2015.
 */
public interface DocumentLoggerFacade {
    DocumentLog log(Transaction transaction, User user, Map oldMap, Map newMap);
    DocumentLog update(DocumentLog documentLog, Map oldMap, Map newMap);
    Map getLedgerAndFileLog(Map mainLogMap, Integer transId);

    Map makeLog(PurchaseRequest purchaseRequest);
    Map makeLog(PurchaseOrder po);
    Map makeLog(AccountsPayableVoucher voucher);
    Map makeLog(CheckVoucher voucher);
    Map makeLog(JournalVoucher voucher);
    Map makeLog(CashReceipts voucher);
    Map makeLog(JobOrder jobOrder);
    Map makeLog(SalesVoucher voucher);
    Map makeLog(PettyCashTrans voucher);
    Map makeLog(CashAdvance voucher);
    Map makeLog(CashAdvanceLiquidation voucher);
    Map makeLog(MaterialIssueRegister voucher);
    Map makeLog(Canvass voucher);
    Map makeLog(JoAcceptance voucher);
    Map makeLog(PaymentRequest voucher);
//    Map makeLog(BankDeposit bankDeposit);
    Map makeLog(Budget budget);
    Map makeLog(AdjustmentJournal voucher);
    Map makeLog(ReceivingReport voucher);
    Map makeLog(StockWithdrawal voucher);
    Map makeLog(StockRelease voucher);
    Map makeLog(MaterialCreditTicket voucher);
    Map makeLog(StockAdjustment voucher);
    Map makeLog(MaterialSalvageTicket voucher);
    Map makeLog(StockTransfer voucher);
    Map makeLog(StockReceive voucher);
    Map makeLog(Quotation quotation);
    Map makeLog(Project project);
    Map makeLog(SiteInspectionReport siteInspectionReport);
    Map makeLog(CostEstimate costEstimate);
    Map makeLog(BillOfMaterial billOfMaterial);
    Map makeLog(ProjectAcceptanceReport projectAcceptanceReport);
    Map makeLog(ProjectAcceptanceCertification projectAcceptanceCertification);
    Map makeLog(MemorandumReceipt memorandumReceipt);
    Map makeLog(ItemsForRepair itemsForRepair);
    Map makeLog(BudgetLineItem budgetLineItem);
    Map makeLog(PettyCashLiquidation pettyCashLiquidation);
    Map makeLog(ReturnMemorandumReceipt returnMemorandumReceipt);
    Map makeLog(ReleasedCheque releasedCheque);
    Map makeLog(CreditCardPurchaseRequest creditCardPurchaseRequest);

    SpecialEquipmentAssignmentLog log(SpecialEquipmentAssignment specialEquipmentAssignment, User user);
    void budgetLineItemDetailLog(BudgetLineItemDetail budgetLineItemDetail, User user);
}
