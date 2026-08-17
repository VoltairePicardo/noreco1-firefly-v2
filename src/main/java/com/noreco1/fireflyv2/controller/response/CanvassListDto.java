package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Personal on 5/12/2015.
 */
public class CanvassListDto {
    private Integer id;
    private String localCode;
    private Date voucherDate;
    private String preparedBy;
    private String supplier;

    public CanvassListDto() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLocalCode() {
        return localCode;
    }

    public void setLocalCode(String localCode) {
        this.localCode = localCode;
    }

    public Date getVoucherDate() {
        return voucherDate;
    }

    public void setVoucherDate(Date voucherDate) {
        this.voucherDate = voucherDate;
    }

    public String getPreparedBy() {
        return preparedBy;
    }

    public void setPreparedBy(String preparedBy) {
        this.preparedBy = preparedBy;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }
}
