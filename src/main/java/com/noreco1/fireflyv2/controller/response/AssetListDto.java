package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.Account;
import com.noreco1.fireflyv2.model.User;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by TSI Admin.
 */
public class AssetListDto {

    public Integer id;
    public Integer fkAccountNo;
    public String refNo;
    public String description;
    public Date acquisitionDate;
    public BigDecimal value;
    public Integer depreciationMonths;
    public BigDecimal depreciationYears;
    public Integer startYear;
    public Byte startMonth;
    public Integer endYear;
    public Byte endMonth;
    public BigDecimal monthlyDepreciation;
    public BigDecimal depreciatedValue;
    public BigDecimal remainingValue;
    public Account fkAssetAccount;
    public Account fkExpenseAccount;
    public Account fkAccountPepAccount;
    public User fkCreatedBy;
    public Date createdAt;

    public AssetListDto() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFkAccountNo() {
        return fkAccountNo;
    }

    public void setFkAccountNo(Integer fkAccountNo) {
        this.fkAccountNo = fkAccountNo;
    }

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(Date acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Integer getDepreciationMonths() {
        return depreciationMonths;
    }

    public void setDepreciationMonths(Integer depreciationMonths) {
        this.depreciationMonths = depreciationMonths;
    }

    public BigDecimal getDepreciationYears() {
        return depreciationYears;
    }

    public void setDepreciationYears(BigDecimal depreciationYears) {
        this.depreciationYears = depreciationYears;
    }

    public Integer getStartYear() {
        return startYear;
    }

    public void setStartYear(Integer startYear) {
        this.startYear = startYear;
    }

    public Byte getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(Byte startMonth) {
        this.startMonth = startMonth;
    }

    public Integer getEndYear() {
        return endYear;
    }

    public void setEndYear(Integer endYear) {
        this.endYear = endYear;
    }

    public Byte getEndMonth() {
        return endMonth;
    }

    public void setEndMonth(Byte endMonth) {
        this.endMonth = endMonth;
    }

    public BigDecimal getMonthlyDepreciation() {
        return monthlyDepreciation;
    }

    public void setMonthlyDepreciation(BigDecimal monthlyDepreciation) {
        this.monthlyDepreciation = monthlyDepreciation;
    }

    public BigDecimal getDepreciatedValue() {
        return depreciatedValue;
    }

    public void setDepreciatedValue(BigDecimal depreciatedValue) {
        this.depreciatedValue = depreciatedValue;
    }

    public BigDecimal getRemainingValue() {
        return remainingValue;
    }

    public void setRemainingValue(BigDecimal remainingValue) {
        this.remainingValue = remainingValue;
    }

    public Account getFkAssetAccount() {
        return fkAssetAccount;
    }

    public void setFkAssetAccount(Account fkAssetAccount) {
        this.fkAssetAccount = fkAssetAccount;
    }

    public Account getFkExpenseAccount() {
        return fkExpenseAccount;
    }

    public void setFkExpenseAccount(Account fkExpenseAccount) {
        this.fkExpenseAccount = fkExpenseAccount;
    }

    public Account getFkAccountPepAccount() {
        return fkAccountPepAccount;
    }

    public void setFkAccountPepAccount(Account fkAccountPepAccount) {
        this.fkAccountPepAccount = fkAccountPepAccount;
    }

    public User getFkCreatedBy() {
        return fkCreatedBy;
    }

    public void setFkCreatedBy(User fkCreatedBy) {
        this.fkCreatedBy = fkCreatedBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
