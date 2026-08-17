package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.Employee;
import com.noreco1.fireflyv2.model.SlEntity;
import com.noreco1.fireflyv2.model.Transaction;
import com.noreco1.fireflyv2.model.User;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Tri-Nvent on 1/8/2020.
 */
public class CashAdvanceDto {

    private Integer id;
    private String code;
    private Date voucherDate;
    private BigDecimal amount;
    private Employee employee;
    private Date cashAdvanceDate;
    private Integer accountNo;
    private String purpose;
    private String remarks;
    private Byte type;
    private User notedBy;
    private User approvingOfficer;
    private Transaction transaction;

    public CashAdvanceDto() {}

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

    public Date getVoucherDate() {
        return voucherDate;
    }

    public void setVoucherDate(Date voucherDate) {
        this.voucherDate = voucherDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Date getCashAdvanceDate() {
        return cashAdvanceDate;
    }

    public void setCashAdvanceDate(Date cashAdvanceDate) {
        this.cashAdvanceDate = cashAdvanceDate;
    }

    public Integer getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(Integer accountNo) {
        this.accountNo = accountNo;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public User getNotedBy() {
        return notedBy;
    }

    public void setNotedBy(User notedBy) {
        this.notedBy = notedBy;
    }

    public User getApprovingOfficer() {
        return approvingOfficer;
    }

    public void setApprovingOfficer(User approvingOfficer) {
        this.approvingOfficer = approvingOfficer;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}
