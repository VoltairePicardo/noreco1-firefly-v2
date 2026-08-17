package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;

/**
 * Created by TSI Admin.
 */
public class PettyCashTransDetailDto {
    private Integer id;
    private Integer pettyCashTransId;
    private Integer expenseId;
    private BigDecimal expenseAmount;
    private Integer segmentAccountId;
    private String remarks;
    private BigDecimal balance;

    public PettyCashTransDetailDto() {
    }

    public PettyCashTransDetailDto(Integer id, Integer pettyCashTransId, Integer expenseId, BigDecimal expenseAmount, Integer segmentAccountId, String remarks, BigDecimal balance) {
        this.id = id;
        this.pettyCashTransId = pettyCashTransId;
        this.expenseId = expenseId;
        this.expenseAmount = expenseAmount;
        this.segmentAccountId = segmentAccountId;
        this.remarks = remarks;
        this.balance = balance;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPettyCashTransId() {
        return pettyCashTransId;
    }

    public void setPettyCashTransId(Integer pettyCashTransId) {
        this.pettyCashTransId = pettyCashTransId;
    }

    public Integer getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(Integer expenseId) {
        this.expenseId = expenseId;
    }

    public BigDecimal getExpenseAmount() {
        return expenseAmount;
    }

    public void setExpenseAmount(BigDecimal expenseAmount) {
        this.expenseAmount = expenseAmount;
    }

    public Integer getSegmentAccountId() {
        return segmentAccountId;
    }

    public void setSegmentAccountId(Integer segmentAccountId) {
        this.segmentAccountId = segmentAccountId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
