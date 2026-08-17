package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum DisplayStatus {

    ALL(0, "All"),
    PENDING(-1, "Pending");

    private int id;
    private String description;

    DisplayStatus(int id, String description) {
        this.id = id;
        this.description = description;
    }

}
