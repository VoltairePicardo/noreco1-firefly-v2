package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum AssetAdjustmentType {

    ADD_TO_ASSET("Add to Asset"),
    DEDUCT_FROM_ASSET("Deduct from Asset"),
    ADD_DEPRECIATION("Add depreciation"),
    DEDUCT_DEPRECIATION("Deduct depreciation");

    private String description;

    AssetAdjustmentType(String description) {
        this.description = description;
    }

}
