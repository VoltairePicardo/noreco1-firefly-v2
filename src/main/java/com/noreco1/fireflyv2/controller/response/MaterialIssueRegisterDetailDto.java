package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.Item;
import com.noreco1.fireflyv2.model.MaterialIssueRegisterDetail;
import com.noreco1.fireflyv2.model.UnitMeasure;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by nsutgio2015 on 4/27/2015.
 */
public class MaterialIssueRegisterDetailDto implements Serializable {

    private Integer id;
    private Item item;
    private String description;
    private UnitMeasure unitMeasure;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UnitMeasure getUnitMeasure() {
        return unitMeasure;
    }

    public void setUnitMeasure(UnitMeasure unitMeasure) {
        this.unitMeasure = unitMeasure;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public MaterialIssueRegisterDetail toModel(){

        MaterialIssueRegisterDetail ret = new MaterialIssueRegisterDetail();

        ret.setId(this.getId());
        ret.setAmount(this.getAmount());
        ret.setDescription(this.getDescription());
        ret.setQuantity(this.getQuantity());
        ret.setUnitPrice(this.getUnitPrice());

        return ret;

    }
}
