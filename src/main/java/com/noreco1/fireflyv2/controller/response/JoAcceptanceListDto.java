package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.SlEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Personal on 7/7/2015.
 */
public class JoAcceptanceListDto {
    private Integer id;
    private String localCode;
    private String supplier;
    private BigDecimal amount;
    private Date voucherDate;
    private String status;
    private String preparedBy;
    private SlEntity vendor;

    public JoAcceptanceListDto() {}

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

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPreparedBy() {
        return preparedBy;
    }

    public void setPreparedBy(String preparedBy) {
        this.preparedBy = preparedBy;
    }

    public SlEntity getVendor() {
        return vendor;
    }

    public void setVendor(SlEntity vendor) {
        this.vendor = vendor;
    }
}
