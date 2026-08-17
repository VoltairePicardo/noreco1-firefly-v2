package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum MathOp {

    ADD("+"),
    SUBTRACT("-");

    private String op;

    MathOp(String op) {
        this.op = op;
    }

}
