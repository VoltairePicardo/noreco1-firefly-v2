package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;

/**
 * Created by TSI Admin on 4/24/2015.
 */
public class SubLedgerDto {
    private Integer id;
    private Integer accountNo;
    private String name;
    private Integer segmentAccountId;
    private Integer accountId;
    private BigDecimal amount = BigDecimal.ZERO;
    private BigDecimal debit = BigDecimal.ZERO;
    private BigDecimal credit = BigDecimal.ZERO;
    private Integer generalLedgerId;
    private Integer generalLedgerLineId;

    private String segmentAccountCode;
    private String segmentAccount;
    private BigDecimal balance = BigDecimal.ZERO;
    private Integer generalLedgerLineIndex;

    public SubLedgerDto() {}

    public String getSegmentAccountCode() {
        return segmentAccountCode;
    }

    public void setSegmentAccountCode(String segmentAccountCode) {
        this.segmentAccountCode = segmentAccountCode;
    }

    public String getSegmentAccount() {
        return segmentAccount;
    }

    public void setSegmentAccount(String segmentAccount) {
        this.segmentAccount = segmentAccount;
    }

    public Integer getGeneralLedgerId() {
        return generalLedgerId;
    }

    public void setGeneralLedgerId(Integer generalLedgerId) {
        this.generalLedgerId = generalLedgerId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(Integer accountNo) {
        this.accountNo = accountNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSegmentAccountId() {
        return segmentAccountId;
    }

    public void setSegmentAccountId(Integer segmentAccountId) {
        this.segmentAccountId = segmentAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Integer getGeneralLedgerLineIndex() {
        return generalLedgerLineIndex;
    }

    public void setGeneralLedgerLineIndex(Integer generalLedgerLineIndex) {
        this.generalLedgerLineIndex = generalLedgerLineIndex;
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

    public Integer getGeneralLedgerLineId() {
        return generalLedgerLineId;
    }

    public void setGeneralLedgerLineId(Integer generalLedgerLineId) {
        this.generalLedgerLineId = generalLedgerLineId;
    }
}