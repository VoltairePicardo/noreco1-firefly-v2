package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;

/**
 * Created by Personal on 9/7/2016.
 */
public class ReplenishmentDto {
    private Integer id;
    private Integer transId;
    private Integer accountNo;
    private BigDecimal checkAmount;

    public ReplenishmentDto() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTransId() {
        return transId;
    }

    public void setTransId(Integer transId) {
        this.transId = transId;
    }

    public Integer getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(Integer accountNo) {
        this.accountNo = accountNo;
    }

    public BigDecimal getCheckAmount() {
        return checkAmount;
    }

    public void setCheckAmount(BigDecimal checkAmount) {
        this.checkAmount = checkAmount;
    }
}
