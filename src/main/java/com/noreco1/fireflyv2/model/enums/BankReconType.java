package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum BankReconType {

    RC(1, "Released Check", "RC"),
    OD(2, "Other Deposit", "OD"),
    DT(3, "Deposit Transit", "DT");

    private int id;
    private String description;
    private String code;

    BankReconType(int id, String description, String code) {
        this.id = id;
        this.description = description;
        this.code = code;
    }

}
