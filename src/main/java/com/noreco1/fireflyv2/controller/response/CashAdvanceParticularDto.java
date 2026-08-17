package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;

/**
 * Created by megeh.
 */
public class CashAdvanceParticularDto {
    private Integer id;
    private String particular;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
