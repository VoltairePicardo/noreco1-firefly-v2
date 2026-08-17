package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.BankAccount;

import java.util.List;

/**
 * Created by TSI on 3/21/2023.
 */
public class UploadBankDepositDto {
    List<String> bankDepositLines;
    BankAccount bankAccount;

    public List<String> getBankDepositLines() {
        return bankDepositLines;
    }

    public void setBankDepositLines(List<String> bankDepositLines) {
        this.bankDepositLines = bankDepositLines;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }
}
