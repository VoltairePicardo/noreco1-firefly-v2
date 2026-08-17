package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum DocumentTypeClass {

    FOR_PURCHASING(1, "For Purchasing"),
    FOR_ACCOUNTING(2, "For Accounting");

    private int id;
    private String description;

    DocumentTypeClass(int id, String description) {
        this.id = id;
        this.description = description;
    }

}
