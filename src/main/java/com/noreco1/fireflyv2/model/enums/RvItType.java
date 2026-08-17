package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum RvItType {

    HARDWARE(1, "Hardware"),
    SOFTWARE(2, "Software");

    private int id;
    private String description;

    RvItType(int id, String description) {
        this.id = id;
        this.description = description;
    }

}
