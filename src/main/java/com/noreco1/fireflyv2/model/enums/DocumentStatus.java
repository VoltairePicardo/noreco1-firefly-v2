package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum DocumentStatus {

    DOCUMENT_CREATED(1),
    FOR_CHECKING(2),
    FOR_RECOMMENDATION(3),
    FOR_AUDIT(4),
    FOR_APPROVAL(5),
    FOR_CHECK_WRITING(6),
    APPROVED(7),
    DENIED(8),
    FOR_CANVASSING(9),
    CLOSED(10),
    RETURNED_TO_CREATOR(11),
    FOR_RECEIVING(18),
    FOR_REPAIR(20),
    REPAIRED(21),
    RECEIVED(22),
    RELEASED(25),
    CANCELLED(26),
    CANVASSED(32),
    FOR_IT_CHECKING(33),
    SUBMITTED_TO_HR(34),
    FOR_VERIFICATION(35),
    RIV_FOR_PO_JO(36),
    FOR_BUDGET_OFFICER(37),
    FOR_BAC_REPRESENTATIVE(38),
    FOR_NOTED_BY(39),
    NOTED(40),
    PROJECT_CREATED(41),
    FOR_SITE_INSPECTION(45),
    FOR_FINAL_INSPECTION(46),
    CLOSED_OUT(48),
    FOR_CERTIFICATION(49),
    CERTIFIED(50),
    FOR_BUDGET_CHECKING(53),
    FOR_REVIEWING_AND_ACCEPTANCE(54),
    REVIEWED_AND_ACCEPTED(55),
    FOR_GM_APPROVAL(56),
    FOR_REVIEWING(57),
    LIQUIDATED(58),
    PROJECT_STARTED(59),
    FOR_COST_ESTIMATE(42);

    private int id;

    DocumentStatus(int id) {
        this.id = id;
    }

    public static DocumentStatus typeFromInt(int id) {
        DocumentStatus[] values = values();

        for(int x=0; x<values.length; x++) {
            DocumentStatus status = values[x];
            if (status.getId() == id) {
                return status;
            }
        }
        return null;
    }
}