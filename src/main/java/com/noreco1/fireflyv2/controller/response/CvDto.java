package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by TSI Admin on 4/23/2015.
 */
public class CvDto {
    private Integer id;
    private SlEntity payee;
    private String particulars;
    private Date voucherDate;
    private Date dueDate;
    private String localCode;
    private SlEntity preparedBy;
    private SlEntity verifier;
    private SlEntity checker;
    private SlEntity budgetOfficer;
    private SlEntity checkPrinter;
    private SlEntity reviewer;
    private SlEntity approvingOfficer;
    private SlEntity recommendingOfficer;
    private SlEntity postedBy;
    private SlEntity auditor;
    private SlEntity secondCheckSign;
    private Integer transId;
    private BigDecimal amount;
    private BigDecimal checkAmount;
    private DocumentStatus documentStatus;
    private Date lastUpdated;
    private Date created;
    private List<ApvDto> apvDtos = new ArrayList<>();
    private String rrNumber;
    private Office office;
    private CvVoucherDto ApvDto;
    private CvVoucherDto cashAdvanceDto;
    private CvVoucherDto jvDto;
    private CvVoucherDto rrDto;
    private CvVoucherDto joaDto;
    private String additionalPayeeInfo;
    private Bank bank;
    private BudgetDetail budgetDetail;
    private BudgetLineItemDetail budgetLineItemDetail;
    private BudgetSubItem budgetSubItem;
    private List<Map> bankAccountDetailsMap = new ArrayList<>();
    private PurchaseOrder purchaseOrder;
    private JobOrder jobOrder;
    private List<Map>  cashAdvances;
    private List<IEMOPBilling> iemopBillings;
    private List<CheckVoucherBudgetDetail> budgetDetails;

    private boolean isForAddingBudgetDetail = false;

    public CvDto() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SlEntity getPayee() {
        return payee;
    }

    public void setPayee(SlEntity payee) {
        this.payee = payee;
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

    public SlEntity getChecker() {
        return checker;
    }

    public void setChecker(SlEntity checker) {
        this.checker = checker;
    }

    public SlEntity getBudgetOfficer() {
        return budgetOfficer;
    }

    public void setBudgetOfficer(SlEntity budgetOfficer) {
        this.budgetOfficer = budgetOfficer;
    }

    public SlEntity getApprovingOfficer() {
        return approvingOfficer;
    }

    public void setApprovingOfficer(SlEntity approvingOfficer) {
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

    public SlEntity getRecommendingOfficer() {
        return recommendingOfficer;
    }

    public void setRecommendingOfficer(SlEntity recommendingOfficer) {
        this.recommendingOfficer = recommendingOfficer;
    }

    public SlEntity getAuditor() {
        return auditor;
    }

    public void setAuditor(SlEntity auditor) {
        this.auditor = auditor;
    }

    public BigDecimal getCheckAmount() {
        return checkAmount;
    }

    public void setCheckAmount(BigDecimal checkAmount) {
        this.checkAmount = checkAmount;
    }

    public SlEntity getVerifier() {
        return verifier;
    }

    public void setVerifier(SlEntity verifier) {
        this.verifier = verifier;
    }

    public SlEntity getPreparedBy() {
        return preparedBy;
    }

    public void setPreparedBy(SlEntity preparedBy) {
        this.preparedBy = preparedBy;
    }

    public SlEntity getSecondCheckSign() {
        return secondCheckSign;
    }

    public void setSecondCheckSign(SlEntity secondCheckSign) {
        this.secondCheckSign = secondCheckSign;
    }

    public String getRrNumber() {
        return rrNumber;
    }

    public void setRrNumber(String rrNumber) {
        this.rrNumber = rrNumber;
    }

    public SlEntity getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(SlEntity postedBy) {
        this.postedBy = postedBy;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public List<ApvDto> getApvDtos() {
        return apvDtos;
    }

    public void setApvDtos(List<ApvDto> apvDtos) {
        this.apvDtos = apvDtos;
    }

    public String getAdditionalPayeeInfo() {
        return additionalPayeeInfo;
    }

    public void setAdditionalPayeeInfo(String additionalPayeeInfo) {
        this.additionalPayeeInfo = additionalPayeeInfo;
    }

    public CvVoucherDto getCashAdvanceDto() {
        return cashAdvanceDto;
    }

    public void setCashAdvanceDto(CvVoucherDto cashAdvanceDto) {
        this.cashAdvanceDto = cashAdvanceDto;
    }

    public CvVoucherDto getApvDto() {
        return ApvDto;
    }

    public void setApvDto(CvVoucherDto apvDto) {
        ApvDto = apvDto;
    }

    public CvVoucherDto getJvDto() {
        return jvDto;
    }

    public void setJvDto(CvVoucherDto jvDto) {
        this.jvDto = jvDto;
    }

    public CvVoucherDto getRrDto() {
        return rrDto;
    }

    public void setRrDto(CvVoucherDto rrDto) {
        this.rrDto = rrDto;
    }

    public CvVoucherDto getJoaDto() {
        return joaDto;
    }

    public void setJoaDto(CvVoucherDto joaDto) {
        this.joaDto = joaDto;
    }

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public SlEntity getCheckPrinter() {
        return checkPrinter;
    }

    public void setCheckPrinter(SlEntity checkPrinter) {
        this.checkPrinter = checkPrinter;
    }

    public SlEntity getReviewer() {
        return reviewer;
    }

    public void setReviewer(SlEntity reviewer) {
        this.reviewer = reviewer;
    }

    public List<Map> getBankAccountDetailsMap() {
        return bankAccountDetailsMap;
    }

    public void setBankAccountDetailsMap(List<Map> bankAccountDetailsMap) {
        this.bankAccountDetailsMap = bankAccountDetailsMap;
    }

    public BudgetDetail getBudgetDetail() {
        return budgetDetail;
    }

    public void setBudgetDetail(BudgetDetail budgetDetail) {
        this.budgetDetail = budgetDetail;
    }

    public BudgetLineItemDetail getBudgetLineItemDetail() {
        return budgetLineItemDetail;
    }

    public void setBudgetLineItemDetail(BudgetLineItemDetail budgetLineItemDetail) {
        this.budgetLineItemDetail = budgetLineItemDetail;
    }

    public BudgetSubItem getBudgetSubItem() {
        return budgetSubItem;
    }

    public void setBudgetSubItem(BudgetSubItem budgetSubItem) {
        this.budgetSubItem = budgetSubItem;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public JobOrder getJobOrder() {
        return jobOrder;
    }

    public void setJobOrder(JobOrder jobOrder) {
        this.jobOrder = jobOrder;
    }

    public List<Map> getCashAdvances() {
        return cashAdvances;
    }

    public void setCashAdvances(List<Map> cashAdvances) {
        this.cashAdvances = cashAdvances;
    }

    public List<IEMOPBilling> getIemopBillings() {
        return iemopBillings;
    }

    public void setIemopBillings(List<IEMOPBilling> iemopBillings) {
        this.iemopBillings = iemopBillings;
    }

    public List<CheckVoucherBudgetDetail> getBudgetDetails() {
        return budgetDetails;
    }

    public void setBudgetDetails(List<CheckVoucherBudgetDetail> budgetDetails) {
        this.budgetDetails = budgetDetails;
    }

    public boolean isForAddingBudgetDetail() {
        return isForAddingBudgetDetail;
    }

    public void setForAddingBudgetDetail(boolean forAddingBudgetDetail) {
        isForAddingBudgetDetail = forAddingBudgetDetail;
    }
}
