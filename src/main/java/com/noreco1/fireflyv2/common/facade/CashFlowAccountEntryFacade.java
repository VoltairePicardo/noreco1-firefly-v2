package com.noreco1.fireflyv2.common.facade;

import com.noreco1.fireflyv2.model.GeneralLedger;
import com.noreco1.fireflyv2.model.SegmentAccount;
import com.noreco1.fireflyv2.controller.response.GeneralLedgerLineDto2;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * Created by Tri-Nvent on 6/1/2018.
 */
public interface CashFlowAccountEntryFacade {
    void setCashFlowAccountEntries(GeneralLedger newGl, GeneralLedgerLineDto2 ledgerLine, Date voucherDate, boolean lastGLEntry, BigDecimal percentage);
    void setCashFlowAccountEntries(GeneralLedger newGl, GeneralLedgerLineDto2 ledgerLine, Date voucherDate);
}