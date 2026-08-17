package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.Asset;
import com.noreco1.fireflyv2.model.Transaction;

import java.util.Date;

/**
 * Created by Tri-Nvent on 3/3/2020.
 */
public class VoucherDto {

    private Integer transactionId;
    private String code;
    private String particulars;
    private Date date;

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
}
