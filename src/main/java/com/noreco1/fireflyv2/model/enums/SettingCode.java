package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum SettingCode {

    WTAX_ACCOUNT("WTAX_ACCOUNT"),
    INTEREST("INTEREST"),
    EXTRA_ORD_ITEMS("EXTRA_ORD_ITEMS"),
    TAX("TAX"),
    INPUT_TAX_RATE("INPUT_TAX_RATE"),
    VAT_ACCOUNT("VAT_ACCOUNT"),
    BIR_FORM_1601E_SIGNATORIES("BIR_FORM_1601E_SIGNATORIES"),
    SUPPLIER_ACCOUNT("SUPPLIER_ACCOUNT"),
    APPROVED_VOUCHERS_USER_ROLES("APPROVED_VOUCHERS_USER_ROLES"),
    RV_WORKFLOW_BY_POSITION_LEVEL("RV_WORKFLOW_BY_POSITION_LEVEL"),
    WAREHOUSE_POSITIONS("WAREHOUSE_POSITIONS"),
    IEMOP_BILLING_ATC("IEMOP_BILLING_ATC");

    private String code;

    SettingCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return this.code;
    }
}