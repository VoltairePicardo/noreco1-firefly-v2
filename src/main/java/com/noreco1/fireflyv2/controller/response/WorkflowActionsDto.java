package com.noreco1.fireflyv2.controller.response;

/**
 * Created by TSI Admin on 5/5/2015.
 */
public class WorkflowActionsDto {
    private Integer actionMapId;
    private String action;
    private Integer actionId;
    private Integer sequence;

    public WorkflowActionsDto() {}

    public WorkflowActionsDto(Integer actionMapId, String action, Integer actionId, Integer sequence) {
        this.actionMapId = actionMapId;
        this.action = action;
        this.actionId = actionId;
        this.sequence = sequence;
    }

    public Integer getActionMapId() {
        return actionMapId;
    }

    public void setActionMapId(Integer actionMapId) {
        this.actionMapId = actionMapId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getActionId() {
        return actionId;
    }

    public void setActionId(Integer actionId) {
        this.actionId = actionId;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }
}
