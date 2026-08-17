package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum DocumentToTableMap {

    JV("JournalVoucher"),
    AJ("AdjustmentJournal");

    private String tableName;

    DocumentToTableMap(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        return this.tableName;
    }
}