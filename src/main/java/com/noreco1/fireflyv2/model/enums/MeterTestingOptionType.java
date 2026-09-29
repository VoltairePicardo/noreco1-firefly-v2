package com.noreco1.fireflyv2.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MeterTestingOptionType {

    REASON(1),
    REMARK(2),
    RECOMMENDATION(3);

    private Integer id;

    MeterTestingOptionType(Integer id) {
        this.id = id;
    }
}
