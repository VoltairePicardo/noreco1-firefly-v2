package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;

/**
 * Created by lenovo on 9/10/2015.
 */
public class BankDepositDetailDto {
    private Integer transactionId;
    private Integer bankDepositId;
    private Integer segmentAccountId;
    private BigDecimal amount;
    private Boolean cleared;

    public BankDepositDetailDto() {
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getBankDepositId() {
        return bankDepositId;
    }

    public void setBankDepositId(Integer bankDepositId) {
        this.bankDepositId = bankDepositId;
    }

    public Integer getSegmentAccountId() {
        return segmentAccountId;
    }

    public void setSegmentAccountId(Integer segmentAccountId) {
        this.segmentAccountId = segmentAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Boolean getCleared() {
        return cleared;
    }

    public void setCleared(Boolean cleared) {
        this.cleared = cleared;
    }
}
