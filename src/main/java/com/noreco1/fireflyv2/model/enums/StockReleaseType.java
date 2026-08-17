package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum StockReleaseType {

    MCT(1, "MCT", "Material Charge Ticket"),
    OSSP(2, "OSSP", "Office Supplies or Spare Parts Issuance Slip"),
    OFE(3, "OFE", "Office Furniture, Equipment, And Tools Acquired and Issued"),
    STRL(4, "STRL", "Stock Transfer Release");

    private Integer id;
    private String code;
    private String description;

    StockReleaseType(Integer id, String code, String description) {
        this.id = id;
        this.code = code;
        this.description = description;
    }

    public static StockReleaseType typeFromInt(int id) {
        StockReleaseType[] values = values();

        for(int x=0; x<values.length; x++) {
            StockReleaseType stockAdj = values[x];
            if (stockAdj.getId() == id) {
                return stockAdj;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.code;
    }
}