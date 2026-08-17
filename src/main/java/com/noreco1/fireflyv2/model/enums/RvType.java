package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum RvType {

    FOR_PO(1, "For PO"),
    FOR_IT(2, "For IT"),
    FOR_REP(3, "For REP"),
    FOR_LAB(4, "For Labor");

    private int id;
    private String description;

    RvType(int id, String description) {
        this.id = id;
        this.description = description;
    }

}
