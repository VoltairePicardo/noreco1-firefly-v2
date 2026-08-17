package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.Account;
import com.noreco1.fireflyv2.model.SegmentAccount;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Personal on 7/7/2015.
 */
public class OtherDepositDto {
    private Integer id;
    private Account account;
    private BigDecimal amount = BigDecimal.ZERO;
    private Date voucherDate;
    private String localCode;
    private String particulars;
    private Integer transId;
    private Date lastUpdated;
    private Date created;

    public OtherDepositDto() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getVoucherDate() {
        return voucherDate;
    }

    public void setVoucherDate(Date voucherDate) {
        this.voucherDate = voucherDate;
    }

    public String getLocalCode() {
        return localCode;
    }

    public void setLocalCode(String localCode) {
        this.localCode = localCode;
    }

    public Integer getTransId() {
        return transId;
    }

    public void setTransId(Integer transId) {
        this.transId = transId;
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

    public String getParticulars() {
        return particulars;
    }

    public void setParticulars(String particulars) {
        this.particulars = particulars;
    }
}
