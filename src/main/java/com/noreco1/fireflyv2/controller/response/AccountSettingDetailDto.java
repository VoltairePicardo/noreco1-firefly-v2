package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.Account;

import java.io.Serializable;

public class AccountSettingDetailDto implements Serializable {

    private Integer itemStockDetailId;
    private String itemCode;
    private String itemDescription;
    private String unitCode;
    private Account debitAccount;
    private Account creditAccount;

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }


    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public Account getDebitAccount() {
        return debitAccount;
    }

    public void setDebitAccount(Account debitAccount) {
        this.debitAccount = debitAccount;
    }

    public Account getCreditAccount() {
        return creditAccount;
    }

    public void setCreditAccount(Account creditAccount) {
        this.creditAccount = creditAccount;
    }

    public Integer getItemStockDetailId() {
        return itemStockDetailId;
    }

    public void setItemStockDetailId(Integer itemStockDetailId) {
        this.itemStockDetailId = itemStockDetailId;
    }
}
