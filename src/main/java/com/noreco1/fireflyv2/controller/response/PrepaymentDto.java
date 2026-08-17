package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by Personal on 6/3/2015.
 */
public class PrepaymentDto {
    private Integer id;
    private Date datePaid;
    private Integer accountNo;
    private String description;
    private String code;
    private SlEntity createdBy;
    private SlEntity approvedBy;
    private Account prepaymentAccount;
    private Account expenseAccount;
    private BigDecimal totalCost = BigDecimal.ZERO;
    private Integer noOfMonths;
    private BigDecimal monthlyCost = BigDecimal.ZERO;
    private BigDecimal appliedCost = BigDecimal.ZERO;
    private BigDecimal balance = BigDecimal.ZERO;
    private Integer transId;
    private DocumentStatus documentStatus;
    private Date lastUpdated;
    private Date created;
    private TemporaryBatch temporaryBatch;
    private String startMonthStr;
    private Integer startMonth;
    private Integer startYear;
    private boolean hasPpd;

    public PrepaymentDto() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDatePaid() {
        return datePaid;
    }

    public void setDatePaid(Date datePaid) {
        this.datePaid = datePaid;
    }

    public Integer getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(Integer accountNo) {
        this.accountNo = accountNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SlEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(SlEntity createdBy) {
        this.createdBy = createdBy;
    }

    public SlEntity getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(SlEntity approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Account getPrepaymentAccount() {
        return prepaymentAccount;
    }

    public void setPrepaymentAccount(Account prepaymentAccount) {
        this.prepaymentAccount = prepaymentAccount;
    }

    public Account getExpenseAccount() {
        return expenseAccount;
    }

    public void setExpenseAccount(Account expenseAccount) {
        this.expenseAccount = expenseAccount;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public Integer getNoOfMonths() {
        return noOfMonths;
    }

    public void setNoOfMonths(Integer noOfMonths) {
        this.noOfMonths = noOfMonths;
    }

    public BigDecimal getMonthlyCost() {
        return monthlyCost;
    }

    public void setMonthlyCost(BigDecimal monthlyCost) {
        this.monthlyCost = monthlyCost;
    }

    public BigDecimal getAppliedCost() {
        return appliedCost;
    }

    public void setAppliedCost(BigDecimal appliedCost) {
        this.appliedCost = appliedCost;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Integer getTransId() {
        return transId;
    }

    public void setTransId(Integer transId) {
        this.transId = transId;
    }

    public DocumentStatus getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(DocumentStatus documentStatus) {
        this.documentStatus = documentStatus;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public TemporaryBatch getTemporaryBatch() {
        return temporaryBatch;
    }

    public void setTemporaryBatch(TemporaryBatch temporaryBatch) {
        this.temporaryBatch = temporaryBatch;
    }

    public String getStartMonthStr() {
        return startMonthStr;
    }

    public void setStartMonthStr(String startMonthStr) {
        this.startMonthStr = startMonthStr;
    }

    public Integer getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(Integer startMonth) {
        this.startMonth = startMonth;
    }

    public Integer getStartYear() {
        return startYear;
    }

    public void setStartYear(Integer startYear) {
        this.startYear = startYear;
    }

    public boolean isHasPpd() {
        return hasPpd;
    }

    public void setHasPpd(boolean hasPpd) {
        this.hasPpd = hasPpd;
    }
}
