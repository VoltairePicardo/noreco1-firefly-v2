package com.noreco1.fireflyv2.controller.response;

public class ApproveDocumentDto {
    private Integer documentId;
    private String remarks;
    private String documentType;

    public ApproveDocumentDto() {}

    public ApproveDocumentDto(Integer documentId, String remarks, String documentType) {
        this.documentId = documentId;
        this.remarks = remarks;
        this.documentType = documentType;
    }

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
}
