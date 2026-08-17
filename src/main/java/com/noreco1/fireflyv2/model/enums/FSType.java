package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum FSType {

    Audited(1),
    Unaudited(2),
    Closed(3);

    private Integer id;

    FSType(Integer id) {
        this.id = id;
    }

}
