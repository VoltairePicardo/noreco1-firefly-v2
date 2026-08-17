package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum AccountClassification {

    NEA(1),
    BSUP(2);

    private Integer id;

    AccountClassification(Integer id) {
        this.id = id;
    }

}
