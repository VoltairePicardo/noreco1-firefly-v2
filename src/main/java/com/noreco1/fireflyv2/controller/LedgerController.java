package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.facade.LedgerFacade;
import com.noreco1.fireflyv2.controller.form.GeneralLedgerForm;
import com.noreco1.fireflyv2.dtoers.LedgerDtoer;
import com.noreco1.fireflyv2.model.SubLedger;
import com.noreco1.fireflyv2.controller.response.GeneralLedgerLineDto2;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.SubLedgerDto;
import com.noreco1.fireflyv2.service.LedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    @Autowired
    LedgerDtoer ledgerDtoer;

    @Autowired
    LedgerService ledgerService;

    @Autowired
    LedgerFacade ledgerFacade;

    @GetMapping(value = "/gl/{transId}")
    
    public List<GeneralLedgerLineDto2> getGLEntries(@PathVariable Integer transId, HttpServletRequest request) {
        return ledgerDtoer.getGLAccountEntriesDtoByTrans(transId, false);
    }

    @GetMapping(value = "/gl-without-withholding-tax/{transId}")
    
    public List<GeneralLedgerLineDto2> getGLEntriesWithoutWithholdingTax(@PathVariable Integer transId, HttpServletRequest request) {
        return ledgerDtoer.getGLAccountEntriesDtoByTrans(transId, true);
    }

    @GetMapping(value = "/sl/{glId}")
    
    public List<SubLedgerDto> getSLEntries(@PathVariable Integer glId, HttpServletRequest request) {
        return ledgerDtoer.getSLEntriesDtoByGl(glId);
    }

    @GetMapping(value = "/sl/{transId}/{accountId}")
    
    public List<SubLedgerDto> getSLEntriesGrouped(@PathVariable Integer transId, @PathVariable Integer accountId,
                                                  @RequestParam(required = false) Boolean isDebit,
                                                  HttpServletRequest request) {
        return ledgerDtoer.getSLEntriesDtoByTransAndAccount(transId, accountId, isDebit);
    }

    @GetMapping(value = "/gl/{transId}/{accountId}")
    
    public List<Map> getGLEntriesByTransAndAccount(@PathVariable Integer transId, @PathVariable Integer accountId, HttpServletRequest request) {
        return ledgerDtoer.getGLEntriesDtoByTransAndAccount(transId, accountId);
    }

    @GetMapping(value = "/temp/all")
    
    public List<Map> getBatchTemp(HttpServletRequest request) {
        return ledgerDtoer.getAllTempBatches();
    }

    @GetMapping(value = "/temp/gl/{tempBatchId}")
    
    public List<GeneralLedgerLineDto2> getTempGLEntries(@PathVariable Integer tempBatchId, HttpServletRequest request) {
        return ledgerDtoer.getGLAccountEntriesDtoTempBatchId(tempBatchId);
    }

    @GetMapping(value = "/temp/sl/{tempBatchId}/{accountId}")
    
    public List<SubLedgerDto> getTempSLEntriesGrouped(@PathVariable Integer tempBatchId, @PathVariable Integer accountId, HttpServletRequest request) {
        return ledgerDtoer.getSLAccountEntriesDtoTempBatchId(tempBatchId, accountId);
    }

    @GetMapping(value = "/sl/pp/{accountNo}")
    
    public SubLedger getSLEntryForPrep(@PathVariable Integer accountNo, HttpServletRequest request) {
        return ledgerDtoer.getSLEntryByAccountNoForPrep(accountNo);
    }

    @GetMapping(value = "/cashflow-accounts/{glId}")
    
    public List<Map> getCashflowAccountsByGlId(@PathVariable Integer glId) {
        return ledgerDtoer.getCashflowAccountsByGlId(glId);
    }

    @GetMapping(value = "/mir/{transId}/{isMST}")
    
    public List<GeneralLedgerLineDto2> getMIREntries(@PathVariable Integer transId, @PathVariable Boolean isMST, HttpServletRequest request) {
        if (isMST) {
            return ledgerDtoer.getGLAccountEntriesDtoMIRTransId(transId);
        } else {
            return ledgerDtoer.getGLAccountEntriesDtoMIRTransIdAndMST(transId);
        }
    }

    @PostMapping(value = "/temp/check-segments-validity/{tempBatchId}/{voucherDate}")
    
    public PostResponse checkSegmentsValidity(@PathVariable Integer tempBatchId, @PathVariable String voucherDate, HttpServletRequest request) {
        return ledgerService.checkSegmentsValidity(tempBatchId, voucherDate);
    }

    @GetMapping(value = "/gl-for-ca/{caId}")
    
    public List<GeneralLedgerLineDto2> getGLEntriesForCashAdvance(@PathVariable Integer caId, HttpServletRequest request) {
        return ledgerDtoer.setGLAccountEntriesDtoForCashAdvance(caId);
    }

    @GetMapping(value = "/sl-for-ca/{caId}/{accountId}")
    
    public List<SubLedgerDto> getSLEntriesForCa(@PathVariable Integer caId, @PathVariable Integer accountId, HttpServletRequest request) {
        return ledgerDtoer.setSLEntryDtoForCashAdvance(caId, accountId);
    }

    @PostMapping(value = "/calculate-totals")
    
    public Map calculateTotals(@RequestBody GeneralLedgerForm form) {
        return ledgerFacade.computeGLTotals(form.getLedgerLines(), form.getVoucherDate());
    }

    @GetMapping(value = "/account-setting/{transId}")
    
    public List<GeneralLedgerLineDto2> getAccountSettingEntriesForJV(@PathVariable Integer transId, HttpServletRequest request) {
        return ledgerDtoer.getAccountSettingEntriesForJV(transId);
    }

}
