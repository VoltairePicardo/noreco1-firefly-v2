package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum StockWithdrawalType {

    MRS(1, "MRS", "Material Requisition Slip"),
    OSSPRS(2, "OSSPRS", "Office Supplies or Spare Parts Requisition Slip"),
    OFTERS(3, "OFTERS", "Office Furniture, Tools, and Equipment Requisition Slip");

    private Integer id;
    private String code;
    private String description;

    StockWithdrawalType(int id, String code, String description) {
        this.id = id;
        this.code = code;
        this.description = description;
    }

    public static StockWithdrawalType typeFromInt(int id) {
        StockWithdrawalType[] values = values();

        for(int x=0; x<values.length; x++) {
            StockWithdrawalType sw = values[x];
            if (sw.getId() == id) {
                return sw;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.code;
    }
}
