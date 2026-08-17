package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum IncomeStatementBSUPAccountType {

    REVENUE("REVENUE"),
    EXPENSE("EXPENSE"),
    MARGIN("MARGIN"),
    NON_OP_REVENUE("NON_OP_REVENUE"),
    NON_OP_EXPENSE("NON_OP_EXPENSE"),
    INTEREST("INTEREST"),
    INCOME_TAX("INCOME_TAX"),
    EXTRA_ITEMS("EXTRA_ITEMS");

    private String name;

    IncomeStatementBSUPAccountType(String name) {
        this.name = name;
    }

}
