package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum BankTransactionType {

    SAVINGS(1, "Savings"),
    CHECK(2, "Checking");

    private int id;
    private String description;

    BankTransactionType(int id, String description) {
        this.id = id;
        this.description = description;
    }

}
