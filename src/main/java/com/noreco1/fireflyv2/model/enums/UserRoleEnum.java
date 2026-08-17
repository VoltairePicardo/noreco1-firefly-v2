package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum UserRoleEnum {

    PURCHASING_OFFICER(24, "Purchasing Officer");

    private Integer id;
    private String desc;

}
