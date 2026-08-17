package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum BusinessActivity {

    GENERATION(1, "G"),
    DISTRIBUTION(2, "D"),
    NEA(3, "N");

    private Integer id;
    private String code;

    BusinessActivity(Integer id, String code) {
        this.id = id;
        this.code = code;
    }

}
