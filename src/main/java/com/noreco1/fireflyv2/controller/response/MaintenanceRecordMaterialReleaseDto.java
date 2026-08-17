package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.MaintenanceRecord;
import com.noreco1.fireflyv2.model.StockRelease;
import com.noreco1.fireflyv2.model.StockTransactionDetail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Tri-Nvent on 3/5/2020.
 */
public class MaintenanceRecordMaterialReleaseDto {

    private Integer id;
    private String code;
    private String description;
    private Date voucherDate;
    private StockRelease stockRelease;
    private List items;
    private List stockReleaseItems;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Date getVoucherDate() {
        return voucherDate;
    }

    public void setVoucherDate(Date voucherDate) {
        this.voucherDate = voucherDate;
    }

    public StockRelease getStockRelease() {
        return stockRelease;
    }

    public void setStockRelease(StockRelease stockRelease) {
        this.stockRelease = stockRelease;
    }

    public List getItems() {
        return items;
    }

    public void setItems(List items) {
        this.items = items;
    }

    public List getStockReleaseItems() {
        return stockReleaseItems;
    }

    public void setStockReleaseItems(List stockReleaseItems) {
        this.stockReleaseItems = stockReleaseItems;
    }
}
