package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;

public class AssemblyUnitDetailDto {
    private Integer id;
    private Integer assemblyUnitId;
    private Integer itemId;
    private BigDecimal quantity;
    private String code;
    private String description;
    private String unit;

    public AssemblyUnitDetailDto() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAssemblyUnitId() {
        return assemblyUnitId;
    }

    public void setAssemblyUnitId(Integer assemblyUnitId) {
        this.assemblyUnitId = assemblyUnitId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
