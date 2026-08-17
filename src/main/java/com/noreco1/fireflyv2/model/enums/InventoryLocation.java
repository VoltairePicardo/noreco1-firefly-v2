package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum InventoryLocation {
    MAIN(1),
    SEP(2);

    private Integer id;

    InventoryLocation(Integer id) {
        this.id = id;
    }

}
