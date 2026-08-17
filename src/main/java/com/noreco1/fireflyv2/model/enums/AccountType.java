package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum AccountType {

    ASSET(1),
    LIABILITY(2),
    EQUITY(3),
    REVENUE(4),
    EXPENSE(5);

    private Integer id;

    AccountType(Integer id) {
        this.id = id;
    }

}
