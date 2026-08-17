package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Personal on 11/9/2015.
 */
public class PrepaymentDetailDto {
    private Integer id;
    private Integer ppId;
    private Integer accountNo;
    private Integer year;
    private Integer month;
    private BigDecimal amount;
    private BigDecimal balance;
    private Date processDate;

    public PrepaymentDetailDto() {
    }

    public PrepaymentDetailDto(Integer id, Integer ppId, Integer accountNo, Integer year, Integer month, BigDecimal amount, BigDecimal balance, Date processDate) {
        this.id = id;
        this.ppId = ppId;
        this.accountNo = accountNo;
        this.year = year;
        this.month = month;
        this.amount = amount;
        this.balance = balance;
        this.processDate = processDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPpId() {
        return ppId;
    }

    public void setPpId(Integer ppId) {
        this.ppId = ppId;
    }

    public Integer getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(Integer accountNo) {
        this.accountNo = accountNo;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Date getProcessDate() {
        return processDate;
    }

    public void setProcessDate(Date processDate) {
        this.processDate = processDate;
    }
}
