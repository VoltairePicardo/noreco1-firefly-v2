package com.noreco1.fireflyv2.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum MeterStatus {

    ENDORSED(1, "Endorsed"),
    TESTED(2, "Tested"),
    ISSUED(3, "Issued"),
    CONNECTED(4, "Connected"),
    DISCONNECTED(5, "Disconnected"),
    REMOVED(6, "Removed"),
    DISPOSED(7, "Disposed"),
    RECEIVED(8, "Received");

    private Integer id;
    private String description;

}
