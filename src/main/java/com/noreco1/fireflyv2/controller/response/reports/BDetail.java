package com.noreco1.fireflyv2.controller.response.reports;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by TSI Admin.
 */
public class BDetail {
    private String code;
    private String cashflowItemName;
    private BigDecimal amount;
    private Integer year;
    private String departmentName;
    private Date voucherDate;
    private String cashflowChildName;
    private String cashflowParentName;
    private BigDecimal amtOgm;
    private BigDecimal amtAod;
    private BigDecimal amtIsd;
    private BigDecimal amtFsd;
    private BigDecimal amtTsd;
    private BigDecimal amtBod;
    private BigDecimal totalYearBudget;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCashflowItemName() {
        return cashflowItemName;
    }

    public void setCashflowItemName(String cashflowItemName) {
        this.cashflowItemName = cashflowItemName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Date getVoucherDate() {
        return voucherDate;
    }

    public void setVoucherDate(Date voucherDate) {
        this.voucherDate = voucherDate;
    }

    public String getCashflowChildName() {
        return cashflowChildName;
    }

    public void setCashflowChildName(String cashflowChildName) {
        this.cashflowChildName = cashflowChildName;
    }

    public String getCashflowParentName() {
        return cashflowParentName;
    }

    public void setCashflowParentName(String cashflowParentName) {
        this.cashflowParentName = cashflowParentName;
    }

    public BigDecimal getAmtOgm() {
        return amtOgm;
    }

    public void setAmtOgm(BigDecimal amtOgm) {
        this.amtOgm = amtOgm;
    }

    public BigDecimal getAmtAod() {
        return amtAod;
    }

    public void setAmtAod(BigDecimal amtAod) {
        this.amtAod = amtAod;
    }

    public BigDecimal getAmtIsd() {
        return amtIsd;
    }

    public void setAmtIsd(BigDecimal amtIsd) {
        this.amtIsd = amtIsd;
    }

    public BigDecimal getAmtFsd() {
        return amtFsd;
    }

    public void setAmtFsd(BigDecimal amtFsd) {
        this.amtFsd = amtFsd;
    }

    public BigDecimal getAmtTsd() {
        return amtTsd;
    }

    public void setAmtTsd(BigDecimal amtTsd) {
        this.amtTsd = amtTsd;
    }

    public BigDecimal getAmtBod() {
        return amtBod;
    }

    public void setAmtBod(BigDecimal amtBod) {
        this.amtBod = amtBod;
    }

    public BigDecimal getTotalYearBudget() {
        return totalYearBudget;
    }

    public void setTotalYearBudget(BigDecimal totalYearBudget) {
        this.totalYearBudget = totalYearBudget;
    }
}
