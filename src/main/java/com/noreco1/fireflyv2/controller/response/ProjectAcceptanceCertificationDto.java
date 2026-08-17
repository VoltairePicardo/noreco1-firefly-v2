package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Project;
import com.noreco1.fireflyv2.model.ProjectAcceptanceReport;
import com.noreco1.fireflyv2.model.SlEntity;

import java.util.Date;

/**
 * Created by Tri-Nvent on 11/4/2019.
 */
public class ProjectAcceptanceCertificationDto {

    private Integer id;
    private String code;
    private Date date;
    private Project project;
    private ProjectAcceptanceReportDto projectAcceptanceReport;
    private SlEntity createdBy;
    private SlEntity approvedBy;
    private SlEntity recommendedBy;
    private Integer transId;
    private DocumentStatus documentStatus;
    private Date lastUpdated;
    private Date created;

    public ProjectAcceptanceCertificationDto() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public SlEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(SlEntity createdBy) {
        this.createdBy = createdBy;
    }

    public SlEntity getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(SlEntity approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Integer getTransId() {
        return transId;
    }

    public void setTransId(Integer transId) {
        this.transId = transId;
    }

    public DocumentStatus getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(DocumentStatus documentStatus) {
        this.documentStatus = documentStatus;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public ProjectAcceptanceReportDto getProjectAcceptanceReport() {
        return projectAcceptanceReport;
    }

    public void setProjectAcceptanceReport(ProjectAcceptanceReportDto projectAcceptanceReport) {
        this.projectAcceptanceReport = projectAcceptanceReport;
    }

    public SlEntity getRecommendedBy() {
        return recommendedBy;
    }

    public void setRecommendedBy(SlEntity recommendedBy) {
        this.recommendedBy = recommendedBy;
    }
}
