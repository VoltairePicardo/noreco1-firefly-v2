package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Personal on 6/3/2015.
 */
public class PrepaymentListDto {
    private Integer id;
    private String code;
    private String description;
    private Date datePaid;
    private BigDecimal totalCost;
    private Integer noOfMonths;
    private BigDecimal monthlyCost;
    private BigDecimal appliedCost;
    private BigDecimal balance;
    private Boolean hasPpd;

    public PrepaymentListDto() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDatePaid() {
        return datePaid;
    }

    public void setDatePaid(Date datePaid) {
        this.datePaid = datePaid;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public Integer getNoOfMonths() {
        return noOfMonths;
    }

    public void setNoOfMonths(Integer noOfmonths) {
        this.noOfMonths = noOfmonths;
    }

    public BigDecimal getMonthlyCost() {
        return monthlyCost;
    }

    public void setMonthlyCost(BigDecimal monthlyCost) {
        this.monthlyCost = monthlyCost;
    }

    public BigDecimal getAppliedCost() {
        return appliedCost;
    }

    public void setAppliedCost(BigDecimal appliedCost) {
        this.appliedCost = appliedCost;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getHasPpd() {
        return hasPpd;
    }

    public void setHasPpd(Boolean hasPpd) {
        this.hasPpd = hasPpd;
    }
}
