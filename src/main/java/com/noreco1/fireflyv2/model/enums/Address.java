package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum Address {
    HOME(1),
    MAILING(2),
    PROVINCIAL(3);

    private int id;

    Address(int id) {
        this.id = id;
    }

}
