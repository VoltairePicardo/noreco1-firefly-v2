package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by TSI Admin on 5/6/2015.
 */
public class VoucherCashflowItemDto {
    private Integer voucherId;
    private List<Map> cashflowData;

    public VoucherCashflowItemDto() {}

    public Integer getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(Integer voucherId) {
        this.voucherId = voucherId;
    }

    public List<Map> getCashflowData() {
        return cashflowData;
    }

    public void setCashflowData(List<Map> cashflowData) {
        this.cashflowData = cashflowData;
    }
}
