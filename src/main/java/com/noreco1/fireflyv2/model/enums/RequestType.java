package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum RequestType {
    MCT(1),
    MR(2),
    OTHERS(3)
    ;
    private int id;

    RequestType(int id) {
        this.id = id;
    }

    public static RequestType parse(int id) {
        RequestType type = null; // Default
        for (RequestType item : RequestType.values()) {
            if (item.getId()==id) {
                type = item;
                break;
            }
        }
        return type;
    }

}
