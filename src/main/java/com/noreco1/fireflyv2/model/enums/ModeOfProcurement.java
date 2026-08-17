package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum ModeOfProcurement {

    OPEN_CANVASS(1),
    SEALED_CANVASS(2),
    PUBLIC_BIDDING(3),
    ACCREDITED_SUPPLIER(4);

    private Integer id;

    ModeOfProcurement(Integer id) {
        this.id = id;
    }

}
