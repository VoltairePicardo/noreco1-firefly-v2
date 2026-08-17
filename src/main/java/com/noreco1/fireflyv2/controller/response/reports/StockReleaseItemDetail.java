package com.noreco1.fireflyv2.controller.response.reports;

import com.noreco1.fireflyv2.model.StockTransactionDetail;

import java.text.DecimalFormat;
import java.util.List;

public class StockReleaseItemDetail {

    private Integer id;
    private String code;
    private String description;
    private List<StockTransactionDetail> stockTransactionDetails;
    private String items;

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

    public List<StockTransactionDetail> getStockTransactionDetails() {
        return stockTransactionDetails;
    }

    public void setStockTransactionDetails(List<StockTransactionDetail> stockTransactionDetails) {
        this.stockTransactionDetails = stockTransactionDetails;
    }

    public String getItems() {

        StringBuilder items = new StringBuilder();
        DecimalFormat formatter = new DecimalFormat("#,##0.00");

        if (!this.stockTransactionDetails.isEmpty()) {

            for (StockTransactionDetail stockTransactionDetail : this.stockTransactionDetails) {
                items.append(this.stockTransactionDetails.size() > 1 ? " * " : "").append(stockTransactionDetail.getItemStock().getItem().getDescription()).append(" - ").append(formatter.format(stockTransactionDetail.getTotalCost())).append("\n");
            }

            return items.substring(0, items.length() - 1);
        }

        return items.toString();

    }

    public void setItems(String items) {
        this.items = items;
    }

}
