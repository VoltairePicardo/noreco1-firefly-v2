package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;

/**
 * Created by TSI on 5/27/2023.
 */
public class CashAdvanceLiquidationItemDto {
    private Integer id;
    private String particular;
    private String orNumber;
    private BigDecimal amount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getParticular() {
        return particular;
    }

    public void setParticular(String particular) {
        this.particular = particular;
    }

    public String getOrNumber() {
        return orNumber;
    }

    public void setOrNumber(String orNumber) {
        this.orNumber = orNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
