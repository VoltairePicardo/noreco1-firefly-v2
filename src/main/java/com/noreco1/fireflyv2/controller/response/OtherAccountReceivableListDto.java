package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Personal on 1/26/2016.
 */
public class OtherAccountReceivableListDto {
    private Integer id;
    private Integer transId;
    private String localCode;
    private BigDecimal amount;
    private String particulars;
    private Date date;
    private String status;

    public OtherAccountReceivableListDto() {
    }

    public OtherAccountReceivableListDto(Integer id, Integer transId, String localCode, BigDecimal amount, String particulars, Date date, String status) {
        this.id = id;
        this.transId = transId;
        this.localCode = localCode;
        this.amount = amount;
        this.particulars = particulars;
        this.date = date;
        this.status = status;
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

    public String getLocalCode() {
        return localCode;
    }

    public void setLocalCode(String localCode) {
        this.localCode = localCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getParticulars() {
        return particulars;
    }

    public void setParticulars(String particulars) {
        this.particulars = particulars;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
