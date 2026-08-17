package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum InventoryCategory {
    LINE_MATERIALS(1),
    OFFICE_SUPPLIES(2),
    LINE_TOOLS(3),
    SPECIAL_EQUIPMENT(4),
    OFFICE_EQUIPMENT_FIXTURES_AND_FURNITURE(5),
    METER(6),
    TRANSPORTATION_EQUIPMENT(7),
    MAINTAINANCE_OFFICE_AND_GENERAL_PLANT(8),
    SPARE_PARTS(9),
    ELECTRICAL_MATERIALS(10),
    HOUSEWIRING_MATERIALS(11),
    ADMINISTRATIVE_AND_GENERAL_EXPENSE(12)
    ;
    private int id;

    InventoryCategory(int id) {
        this.id = id;
    }

    public static InventoryCategory parse(int id) {
        InventoryCategory category = null; // Default
        for (InventoryCategory item : InventoryCategory.values()) {
            if (item.getId()==id) {
                category = item;
                break;
            }
        }
        return category;
    }

}
