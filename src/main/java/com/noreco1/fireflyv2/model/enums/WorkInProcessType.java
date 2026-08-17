package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum WorkInProcessType {

    ADMINISTRATION(1, "Administration"),
    CONTRACT(2, "Contract");

    private Integer id;
    private String desc;

}
