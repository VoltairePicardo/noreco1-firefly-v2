package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum DeliveryTerm {

    DELIVERED_TO_WAREHOUSE("Delivered to Warehouse"),
    PICK_UP("PICK-UP"),
    DOOR_TO_DOOR("DOOR TO DOOR");

    private String description;

    DeliveryTerm(String description) {
        this.description = description;
    }

}
