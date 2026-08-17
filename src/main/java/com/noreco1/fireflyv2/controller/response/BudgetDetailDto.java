package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by TSI Admin.
 */
public class BudgetDetailDto {
    private Integer id;
    private Integer budgetId;
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

    public BudgetDetailDto() {
    }

    public BudgetDetailDto(Integer id, Integer budgetId, Integer year, String departmentName, Date voucherDate, String cashflowChildName, String cashflowParentName, BigDecimal amtOgm, BigDecimal amtAod, BigDecimal amtIsd, BigDecimal amtFsd, BigDecimal amtTsd, BigDecimal amtBod) {
        this.id = id;
        this.budgetId = budgetId;
        this.year = year;
        this.departmentName = departmentName;
        this.voucherDate = voucherDate;
        this.cashflowChildName = cashflowChildName;
        this.cashflowParentName = cashflowParentName;
        this.amtOgm = amtOgm;
        this.amtAod = amtAod;
        this.amtIsd = amtIsd;
        this.amtFsd = amtFsd;
        this.amtTsd = amtTsd;
        this.amtBod = amtBod;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Integer budgetId) {
        this.budgetId = budgetId;
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
}
