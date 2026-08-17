package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum StockAdjustmentType {

    TRANSFER_LOCATION(1, "Transfer Location"),
    UPDATE_QUANTITY(2, "Update Quantity");

    private int id;
    private String description;

    StockAdjustmentType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public static StockAdjustmentType typeFromInt(int id) {
        StockAdjustmentType[] values = values();

        for(int x=0; x<values.length; x++) {
            StockAdjustmentType stockAdj = values[x];
            if (stockAdj.getId() == id) {
                return stockAdj;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.description;
    }
}
