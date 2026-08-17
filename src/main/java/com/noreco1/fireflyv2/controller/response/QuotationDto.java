package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuotationDto {

    private Integer id;
    private String code;
    private Date date;
    private String particular;
    private PurchaseRequest purchaseRequest;
    private String documentStatus;
    private String preparedBy;
    private String approvedByProcurementOfficer;
    private String approvedByFinanceManager;
    private String approvedByGeneralManager;
    private Integer transId;
    private Date createdAt;
    private Date updatedAt;

    private List<Supplier> suppliers = new ArrayList<>();
    private List<QuotationTerm> terms = new ArrayList<>();

    private Map<String, Object> approvingOfficerObj;
    private Map<String, Object> generalManagerObj;

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

    public String getParticular() {
        return particular;
    }

    public void setParticular(String particular) {
        this.particular = particular;
    }

    public PurchaseRequest getPurchaseRequest() {
        return purchaseRequest;
    }

    public void setPurchaseRequest(PurchaseRequest purchaseRequest) {
        this.purchaseRequest = purchaseRequest;
    }

    public String getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(String documentStatus) {
        this.documentStatus = documentStatus;
    }

    public String getPreparedBy() {
        return preparedBy;
    }

    public void setPreparedBy(String preparedBy) {
        this.preparedBy = preparedBy;
    }

    public String getApprovedByProcurementOfficer() {
        return approvedByProcurementOfficer;
    }

    public void setApprovedByProcurementOfficer(String approvedByProcurementOfficer) {
        this.approvedByProcurementOfficer = approvedByProcurementOfficer;
    }

    public String getApprovedByGeneralManager() {
        return approvedByGeneralManager;
    }

    public void setApprovedByGeneralManager(String approvedByGeneralManager) {
        this.approvedByGeneralManager = approvedByGeneralManager;
    }

    public Integer getTransId() {
        return transId;
    }

    public void setTransId(Integer transId) {
        this.transId = transId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Supplier> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<Supplier> suppliers) {
        this.suppliers = suppliers;
    }

    public List<QuotationTerm> getTerms() {
        return terms;
    }

    public void setTerms(List<QuotationTerm> terms) {
        this.terms = terms;
    }

    public String getApprovedByFinanceManager() {
        return approvedByFinanceManager;
    }

    public void setApprovedByFinanceManager(String approvedByFinanceManager) {
        this.approvedByFinanceManager = approvedByFinanceManager;
    }

    public Map<String, Object> getApprovingOfficerObj() { return approvingOfficerObj; }
    public void setApprovingOfficerObj(Map<String, Object> approvingOfficerObj) { this.approvingOfficerObj = approvingOfficerObj; }
    public Map<String, Object> getGeneralManagerObj() { return generalManagerObj; }
    public void setGeneralManagerObj(Map<String, Object> generalManagerObj) { this.generalManagerObj = generalManagerObj; }
}
