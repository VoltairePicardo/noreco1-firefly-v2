package com.noreco1.fireflyv2.controller.response;


import java.math.BigDecimal;

/**
 * Created by Personal on 5/12/2015.
 */
public class CanvassDetailDto {
    private Integer id;
    private Integer canvassId;
    private Integer rvDetailId;
    private String itemCode;
    private String unitCode;
    private String itemDescription;
    private BigDecimal quantity = BigDecimal.ZERO;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private String rvNumber;
    private BigDecimal priceSupplier1 = BigDecimal.ZERO;
    private BigDecimal priceSupplier2 = BigDecimal.ZERO;
    private BigDecimal priceSupplier3 = BigDecimal.ZERO;

    public CanvassDetailDto() {}

    public CanvassDetailDto(Integer id, Integer canvassId, Integer rvDetailId, String itemCode, String unitCode,
                            String itemDescription, BigDecimal quantity, BigDecimal unitPrice, String rvNumber,
                            BigDecimal priceSupplier1, BigDecimal priceSupplier2, BigDecimal priceSupplier3 ) {
        this.id = id;
        this.rvDetailId = rvDetailId;
        this.canvassId = canvassId;
        this.itemCode = itemCode;
        this.unitCode = unitCode;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.rvNumber = rvNumber;
        this.unitPrice = unitPrice;
        this.priceSupplier1 = priceSupplier1;
        this.priceSupplier2 = priceSupplier2;
        this.priceSupplier3 = priceSupplier3;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRvDetailId() {
        return rvDetailId;
    }

    public void setRvDetailId(Integer rvDetailId) {
        this.rvDetailId = rvDetailId;
    }

    public Integer getCanvassId() {
        return canvassId;
    }

    public void setCanvassId(Integer canvassId) {
        this.canvassId = canvassId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getRvNumber() {
        return rvNumber;
    }

    public void setRvNumber(String rvNumber) {
        this.rvNumber = rvNumber;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getPriceSupplier1() {
        return priceSupplier1;
    }

    public void setPriceSupplier1(BigDecimal priceSupplier1) {
        this.priceSupplier1 = priceSupplier1;
    }

    public BigDecimal getPriceSupplier2() {
        return priceSupplier2;
    }

    public void setPriceSupplier2(BigDecimal priceSupplier2) {
        this.priceSupplier2 = priceSupplier2;
    }

    public BigDecimal getPriceSupplier3() {
        return priceSupplier3;
    }

    public void setPriceSupplier3(BigDecimal priceSupplier3) {
        this.priceSupplier3 = priceSupplier3;
    }
}
