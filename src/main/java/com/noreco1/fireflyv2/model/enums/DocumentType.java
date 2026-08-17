package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum DocumentType {

    CASHFLOW_STATEMENT(30, "CASHFLOW_STATEMENT", "Statement of Cashflow", "SETUP", false),
    BIR_FORM_1601E(30, "BIR_FORM_1601E", "Bir Form 1601-E", "SETUP", false),
    RV(1, "RV", "Purchase or Work Request", "PURCHASING", true),
    CF(20, "CF", "Canvass", "PURCHASING", true),
    QUOTATION_SUMMARY(32, "QUOTATION_SUMMARY", "Summary of Quotation", "PURCHASING", true),
    PO(2, "PO", "Purchase Order", "PURCHASING", true),
    JO(10, "JO", "Job Order", "PURCHASING", true),
    JOA(21, "JOA", "JO Acceptance", "PURCHASING", true),
    PR(22, "PR", "Payment Request", "PURCHASING", false),
    RFP(22, "PR", "Request for Payment", "PURCHASING", true),
    CCPR(512, "CCPR", "Credit Card Purchase Request", "PURCHASING", true),
    APV(4, "APV", "Accounts Payable", "ACCOUNTING CORE", true),
    CV(5, "CV", "Check Voucher", "ACCOUNTING CORE", true),
    JV(6, "JV", "Journal Voucher", "ACCOUNTING CORE", true),
    CRV(9, "CRV", "Cash Receipts", "ACCOUNTING CORE", true),
    SV(13, "SV", "Sales Voucher", "ACCOUNTING CORE", true),
    BAD(23, "BAD", "Bank Deposits", "ACCOUNTING CORE", false),
    AJ(28, "AJ", "Adjustment Journal", "ACCOUNTING CORE", true),
    MR(19, "MR", "Material Issue Voucher", "ACCOUNTING CORE", true),
    OAR(29, "OAR", "Other Accounts Receivable", "ACCOUNTING CORE", false),
    WF(14, "WF", "Working Fund Voucher", "SUPPORT MODULE", false),
    WP(15, "WP", "Work In Process", "SUPPORT MODULE", false),
    CA(16, "CA", "Cash Advance", "SUPPORT MODULE", true),
    BUDG(24, "BUDG", "Budget", "SUPPORT MODULE", false),
    Budget(24, "BUDG", "Budget", "SUPPORT MODULE", true),
    PCV(25, "PCV", "Petty Cash Voucher", "SUPPORT MODULE", true),
    DEPRECIATION(26, "DEPRECIATION", "Depreciation", "SUPPORT MODULE", false),
    BUDGET_LINE_ITEM(507, "BUDGET_LINE_ITEM", "Budget Line Item", "SUPPORT MODULE", false),
    PETTY_CASH_LIQUIDATION(511, "PETTY_CASH_LIQUIDATION", "Petty Cash Liquidation", "SUPPORT MODULE", false),
    CAL(508, "CAL", "Cash Advance Liquidation", "SUPPORT MODULE", true),
    PCV_SUMMARY(27, "PCV_SUMMARY", "Petty Cash Voucher Summary", "REPORTS", false),
    GL_INQUIRY_SUMMARY(31, "GL_INQUIRY_SUMMARY", "GL Inquiry Summary", "REPORTS", false),
    RR(3, "RR", "Receiving Report", "INVENTORY", true),
    SW(8, "SW", "Stock Withdrawal", "INVENTORY", true),
    SRL(11, "SRL", "Stock Release", "INVENTORY", true),
    MCT(33, "MCT", "Material Credit Ticket", "INVENTORY", true),
    MST(34, "MST", "Material Salvage Ticket", "INVENTORY", true),
    SA(18, "SA", "Stock Adjustment", "INVENTORY", true),
    ST(35, "ST", "Stock Transfer", "INVENTORY", true),
    SRC(36, "SRC", "Stock Receive", "INVENTORY", true),
    MRTE(509, "MRTE", "Memorandum Receipt", "INVENTORY", true),
    RMRTE(510, "RMRTE", "Return Memorandum Receipt", "INVENTORY", true),
    SRL_OFE_OSSP(37, "SRL_OFE_OSSP", "Stock Release OFE & OSSP", "INVENTORY", true),
    IFR(46, "IFR", "Items For Repair", "INVENTORY", true),
    SITE_INSPECTION_REPORT(39, "SITE_INSPECTION_REPORT", "Site Inspection Report", "WORK ORDER", true),
    CE(40, "CE", "Cost Estimate", "WORK ORDER", false),
    PROJECT(41, "PROJECT", "Project", "WORK ORDER", true),
    PROJECT_ACCEPTANCE(42, "PROJECT_ACCEPTANCE", "Project Acceptance Report", "WORK ORDER", true),
    PROJECT_ACCEPTANCE_CERTIFICATION(43, "PROJECT_ACCEPTANCE_CERTIFICATION", "Project Acceptance Certification", "WORK ORDER", true),
    ASSET_RETIREMENT(45, "ASSET_RETIREMENT", "Asset Retirement", "SUPPORT MODULE", false),
    BOM(512, "BOM", "Bill of Materials", "WORK ORDER", false);

    private int id;
    private String description;
    private String code;
    private String module;
    private boolean isCancellable;

    public static DocumentType typeFromInt(int id) {
        for (DocumentType type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.description;
    }
}
