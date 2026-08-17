package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by arjayadong on 9/9/14.
 */
public class AccountDto implements Serializable {

    private int id;
    private String code;
    private String title;
    private AccountType accountType;
    private String GLAccount;
    private String SLAccount;
    private String auxAccount;
    private String classification;
    private AccountGroup accountGroup;
    private int isActive;
    private Integer level;
    private boolean hasSL;
    private Account parentAccount;
    private Integer parentAccountId;
    private int normalBalance;
    private int isHeader;
    private List<SegmentAccount> segmentAccounts = new ArrayList<>();
    private Account bsupAccount;
    private String searchText;
    private String searchTextDisplay;
    private Factor factor;
    private Set<FactorPercentageDistro> percentageDistros;
    private boolean isManualAllocation = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {

        return this.code + " => " + this.title;
    }

    public String getGLAccount() {
        return GLAccount;
    }

    public void setGLAccount(String GLAccount) {
        this.GLAccount = GLAccount;
    }

    public String getSLAccount() {
        return SLAccount;
    }

    public void setSLAccount(String SLAccount) {
        this.SLAccount = SLAccount;
    }

    public String getAuxAccount() {
        return auxAccount;
    }

    public void setAuxAccount(String auxAccount) {
        this.auxAccount = auxAccount;
    }

    public int getIsActive() {
        return isActive;
    }

    public void isActive(int isActive) {
        this.isActive = isActive;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public boolean getHasSL() {
        return hasSL;
    }

    public void hasSL(boolean hasSL) {
        this.hasSL = hasSL;
    }

    public Account getParentAccount() {
        return parentAccount;
    }

    public void setParentAccount(Account parentAccount) {
        this.parentAccount = parentAccount;
    }

    public int getNormalBalance() {
        return normalBalance;
    }

    public void setNormalBalance(int normalBalance) {
        this.normalBalance = normalBalance;
    }

    public int getIsHeader() {
        return isHeader;
    }

    public void setIsHeader(int isHeader) {
        this.isHeader = isHeader;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public AccountGroup getAccountGroup() {
        return accountGroup;
    }

    public void setAccountGroup(AccountGroup accountGroup) {
        this.accountGroup = accountGroup;
    }

    public Integer getParentAccountId() {
        return parentAccountId;
    }

    public void setParentAccountId(Integer parentAccountId) {
        this.parentAccountId = parentAccountId;
    }

    public List<SegmentAccount> getSegmentAccounts() {
        return segmentAccounts;
    }

    public void setSegmentAccounts(List<SegmentAccount> segmentAccounts) {
        this.segmentAccounts = segmentAccounts;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public Account getBsupAccount() {
        return bsupAccount;
    }

    public void setBsupAccount(Account bsupAccount) {
        this.bsupAccount = bsupAccount;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public Factor getFactor() {
        return factor;
    }

    public void setFactor(Factor factor) {
        this.factor = factor;
    }

    public Set<FactorPercentageDistro> getPercentageDistros() {
        return percentageDistros;
    }

    public void setPercentageDistros(Set<FactorPercentageDistro> percentageDistros) {
        this.percentageDistros = percentageDistros;
    }

    public String getSearchTextDisplay() {
        return searchTextDisplay;
    }

    public void setSearchTextDisplay(String searchTextDisplay) {
        this.searchTextDisplay = searchTextDisplay;
    }

    public boolean isManualAllocation() {
        return isManualAllocation;
    }

    public void setManualAllocation(boolean manualAllocation) {
        isManualAllocation = manualAllocation;
    }
}
