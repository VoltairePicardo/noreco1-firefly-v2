package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum AdjustmentJournalTransactionType {

    ADJUSTMENT(1, "Adjustment"),
    CLOSING(2, "Closing"),
    REOPENING(3, "Reopening");

    private Integer id;
    private String desc;

    AdjustmentJournalTransactionType(Integer id, String desc) {
        this.id = id;
        this.desc = desc;
    }

}
