package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.ModeOfProcurement;


public class SetModeOfProcurementDto {
    private Integer transId;
    private Integer documentId;
    private ModeOfProcurement mode;
    private String remarks;

    public SetModeOfProcurementDto() {}

    public SetModeOfProcurementDto(Integer transId, Integer documentId, ModeOfProcurement mode, String remarks) {
        this.transId = transId;
        this.documentId = documentId;
        this.mode = mode;
        this.remarks = remarks;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }


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

    public ModeOfProcurement getMode() {
        return mode;
    }

    public void setMode(ModeOfProcurement mode) {
        this.mode = mode;
    }
}
