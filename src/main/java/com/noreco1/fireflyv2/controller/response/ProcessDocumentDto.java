package com.noreco1.fireflyv2.controller.response;

/**
 * Created by TSI Admin on 5/6/2015.
 */
public class ProcessDocumentDto {
    private Integer transId;
    private Integer documentId;
    private WorkflowActionsDto workflowActionsDto;

    public ProcessDocumentDto() {}

    public ProcessDocumentDto(Integer transId, Integer documentId, WorkflowActionsDto workflowActionsDto, String remarks) {
        this.transId = transId;
        this.documentId = documentId;
        this.workflowActionsDto = workflowActionsDto;
        this.remarks = remarks;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    private String remarks;


    public Integer getTransId() {
        return transId;
    }

    public void setTransId(Integer transId) {
        this.transId = transId;
    }

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

    public WorkflowActionsDto getWorkflowActionsDto() {
        return workflowActionsDto;
    }

    public void setWorkflowActionsDto(WorkflowActionsDto workflowActionsDto) {
        this.workflowActionsDto = workflowActionsDto;
    }
}
