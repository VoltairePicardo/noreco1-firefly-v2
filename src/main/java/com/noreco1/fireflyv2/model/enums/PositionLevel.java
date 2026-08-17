package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum PositionLevel {

    RANK_AND_FILE(1),
    DIVISION_CHIEF(2),
    DEPARTMENT_MANAGER(3),
    SECTION_HEAD(4),
    OFFICER(5),
    SPECIALIST(6),
    RANK_AND_FILE_DIRECT(7),
    GENERAL_MANAGER(8);

    private int id;

    PositionLevel(int id) {
        this.id = id;
    }

}