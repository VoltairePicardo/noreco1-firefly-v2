package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.controller.response.reports.CommonLedgerDetail;
import com.noreco1.fireflyv2.model.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by nsutgio2015 on 4/27/2015.
 */
public class MaterialIssueRegisterDto implements Serializable {

    private Integer id;
    private SlEntity vendor;
    private String particulars;
    private Date voucherDate;
    private Date dueDate;
    private String localCode;
    private SlEntity sLChecker;
    private SlEntity sLRecommendingOfficer;
    private SlEntity sLApprovingOfficer;
    private User createdBy;
    private User checker;
    private User approvingOfficer;
    private Integer transId;
    private BigDecimal amount;
    private String status;
    private Date lastUpdated;
    private Date created;
    private Map inventoryDoc;
    private String inventoryDocType;
    private Integer docId;
    private InventoryDocumentDto inventoryDocument;
    private Office office;
    private DocumentStatus documentStatus;
    private Boolean enableCheckBox;
    private Boolean selected;
    private String documentCode;

    public String getInventoryDocType() {
        return inventoryDocType;
    }

    public void setInventoryDocType(String inventoryDocType) {
        this.inventoryDocType = inventoryDocType;
    }

    private ArrayList<GeneralLedgerLineDto2> generalLedgerLines;
    private ArrayList<SubLedgerDto> subLedgerLines;
    private ArrayList<MaterialIssueRegisterDetailDto> materialIssueRegisterDetails;
    private List<MrctDetail> mrctDetails = new ArrayList<>();
    private List<CommonLedgerDetail> journalEntries;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SlEntity getVendor() {
        return vendor;
    }

    public void setVendor(SlEntity vendor) {
        this.vendor = vendor;
    }

    public String getParticulars() {
        return particulars;
    }

    public void setParticulars(String particulars) {
        this.particulars = particulars;
    }

    public Date getVoucherDate() {
        return voucherDate;
    }

    public void setVoucherDate(Date voucherDate) {
        this.voucherDate = voucherDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getLocalCode() {
        return localCode;
    }

    public void setLocalCode(String localCode) {
        this.localCode = localCode;
    }

    public SlEntity getsLChecker() {
        return sLChecker;
    }

    public void setsLChecker(SlEntity sLChecker) {
        this.sLChecker = sLChecker;
    }

    public SlEntity getsLApprovingOfficer() {
        return sLApprovingOfficer;
    }

    public void setsLApprovingOfficer(SlEntity sLApprovingOfficer) {
        this.sLApprovingOfficer = sLApprovingOfficer;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getChecker() {
        return checker;
    }

    public void setChecker(User checker) {
        this.checker = checker;
    }

    public User getApprovingOfficer() {
        return approvingOfficer;
    }

    public void setApprovingOfficer(User approvingOfficer) {
        this.approvingOfficer = approvingOfficer;
    }

    public Integer getTransId() {
        return transId;
    }

    public void setTransId(Integer transId) {
        this.transId = transId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public ArrayList<GeneralLedgerLineDto2> getGeneralLedgerLines() {
        return generalLedgerLines;
    }

    public void setGeneralLedgerLines(ArrayList<GeneralLedgerLineDto2> generalLedgerLines) {
        this.generalLedgerLines = generalLedgerLines;
    }

    public ArrayList<SubLedgerDto> getSubLedgerLines() {
        return subLedgerLines;
    }

    public void setSubLedgerLines(ArrayList<SubLedgerDto> subLedgerLines) {
        this.subLedgerLines = subLedgerLines;
    }

    public ArrayList<MaterialIssueRegisterDetailDto> getMaterialIssueRegisterDetails() {
        return materialIssueRegisterDetails;
    }

    public void setMaterialIssueRegisterDetails(ArrayList<MaterialIssueRegisterDetailDto> materialIssueRegisterDetails) {
        this.materialIssueRegisterDetails = materialIssueRegisterDetails;
    }

    public List<MrctDetail> getMrctDetails() {
        return mrctDetails;
    }

    public void setMrctDetails(List<MrctDetail> mrctDetails) {
        this.mrctDetails = mrctDetails;
    }

    public Map getInventoryDoc() {
        return inventoryDoc;
    }

    public void setInventoryDoc(Map inventoryDoc) {
        this.inventoryDoc = inventoryDoc;
    }

    public Integer getDocId() {
        return docId;
    }

    public void setDocId(Integer docId) {
        this.docId = docId;
    }

    public InventoryDocumentDto getInventoryDocument() {
        return inventoryDocument;
    }

    public void setInventoryDocument(InventoryDocumentDto inventoryDocument) {
        this.inventoryDocument = inventoryDocument;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public SlEntity getsLRecommendingOfficer() {
        return sLRecommendingOfficer;
    }

    public void setsLRecommendingOfficer(SlEntity sLRecommendingOfficer) {
        this.sLRecommendingOfficer = sLRecommendingOfficer;
    }

    public DocumentStatus getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(DocumentStatus documentStatus) {
        this.documentStatus = documentStatus;
    }

    public Boolean getEnableCheckBox() {
        return enableCheckBox;
    }

    public void setEnableCheckBox(Boolean enableCheckBox) {
        this.enableCheckBox = enableCheckBox;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public String getDocumentCode() {
        return documentCode;
    }

    public void setDocumentCode(String documentCode) {
        this.documentCode = documentCode;
    }

    public List<CommonLedgerDetail> getJournalEntries() {
        return journalEntries;
    }

    public void setJournalEntries(List<CommonLedgerDetail> journalEntries) {
        this.journalEntries = journalEntries;
    }
}
