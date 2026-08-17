package com.noreco1.fireflyv2.common.facade;

import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.enums.DocumentType;

import java.util.Map;

/**
 * Created by TSI Admin on 8/11/2015.
 */
public interface SignatoryFacade {
    void apv(AccountsPayableVoucher apv);
    void cv(CheckVoucher cv);
    void jv(JournalVoucher jv);
    void rv(PurchaseRequest rv);
    void po(PurchaseOrder po);
    void quotation(Quotation quotation);
    void jo(JobOrder jo);
    void joAcceptance(JoAcceptance joAcceptance);
    void ca(CashAdvance cashAdvance);
    void cal(CashAdvanceLiquidation cashAdvanceLiquidation);
    void pr(PaymentRequest paymentRequest);
    void crv(CashReceipts cashReceipts);
    void sv(SalesVoucher salesVoucher); // energy sales
//    void bad(BankDeposit bankDeposit);
    void budget(Budget budget);
    void pcv(PettyCashTrans pettyCashVoucher);
    void pettyCashLiquidation(PettyCashLiquidation pettyCashLiquidation);
    void miv(MaterialIssueRegister issueRegister);
    void pcvSummary(Integer checkedByAcctNo, Integer replenishedByAcctNo, User createdBy);
    void cashflowStatement(Integer checkedByAcctNo, Integer notedByAcctNo, User createdBy);
    Map defaultSignatories(DocumentType documentType);
//    void canvass(Canvass canvass);
    void aj(AdjustmentJournal aj);
    void birForm1601E(Integer accountNo, User createdBy);
    void rr(ReceivingReport rr);
    void glInquirySummary(Integer accountNo, User createdBy);
    void sw(StockWithdrawal withdrawal);
    void sr(StockRelease stockRelease);
    void mct(MaterialCreditTicket materialCreditTicket);
    void sa(StockAdjustment stockAdjustment);
    void mst(MaterialSalvageTicket materialSalvageTicket);
    void st(StockTransfer stockTransfer);
    void src(StockReceive stockReceive);
    void summaryOfQuotation(Integer validatedByAcctNo, Integer approvedByAccountNo, User createdBy);
    void ce(CostEstimate costEstimate);
    void bom(BillOfMaterial billOfMaterial);
    void siteInspectionReport(SiteInspectionReport report);
    void projectAcceptanceReport(ProjectAcceptanceReport projectAcceptanceReport);
    void projectAcceptanceCertification(ProjectAcceptanceCertification projectAcceptanceCertification);
    void budgetLineItem(BudgetLineItem budgetLineItem);
    void memorandumReceipt(MemorandumReceipt memorandumReceipt);
    void creditCardPurchaseRequest(CreditCardPurchaseRequest creditCardPurchaseRequest);
    Map defaultSignatories(DocumentType documentType, Map params);
}
