package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum SpecialEquipmentStatus {

    RECEIVED(1,"Received"),
    TESTED(2,"Tested");

    private int id;
    private String description;

    SpecialEquipmentStatus(int id, String description) {
        this.id = id;
        this.description = description;
    }

}
