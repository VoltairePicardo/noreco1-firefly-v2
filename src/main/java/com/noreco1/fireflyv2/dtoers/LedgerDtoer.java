package com.noreco1.fireflyv2.dtoers;

import com.noreco1.fireflyv2.model.SubLedger;
import com.noreco1.fireflyv2.controller.response.GeneralLedgerLineDto;
import com.noreco1.fireflyv2.controller.response.GeneralLedgerLineDto2;
import com.noreco1.fireflyv2.controller.response.SubLedgerDto;
import com.noreco1.fireflyv2.controller.response.reports.CommonLedgerDetail;

import java.util.List;
import java.util.Map;

/**
 * Created by TSI Admin on 5/3/2015.
 */
public interface LedgerDtoer {
    public List<SubLedgerDto> getSLEntriesDtoByGl(Integer glId);
    public List<SubLedgerDto> getSLEntriesDtoByTrans(Integer transId);
    List<GeneralLedgerLineDto> getGLEntriesDtoByTrans(Integer transId);
    List<GeneralLedgerLineDto2> getGLAccountEntriesDtoByTrans(Integer accountId, boolean withoutTax);
    public List<SubLedgerDto> getSLEntriesDtoByTransAndAccount(Integer transId, Integer accountId, Boolean isDebit);
    public List<Map> getGLEntriesDtoByTransAndAccount(Integer transId, Integer accountId);
    public  List<CommonLedgerDetail> getVoucherLedgerLines(Integer transId);

    // from temps
    public List<Map> getAllTempBatches();
    List<GeneralLedgerLineDto2> getGLAccountEntriesDtoTempBatchId(Integer tempBatchId);
    List<SubLedgerDto> getSLAccountEntriesDtoTempBatchId(Integer tempBatchId, Integer accountId);

    public SubLedger getSLEntryByAccountNoForPrep(Integer accountNo);
    List<Map> getCashflowAccountsByGlId(Integer glId);

    List<GeneralLedgerLineDto2> getGLAccountEntriesDtoMIRTransId(Integer transId);
    List<GeneralLedgerLineDto2> getGLAccountEntriesDtoMIRTransIdAndMST(Integer transId);
    List<GeneralLedgerLineDto2> setGLAccountEntriesDtoForCashAdvance(Integer caId);
    List<SubLedgerDto> setSLEntryDtoForCashAdvance(Integer caId, Integer accountId);
    List<GeneralLedgerLineDto2> getAccountSettingEntriesForJV(Integer transId);
}
