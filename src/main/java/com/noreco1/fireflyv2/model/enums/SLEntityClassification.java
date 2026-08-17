package com.noreco1.fireflyv2.model.enums;

import lombok.*;


@NoArgsConstructor
@Getter
public enum SLEntityClassification {

    EMPLOYEE(1),
    SUPPLIER(2),
    ASSET(3),
    WORK_ORDER(4),
    PREPAYMENT(5),
    PETTY_CASH(6),
    OTHER_ACCOUNTS_RECEIVABLE(7),
    CONSUMER(8),
    OTHER_SL_ENTITIES(9);

    private Integer id;

    SLEntityClassification(Integer id) {
        this.id = id;
    }

    public static SLEntityClassification findByID(int id) {
        for (SLEntityClassification type : SLEntityClassification.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant found with id: " + id);
    }
}
