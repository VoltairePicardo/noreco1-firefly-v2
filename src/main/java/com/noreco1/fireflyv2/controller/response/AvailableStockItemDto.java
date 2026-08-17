package com.noreco1.fireflyv2.controller.response;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by repryd2017 on 9/30/2017.
 */
public class AvailableStockItemDto implements Serializable {
    private String location;
    private BigDecimal quantity;

    public AvailableStockItemDto() {
    }

    public AvailableStockItemDto(String location, BigDecimal quantity) {
        this.location = location;
        this.quantity = quantity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}
