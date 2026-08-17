package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.SegmentAccount;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by TSI Admin on 4/24/2015.
 */
public class GeneralLedgerLineDto2 {
    private Integer id = 0;
    private Integer accountId;
    private String code;
    private String description;
    private String searchText;
    private String creditStr;
    private String debitStr;
    private Integer transactionId;
    private BigDecimal debit = BigDecimal.ZERO;
    private BigDecimal credit = BigDecimal.ZERO;
    private Boolean hasSL = false;
    private String checkNumber;
    private List<Map> distribution = new ArrayList<>();
    private List<Map> cashFlowAccounts = new ArrayList<>();
    private Map wTaxEntry;
    private Map vatEntry;
    private List<SubLedgerDto> slentries = new ArrayList<>();

    public GeneralLedgerLineDto2() {}

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getDebit() {
        return debit;
    }

    public void setDebit(BigDecimal debit) {
        this.debit = debit;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }

    public List<Map> getDistribution() {
        return distribution;
    }

    public void setDistribution(List<Map> distribution) {
        this.distribution = distribution;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getHasSL() {
        return hasSL;
    }

    public void setHasSL(Boolean hasSL) {
        this.hasSL = hasSL;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public Map getwTaxEntry() {
        return wTaxEntry;
    }

    public void setwTaxEntry(Map wTaxEntry) {
        this.wTaxEntry = wTaxEntry;
    }

    public Map getVatEntry() {
        return vatEntry;
    }

    public void setVatEntry(Map vatEntry) {
        this.vatEntry = vatEntry;
    }


    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getCreditStr() {
        return creditStr;
    }

    public void setCreditStr(String creditStr) {
        this.creditStr = creditStr;
    }

    public String getDebitStr() {
        return debitStr;
    }

    public void setDebitStr(String debitStr) {
        this.debitStr = debitStr;
    }

    public List<Map> getCashFlowAccounts() {
        return cashFlowAccounts;
    }

    public void setCashFlowAccounts(List<Map> cashFlowAccounts) {
        this.cashFlowAccounts = cashFlowAccounts;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<SubLedgerDto> getSlentries() {
        return slentries;
    }

    public void setSlentries(List<SubLedgerDto> slentries) {
        this.slentries = slentries;
    }
}
