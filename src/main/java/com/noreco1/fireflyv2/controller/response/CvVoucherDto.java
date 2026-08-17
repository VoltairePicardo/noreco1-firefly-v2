package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CvVoucherDto {

    private Integer id;
    private String localCode;
    private BigDecimal amount;
    private String particulars;
    private Date voucherDate;
    private String preparedBy;
    private Integer slentityAccountNo;
    private String slentityName;
    private Date invoiceDate;
    private Integer paymentTerm;
    private Date dueDate;
    private Integer transId;
    private Employee employee;
    private String extensionUrl;
    private boolean hasTax;
    private BudgetLineItemDetail budgetLineItemDetail;
    private BudgetSubItem budgetSubItem;
    private PurchaseOrder purchaseOrder;
    private JobOrder jobOrder;

    @Getter @Setter private Boolean forInstallment = Boolean.FALSE;
    @Getter @Setter private Integer numberOfPayments = 0;
    @Getter @Setter private List<AccountsPayableVoucherInstallmentDetail> installmentDetails = new ArrayList<>();

    public CvVoucherDto() {
    }

    public CvVoucherDto(Integer id, String localCode, BigDecimal amount, String particulars, Date voucherDate, String preparedBy, Integer slentityAccountNo, String slentityName, Date invoiceDate, Integer paymentTerm, Date dueDate, Integer transId, Employee employee,PurchaseOrder purchaseOrder, JobOrder jobOrder) {
        this.id = id;
        this.localCode = localCode;
        this.amount = amount;
        this.particulars = particulars;
        this.voucherDate = voucherDate;
        this.preparedBy = preparedBy;
        this.slentityAccountNo = slentityAccountNo;
        this.slentityName = slentityName;
        this.invoiceDate = invoiceDate;
        this.paymentTerm = paymentTerm;
        this.dueDate = dueDate;
        this.transId = transId;
        this.employee = employee;
        this.purchaseOrder = purchaseOrder;
        this.jobOrder = jobOrder;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLocalCode() {
        return localCode;
    }

    public void setLocalCode(String localCode) {
        this.localCode = localCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public String getPreparedBy() {
        return preparedBy;
    }

    public void setPreparedBy(String preparedBy) {
        this.preparedBy = preparedBy;
    }

    public Integer getSlentityAccountNo() {
        return slentityAccountNo;
    }

    public void setSlentityAccountNo(Integer slentityAccountNo) {
        this.slentityAccountNo = slentityAccountNo;
    }

    public String getSlentityName() {
        return slentityName;
    }

    public void setSlentityName(String slentityName) {
        this.slentityName = slentityName;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public Integer getPaymentTerm() {
        return paymentTerm;
    }

    public void setPaymentTerm(Integer paymentTerm) {
        this.paymentTerm = paymentTerm;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getTransId() {
        return transId;
    }

    public void setTransId(Integer transId) {
        this.transId = transId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getExtensionUrl() {
        return extensionUrl;
    }

    public void setExtensionUrl(String extensionUrl) {
        this.extensionUrl = extensionUrl;
    }

    public boolean isHasTax() {
        return hasTax;
    }

    public void setHasTax(boolean hasTax) {
        this.hasTax = hasTax;
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

}
