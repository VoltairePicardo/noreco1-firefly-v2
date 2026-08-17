package com.noreco1.fireflyv2.common.facade;

import com.noreco1.fireflyv2.model.TemporaryBatch;
import com.noreco1.fireflyv2.model.Transaction;
import com.noreco1.fireflyv2.controller.response.GeneralLedgerLineDto;
import com.noreco1.fireflyv2.controller.response.GeneralLedgerLineDto2;
import com.noreco1.fireflyv2.controller.response.SubLedgerDto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by TSI Admin on 5/1/2015.
 */
public interface LedgerFacade {
    Long postGeneralLedger(Transaction transaction, List<GeneralLedgerLineDto2> generalLedgerLines, List<SubLedgerDto> subLedgerLines, Date voucherDate);
    void saveTempLedgerEntries(Map<Integer, BigDecimal> accountsDetailMap, java.sql.Date date, TemporaryBatch temporaryBatch, Integer accountNo, boolean isDebit);
    Map computeGLTotals(List<GeneralLedgerLineDto2> generalLedgerLines, Date voucherDate);
    boolean totalsAreBalanced(List<GeneralLedgerLineDto2> generalLedgerLines, Date voucherDate);
}
