package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.Brand;
import com.noreco1.fireflyv2.model.Supplier;

import java.math.BigDecimal;

public class QuotationItemDetailDto {

    private Supplier supplier;
    private BigDecimal price = BigDecimal.ZERO;
    private Boolean isAwarded;
    private Brand brand;

    public QuotationItemDetailDto() {
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getAwarded() {
        return isAwarded;
    }

    public void setAwarded(Boolean awarded) {
        isAwarded = awarded;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

}
