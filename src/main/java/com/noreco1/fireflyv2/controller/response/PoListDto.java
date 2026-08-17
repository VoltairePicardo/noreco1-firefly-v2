package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.BudgetLineItemDetail;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Personal on 5/15/2015.
 */
public class PoListDto {
    private Integer id;
    private String localCode;
    private String supplier;
    private BigDecimal amount;
    private Date voucherDate;
    private String status;
    private String preparedBy;
    private BudgetLineItemDetail budgetLineItemDetail;

    public PoListDto() {}

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

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getVoucherDate() {
        return voucherDate;
    }

    public void setVoucherDate(Date voucherDate) {
        this.voucherDate = voucherDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPreparedBy() {
        return preparedBy;
    }

    public void setPreparedBy(String preparedBy) {
        this.preparedBy = preparedBy;
    }

    public BudgetLineItemDetail getBudgetLineItemDetail() {
        return budgetLineItemDetail;
    }

    public void setBudgetLineItemDetail(BudgetLineItemDetail budgetLineItemDetail) {
        this.budgetLineItemDetail = budgetLineItemDetail;
    }
}
