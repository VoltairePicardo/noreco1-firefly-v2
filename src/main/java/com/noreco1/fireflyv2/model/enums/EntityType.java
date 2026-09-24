package com.noreco1.fireflyv2.model.enums;

public enum EntityType {

    CONSUMER("CONSUMER", "Consumer"),
    APPLICANT("APPLICANT", "Applicant"),
    PMES("PMES", "Pre-Membership Seminar"),
    EMPLOYEE("EMPLOYEE", "Employee"),
    USER("USER", "System User"),
    MEMBER("MEMBER", "Member"),
    SUPPLIER("SUPPLIER", "Supplier"),
    ASSET("ASSET", "Asset"),
    WORK_ORDER("WORK_ORDER", "Work Order"),
    PREPAYMENT("PREPAYMENT", "Prepayment"),
    PETTY_CASH("PETTY_CASH", "Petty Cash"),
    PETTY_CASH_TRANS("PETTY_CASH_TRANS", "Petty Cash Transaction"),
    CASH_ADVANCE("CASH_ADVANCE", "Cash Advance"),
    ACCOUNTS_RECEIVABLE("ACCOUNTS_RECEIVABLE", "Accounts Receivable"),
    SL_ENTITIES("SL_ENTITIES", "SL Entities"),
    BMCOO("BMCOO", "BMCOO"),
    DMCOO("DMCOO", "DMCOO"),
    PAYEE("PAYEE", "Payee");

    private final String code;
    private final String description;

    EntityType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static EntityType fromCode(String code) {
        if (code == null) return null;
        for (EntityType type : values()) {
            if (type.code.equalsIgnoreCase(code)) return type;
        }
        throw new IllegalArgumentException("Unknown entity type: " + code);
    }
}
