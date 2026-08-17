package com.noreco1.fireflyv2.controller.form;

import com.noreco1.fireflyv2.controller.response.GeneralLedgerLineDto2;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GeneralLedgerForm {

    List<GeneralLedgerLineDto2> ledgerLines = new ArrayList<>();
    Date voucherDate;

    public List<GeneralLedgerLineDto2> getLedgerLines() {
        return ledgerLines;
    }

    public void setLedgerLines(List<GeneralLedgerLineDto2> ledgerLines) {
        this.ledgerLines = ledgerLines;
    }

    public Date getVoucherDate() {
        return voucherDate;
    }

    public void setVoucherDate(Date voucherDate) {
        this.voucherDate = voucherDate;
    }
}
