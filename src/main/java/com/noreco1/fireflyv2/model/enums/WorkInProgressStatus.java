package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum WorkInProgressStatus {

    ALL(1, "All"),
    ON_GOING(2, "On Going"),
    CLOSED_OUT(3, "Closed Out");

    private Integer id;
    private String description;

}
