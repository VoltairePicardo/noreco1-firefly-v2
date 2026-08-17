package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.AccountType;

import java.io.Serializable;

/**
 * Created by arjayadong on 9/9/14.
 */
public class SegmentAccountDto implements Serializable {

    private int segmentAccountId;
    private int accountId;
    private String segmentAccountCode;
    private String accountCode;
    private String title;
    private AccountType accountType;

    public int getSegmentAccountId() {
        return segmentAccountId;
    }

    public void setSegmentAccountId(int segmentAccountId) {
        this.segmentAccountId = segmentAccountId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getSegmentAccountCode() {
        return segmentAccountCode;
    }

    public void setSegmentAccountCode(String segmentAccountCode) {
        this.segmentAccountCode = segmentAccountCode;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }
}
