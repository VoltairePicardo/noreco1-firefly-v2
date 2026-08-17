package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.*;

import java.util.Date;
import java.util.List;

/**
 * Created by Tri-Nvent on 3/27/2020.
 */
public class MemorandumReceiptDto {

    private Integer id;
    private String code;
    private Date date;
    private SlEntity employee;
    private Office office;
    private StockWithdrawalDto stockWithdrawal;
    private ReturnMemorandumReceiptDto returnMemorandumReceipt;
    private User user;
    private User approvingOfficer;
    private List<MemorandumReceiptDetail> memorandumReceiptDetails;
    private boolean hasMst;
    private Transaction transaction;
    private DocumentStatus documentStatus;
    private Workflow workflow;

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

    public List<MemorandumReceiptDetail> getMemorandumReceiptDetails() {
        return memorandumReceiptDetails;
    }

    public void setMemorandumReceiptDetails(List<MemorandumReceiptDetail> memorandumReceiptDetails) {
        this.memorandumReceiptDetails = memorandumReceiptDetails;
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

    public boolean isHasMst() {
        return hasMst;
    }

    public void setHasMst(boolean hasMst) {
        this.hasMst = hasMst;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public User getApprovingOfficer() {
        return approvingOfficer;
    }

    public void setApprovingOfficer(User approvingOfficer) {
        this.approvingOfficer = approvingOfficer;
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

    public ReturnMemorandumReceiptDto getReturnMemorandumReceipt() {
        return returnMemorandumReceipt;
    }

    public void setReturnMemorandumReceipt(ReturnMemorandumReceiptDto returnMemorandumReceipt) {
        this.returnMemorandumReceipt = returnMemorandumReceipt;
    }
}
