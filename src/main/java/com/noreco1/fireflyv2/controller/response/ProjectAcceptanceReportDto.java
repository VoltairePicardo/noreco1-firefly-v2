package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Office;
import com.noreco1.fireflyv2.model.Project;
import com.noreco1.fireflyv2.model.SlEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Tri-Nvent on 11/4/2019.
 */
public class ProjectAcceptanceReportDto {

    private Integer id;
    private String code;
    private Date date;
    private Project project;
    private SlEntity inspector1;
    private SlEntity inspector2;
    private SlEntity inspector3;
    private SlEntity createdBy;
    private SlEntity notedBy;
    private SlEntity recommendedBy;
    private SlEntity approvedBy;
    private Integer transId;
    private DocumentStatus documentStatus;
    private Date lastUpdated;
    private Date created;
    private String workOrderCode;
    private String costEstimateCode;

    public ProjectAcceptanceReportDto() {}

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

    public SlEntity getInspector1() {
        return inspector1;
    }

    public void setInspector1(SlEntity inspector1) {
        this.inspector1 = inspector1;
    }

    public SlEntity getInspector2() {
        return inspector2;
    }

    public void setInspector2(SlEntity inspector2) {
        this.inspector2 = inspector2;
    }

    public SlEntity getInspector3() {
        return inspector3;
    }

    public void setInspector3(SlEntity inspector3) {
        this.inspector3 = inspector3;
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

    public SlEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(SlEntity createdBy) {
        this.createdBy = createdBy;
    }

    public String getWorkOrderCode() {
        return workOrderCode;
    }

    public void setWorkOrderCode(String workOrderCode) {
        this.workOrderCode = workOrderCode;
    }

    public String getCostEstimateCode() {
        return costEstimateCode;
    }

    public void setCostEstimateCode(String costEstimateCode) {
        this.costEstimateCode = costEstimateCode;
    }

    public SlEntity getNotedBy() {
        return notedBy;
    }

    public void setNotedBy(SlEntity notedBy) {
        this.notedBy = notedBy;
    }

    public SlEntity getRecommendedBy() {
        return recommendedBy;
    }

    public void setRecommendedBy(SlEntity recommendedBy) {
        this.recommendedBy = recommendedBy;
    }

    public SlEntity getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(SlEntity approvedBy) {
        this.approvedBy = approvedBy;
    }
}
