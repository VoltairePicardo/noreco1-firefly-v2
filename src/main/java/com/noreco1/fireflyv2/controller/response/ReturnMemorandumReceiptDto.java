package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.*;

import java.util.Date;
import java.util.List;

public class ReturnMemorandumReceiptDto {

    private Integer id;
    private String code;
    private Date date;
    private SlEntity employee;
    private Office office;
    private StockWithdrawalDto stockWithdrawal;
    private User user;
    private List<ReturnMemorandumReceiptDetail> returnMemorandumReceiptDetails;
    private Transaction transaction;
    private DocumentStatus documentStatus;
    private Workflow workflow;
    private MemorandumReceipt memorandumReceipt;
    private String remarks;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public SlEntity getEmployee() {
        return employee;
    }

    public void setEmployee(SlEntity employee) {
        this.employee = employee;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public StockWithdrawalDto getStockWithdrawal() {
        return stockWithdrawal;
    }

    public void setStockWithdrawal(StockWithdrawalDto stockWithdrawal) {
        this.stockWithdrawal = stockWithdrawal;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<ReturnMemorandumReceiptDetail> getReturnMemorandumReceiptDetails() {
        return returnMemorandumReceiptDetails;
    }

    public void setReturnMemorandumReceiptDetails(List<ReturnMemorandumReceiptDetail> returnMemorandumReceiptDetails) {
        this.returnMemorandumReceiptDetails = returnMemorandumReceiptDetails;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public DocumentStatus getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(DocumentStatus documentStatus) {
        this.documentStatus = documentStatus;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public MemorandumReceipt getMemorandumReceipt() {
        return memorandumReceipt;
    }

    public void setMemorandumReceipt(MemorandumReceipt memorandumReceipt) {
        this.memorandumReceipt = memorandumReceipt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
