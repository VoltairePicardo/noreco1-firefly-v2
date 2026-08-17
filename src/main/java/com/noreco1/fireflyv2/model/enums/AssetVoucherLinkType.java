package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@NoArgsConstructor
@Getter
public enum AssetVoucherLinkType {

    ACQUISITION(1, "Acquisition"),
    MAJOR_REPAIR(2, "Major Repair (Capitalize)"),
    MINOR_REPAIR(3, "Minor Repair or Maintenance (Expense)"),
    RETIREMENT(4, "Retirement"),
    ADJUSTMENT(5, "Adjustment");

    private Integer id;
    private String description;

    AssetVoucherLinkType(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

}
