package com.noreco1.fireflyv2.model.enums;

import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum WorkflowAction {

    RETURN_TO_CREATOR(1),
    SEND_FOR_CHECKING(2),
    SEND_FOR_AUDITING(3),
    SEND_FOR_APPROVAL(4),
    APPROVE(5),
    DISAPPROVE(6),
    RECEIVE_ITEM(22),
    SEND_FOR_WORK_ORDER_CREATION(24),
    SEND_FOR_START_OF_CONSTRUCTION(25),
    SEND_FOR_SITE_INSPECTION(26),
    SEND_FOR_FINAL_INSPECTION(27),
    SEND_FOR_REPAIR(32),
    CERTIFY(31),
    SEND_FOR_BUDGET_CHECKING(34),
    SEND_FOR_REVIEWING_AND_ACCEPTANCE(35),
    ACCEPT(36),
    SEND_FOR_GM_APPROVAL(37),
    SEND_FOR_REVIEWING(38),
    RELEASE(15),
    RECEIVE(41),
    CANCEL(40);

    private int id;

}