package com.noreco1.fireflyv2.controller.form;

import com.noreco1.fireflyv2.model.DocumentType;
import com.noreco1.fireflyv2.model.Prepayment;
import com.noreco1.fireflyv2.model.Transaction;

import java.math.BigDecimal;

public class PrepaymentVoucherLinkForm {

    private Prepayment prepayment;
    private BigDecimal totalCost = BigDecimal.ZERO;
    private BigDecimal monthlyCost = BigDecimal.ZERO;
    private BigDecimal balance;
    private Transaction voucherTransaction;
    private DocumentType documentType;

    public PrepaymentVoucherLinkForm() {
    }

    public PrepaymentVoucherLinkForm(Prepayment prepayment, BigDecimal totalCost, BigDecimal monthlyCost,
                                     BigDecimal balance, Transaction voucherTransaction, DocumentType documentType) {
        this.prepayment = prepayment;
        this.totalCost = totalCost;
        this.monthlyCost = monthlyCost;
        this.balance = balance;
        this.voucherTransaction = voucherTransaction;
        this.documentType = documentType;
    }

    public Prepayment getPrepayment() {
        return prepayment;
    }

    public void setPrepayment(Prepayment prepayment) {
        this.prepayment = prepayment;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getMonthlyCost() {
        return monthlyCost;
    }

    public void setMonthlyCost(BigDecimal monthlyCost) {
        this.monthlyCost = monthlyCost;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Transaction getVoucherTransaction() {
        return voucherTransaction;
    }

    public void setVoucherTransaction(Transaction voucherTransaction) {
        this.voucherTransaction = voucherTransaction;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }
}
