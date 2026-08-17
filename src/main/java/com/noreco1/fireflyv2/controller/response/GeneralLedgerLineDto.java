package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;

/**
 * Created by TSI Admin on 4/24/2015.
 */
public class GeneralLedgerLineDto {
    private Integer id;
    private Integer accountId;
    private Integer segmentAccountId;
    private String segmentAccountCode;
    private String description;
    private String checkNumber;
    private BigDecimal debit = BigDecimal.ZERO;
    private BigDecimal credit = BigDecimal.ZERO;
    private Boolean hasSL = false;

    public GeneralLedgerLineDto() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Integer getSegmentAccountId() {
        return segmentAccountId;
    }

    public void setSegmentAccountId(Integer segmentAccountId) {
        this.segmentAccountId = segmentAccountId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getDebit() {
        return debit;
    }

    public void setDebit(BigDecimal debit) {
        this.debit = debit;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }

    public String getSegmentAccountCode() {
        return segmentAccountCode;
    }

    public void setSegmentAccountCode(String segmentAccountCode) {
        this.segmentAccountCode = segmentAccountCode;
    }

    public String getCode() {
        return segmentAccountCode;
    }

    public Boolean getHasSL() {
        return hasSL;
    }

    public void setHasSL(Boolean hasSL) {
        this.hasSL = hasSL;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }
}
