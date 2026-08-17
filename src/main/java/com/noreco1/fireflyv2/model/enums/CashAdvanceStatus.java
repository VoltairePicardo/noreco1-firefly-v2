package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum CashAdvanceStatus {

    ALL(1, "All"),
    OVERDUE(2, "Overdue"),
    NOT_OVERDUE(3, "Not Overdue");

    private Integer id;
    private String description;

    CashAdvanceStatus(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

}
